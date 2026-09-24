package com.taller.m01.service;

import com.taller.m01.dto.*;
import com.taller.m01.entity.*;
import java.util.*;

public final class DtoMapper {
    private DtoMapper() { }
    public static UserDtos.PermissionResponse permission(Permission p) { return new UserDtos.PermissionResponse(p.getId(), p.getCode(), p.getName(), p.getDescription()); }
    public static UserDtos.RoleResponse role(Role r) { return new UserDtos.RoleResponse(r.getId(), r.getCode(), r.getName(), r.getDescription(), r.getPermissions().stream().map(DtoMapper::permission).sorted(Comparator.comparing(UserDtos.PermissionResponse::name)).toList()); }
    public static UserDtos.UserResponse user(UserAccount u) {
        List<String> roles = u.getRoles().stream().map(Role::getCode).sorted().toList();
        return new UserDtos.UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getStatus(), roles, roles.isEmpty() ? null : roles.getFirst(), u.isMustChangePassword(), u.isInitialAdmin(), u.getLastLoginAt(), u.getCreatedAt());
    }
    public static AuthDtos.AuthenticatedUser authenticated(UserAccount u) {
        List<String> roles = u.getRoles().stream().map(Role::getCode).sorted().toList();
        List<String> permissions = u.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getCode).distinct().sorted().toList();
        return new AuthDtos.AuthenticatedUser(u.getId(), u.getFullName(), u.getEmail(), roles.isEmpty() ? null : roles.getFirst(), roles, permissions, u.isMustChangePassword());
    }
}
