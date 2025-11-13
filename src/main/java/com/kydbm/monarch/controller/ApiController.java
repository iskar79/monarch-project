package com.kydbm.monarch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kydbm.monarch.domain.CustomUserDetails;
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
    private final UserMapper userMapper;

    public ApiController(DynamicQueryService dynamicQueryService, UserMapper userMapper) {
        this.dynamicQueryService = dynamicQueryService;
        this.userMapper = userMapper;
    }

    @GetMapping("/auth/status")
    public ResponseEntity<Void> getAuthStatus() {
        // Spring Security가 이 경로를 보호하므로, 이 메소드가 실행되면 사용자는 인증된 것입니다.
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal CustomUserDetails user) {
        // 로그인 성공 후, 사용자의 모든 상세 정보를 조회하여 반환합니다.
        try {
            String serviceName = "MON_COMMON";
            String methodName = "USER_INFO";
            Long mUsiteNo = 1L; // 기본 회원사 번호
            Map<String, Object> params = Map.of("M_USER_NO", user.getMUserNo());

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
    public List<Map<String, Object>> executeServiceQuery(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("methodName") String methodName,
            @RequestParam Map<String, String> allRequestParams) {

        Long mUsiteNo = Long.parseLong(allRequestParams.getOrDefault("mUsiteNo", "1"));
        allRequestParams.remove("serviceName");
        allRequestParams.remove("methodName");

        Map<String, Object> queryParams = new HashMap<>(allRequestParams);
        // 동적 쿼리에서 @USITE@ 파라미터를 사용할 수 있도록 값을 추가합니다.
        queryParams.put("USITE", mUsiteNo);

        return dynamicQueryService.executeDynamicQuery(serviceName, methodName, mUsiteNo, queryParams);
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