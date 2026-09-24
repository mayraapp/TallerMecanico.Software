package com.taller.m01.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permisos")
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 255)
    private String description;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
