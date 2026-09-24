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
public class AuthController {
    private final AuthService auth; private final CurrentUserService current; private final RateLimitService rateLimit; private final JwtService jwt; private final AppSecurityProperties properties;
    public AuthController(AuthService auth, CurrentUserService current, RateLimitService rateLimit, JwtService jwt, AppSecurityProperties properties) { this.auth = auth; this.current = current; this.rateLimit = rateLimit; this.jwt = jwt; this.properties = properties; }
    @PostMapping("/login")
    public ResponseEntity<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request, HttpServletRequest servletResponse) {
        rateLimit.check("login", RequestInfo.ip(servletResponse)); AuthService.LoginResult result = auth.login(request, RequestInfo.ip(servletResponse)); return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, sessionCookie(result.token()).toString()).body(result.response());
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
