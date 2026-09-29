package com.taller.m01.repository;

import com.taller.m01.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

/** Persistence-only repository for clients and their duplicate keys. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    /** Tests the normalized personal email unique key. */
    boolean existsByCorreoPersonalNormalizado(String correoPersonalNormalizado);
    /** Tests the normalized personal phone unique key. */
    boolean existsByTelefonoPersonalNormalizado(String telefonoPersonalNormalizado);
    /** Tests the complementary normalized-name plus birth-date unique key. */
    boolean existsByNombreNormalizadoAndFechaNacimiento(String nombreNormalizado, LocalDate fechaNacimiento);
    /** Retrieves by the normalized personal email when a protected use case needs it. */
    Optional<Cliente> findByCorreoPersonalNormalizado(String correoPersonalNormalizado);
}
