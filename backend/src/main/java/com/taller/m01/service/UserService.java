package com.taller.m01.service;

import com.taller.m01.dto.UserDtos;
import com.taller.m01.entity.*;
import com.taller.m01.exception.ApiException;
import com.taller.m01.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class UserService {
    private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder encoder; private final PasswordPolicy policy; private final CurrentUserService current; private final AuditService audit;
    public UserService(UserRepository users, RoleRepository roles, PasswordEncoder encoder, PasswordPolicy policy, CurrentUserService current, AuditService audit) { this.users = users; this.roles = roles; this.encoder = encoder; this.policy = policy; this.current = current; this.audit = audit; }
    @Transactional(readOnly = true)
    public UserDtos.UserPage list(String query, String role, UserStatus status, int page, int size) {
        Page<UserAccount> result = users.search(query == null ? "" : query.trim(), role == null ? "" : role.trim(), status, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("createdAt").descending()));
        return new UserDtos.UserPage(result.getContent().stream().map(DtoMapper::user).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
    @Transactional(readOnly = true)
    public UserDtos.UserResponse get(Long id) { return DtoMapper.user(find(id)); }
    @Transactional
    public UserDtos.UserResponse create(UserDtos.CreateUserRequest request, String ip) {
        UserAccount actor = superAdmin(); policy.validate(request.temporaryPassword(), request.confirmation());
        if (users.existsByEmailIgnoreCase(request.email())) throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "El correo ya está registrado.");
        Role role = assignableRole(request.roleCode());
        UserAccount user = new UserAccount(); user.setFullName(request.fullName().trim()); user.setEmail(request.email()); user.setPhone(blankToNull(request.phone())); user.setPasswordHash(encoder.encode(request.temporaryPassword())); user.setStatus(request.status()); user.setMustChangePassword(true); user.setRoles(Set.of(role)); users.save(user); audit.record(actor, user, "USER_CREATED", ip, "Usuario creado con rol " + role.getCode() + '.'); return DtoMapper.user(user);
    }
    @Transactional
    public void registerPublic(UserDtos.PublicRegistrationRequest request, String ip) {
        policy.validate(request.password(), request.confirmation());
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) return;
        Role pending = roles.findByCode("PENDING").orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PENDING_ROLE_MISSING", "No se pudo procesar la solicitud."));
        UserAccount user = new UserAccount(); user.setFullName(request.fullName().trim()); user.setEmail(email); user.setPhone(blankToNull(request.phone())); user.setPasswordHash(encoder.encode(request.password())); user.setStatus(UserStatus.INACTIVE); user.setMustChangePassword(true); user.setRoles(Set.of(pending)); users.save(user);
        audit.record(null, user, "PUBLIC_REGISTRATION_REQUESTED", ip, "Solicitud pública pendiente de aprobación.");
    }
    @Transactional
    public UserDtos.UserResponse update(Long id, UserDtos.UpdateUserRequest request, String ip) {
        UserAccount actor = superAdmin(); UserAccount target = manageTarget(id, actor); if (!target.getEmail().equalsIgnoreCase(request.email()) && users.existsByEmailIgnoreCase(request.email())) throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "El correo ya está registrado.");
        target.setFullName(request.fullName().trim()); target.setEmail(request.email()); target.setPhone(blankToNull(request.phone())); users.save(target); audit.record(actor, target, "USER_UPDATED", ip, "Datos de usuario actualizados."); return DtoMapper.user(target);
    }
    @Transactional
    public UserDtos.UserResponse updateStatus(Long id, UserDtos.StatusRequest request, String ip) {
        UserAccount actor = superAdmin(); UserAccount target = manageTarget(id, actor); if (request.status() == UserStatus.BLOCKED) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATUS", "El bloqueo se aplica automáticamente por intentos fallidos.");
        if (request.status() == UserStatus.INACTIVE && target.hasRole("SUPERADMIN") && users.countActiveSuperAdmins() <= 1) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "LAST_SUPERADMIN", "No puede desactivar al último Superadministrador activo.");
        target.setStatus(request.status()); if (request.status() == UserStatus.ACTIVE) { target.setLockedUntil(null); target.setFailedAttempts(0); } users.save(target); audit.record(actor, target, request.status() == UserStatus.ACTIVE ? "USER_ACTIVATED" : "USER_DEACTIVATED", ip, "Estado cambiado a " + request.status() + '.'); return DtoMapper.user(target);
    }
    @Transactional
    public UserDtos.UserResponse changeRole(Long id, UserDtos.RoleRequest request, String ip) {
        UserAccount actor = superAdmin(); UserAccount target = manageTarget(id, actor); Role role = assignableRole(request.roleCode()); target.setRoles(Set.of(role)); users.save(target); audit.record(actor, target, "ROLE_CHANGED", ip, "Rol principal cambiado a " + role.getCode() + '.'); return DtoMapper.user(target);
    }
    @Transactional
    public UserDtos.UserResponse unlock(Long id, String ip) {
        UserAccount actor = superAdmin(); UserAccount target = manageTarget(id, actor); target.setStatus(UserStatus.ACTIVE); target.setFailedAttempts(0); target.setLockedUntil(null); users.save(target); audit.record(actor, target, "ACCOUNT_UNLOCKED", ip, "Cuenta desbloqueada manualmente."); return DtoMapper.user(target);
    }
    private UserAccount superAdmin() { current.requireSuperAdmin(); return current.require(); }
    private UserAccount find(Long id) { return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.")); }
    private UserAccount manageTarget(Long id, UserAccount actor) {
        UserAccount target = find(id); if (target.getId().equals(actor.getId())) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "SELF_PRIVILEGE_CHANGE", "No puede administrar su propia cuenta desde este módulo.");
        if (target.isInitialAdmin()) throw new ApiException(HttpStatus.FORBIDDEN, "INITIAL_ADMIN_PROTECTED", "No se puede modificar al Superadministrador principal.");
        if (target.hasRole("SUPERADMIN")) throw new ApiException(HttpStatus.FORBIDDEN, "ROLE_HIERARCHY", "Solo se administran cuentas con roles inferiores.");
        return target;
    }
    private Role assignableRole(String roleCode) { Role role = roles.findByCode(roleCode.trim().toUpperCase()).orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ROLE_INVALID", "El rol indicado no existe.")); if ("SUPERADMIN".equals(role.getCode())) throw new ApiException(HttpStatus.FORBIDDEN, "ROLE_HIERARCHY", "No puede asignar el rol Superadministrador desde este módulo."); return role; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
