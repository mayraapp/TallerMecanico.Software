package com.taller.m01.dto;

import com.taller.m01.entity.UserStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public final class UserDtos {
    private UserDtos() { }
    public record CreateUserRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 160) String email,
        @Pattern(regexp = "^[0-9+() .-]{7,30}$", message = "El teléfono tiene formato inválido") String phone,
        @NotBlank String temporaryPassword,
        @NotBlank String confirmation,
        @NotBlank String roleCode,
        @NotNull UserStatus status
    ) { }
    public record PublicRegistrationRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 160) String email,
        @Pattern(regexp = "^[0-9+() .-]{7,30}$", message = "El teléfono tiene formato inválido") String phone,
        @NotBlank String password,
        @NotBlank String confirmation
    ) { }
    public record UpdateUserRequest(@NotBlank @Size(max = 120) String fullName, @NotBlank @Email @Size(max = 160) String email, @Pattern(regexp = "^[0-9+() .-]{7,30}$", message = "El teléfono tiene formato inválido") String phone) { }
    public record StatusRequest(@NotNull UserStatus status) { }
    public record RoleRequest(@NotBlank String roleCode) { }
    public record UserResponse(Long id, String fullName, String email, String phone, UserStatus status, List<String> roles, String primaryRole, boolean mustChangePassword, boolean initialAdmin, Instant lastLoginAt, Instant createdAt) { }
    public record UserPage(List<UserResponse> content, int page, int size, long totalElements, int totalPages) { }
    public record RoleResponse(Long id, String code, String name, String description, List<PermissionResponse> permissions) { }
    public record PermissionResponse(Long id, String code, String name, String description) { }
    public record UpdateRolePermissionsRequest(@NotEmpty List<Long> permissionIds) { }
    public record AuditResponse(Long id, String action, String actor, String target, String ipAddress, String details, Instant createdAt) { }
    public record AuditPage(List<AuditResponse> content, int page, int size, long totalElements, int totalPages) { }
}
