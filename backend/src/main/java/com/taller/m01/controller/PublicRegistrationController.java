package com.taller.m01.controller;

import com.taller.m01.dto.AuthDtos;
import com.taller.m01.dto.UserDtos;
import com.taller.m01.service.RateLimitService;
import com.taller.m01.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicRegistrationController {
    private final UserService users;
    private final RateLimitService rateLimit;

    public PublicRegistrationController(UserService users, RateLimitService rateLimit) { this.users = users; this.rateLimit = rateLimit; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.PublicMessage register(@Valid @RequestBody UserDtos.PublicRegistrationRequest request, HttpServletRequest servletRequest) {
        rateLimit.check("public-registration", RequestInfo.ip(servletRequest));
        users.registerPublic(request, RequestInfo.ip(servletRequest));
        return new AuthDtos.PublicMessage("Solicitud recibida. Un Superadministrador debe aprobar la cuenta antes de que pueda iniciar sesión.");
    }
}
