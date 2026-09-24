package com.taller.m01.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public final class AuthDtos {
    private AuthDtos() { }
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }
    public record ForgotPasswordRequest(@NotBlank @Email String email) { }
    public record VerifyCodeRequest(@NotBlank @Email String email, @Pattern(regexp = "\\d{6}") String code) { }
    public record ResetPasswordRequest(@NotBlank @Email String email, @Pattern(regexp = "\\d{6}") String code, @NotBlank String newPassword, @NotBlank String confirmation) { }
    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword, @NotBlank String confirmation) { }
    public record AuthenticatedUser(Long id, String fullName, String email, String primaryRole, List<String> roles, List<String> permissions, boolean requiresPasswordChange) { }
    public record LoginResponse(AuthenticatedUser user, Instant expiresAt) { }
    public record PublicMessage(String message) { }
}
