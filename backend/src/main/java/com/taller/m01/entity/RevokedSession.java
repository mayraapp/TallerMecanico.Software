package com.taller.m01.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sesiones_revocadas")
public class RevokedSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String jti;
    @Column(name = "expira_en", nullable = false)
    private Instant expiresAt;
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist void created() { createdAt = Instant.now(); }
    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
