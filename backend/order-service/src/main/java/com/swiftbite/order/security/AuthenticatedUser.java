package com.swiftbite.order.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUser {

    private final Long userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(Long userId, String email, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.authorities = authorities != null ? authorities : List.of();
    }

    public Long getUserId() { return userId; }

    public String getEmail() { return email; }

    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String toString() {
        return "AuthenticatedUser{userId=" + userId + ", email='" + email + "'}";
    }
}
