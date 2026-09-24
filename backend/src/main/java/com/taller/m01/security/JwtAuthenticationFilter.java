package com.taller.m01.security;

import com.taller.m01.entity.UserStatus;
import com.taller.m01.repository.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService; private final UserRepository users; private final RevokedSessionRepository revoked;
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository users, RevokedSessionRepository revoked) { this.jwtService = jwtService; this.users = users; this.revoked = revoked; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = token(request);
        if (token != null) try {
            JwtService.TokenData data = jwtService.parse(token);
            users.findById(data.userId()).filter(user -> user.getStatus() == UserStatus.ACTIVE && user.getSessionVersion() == data.sessionVersion() && !revoked.existsByJti(data.jti())).ifPresent(user -> {
                AuthenticatedUser principal = new AuthenticatedUser(user);
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
            });
        } catch (Exception ignored) { SecurityContextHolder.clearContext(); }
        chain.doFilter(request, response);
    }
    private String token(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) if ("TM_SESSION".equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
