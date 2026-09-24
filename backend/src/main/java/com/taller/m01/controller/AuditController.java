package com.taller.m01.controller;

import com.taller.m01.dto.UserDtos;
import com.taller.m01.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/audit")
public class AuditController {
    private final AuditService audit; public AuditController(AuditService audit) { this.audit = audit; }
    @GetMapping @PreAuthorize("hasAuthority('AUDIT_VIEW')") public UserDtos.AuditPage list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return audit.list(page, size); }
}
