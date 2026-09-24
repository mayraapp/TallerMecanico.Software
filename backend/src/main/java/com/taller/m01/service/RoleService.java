package com.taller.m01.service;

import com.taller.m01.dto.UserDtos;
import com.taller.m01.entity.*;
import com.taller.m01.exception.ApiException;
import com.taller.m01.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RoleService {
    private final RoleRepository roles; private final PermissionRepository permissions; private final CurrentUserService current; private final AuditService audit;
    public RoleService(RoleRepository roles, PermissionRepository permissions, CurrentUserService current, AuditService audit) { this.roles = roles; this.permissions = permissions; this.current = current; this.audit = audit; }
    @Transactional(readOnly = true) public List<UserDtos.RoleResponse> roles() { return roles.findAllByOrderByNameAsc().stream().map(DtoMapper::role).toList(); }
    @Transactional(readOnly = true) public List<UserDtos.PermissionResponse> permissions() { return permissions.findAllByOrderByNameAsc().stream().map(DtoMapper::permission).toList(); }
    @Transactional
    public UserDtos.RoleResponse updatePermissions(Long roleId, UserDtos.UpdateRolePermissionsRequest request, String ip) {
        current.requireSuperAdmin(); UserAccount actor = current.require(); Role role = roles.findById(roleId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "Rol no encontrado.")); if ("SUPERADMIN".equals(role.getCode())) throw new ApiException(HttpStatus.FORBIDDEN, "SUPERADMIN_PERMISSIONS", "Los permisos del Superadministrador no pueden modificarse.");
        Set<Permission> selected = new LinkedHashSet<>(permissions.findAllById(request.permissionIds())); if (selected.size() != request.permissionIds().size()) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PERMISSION_INVALID", "Uno o más permisos no existen.");
        role.setPermissions(selected); roles.save(role); audit.record(actor, null, "ROLE_PERMISSIONS_CHANGED", ip, "Permisos modificados para el rol " + role.getCode() + '.'); return DtoMapper.role(role);
    }
}
