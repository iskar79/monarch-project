package com.kydbm.monarch.service;

import com.kydbm.monarch.mapper.MServiceMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.context.ApplicationContextAware;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Clob;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

@Service
public class DynamicQueryService implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DynamicQueryService.class);
    private final MServiceMapper mServiceMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public DynamicQueryService(MServiceMapper mServiceMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.mServiceMapper = mServiceMapper;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Transactional
    public List<Map<String, Object>> executeDynamicQuery(String serviceName, String methodName, Long mUsiteNo, Map<String, Object> queryParams) {
        // 1. M_SERVICE 테이블에서 쿼리문 조회 (MyBatis Mapper 사용)
        Map<String, Object> serviceInfo = mServiceMapper.findServiceQuery(mUsiteNo, serviceName, methodName);

        if (serviceInfo == null) {
            log.error("Service not found for: serviceName={}, methodName={}, mUsiteNo={}", serviceName, methodName, mUsiteNo);
            throw new IllegalArgumentException("Service not found for: " + serviceName + ", " + methodName + ", " + mUsiteNo);
        }

        String execType = (String) serviceInfo.get("EXEC_TYPE");
        Object queryStmtObject = serviceInfo.get("QUERY_STMT");
        String queryStmt;

        // CLOB/NCLOB 타입을 String으로 변환
        if (queryStmtObject instanceof Clob) {
            try {
                queryStmt = ((Clob) queryStmtObject).getSubString(1, (int) ((Clob) queryStmtObject).length());
            } catch (Exception e) {
                throw new RuntimeException("Failed to read CLOB data", e);
            }
        } else {
            queryStmt = (String) queryStmtObject;
        }

        if (queryStmt == null || queryStmt.trim().isEmpty()) {
            throw new IllegalArgumentException("Query statement is empty for the specified service.");
        }

        // 2. 동적 SQL 블록 처리 (예: /* AND USER_NAME = @USER_NAME@ */)
        // 정규식을 사용하여 /* ... @PARAM@ ... */ 형태의 주석 블록을 찾습니다.
        Pattern dynamicBlockPattern = Pattern.compile("/\\*([\\s\\S]*?@[a-zA-Z0-9_]+@[\\s\\S]*?)\\*/");
        Matcher matcher = dynamicBlockPattern.matcher(queryStmt);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String blockContent = matcher.group(1); // 주석 안의 내용
            // 블록 안의 파라미터(@PARAM@)가 queryParams에 존재하는지 확인
            boolean paramExists = queryParams.keySet().stream()
                    .anyMatch(key -> blockContent.contains("@" + key + "@"));

            if (paramExists) {
                // 파라미터가 존재하면 주석을 제거하고 SQL 구문을 활성화
                matcher.appendReplacement(sb, Matcher.quoteReplacement(blockContent));
            } else {
                // 파라미터가 없으면 블록 전체를 제거
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
        String finalQuery = sb.toString();

        // 3. @PARAM@ 플레이스홀더를 :PARAM 형태로 치환
        // NamedParameterJdbcTemplate은 SQL Injection 방지를 위해 파라미터 바인딩을 자동으로 처리합니다.
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        Pattern placeholderPattern = Pattern.compile("@[a-zA-Z0-9_]+@");
        Matcher placeholderMatcher = placeholderPattern.matcher(finalQuery);
        StringBuffer querySb = new StringBuffer();

        while (placeholderMatcher.find()) {
            String placeholder = placeholderMatcher.group();
            String paramName = placeholder.substring(1, placeholder.length() - 1); // @ 제거
            Object paramValue = queryParams.get(paramName);
            // 파라미터 이름에 'password'가 포함되면 값을 암호화합니다.
            if (paramName.toLowerCase().contains("password")) {
                PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
                paramValue = (paramValue != null) ? passwordEncoder.encode(paramValue.toString()) : null;
            }
            parameters.addValue(paramName, paramValue);
            placeholderMatcher.appendReplacement(querySb, ":" + paramName);
        }
        placeholderMatcher.appendTail(querySb);
        String executableQuery = querySb.toString();

        // 4. 쿼리 실행 및 결과 반환
        log.info("Executing dynamic query ({}): {}", execType, executableQuery);

        try {
            if ("READ".equalsIgnoreCase(execType)) {
                return namedParameterJdbcTemplate.queryForList(executableQuery, parameters);
            } else if ("LIST".equalsIgnoreCase(execType)) {
                // 페이징 처리를 위한 파라미터 추출
                int page = Integer.parseInt(queryParams.getOrDefault("_page", "1").toString());
                int size = Integer.parseInt(queryParams.getOrDefault("_size", "20").toString());
                String sort = (String) queryParams.getOrDefault("_sort", "");
 
                // 정렬 조건 추가
                String orderByClause = "";
                if (sort != null && !sort.trim().isEmpty()) {
                    // 기본적인 SQL Injection 방지
                    if (!sort.matches("^[a-zA-Z0-9_.,\\sASCascDESCdesc]+$")) {
                        throw new IllegalArgumentException("Invalid sort parameter.");
                    }
                    orderByClause = " ORDER BY " + sort;
                }
 
                // 1. 전체 카운트 조회
                String countQuery = "SELECT COUNT(*) FROM (" + executableQuery + ")";
                Integer totalCount = namedParameterJdbcTemplate.queryForObject(countQuery, parameters, Integer.class);
 
                // 2. 페이징된 데이터 조회
                int startRow = (page - 1) * size;
                int endRow = page * size;
                String pagingQuery = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (" + executableQuery + orderByClause + ") a WHERE ROWNUM <= " + endRow + ") WHERE rnum > " + startRow;
                List<Map<String, Object>> data = namedParameterJdbcTemplate.queryForList(pagingQuery, parameters);
 
                // 3. 데이터와 전체 카운트를 함께 반환
                return List.of(Map.of("data", data, "totalCount", totalCount));
            } else if ("INSERT".equalsIgnoreCase(execType) || "UPDATE".equalsIgnoreCase(execType) || "DELETE".equalsIgnoreCase(execType)) {
                int affectedRows = namedParameterJdbcTemplate.update(executableQuery, parameters);
                // 변경 작업의 결과로 영향받은 행의 수를 반환
                return List.of(Map.of("affectedRows", affectedRows));
            } else {
                log.warn("Unsupported EXEC_TYPE: {}", execType);
                throw new IllegalArgumentException("Unsupported EXEC_TYPE: " + execType);
            }
        } catch (Exception e) {
            // 쿼리 실행 중 발생한 예외 처리
            log.error("Error executing dynamic query", e);
            throw new RuntimeException("Error executing dynamic query: " + e.getMessage(), e);
        }
    }
}