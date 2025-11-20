package com.kydbm.monarch.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/** Spring Security가 인증된 사용자의 정보를 관리하기 위해 사용하는 객체 */
public class AuthUser extends User {

    // M_USER 테이블의 PK인 사용자 번호
    private final Long mUserNo;

    public AuthUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long mUserNo) {
        super(username, password, authorities);
        this.mUserNo = mUserNo;
    }

    /**
     * 계정 상태(활성, 만료, 잠김 등)를 포함하는 상세 생성자.
     * AuthUserService에서 DB 조회 결과를 바탕으로 사용자의 모든 상태를 포함하여 객체를 생성할 때 사용됩니다.
     * @param username 사용자 아이디 (USER_CODE)
     * @param password 암호화된 비밀번호
     * @param enabled 계정 활성화 여부 (USE_FLAG)
     * @param accountNonExpired 계정 만료 여부 (현재는 항상 true)
     * @param credentialsNonExpired 비밀번호 만료 여부 (현재는 항상 true)
     * @param accountNonLocked 계정 잠김 여부 (LOGIN_FAIL_CNT 기반)
     * @param authorities 사용자가 가진 권한 목록 (ROLE_USER 등)
     * @param mUserNo 사용자 번호 (M_USER_NO)
     */
    public AuthUser(String username, String password, boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities, Long mUserNo) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.mUserNo = mUserNo;
    }

    // 사용자 번호(PK)를 반환하는 getter
    public Long getMUserNo() {
        return mUserNo;
    }
}