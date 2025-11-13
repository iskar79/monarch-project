package com.kydbm.monarch.config;

import com.kydbm.monarch.mapper.UserMapper;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    private final UserMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationEventListener.class);

    public AuthenticationEventListener(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        log.error("Login failed for user '{}'. Reason: {}", username, event.getException().getMessage());
        // 로그인 실패 시, 해당 사용자의 LOGIN_FAIL_CNT를 1 증가시킵니다.
        userMapper.incrementLoginFailCount(username);
    }

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = ((User) event.getAuthentication().getPrincipal()).getUsername();
        log.info("Login successful for user '{}'. Resetting fail count.", username);
        // 로그인 성공 시, 해당 사용자의 LOGIN_FAIL_CNT를 0으로 초기화합니다.
        userMapper.resetLoginFailCount(username);
    }
}