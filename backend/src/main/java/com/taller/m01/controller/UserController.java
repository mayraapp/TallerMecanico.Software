package com.taller.m01.controller;

import com.taller.m01.dto.UserDtos;
import com.taller.m01.entity.UserStatus;
import com.taller.m01.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users")
public class UserController {
    private final UserService users; public UserController(UserService users) { this.users = users; }
    @GetMapping @PreAuthorize("hasAuthority('USERS_VIEW')") public UserDtos.UserPage list(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "") String role, @RequestParam(required = false) UserStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) { return users.list(query, role, status, page, size); }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('USERS_VIEW')") public UserDtos.UserResponse get(@PathVariable Long id) { return users.get(id); }
    @PostMapping @PreAuthorize("hasAuthority('USERS_CREATE')") public ResponseEntity<UserDtos.UserResponse> create(@Valid @RequestBody UserDtos.CreateUserRequest request, HttpServletRequest servletRequest) { return ResponseEntity.status(HttpStatus.CREATED).body(users.create(request, RequestInfo.ip(servletRequest))); }
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('USERS_EDIT')") public UserDtos.UserResponse update(@PathVariable Long id, @Valid @RequestBody UserDtos.UpdateUserRequest request, HttpServletRequest servletRequest) { return users.update(id, request, RequestInfo.ip(servletRequest)); }
    @PatchMapping("/{id}/status") @PreAuthorize("hasAnyAuthority('USERS_ACTIVATE','USERS_DEACTIVATE')") public UserDtos.UserResponse status(@PathVariable Long id, @Valid @RequestBody UserDtos.StatusRequest request, HttpServletRequest servletRequest) { return users.updateStatus(id, request, RequestInfo.ip(servletRequest)); }
    @PatchMapping("/{id}/role") @PreAuthorize("hasAuthority('ROLES_ASSIGN')") public UserDtos.UserResponse role(@PathVariable Long id, @Valid @RequestBody UserDtos.RoleRequest request, HttpServletRequest servletRequest) { return users.changeRole(id, request, RequestInfo.ip(servletRequest)); }
    @PostMapping("/{id}/unlock") @PreAuthorize("hasAuthority('USERS_UNLOCK')") public UserDtos.UserResponse unlock(@PathVariable Long id, HttpServletRequest servletRequest) { return users.unlock(id, RequestInfo.ip(servletRequest)); }
}
