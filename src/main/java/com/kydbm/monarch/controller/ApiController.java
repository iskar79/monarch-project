package com.kydbm.monarch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kydbm.monarch.domain.AuthUser;
import com.kydbm.monarch.mapper.MServiceMapper;
import com.kydbm.monarch.service.DynamicGridStructure;
import com.kydbm.monarch.service.DynamicQueryService;

import java.util.Map;
import com.kydbm.monarch.mapper.UserMapper;
import java.util.HashMap;
import java.util.List;

/**
 * 프론트엔드의 모든 API 요청을 처리하는 메인 컨트롤러. 
 * 모든 메소드는 JSON 형태의 데이터를 반환합니다.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }

    private final DynamicQueryService dynamicQueryService;
    private final DynamicGridStructure dynamicGridStructure;
    private final UserMapper userMapper;
    private final MServiceMapper mServiceMapper;

    /**
     * 생성자 주입 방식. Spring이 필요한 서비스와 매퍼의 구현체를 자동으로 주입합니다. 
     * @param dynamicQueryService 동적 쿼리 실행 서비스 
     * @param dynamicGridStructure 그리드 구조 정보 조회 서비스 
     * @param userMapper 사용자 정보 관련 매퍼 
     * @param mServiceMapper 서비스(쿼리) 정보 관련 매퍼 
     */
    public ApiController(DynamicQueryService dynamicQueryService, DynamicGridStructure dynamicGridStructure,
                         UserMapper userMapper, MServiceMapper mServiceMapper) {
        this.dynamicQueryService = dynamicQueryService;
        this.userMapper = userMapper;
        this.dynamicGridStructure = dynamicGridStructure;
        this.mServiceMapper = mServiceMapper;
    }

    /** 
     * 프론트엔드에서 사용자의 로그인 상태를 확인하기 위한 엔드포인트. 
     */
    @GetMapping("/auth/status")
    public ResponseEntity<Void> getAuthStatus() {
        // Spring Security에 의해 보호되는 경로이므로, 이 메소드가 실행된다는 것 자체가 인증된 상태임을 의미합니다.
        // 성공(200 OK) 응답을 보내 사용자가 로그인 상태임을 알립니다.
        return ResponseEntity.ok().build();
    }

    /** 
     * 현재 로그인된 사용자의 상세 정보를 조회하여 반환합니다. 
     */
    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal AuthUser user) {
        // 로그인 성공 후, 사용자의 모든 상세 정보를 조회하여 반환합니다.
        try {
            String serviceName = "MON_COMMON";
            String methodName = "USER_INFO";
            Long mUsiteNo = 1L; // 기본 회원사 번호
            Map<String, Object> params = Map.of("M_USER_NO", user.getMUserNo()); // AuthUser의 메소드 호출

            List<Map<String, Object>> userInfoList = dynamicQueryService.executeDynamicQuery(serviceName, methodName, mUsiteNo, params);
            if (userInfoList != null && !userInfoList.isEmpty()) {
                return userInfoList.get(0);
            } else {
                return Map.of("error", "User information not found"); // or handle the case where user info is not available
            }
        } catch (Exception e) {
            log.error("Error fetching user information", e);
            return Map.of("error", "Failed to retrieve user information"); // Handle the exception and return an error message
        }
    }

    /** 
     * 프론트엔드의 모든 동적 데이터 조회 요청을 처리하는 핵심 엔드포인트. 
     */
    @GetMapping("/data/execute")
    public ResponseEntity<?> executeServiceQuery(@RequestParam Map<String, String> allRequestParams) {
        String serviceName = allRequestParams.get("serviceName");
        String methodName = allRequestParams.get("methodName");
        Long mUsiteNo = Long.parseLong(allRequestParams.getOrDefault("usiteNo", "1"));

        if ("M_STRUCTURE".equals(serviceName)) {
            // --- 1. 그리드 구성 정보 조회 요청 처리 ---
            String structureName = allRequestParams.get("structureName");

            String structureContString = dynamicGridStructure.getStructureByName(structureName, mUsiteNo);

            if (structureContString != null && !structureContString.trim().isEmpty()) {
                // 1. 배열 또는 객체의 마지막에 있는 불필요한 쉼표(trailing comma)를 제거합니다.
                structureContString = structureContString.replaceAll(",(\\s*})", "$1");
                structureContString = structureContString.replaceAll(",(\\s*])", "$1");
                // 2. 줄바꿈, 탭 등 불필요한 공백을 제거하여 파싱 오류 가능성을 줄입니다.
                structureContString = structureContString.replaceAll("\\s+", " ").trim();
            }
            return ResponseEntity.ok(Map.of("structureCont", structureContString));

        } else if ("M_SERVICE_ADMIN".equals(serviceName)) {
            // --- 2. 서비스 정보 관리 페이지의 쿼리문 조회 요청 처리 ---
            String searchServiceName = allRequestParams.get("searchServiceName");
            String searchMethodName = allRequestParams.get("searchMethodName");

            Map<String, Object> serviceQuery = mServiceMapper.findServiceQuery(mUsiteNo, searchServiceName, searchMethodName);
            // 결과를 List 형태로 감싸서 다른 API 응답 형식과 일관성을 맞춥니다.
            return ResponseEntity.ok(serviceQuery != null ? List.of(serviceQuery) : List.of());

        } else {
            // --- 3. 그 외 모든 일반적인 동적 그리드 데이터 조회 요청 처리 ---
            Map<String, Object> queryParams = new HashMap<>(allRequestParams);
            queryParams.remove("serviceName");
            queryParams.remove("methodName");
            queryParams.put("USITE", mUsiteNo);

            List<Map<String, Object>> result = dynamicQueryService.executeDynamicQuery(serviceName, methodName, mUsiteNo, queryParams);
            return ResponseEntity.ok(result);
        }
    }

    /** 
     * 사용자 코드(ID)로 상세 정보를 조회합니다. (MyBatis 직접 호출 테스트용) 
     */
    @GetMapping("/user/details")
    public List<Map<String, Object>> getUserDetails(@RequestParam("userCode") String userCode) {
        log.info("'/api/user/details' 요청 수신 (using MyBatis). userCode: {}", userCode);
        List<Map<String, Object>> userDetails = userMapper.findUserDetailsByUserCode(userCode);
        if (userDetails.isEmpty()) {
            log.warn("데이터베이스에서 userCode '{}'에 해당하는 사용자를 찾지 못했습니다.", userCode);
        } else {
            log.info("데이터베이스에서 사용자 정보를 찾았습니다. count: {}", userDetails.size());
        }
        return userDetails;
    }
}