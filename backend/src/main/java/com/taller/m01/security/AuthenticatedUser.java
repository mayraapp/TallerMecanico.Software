package com.taller.m01.security;

import com.taller.m01.entity.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

/**
 * Adapts a persisted account to Spring Security's authenticated principal contract.
 *
 * <p>Phase 02 adds explicit role authorities alongside existing permission authorities so client
 * endpoints can require both a role and {@code CLIENTE_CREAR}.</p>
 */
public class AuthenticatedUser implements UserDetails {
    private final UserAccount user;
    public AuthenticatedUser(UserAccount user) { this.user = user; }
    public UserAccount account() { return user; }
    /**
     * Returns permission and role authorities for backend method-security checks.
     * Role authorities are prefixed with {@code ROLE_}; no role grants a permission by itself.
     */
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.stream.Stream.concat(
            user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode())),
            user.getRoles().stream().flatMap(role -> role.getPermissions().stream()).map(p -> new SimpleGrantedAuthority(p.getCode()))
        ).distinct().toList();
    }
    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return user.getStatus() != UserStatus.BLOCKED; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getStatus() == UserStatus.ACTIVE; }
}
