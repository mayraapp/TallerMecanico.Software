package com.taller.m01.service;

import com.taller.m01.config.AppSecurityProperties;
import com.taller.m01.dto.AuthDtos;
import com.taller.m01.entity.*;
import com.taller.m01.exception.ApiException;
import com.taller.m01.repository.*;
import com.taller.m01.security.JwtService;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String GENERIC_AUTH = "Correo o contraseña incorrectos.";
    private final UserRepository users; private final LoginAttemptRepository attempts; private final RecoveryCodeRepository recoveryCodes; private final RevokedSessionRepository revoked; private final PasswordEncoder encoder; private final PasswordPolicy policy; private final JwtService jwt; private final AuditService audit; private final AppSecurityProperties props;
    public AuthService(UserRepository users, LoginAttemptRepository attempts, RecoveryCodeRepository recoveryCodes, RevokedSessionRepository revoked, PasswordEncoder encoder, PasswordPolicy policy, JwtService jwt, AuditService audit, AppSecurityProperties props) {
        this.users = users; this.attempts = attempts; this.recoveryCodes = recoveryCodes; this.revoked = revoked; this.encoder = encoder; this.policy = policy; this.jwt = jwt; this.audit = audit; this.props = props;
    }
    public record LoginResult(AuthDtos.LoginResponse response, String token) { }
    @Transactional
    public LoginResult login(AuthDtos.LoginRequest request, String ip) {
        String email = request.email().trim().toLowerCase();
        Optional<UserAccount> found = users.findByEmailIgnoreCase(email);
        if (found.isEmpty()) { recordAttempt(null, email, false, ip); throw invalid(); }
        UserAccount user = found.get();
        unlockIfExpired(user);
        if (user.getStatus() != UserStatus.ACTIVE || !encoder.matches(request.password(), user.getPasswordHash())) {
            recordAttempt(user, email, false, ip);
            if (user.getStatus() == UserStatus.ACTIVE) {
                user.setFailedAttempts(user.getFailedAttempts() + 1);
                if (user.getFailedAttempts() >= 5) { user.setStatus(UserStatus.BLOCKED); user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(15))); audit.record(null, user, "ACCOUNT_LOCKED", ip, "Bloqueo temporal tras cinco intentos fallidos."); }
                users.save(user);
            }
            throw invalid();
        }
        user.setFailedAttempts(0); user.setLockedUntil(null); user.setLastLoginAt(Instant.now()); users.save(user); recordAttempt(user, email, true, ip); audit.record(user, user, "LOGIN_SUCCESS", ip, "Inicio de sesión correcto.");
        String token = jwt.create(user);
        return new LoginResult(new AuthDtos.LoginResponse(DtoMapper.authenticated(user), jwt.expiration(token)), token);
    }
    public String issueToken(UserAccount user) { return jwt.create(user); }
    public AuthDtos.AuthenticatedUser me(UserAccount user) { return DtoMapper.authenticated(user); }
    @Transactional
    public void logout(String token, UserAccount user, String ip) {
        if (token != null) try { JwtService.TokenData data = jwt.parse(token); RevokedSession session = new RevokedSession(); session.setJti(data.jti()); session.setExpiresAt(data.expiresAt()); revoked.save(session); } catch (Exception ignored) { }
        audit.record(user, user, "LOGOUT", ip, "Cierre de sesión.");
    }
    @Transactional
    public String changePassword(UserAccount user, AuthDtos.ChangePasswordRequest request, String ip) {
        if (!encoder.matches(request.currentPassword(), user.getPasswordHash())) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "CURRENT_PASSWORD", "La contraseña actual no es correcta.");
        policy.validate(request.newPassword(), request.confirmation());
        if (encoder.matches(request.newPassword(), user.getPasswordHash())) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_REUSE", "No puede reutilizar inmediatamente la misma contraseña.");
        user.setPasswordHash(encoder.encode(request.newPassword())); user.setMustChangePassword(false); user.setSessionVersion(user.getSessionVersion() + 1); users.save(user); audit.record(user, user, "PASSWORD_CHANGED", ip, "Cambio de contraseña."); return jwt.create(user);
    }
    @Transactional
    public void forgotPassword(AuthDtos.ForgotPasswordRequest request, String ip) {
        users.findByEmailIgnoreCase(request.email().trim().toLowerCase()).filter(u -> u.getStatus() == UserStatus.ACTIVE).ifPresent(user -> {
            recoveryCodes.findUsableByEmail(user.getEmail()).forEach(code -> { code.setUsedAt(Instant.now()); });
            String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            PasswordRecoveryCode recovery = new PasswordRecoveryCode(); recovery.setUser(user); recovery.setCodeHash(sha256(code)); recovery.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10))); recoveryCodes.save(recovery); audit.record(null, user, "PASSWORD_RESET_REQUESTED", ip, "Solicitud de recuperación procesada.");
            if (props.isDevelopmentMode()) log.warn("DEV ONLY recovery code issued for {}: {}", user.getEmail(), code);
        });
    }
    @Transactional
    public void verifyCode(AuthDtos.VerifyCodeRequest request) { validateRecovery(request.email(), request.code()); }
    @Transactional
    public void resetPassword(AuthDtos.ResetPasswordRequest request, String ip) {
        policy.validate(request.newPassword(), request.confirmation());
        PasswordRecoveryCode recovery = validateRecovery(request.email(), request.code()); UserAccount user = recovery.getUser();
        if (encoder.matches(request.newPassword(), user.getPasswordHash())) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_REUSE", "No puede reutilizar inmediatamente la misma contraseña.");
        recovery.setUsedAt(Instant.now()); user.setPasswordHash(encoder.encode(request.newPassword())); user.setMustChangePassword(false); user.setSessionVersion(user.getSessionVersion() + 1); users.save(user); audit.record(user, user, "PASSWORD_RESET", ip, "Contraseña restablecida mediante recuperación.");
    }
    private PasswordRecoveryCode validateRecovery(String email, String rawCode) {
        List<PasswordRecoveryCode> codes = recoveryCodes.findUsableByEmail(email.trim().toLowerCase());
        if (codes.isEmpty()) throw invalidCode(); PasswordRecoveryCode code = codes.getFirst();
        if (code.getExpiresAt().isBefore(Instant.now()) || code.getAttempts() >= 5) throw invalidCode();
        if (!MessageDigest.isEqual(code.getCodeHash().getBytes(StandardCharsets.UTF_8), sha256(rawCode).getBytes(StandardCharsets.UTF_8))) { code.setAttempts(code.getAttempts() + 1); recoveryCodes.save(code); throw invalidCode(); }
        return code;
    }
    private void unlockIfExpired(UserAccount user) { if (user.getStatus() == UserStatus.BLOCKED && user.getLockedUntil() != null && user.getLockedUntil().isBefore(Instant.now())) { user.setStatus(UserStatus.ACTIVE); user.setFailedAttempts(0); user.setLockedUntil(null); users.save(user); } }
    private void recordAttempt(UserAccount user, String email, boolean success, String ip) { LoginAttempt attempt = new LoginAttempt(); attempt.setUser(user); attempt.setEmail(email); attempt.setSuccess(success); attempt.setIpAddress(ip); attempts.save(attempt); if (!success) audit.record(null, user, "LOGIN_FAILED", ip, "Intento de inicio de sesión fallido."); }
    private ApiException invalid() { return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID", GENERIC_AUTH); }
    private ApiException invalidCode() { return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "RECOVERY_CODE_INVALID", "El código es inválido, venció o ya fue utilizado."); }
    private String sha256(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); } }
}
