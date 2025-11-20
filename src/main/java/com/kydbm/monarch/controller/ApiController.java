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

    public ApiController(DynamicQueryService dynamicQueryService, DynamicGridStructure dynamicGridStructure,
                         UserMapper userMapper, MServiceMapper mServiceMapper) {
        this.dynamicQueryService = dynamicQueryService;
        this.userMapper = userMapper;
        this.dynamicGridStructure = dynamicGridStructure;
        this.mServiceMapper = mServiceMapper;
    }

    @GetMapping("/auth/status")
    public ResponseEntity<Void> getAuthStatus() {
        // Spring Security가 이 경로를 보호하므로, 이 메소드가 실행되면 사용자는 인증된 것입니다.
        return ResponseEntity.ok().build();
    }

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

    @GetMapping("/data/execute")
    public ResponseEntity<?> executeServiceQuery(@RequestParam Map<String, String> allRequestParams) {
        String serviceName = allRequestParams.get("serviceName");
        String methodName = allRequestParams.get("methodName");
        Long mUsiteNo = Long.parseLong(allRequestParams.getOrDefault("usiteNo", "1"));

        if ("M_STRUCTURE".equals(serviceName)) {
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
            String searchServiceName = allRequestParams.get("searchServiceName");
            String searchMethodName = allRequestParams.get("searchMethodName");

            Map<String, Object> serviceQuery = mServiceMapper.findServiceQuery(mUsiteNo, searchServiceName, searchMethodName);
            // 결과를 List 형태로 감싸서 다른 API 응답 형식과 일관성을 맞춥니다.
            return ResponseEntity.ok(serviceQuery != null ? List.of(serviceQuery) : List.of());

        } else {
            Map<String, Object> queryParams = new HashMap<>(allRequestParams);
            queryParams.remove("serviceName");
            queryParams.remove("methodName");
            queryParams.put("USITE", mUsiteNo);

            List<Map<String, Object>> result = dynamicQueryService.executeDynamicQuery(serviceName, methodName, mUsiteNo, queryParams);
            return ResponseEntity.ok(result);
        }
    }

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