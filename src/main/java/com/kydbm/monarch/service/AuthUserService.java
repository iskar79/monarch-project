package com.kydbm.monarch.service;

import com.kydbm.monarch.domain.AuthUser;
import com.kydbm.monarch.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.LockedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Spring Security의 `UserDetailsService`를 구현한 클래스.
 * 로그인 시 사용자가 입력한 아이디(username)를 받아 DB에서 사용자 정보를 조회하는 핵심 역할을 합니다.
 */
@Service
public class AuthUserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AuthUserService.class);

    private final UserMapper userMapper;

    public AuthUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 사용자 아이디(username)로 DB에서 사용자 정보를 찾아 Spring Security가 사용할 수 있는 `UserDetails` 객체로 변환합니다.
     * @param username 로그인 폼에서 입력된 사용자 아이디
     * @return `UserDetails` 인터페이스를 구현한 `AuthUser` 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     * @throws LockedException 로그인 실패 횟수 초과로 계정이 잠겼을 때
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", username);
        List<Map<String, Object>> userDetailsList = userMapper.findUserDetailsByUserCode(username);

        if (userDetailsList.isEmpty()) {
            log.warn("User '{}' not found in database (or USE_FLAG is not '1').", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        Map<String, Object> userDetails = userDetailsList.get(0);
        log.info("User '{}' found. Details: {}", username, userDetails);

        // 'khma' 사용자는 계정 잠금 정책에서 제외합니다. (개발/테스트용)
        if (!"khma".equals(username)) {
            Object failCntObj = userDetails.get("LOGIN_FAIL_CNT");
            // DB의 NUMBER 타입은 Java의 BigDecimal로 매핑될 수 있습니다.
            if (failCntObj instanceof BigDecimal && ((BigDecimal) failCntObj).intValue() >= 5) {
                log.warn("User '{}' account is locked.", username);
                throw new LockedException("User account is locked due to 5 failed login attempts.");
            }
        }

        // 계정 상태를 설정합니다.
        boolean enabled = "1".equals(String.valueOf(userDetails.get("USE_FLAG")));
        log.info("User '{}' enabled status: {}", username, enabled);
        boolean accountNonLocked = true; // 위에서 잠김 여부를 이미 체크했으므로 기본값은 true
        boolean credentialsNonExpired = true; // 필요시 PW_UPD_DATE로 만료 여부 체크 가능
        boolean accountNonExpired = true; // 계정 만료 정책은 현재 사용 안 함

        String password = (String) userDetails.get("USER_PASSWORD");

        // Spring Security가 사용할 AuthUser 객체를 생성하여 반환합니다.
        return new AuthUser(
                (String) userDetails.get("USER_CODE"),
                password,
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), // 모든 사용자에게 기본 역할 부여
                ((BigDecimal) userDetails.get("M_USER_NO")).longValue()
        );
    }
}
