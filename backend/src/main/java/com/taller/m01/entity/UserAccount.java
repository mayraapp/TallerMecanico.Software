package com.taller.m01.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String fullName;
    @Column(nullable = false, unique = true, length = 160)
    private String email;
    @Column(length = 30)
    private String phone;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private UserStatus status = UserStatus.ACTIVE;
    @Column(name = "intentos_fallidos", nullable = false)
    private int failedAttempts;
    @Column(name = "bloqueado_hasta")
    private Instant lockedUntil;
    @Column(name = "ultimo_acceso")
    private Instant lastLoginAt;
    @Column(name = "debe_cambiar_password", nullable = false)
    private boolean mustChangePassword = true;
    @Column(name = "version_sesion", nullable = false)
    private int sessionVersion = 1;
    @Column(name = "es_admin_inicial", nullable = false)
    private boolean initialAdmin;
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "actualizado_en", nullable = false)
    private Instant updatedAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    @PrePersist void created() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void updated() { updatedAt = Instant.now(); }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim().toLowerCase(); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public int getSessionVersion() { return sessionVersion; }
    public void setSessionVersion(int sessionVersion) { this.sessionVersion = sessionVersion; }
    public boolean isInitialAdmin() { return initialAdmin; }
    public void setInitialAdmin(boolean initialAdmin) { this.initialAdmin = initialAdmin; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public boolean hasRole(String code) { return roles.stream().anyMatch(role -> role.getCode().equals(code)); }
}
