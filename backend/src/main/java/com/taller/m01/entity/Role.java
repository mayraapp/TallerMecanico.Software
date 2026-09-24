package com.taller.m01.entity;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, unique = true, length = 80)
    private String name;
    @Column(nullable = false, length = 255)
    private String description;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"), inverseJoinColumns = @JoinColumn(name = "permiso_id"))
    private Set<Permission> permissions = new LinkedHashSet<>();

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}
