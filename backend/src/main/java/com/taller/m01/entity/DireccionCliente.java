package com.taller.m01.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "direcciones_cliente")
/**
 * Persistent initial address associated with one client through a foreign key.
 *
 * <p>The separate entity keeps the client independent from future workshop associations.</p>
 */
public class DireccionCliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;
    @Column(name = "calle_numero", nullable = false, length = 180)
    private String calleNumero;
    @Column(nullable = false, length = 120)
    private String colonia;
    @Column(nullable = false, length = 120)
    private String municipio;
    @Column(nullable = false, length = 120)
    private String estado;
    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;
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
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente value) { cliente = value; }
    public String getCalleNumero() { return calleNumero; }
    public void setCalleNumero(String value) { calleNumero = value; }
    public String getColonia() { return colonia; }
    public void setColonia(String value) { colonia = value; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String value) { municipio = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { estado = value; }
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String value) { codigoPostal = value; }
    public String getEstadoRegistro() { return estadoRegistro; }
    public void setEstadoRegistro(String value) { estadoRegistro = value; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public UserAccount getCreadoPor() { return creadoPor; }
    public void setCreadoPor(UserAccount value) { creadoPor = value; }
    public UserAccount getActualizadoPor() { return actualizadoPor; }
    public void setActualizadoPor(UserAccount value) { actualizadoPor = value; }
}
