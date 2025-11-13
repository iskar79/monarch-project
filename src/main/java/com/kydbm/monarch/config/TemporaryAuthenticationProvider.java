package com.kydbm.monarch.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemporaryAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(TemporaryAuthenticationProvider.class);

    public TemporaryAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        log.info("TemporaryAuthenticationProvider is checking username: {}", username);

        // 'khma' 사용자에 대해서만 특별 처리
        if ("khma".equals(username)) {
            log.info("'khma' user detected. Bypassing password check.");
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 비밀번호 검증을 건너뛰고 바로 인증 성공 처리
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }
        log.info("Not 'khma' user. Passing to next authentication provider.");
        return null; // 이 Provider가 처리할 수 없는 경우 null을 반환하여 다음 Provider로 넘김
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}