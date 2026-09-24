package com.taller.m01.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auditoria")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "responsable_id")
    private UserAccount actor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "afectado_id")
    private UserAccount target;
    @Column(nullable = false, length = 70)
    private String action;
    @Column(name = "direccion_ip", length = 64)
    private String ipAddress;
    @Column(nullable = false, length = 500)
    private String details;
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist void created() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public UserAccount getActor() { return actor; }
    public UserAccount getTarget() { return target; }
    public String getAction() { return action; }
    public String getIpAddress() { return ipAddress; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
    public void setActor(UserAccount actor) { this.actor = actor; }
    public void setTarget(UserAccount target) { this.target = target; }
    public void setAction(String action) { this.action = action; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setDetails(String details) { this.details = details; }
}
