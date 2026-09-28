package com.taller.m01.repository;

import com.taller.m01.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCorreoPersonalNormalizado(String correoPersonalNormalizado);
    boolean existsByTelefonoPersonalNormalizado(String telefonoPersonalNormalizado);
    boolean existsByNombreNormalizadoAndFechaNacimiento(String nombreNormalizado, LocalDate fechaNacimiento);
    Optional<Cliente> findByCorreoPersonalNormalizado(String correoPersonalNormalizado);
}
