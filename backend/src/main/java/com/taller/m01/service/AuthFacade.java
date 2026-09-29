package com.taller.m01.service;

import com.taller.m01.dto.AuthDtos;
import org.springframework.stereotype.Component;

/**
 * Coordinates the authentication use case exposed by the REST controller.
 *
 * <p>This facade keeps HTTP concerns out of {@link AuthService} while preserving the existing
 * validation, account-locking, JWT issuance and audit behavior. Authentication failures are
 * propagated as the controlled API exceptions produced by the service.</p>
 */
@Component
public class AuthFacade {
    private final AuthService authService;

    /**
     * Creates the facade with the service that applies the authentication rules.
     *
     * @param authService service that reads users through {@code UserRepository}
     */
    public AuthFacade(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user and returns the response plus the JWT used only by the controller cookie.
     *
     * @param request validated email and password supplied by the login view
     * @param ip origin IP retained for lockout and audit controls
     * @return authenticated DTO and generated JWT
     * @throws com.taller.m01.exception.ApiException when credentials are invalid or the account is not active
     */
    public AuthService.LoginResult iniciarSesion(AuthDtos.LoginRequest request, String ip) {
        return authService.login(request, ip);
    }
}
