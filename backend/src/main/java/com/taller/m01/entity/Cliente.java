package com.taller.m01.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre_completo", nullable = false, length = 160)
    private String nombreCompleto;
    @Column(name = "nombre_normalizado", nullable = false, length = 160)
    private String nombreNormalizado;
    @Column(name = "contacto_alternativo", length = 160)
    private String contactoAlternativo;
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;
    @Column(name = "telefono_personal_normalizado", nullable = false, length = 15)
    private String telefonoPersonalNormalizado;
    @Column(name = "telefono_trabajo_normalizado", length = 15)
    private String telefonoTrabajoNormalizado;
    @Column(name = "correo_personal_normalizado", nullable = false, length = 160)
    private String correoPersonalNormalizado;
    @Column(name = "correo_trabajo_normalizado", length = 160)
    private String correoTrabajoNormalizado;
    @Column(name = "fotografia_referencia", length = 255)
    private String fotografiaReferencia;
    @Column(name = "estado_registro", nullable = false, length = 16)
    private String estadoRegistro = "ACTIVO";
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "creado_por_id", nullable = false)
    private UserAccount creadoPor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actualizado_por_id", nullable = false)
    private UserAccount actualizadoPor;

    @PrePersist void alCrear() { creadoEn = actualizadoEn = Instant.now(); }
    @PreUpdate void alActualizar() { actualizadoEn = Instant.now(); }
    public Long getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String value) { nombreCompleto = value; }
    public String getNombreNormalizado() { return nombreNormalizado; }
    public void setNombreNormalizado(String value) { nombreNormalizado = value; }
    public String getContactoAlternativo() { return contactoAlternativo; }
    public void setContactoAlternativo(String value) { contactoAlternativo = value; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate value) { fechaNacimiento = value; }
    public String getTelefonoPersonalNormalizado() { return telefonoPersonalNormalizado; }
    public void setTelefonoPersonalNormalizado(String value) { telefonoPersonalNormalizado = value; }
    public String getTelefonoTrabajoNormalizado() { return telefonoTrabajoNormalizado; }
    public void setTelefonoTrabajoNormalizado(String value) { telefonoTrabajoNormalizado = value; }
    public String getCorreoPersonalNormalizado() { return correoPersonalNormalizado; }
    public void setCorreoPersonalNormalizado(String value) { correoPersonalNormalizado = value; }
    public String getCorreoTrabajoNormalizado() { return correoTrabajoNormalizado; }
    public void setCorreoTrabajoNormalizado(String value) { correoTrabajoNormalizado = value; }
    public String getFotografiaReferencia() { return fotografiaReferencia; }
    public void setFotografiaReferencia(String value) { fotografiaReferencia = value; }
    public String getEstadoRegistro() { return estadoRegistro; }
    public void setEstadoRegistro(String value) { estadoRegistro = value; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public UserAccount getCreadoPor() { return creadoPor; }
    public void setCreadoPor(UserAccount value) { creadoPor = value; }
    public UserAccount getActualizadoPor() { return actualizadoPor; }
    public void setActualizadoPor(UserAccount value) { actualizadoPor = value; }
}
