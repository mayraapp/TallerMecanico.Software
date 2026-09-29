package com.taller.m01.service;

import com.taller.m01.config.AppSecurityProperties;
import com.taller.m01.entity.Role;
import com.taller.m01.entity.UserAccount;
import com.taller.m01.entity.UserStatus;
import com.taller.m01.repository.RoleRepository;
import com.taller.m01.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Idempotently provisions the two operational accounts requested for the local project.
 *
 * <p>Passwords are read only from ignored environment configuration, BCrypt-hashed before
 * persistence and never logged. Existing accounts are reused and their password hashes are not
 * overwritten; their required active status and operational role are reconciled.</p>
 */
@Service
public class InitialOperationalAccountsService {
    private static final String ADMINISTRATOR_EMAIL = "administrador@taller.com";
    private static final String RECEPTIONIST_EMAIL = "recepcionista@taller.com";

    private final AppSecurityProperties properties;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    /** Creates the initializer with only security and persistence collaborators. */
    public InitialOperationalAccountsService(AppSecurityProperties properties, UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.properties = properties;
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    /** Registers the initialization after Flyway has applied the role-permission migration. */
    @Bean
    ApplicationRunner seedOperationalAccounts() {
        return args -> createIfConfigured();
    }

    /**
     * Creates each missing account once or reconciles its required operational role.
     *
     * @throws IllegalStateException when a configured operational role is unavailable after migration
     */
    @Transactional
    public void createIfConfigured() {
        provision(ADMINISTRATOR_EMAIL, "Administrador del sistema", "ADMIN", properties.getInitialAdministratorPassword());
        provision(RECEPTIONIST_EMAIL, "Recepcionista", "RECEPTIONIST", properties.getInitialReceptionistPassword());
    }

    /**
     * Creates one account only when its password was provided through environment configuration.
     * Existing passwords intentionally remain unchanged to avoid silently replacing credentials.
     *
     * @param email operational account identifier
     * @param name display name assigned only to a new account
     * @param roleCode required technical role
     * @param temporaryPassword local-only value encoded with BCrypt when creation is needed
     */
    private void provision(String email, String name, String roleCode, String temporaryPassword) {
        if (!StringUtils.hasText(temporaryPassword)) return;
        Role role = roles.findByCode(roleCode).orElseThrow(() -> new IllegalStateException("No existe el rol operativo configurado."));
        UserAccount account = users.findByEmailIgnoreCase(email).orElse(null);
        if (account == null) {
            account = new UserAccount();
            account.setFullName(name);
            account.setEmail(email);
            account.setPasswordHash(encoder.encode(temporaryPassword));
            account.setMustChangePassword(true);
            account.setInitialAdmin(false);
        }
        account.setStatus(UserStatus.ACTIVE);
        account.setLockedUntil(null);
        account.setFailedAttempts(0);
        account.setRoles(Set.of(role));
        users.save(account);
    }
}
