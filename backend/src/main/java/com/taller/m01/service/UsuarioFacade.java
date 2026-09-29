package com.taller.m01.service;

import com.taller.m01.dto.UserDtos;
import com.taller.m01.entity.UserStatus;
import org.springframework.stereotype.Component;

/**
 * Facade for the protected internal user-administration use cases.
 *
 * <p>Controllers delegate here and this component delegates to {@link UserService}; it does not
 * expose repositories or persistence entities to HTTP clients. Authorization remains enforced by
 * method security in {@code UserController} and hierarchy validation remains in the service.</p>
 */
@Component
public class UsuarioFacade {
    private final UserService userService;

    /**
     * Creates the facade around the reusable business service.
     *
     * @param userService service responsible for validation, BCrypt hashing and repository access
     */
    public UsuarioFacade(UserService userService) {
        this.userService = userService;
    }

    /** Lists paged users according to protected filter parameters. */
    public UserDtos.UserPage listar(String query, String role, UserStatus status, int page, int size) {
        return userService.list(query, role, status, page, size);
    }

    /** Gets a user-safe DTO without returning its password hash. */
    public UserDtos.UserResponse consultar(Long id) {
        return userService.get(id);
    }

    /**
     * Registers an internal account with a BCrypt temporary password.
     *
     * @param request validated account data and role
     * @param ip caller IP used only in the audit event
     * @return the created safe user DTO
     */
    public UserDtos.UserResponse registrarInterno(UserDtos.CreateUserRequest request, String ip) {
        return userService.create(request, ip);
    }

    /** Updates editable account data while preserving service hierarchy rules. */
    public UserDtos.UserResponse actualizar(Long id, UserDtos.UpdateUserRequest request, String ip) {
        return userService.update(id, request, ip);
    }

    /** Changes an account status after service validation. */
    public UserDtos.UserResponse actualizarEstado(Long id, UserDtos.StatusRequest request, String ip) {
        return userService.updateStatus(id, request, ip);
    }

    /** Reassigns one allowed role under the existing hierarchy controls. */
    public UserDtos.UserResponse cambiarRol(Long id, UserDtos.RoleRequest request, String ip) {
        return userService.changeRole(id, request, ip);
    }

    /** Unlocks a protected account after the existing audit and hierarchy checks. */
    public UserDtos.UserResponse desbloquear(Long id, String ip) {
        return userService.unlock(id, ip);
    }
}
