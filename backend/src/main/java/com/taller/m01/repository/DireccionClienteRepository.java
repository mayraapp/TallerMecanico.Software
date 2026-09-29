package com.taller.m01.repository;

import com.taller.m01.entity.DireccionCliente;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence-only repository for client addresses. */
public interface DireccionClienteRepository extends JpaRepository<DireccionCliente, Long> {
    /** Confirms whether the required address was persisted for a client. */
    boolean existsByClienteId(Long clienteId);
}
