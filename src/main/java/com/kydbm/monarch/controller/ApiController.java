package com.kydbm.monarch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }

    @GetMapping("/auth/status")
    public ResponseEntity<Void> getAuthStatus() {
        // Spring Security가 이 경로를 보호하므로, 이 메소드가 실행되면 사용자는 인증된 것입니다.
        return ResponseEntity.ok().build();
    }
}