package com.taller.m01.controller;

import com.taller.m01.dto.UserDtos;
import com.taller.m01.entity.UserStatus;
import com.taller.m01.service.UsuarioFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users")
/**
 * Protected HTTP boundary for internal user administration.
 *
 * <p>Phase 02 routes every operation through {@link UsuarioFacade}; the existing Spring Security
 * expressions remain the final authorization control and no endpoint becomes public.</p>
 */
public class UserController {
    private final UsuarioFacade usuarios;
    /** Creates the controller with the protected user-use-case facade. */
    public UserController(UsuarioFacade usuarios) { this.usuarios = usuarios; }
    /** Lists accounts only for callers carrying {@code USERS_VIEW}. */
    @GetMapping @PreAuthorize("hasAuthority('USERS_VIEW')") public UserDtos.UserPage list(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "") String role, @RequestParam(required = false) UserStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return usuarios.listar(query, role, status, page, size); }
    /** Returns a safe account DTO only for callers carrying {@code USERS_VIEW}. */
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('USERS_VIEW')") public UserDtos.UserResponse get(@PathVariable Long id) { return usuarios.consultar(id); }
    /** Registers an internal user through the facade; public registration is handled by a different controller. */
    @PostMapping @PreAuthorize("hasAuthority('USERS_CREATE')") public ResponseEntity<UserDtos.UserResponse> create(@Valid @RequestBody UserDtos.CreateUserRequest request, HttpServletRequest servletRequest) { return ResponseEntity.status(HttpStatus.CREATED).body(usuarios.registrarInterno(request, RequestInfo.ip(servletRequest))); }
    /** Delegates account data updates after {@code USERS_EDIT} authorization. */
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('USERS_EDIT')") public UserDtos.UserResponse update(@PathVariable Long id, @Valid @RequestBody UserDtos.UpdateUserRequest request, HttpServletRequest servletRequest) { return usuarios.actualizar(id, request, RequestInfo.ip(servletRequest)); }
    /** Delegates active/inactive updates after existing authorization and hierarchy validation. */
    @PatchMapping("/{id}/status") @PreAuthorize("hasAnyAuthority('USERS_ACTIVATE','USERS_DEACTIVATE')") public UserDtos.UserResponse status(@PathVariable Long id, @Valid @RequestBody UserDtos.StatusRequest request, HttpServletRequest servletRequest) { return usuarios.actualizarEstado(id, request, RequestInfo.ip(servletRequest)); }
    /** Delegates protected role assignment. */
    @PatchMapping("/{id}/role") @PreAuthorize("hasAuthority('ROLES_ASSIGN')") public UserDtos.UserResponse role(@PathVariable Long id, @Valid @RequestBody UserDtos.RoleRequest request, HttpServletRequest servletRequest) { return usuarios.cambiarRol(id, request, RequestInfo.ip(servletRequest)); }
    /** Delegates account unlock after {@code USERS_UNLOCK} authorization. */
    @PostMapping("/{id}/unlock") @PreAuthorize("hasAuthority('USERS_UNLOCK')") public UserDtos.UserResponse unlock(@PathVariable Long id, HttpServletRequest servletRequest) { return usuarios.desbloquear(id, RequestInfo.ip(servletRequest)); }
}
