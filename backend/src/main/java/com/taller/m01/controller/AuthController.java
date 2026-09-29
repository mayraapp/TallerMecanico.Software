package com.taller.m01.controller;

import com.taller.m01.config.AppSecurityProperties;
import com.taller.m01.dto.AuthDtos;
import com.taller.m01.security.JwtService;
import com.taller.m01.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
/**
 * HTTP boundary for authentication and password lifecycle endpoints.
 *
 * <p>For the Phase 02 login flow, this controller delegates credential verification to
 * {@link AuthFacade}; it retains cookies, rate limiting and the M01 password operations.</p>
 */
public class AuthController {
    private final AuthService auth; private final AuthFacade authFacade; private final CurrentUserService current; private final RateLimitService rateLimit; private final JwtService jwt; private final AppSecurityProperties properties;
    /**
     * Creates the controller with the existing security collaborators and Phase 02 facade.
     *
     * @param auth service for established M01 authentication operations
     * @param authFacade facade that coordinates the login use case
     * @param current resolver for the authenticated account
     * @param rateLimit request-attempt limiter
     * @param jwt JWT expiration provider
     * @param properties cookie-security configuration
     */
    public AuthController(AuthService auth, AuthFacade authFacade, CurrentUserService current, RateLimitService rateLimit, JwtService jwt, AppSecurityProperties properties) { this.auth = auth; this.authFacade = authFacade; this.current = current; this.rateLimit = rateLimit; this.jwt = jwt; this.properties = properties; }
    @PostMapping("/login")
    /**
     * Authenticates a validated request through {@link AuthFacade} and stores the JWT only in an HTTP-only cookie.
     *
     * @param request validated credentials; invalid credentials produce the existing controlled 401 response
     * @param servletResponse request used only to read the source IP for rate limiting and auditing
     * @return safe authenticated user data, never a password hash or JWT body field
     */
    public ResponseEntity<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request, HttpServletRequest servletResponse) {
        rateLimit.check("login", RequestInfo.ip(servletResponse)); AuthService.LoginResult result = authFacade.iniciarSesion(request, RequestInfo.ip(servletResponse)); return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, sessionCookie(result.token()).toString()).body(result.response());
    }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(value = "TM_SESSION", required = false) String token, HttpServletRequest request, HttpServletResponse response) { auth.logout(token, current.require(), RequestInfo.ip(request)); response.addHeader(HttpHeaders.SET_COOKIE, clearCookie().toString()); }
    @GetMapping("/me") public AuthDtos.AuthenticatedUser me() { return auth.me(current.require()); }
    @PostMapping("/forgot-password")
    public AuthDtos.PublicMessage forgot(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request, HttpServletRequest servletRequest) { rateLimit.check("forgot", RequestInfo.ip(servletRequest)); auth.forgotPassword(request, RequestInfo.ip(servletRequest)); return new AuthDtos.PublicMessage("Si la cuenta existe y está activa, recibirá instrucciones de recuperación."); }
    @PostMapping("/verify-code")
    public AuthDtos.PublicMessage verify(@Valid @RequestBody AuthDtos.VerifyCodeRequest request, HttpServletRequest servletRequest) { rateLimit.check("verify", RequestInfo.ip(servletRequest)); auth.verifyCode(request); return new AuthDtos.PublicMessage("Código verificado."); }
    @PostMapping("/reset-password")
    public AuthDtos.PublicMessage reset(@Valid @RequestBody AuthDtos.ResetPasswordRequest request, HttpServletRequest servletRequest) { rateLimit.check("reset", RequestInfo.ip(servletRequest)); auth.resetPassword(request, RequestInfo.ip(servletRequest)); return new AuthDtos.PublicMessage("La contraseña fue actualizada. Inicie sesión nuevamente."); }
    @PostMapping("/change-temporary-password")
    public ResponseEntity<AuthDtos.PublicMessage> changeTemporary(@Valid @RequestBody AuthDtos.ChangePasswordRequest request, HttpServletRequest servletRequest) { String token = auth.changePassword(current.require(), request, RequestInfo.ip(servletRequest)); return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, sessionCookie(token).toString()).body(new AuthDtos.PublicMessage("Contraseña actualizada.")); }
    private ResponseCookie sessionCookie(String value) { return ResponseCookie.from("TM_SESSION", value).httpOnly(true).secure(properties.isCookieSecure()).sameSite("Strict").path("/").maxAge(jwt.expirationSeconds()).build(); }
    private ResponseCookie clearCookie() { return ResponseCookie.from("TM_SESSION", "").httpOnly(true).secure(properties.isCookieSecure()).sameSite("Strict").path("/").maxAge(0).build(); }
}
