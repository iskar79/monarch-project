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

/**
 * M_SERVICE 테이블 기반으로 동적 SQL 쿼리를 실행하는 서비스.
 * 프론트엔드에서 전달된 파라미터에 따라 다양한 DB 작업을 수행하며,
 * SQL Injection 방지, 동적 SQL 블록 처리, 페이징 기능을 제공합니다.
 */
@Service
public class DynamicQueryService implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DynamicQueryService.class);
    private final MServiceMapper mServiceMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ApplicationContext applicationContext;

    /** Spring 컨테이너가 Bean 초기화 시 ApplicationContext를 주입합니다. */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** Spring이 필요한 의존성을 자동으로 주입하는 생성자 */
    public DynamicQueryService(MServiceMapper mServiceMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.mServiceMapper = mServiceMapper;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * M_SERVICE 테이블에 정의된 동적 쿼리를 실행합니다.
     * @param serviceName M_SERVICE 테이블의 SERVICE_NAME 컬럼 값
     * @param methodName M_SERVICE 테이블의 METHOD_NAME 컬럼 값
     * @param mUsiteNo 회원사 번호
     * @param queryParams 쿼리 실행에 필요한 파라미터들을 담은 Map
     * @return 쿼리 실행 결과 (List<Map<String, Object>> 형태)
     */
    @Transactional
    public List<Map<String, Object>> executeDynamicQuery(String serviceName, String methodName, Long mUsiteNo, Map<String, Object> queryParams) {
        // 1. M_SERVICE 테이블에서 쿼리문 및 실행 타입(EXEC_TYPE) 조회
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
        Pattern dynamicBlockPattern = Pattern.compile("/\\*([\\s\\S]*?@[a-zA-Z0-9_]+@[\\s\\S]*?)\\*/");
        Matcher matcher = dynamicBlockPattern.matcher(queryStmt);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String blockContent = matcher.group(1);
            boolean paramExists = queryParams.keySet().stream()
                    .anyMatch(key -> blockContent.contains("@" + key + "@"));

            if (paramExists) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(blockContent));
            } else {
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
        String finalQuery = sb.toString();

        // 3. @PARAM@ 플레이스홀더를 :PARAM 형태로 치환 (SQL Injection 방지)
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        Pattern placeholderPattern = Pattern.compile("@[a-zA-Z0-9_]+@");
        Matcher placeholderMatcher = placeholderPattern.matcher(finalQuery);
        StringBuffer querySb = new StringBuffer();

        while (placeholderMatcher.find()) {
            String placeholder = placeholderMatcher.group();
            String paramName = placeholder.substring(1, placeholder.length() - 1);
            Object paramValue = queryParams.get(paramName);
            
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
                int page = Integer.parseInt(queryParams.getOrDefault("_page", "1").toString());
                int size = Integer.parseInt(queryParams.getOrDefault("_size", "20").toString());
                String sort = (String) queryParams.getOrDefault("_sort", "");
 
                String orderByClause = "";
                if (sort != null && !sort.trim().isEmpty()) {
                    if (!sort.matches("^[a-zA-Z0-9_.,\\sASCascDESCdesc]+$")) {
                        throw new IllegalArgumentException("Invalid sort parameter.");
                    }
                    orderByClause = " ORDER BY " + sort;
                }
 
                String countQuery = "SELECT COUNT(*) FROM (" + executableQuery + ")";
                Integer totalCount = namedParameterJdbcTemplate.queryForObject(countQuery, parameters, Integer.class);
 
                int startRow = (page - 1) * size;
                int endRow = page * size;
                String pagingQuery = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (" + executableQuery + orderByClause + ") a WHERE ROWNUM <= " + endRow + ") WHERE rnum > " + startRow;
                List<Map<String, Object>> data = namedParameterJdbcTemplate.queryForList(pagingQuery, parameters);
 
                return List.of(Map.of("data", data, "totalCount", totalCount));
            } else if ("INSERT".equalsIgnoreCase(execType) || "UPDATE".equalsIgnoreCase(execType) || "DELETE".equalsIgnoreCase(execType)) {
                int affectedRows = namedParameterJdbcTemplate.update(executableQuery, parameters);
                return List.of(Map.of("affectedRows", affectedRows));
            } else {
                log.warn("Unsupported EXEC_TYPE: {}", execType);
                throw new IllegalArgumentException("Unsupported EXEC_TYPE: " + execType);
            }
        } catch (Exception e) {
            log.error("Error executing dynamic query: serviceName={}, methodName={}, mUsiteNo={}, queryParams={}", serviceName, methodName, mUsiteNo, queryParams, e);
            throw new RuntimeException("Error executing dynamic query: " + e.getMessage(), e);
        }
    }
}
