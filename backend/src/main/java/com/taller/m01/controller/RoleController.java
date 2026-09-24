package com.taller.m01.controller;

import com.taller.m01.dto.UserDtos;
import com.taller.m01.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api")
public class RoleController {
    private final RoleService roles; public RoleController(RoleService roles) { this.roles = roles; }
    @GetMapping("/roles") @PreAuthorize("hasAuthority('PERMISSIONS_MANAGE')") public List<UserDtos.RoleResponse> listRoles() { return roles.roles(); }
    @GetMapping("/permissions") @PreAuthorize("hasAuthority('PERMISSIONS_MANAGE')") public List<UserDtos.PermissionResponse> permissions() { return roles.permissions(); }
    @PutMapping("/roles/{id}/permissions") @PreAuthorize("hasAuthority('PERMISSIONS_MANAGE')") public UserDtos.RoleResponse update(@PathVariable Long id, @Valid @RequestBody UserDtos.UpdateRolePermissionsRequest request, HttpServletRequest servletRequest) { return roles.updatePermissions(id, request, RequestInfo.ip(servletRequest)); }
}
