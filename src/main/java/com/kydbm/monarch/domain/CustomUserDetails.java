package com.kydbm.monarch.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long mUserNo;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long mUserNo) {
        super(username, password, authorities);
        this.mUserNo = mUserNo;
    }

    public CustomUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities, Long mUserNo) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.mUserNo = mUserNo;
    }

    public Long getMUserNo() {
        return mUserNo;
    }
}