package com.kydbm.monarch.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security가 사용자를 인증할 때 사용하는 UserDetails의 구현체입니다.
 * 데이터베이스에서 조회한 MUser 객체를 감싸서, Spring Security가 필요로 하는 정보(권한, 비밀번호 등)를 제공합니다.
 */
public class AuthUser implements UserDetails {

    private final MUser muser;

    public AuthUser(MUser muser) {
        this.muser = muser;
    }

    /**
     * MUser 객체를 반환하는 getter 메소드입니다.
     * 이 메소드를 통해 사용자의 추가 정보(예: CONN_DUR)에 접근할 수 있습니다.
     * @return MUser 객체
     */
    public MUser getMuser() {
        return muser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 권한을 설정합니다. 여기서는 간단히 "ROLE_USER"를 부여합니다.
        // 필요 시 MUser 객체의 권한 정보를 바탕으로 동적으로 설정할 수 있습니다.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return muser.getUserPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security에서 username으로 사용할 필드를 지정합니다.
        // USER_CODE를 고유 식별자로 사용합니다.
        return muser.getUserCode();
    }

    // 아래는 계정 상태 관련 메소드들입니다. MUser의 USE_FLAG 등을 사용하여 구현할 수 있습니다.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return "Y".equals(muser.getUseFlag()); }
}