package com.taller.m01.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** DTO boundary types for protected client registration; JPA entities are never returned directly. */
public final class ClienteDtos {
    private ClienteDtos() { }
    private static final String NOMBRE = "^[\\p{L}](?:[\\p{L}\\s'’-]*[\\p{L}])?$";
    private static final String TELEFONO = "^[0-9()\\-\\s]{7,25}$";

    /** Input DTO for one client and its required initial address. */
    public record CreateClientRequest(
        @NotBlank @Size(max = 160) @Pattern(regexp = NOMBRE, message = "El nombre solo permite letras, espacios, apóstrofes y guiones") String nombreCompleto,
        @Size(max = 160) @Pattern(regexp = "^$|" + NOMBRE, message = "El contacto alternativo tiene formato inválido") String contactoAlternativo,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank @Pattern(regexp = TELEFONO, message = "El teléfono personal tiene formato inválido") String telefonoPersonal,
        @Pattern(regexp = "^$|" + TELEFONO, message = "El teléfono de trabajo tiene formato inválido") String telefonoTrabajo,
        @NotBlank @Email @Size(max = 160) String emailPersonal,
        @Email @Size(max = 160) String emailTrabajo,
        @NotNull @Valid AddressRequest direccion
    ) { }

    /** Nested input DTO that validates the required address. */
    public record AddressRequest(
        @NotBlank @Size(max = 180) String calleNumero,
        @NotBlank @Size(max = 120) String colonia,
        @NotBlank @Size(max = 120) @Pattern(regexp = NOMBRE, message = "El municipio tiene formato inválido") String municipio,
        @NotBlank @Size(max = 120) @Pattern(regexp = NOMBRE, message = "El estado tiene formato inválido") String estado,
        @NotBlank @Pattern(regexp = "^\\d{5}$", message = "El código postal debe tener cinco dígitos") String codigoPostal
    ) { }

    /** Safe output DTO with calculated age and, when present, a protected photo URL. */
    public record ClientResponse(Long id, String nombreCompleto, LocalDate fechaNacimiento, int edad, String telefonoPersonal, String emailPersonal, String estado, String fotografiaUrl, String mensaje) { }
}
