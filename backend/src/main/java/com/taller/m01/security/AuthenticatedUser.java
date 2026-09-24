package com.taller.m01.security;

import com.taller.m01.entity.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

public class AuthenticatedUser implements UserDetails {
    private final UserAccount user;
    public AuthenticatedUser(UserAccount user) { this.user = user; }
    public UserAccount account() { return user; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream().flatMap(role -> role.getPermissions().stream()).map(p -> new SimpleGrantedAuthority(p.getCode())).distinct().toList();
    }
    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return user.getStatus() != UserStatus.BLOCKED; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getStatus() == UserStatus.ACTIVE; }
}
