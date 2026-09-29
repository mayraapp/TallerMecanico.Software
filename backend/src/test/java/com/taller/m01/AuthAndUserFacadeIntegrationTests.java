package com.taller.m01;

import com.taller.m01.entity.Role;
import com.taller.m01.entity.UserAccount;
import com.taller.m01.entity.UserStatus;
import com.taller.m01.repository.RoleRepository;
import com.taller.m01.repository.UserRepository;
import com.taller.m01.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that the new controller facades preserve the existing login and internal-user behavior.
 * Runtime-generated passwords are BCrypt-hashed and never appear as fixed source credentials.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthAndUserFacadeIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;

    @Test
    void loginThroughAuthFacadeReturnsOnlySafeSessionData() throws Exception {
        String password = passwordTemporalAleatoria();
        String email = "login." + UUID.randomUUID() + "@automanager.test";
        crearUsuario("ADMIN", email, password);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("TM_SESSION=")))
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.user.permissions").isArray())
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void registroInternoThroughUsuarioFacadeKeepsBcryptAndAuthorization() throws Exception {
        UserAccount superadmin = crearUsuario("SUPERADMIN", "superadmin." + UUID.randomUUID() + "@automanager.test", passwordTemporalAleatoria());
        String password = passwordTemporalAleatoria();
        String email = "interno." + UUID.randomUUID() + "@automanager.test";

        mockMvc.perform(post("/api/users")
                .with(user(new AuthenticatedUser(superadmin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Cuenta Interna\",\"email\":\"%s\",\"phone\":null,\"temporaryPassword\":\"%s\",\"confirmation\":\"%s\",\"roleCode\":\"RECEPTIONIST\",\"status\":\"ACTIVE\"}".formatted(email, password, password)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.roles[0]").value("RECEPTIONIST"));

        UserAccount created = users.findByEmailIgnoreCase(email).orElseThrow();
        assertTrue(encoder.matches(password, created.getPasswordHash()));
    }

    /** Creates an active, test-only account with a BCrypt hash and one existing role. */
    private UserAccount crearUsuario(String roleCode, String email, String password) {
        Role role = roles.findByCode(roleCode).orElseThrow();
        UserAccount account = new UserAccount();
        account.setFullName("Prueba " + roleCode);
        account.setEmail(email);
        account.setPhone("5550000000");
        account.setPasswordHash(encoder.encode(password));
        account.setStatus(UserStatus.ACTIVE);
        account.setMustChangePassword(false);
        account.setRoles(Set.of(role));
        return users.saveAndFlush(account);
    }

    /** Produces a non-persisted runtime password satisfying the existing password policy. */
    private String passwordTemporalAleatoria() {
        SecureRandom random = new SecureRandom();
        char uppercase = (char) ('A' + random.nextInt(26));
        char lowercase = (char) ('a' + random.nextInt(26));
        char digit = (char) ('0' + random.nextInt(10));
        return new String(new char[]{uppercase, lowercase, digit, '!'}) + UUID.randomUUID().toString().replace("-", "");
    }
}
