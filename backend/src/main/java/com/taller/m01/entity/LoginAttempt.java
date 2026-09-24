package com.taller.m01.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "intentos_acceso")
public class LoginAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id")
    private UserAccount user;
    @Column(nullable = false, length = 160)
    private String email;
    @Column(nullable = false)
    private boolean success;
    @Column(name = "direccion_ip", length = 64)
    private String ipAddress;
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist void created() { createdAt = Instant.now(); }
    public void setUser(UserAccount user) { this.user = user; }
    public void setEmail(String email) { this.email = email; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
