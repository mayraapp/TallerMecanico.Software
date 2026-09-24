package com.taller.m01.service;

import com.taller.m01.entity.UserAccount;
import com.taller.m01.exception.ApiException;
import com.taller.m01.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public UserAccount require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser principal)) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "No autenticado.");
        return principal.account();
    }
    public void requireSuperAdmin() {
        if (!require().hasRole("SUPERADMIN")) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "No tiene permiso para realizar esta acción.");
    }
}
