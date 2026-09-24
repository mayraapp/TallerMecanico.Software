package com.taller.m01.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "codigos_recuperacion")
public class PasswordRecoveryCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id")
    private UserAccount user;
    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codeHash;
    @Column(name = "expira_en", nullable = false)
    private Instant expiresAt;
    @Column(name = "usado_en")
    private Instant usedAt;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist void created() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
}
