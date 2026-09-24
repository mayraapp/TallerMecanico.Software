package com.taller.m01.service;

import com.taller.m01.config.AppSecurityProperties;
import com.taller.m01.entity.*;
import com.taller.m01.repository.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Set;

@Service
public class InitialAdminService {
    private final AppSecurityProperties properties; private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder encoder; private final PasswordPolicy policy;
    public InitialAdminService(AppSecurityProperties p, UserRepository u, RoleRepository r, PasswordEncoder e, PasswordPolicy policy) { properties = p; users = u; roles = r; encoder = e; this.policy = policy; }
    @Bean ApplicationRunner seedInitialAdmin() { return args -> createIfMissing(); }
    @Transactional
    public void createIfMissing() {
        if (!StringUtils.hasText(properties.getInitialAdminName()) || !StringUtils.hasText(properties.getInitialAdminEmail()) || !StringUtils.hasText(properties.getInitialAdminPassword()))
            throw new IllegalStateException("Configure INITIAL_ADMIN_NAME, INITIAL_ADMIN_EMAIL e INITIAL_ADMIN_PASSWORD en backend/.env.");
        if (users.existsByEmailIgnoreCase(properties.getInitialAdminEmail())) return;
        policy.validate(properties.getInitialAdminPassword(), properties.getInitialAdminPassword());
        Role superAdmin = roles.findByCode("SUPERADMIN").orElseThrow();
        UserAccount admin = new UserAccount(); admin.setFullName(properties.getInitialAdminName().trim()); admin.setEmail(properties.getInitialAdminEmail()); admin.setPasswordHash(encoder.encode(properties.getInitialAdminPassword())); admin.setStatus(UserStatus.ACTIVE); admin.setMustChangePassword(true); admin.setInitialAdmin(true); admin.setRoles(Set.of(superAdmin)); users.save(admin);
    }
}
