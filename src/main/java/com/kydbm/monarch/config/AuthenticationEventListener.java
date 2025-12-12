package com.kydbm.monarch.config;

import com.kydbm.monarch.domain.AuthUser;
import com.kydbm.monarch.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Spring Security의 인증 이벤트를 감지하여 로그인 성공/실패 시 추가 작업을 수행합니다. */
@Component
public class AuthenticationEventListener {

    private final UserMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationEventListener.class);

    public AuthenticationEventListener(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 로그인 실패(주로 비밀번호 오류) 시 호출됩니다.
     * @param event 로그인 실패 정보를 담은 이벤트 객체
     */
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        log.error("Login failed for user '{}'. Reason: {}", username, event.getException().getMessage());
        // 로그인 실패 시, 해당 사용자의 LOGIN_FAIL_CNT를 1 증가시킵니다.
        userMapper.incrementLoginFailCount(username);
    }

    /**
     * 로그인 성공 시 호출됩니다.
     * @param event 로그인 성공 정보를 담은 이벤트 객체
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (!(principal instanceof AuthUser)) {
            return;
        }

        AuthUser authUser = (AuthUser) principal;
        String username = authUser.getUsername();

        log.info("Login successful for user '{}'. Resetting fail count and setting session timeout.", username);
        userMapper.resetLoginFailCount(username); // 로그인 성공 시, 실패 횟수 초기화

        // 현재 요청의 세션을 가져옵니다.
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(false);

        if (session != null) {
            // M_USER 테이블의 CONN_DUR 컬럼 값을 가져옵니다 (단위: 분).
            Long connDur = authUser.getMuser().getConnDur();
            if (connDur != null && connDur > 0) {
                // 세션 타임아웃을 설정합니다. (단위: 초)
                session.setMaxInactiveInterval(connDur.intValue() * 60);
                log.info("Session timeout for user '{}' set to {} minutes.", username, connDur);
            }
        }
    }
}