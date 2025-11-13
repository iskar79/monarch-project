package com.kydbm.monarch.service;

import com.kydbm.monarch.domain.CustomUserDetails;
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

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserMapper userMapper;

    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", username);
        List<Map<String, Object>> userDetailsList = userMapper.findUserDetailsByUserCode(username);

        if (userDetailsList.isEmpty()) {
            log.warn("User '{}' not found in database (or USE_FLAG is not 'Y').", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        Map<String, Object> userDetails = userDetailsList.get(0);
        log.info("User '{}' found. Details: {}", username, userDetails);

        // 'khma'가 아닌 사용자에 대해서만 계정 잠김 상태 확인
        if (!"khma".equals(username)) {
            Object failCntObj = userDetails.get("LOGIN_FAIL_CNT");
            // DB에서 NUMBER 타입은 BigDecimal로 오지만, null일 수도 있으므로 체크
            if (failCntObj instanceof BigDecimal && ((BigDecimal) failCntObj).intValue() >= 5) {
                log.warn("User '{}' account is locked.", username);
                throw new LockedException("User account is locked due to 5 failed login attempts.");
            }
        }

        // USE_FLAG는 CHAR(1)일 수 있으므로 String으로 변환 후 비교
        boolean enabled = "1".equals(String.valueOf(userDetails.get("USE_FLAG")));
        log.info("User '{}' enabled status: {}", username, enabled);
        boolean accountNonLocked = true; // 위에서 이미 체크했으므로 기본값은 true
        boolean credentialsNonExpired = true; // 필요시 PW_UPD_DATE로 만료 여부 체크
        boolean accountNonExpired = true;

        String password = (String) userDetails.get("USER_PASSWORD");

        return new CustomUserDetails((String) userDetails.get("USER_CODE"),
                password, enabled, accountNonExpired, credentialsNonExpired,
                accountNonLocked, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                ((BigDecimal) userDetails.get("M_USER_NO")).longValue());
    }
}