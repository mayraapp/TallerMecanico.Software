package com.taller.m01.repository;

import com.taller.m01.entity.DireccionCliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionClienteRepository extends JpaRepository<DireccionCliente, Long> {
    boolean existsByClienteId(Long clienteId);
}
