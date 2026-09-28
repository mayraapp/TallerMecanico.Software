package com.taller.m01.service;

import com.taller.m01.dto.ClienteDtos;
import com.taller.m01.entity.*;
import com.taller.m01.exception.ApiException;
import com.taller.m01.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.time.*;
import java.util.*;

@Service
public class ClienteService {
    private static final String DUPLICATE_MESSAGE = "Este cliente ya se encuentra registrado. Revisa el correo electrónico o el teléfono personal.";
    private final ClienteRepository clientes;
    private final DireccionClienteRepository direcciones;

    public ClienteService(ClienteRepository clientes, DireccionClienteRepository direcciones) {
        this.clientes = clientes;
        this.direcciones = direcciones;
    }

    public record DatosNormalizados(String nombreCompleto, String nombreNormalizado, String contactoAlternativo, LocalDate fechaNacimiento,
                                    String telefonoPersonal, String telefonoTrabajo, String emailPersonal, String emailTrabajo,
                                    String calleNumero, String colonia, String municipio, String estado, String codigoPostal) { }

    public DatosNormalizados normalizarDatos(ClienteDtos.CreateClientRequest request) {
        ClienteDtos.AddressRequest address = request.direccion();
        return new DatosNormalizados(
            limpiarEspacios(request.nombreCompleto()), normalizarNombre(request.nombreCompleto()), limpiarOpcional(request.contactoAlternativo()), request.fechaNacimiento(),
            normalizarTelefono(request.telefonoPersonal()), normalizarTelefono(request.telefonoTrabajo()), normalizarCorreo(request.emailPersonal()), normalizarCorreo(request.emailTrabajo()),
            limpiarEspacios(address.calleNumero()), limpiarEspacios(address.colonia()), limpiarEspacios(address.municipio()), limpiarEspacios(address.estado()), limpiarEspacios(address.codigoPostal())
        );
    }

    public void validarDatos(DatosNormalizados data) {
        if (!data.fechaNacimiento().isBefore(LocalDate.now()) || data.fechaNacimiento().isBefore(LocalDate.now().minusYears(120))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FECHA_NACIMIENTO_INVALIDA", "La fecha de nacimiento debe ser anterior a hoy y estar dentro de un rango razonable.");
        }
        validarTelefono(data.telefonoPersonal(), "TELÉFONO_PERSONAL_INVALIDO", "El teléfono personal debe contener entre 10 y 15 dígitos.");
        if (data.telefonoTrabajo() != null) validarTelefono(data.telefonoTrabajo(), "TELÉFONO_TRABAJO_INVALIDO", "El teléfono de trabajo debe contener entre 10 y 15 dígitos.");
        if (!data.codigoPostal().matches("\\d{5}")) throw new ApiException(HttpStatus.BAD_REQUEST, "CODIGO_POSTAL_INVALIDO", "El código postal debe contener cinco dígitos.");
    }

    @Transactional(readOnly = true)
    public void verificarDuplicado(DatosNormalizados data) {
        if (clientes.existsByCorreoPersonalNormalizado(data.emailPersonal())
            || clientes.existsByTelefonoPersonalNormalizado(data.telefonoPersonal())
            || clientes.existsByNombreNormalizadoAndFechaNacimiento(data.nombreNormalizado(), data.fechaNacimiento())) {
            throw clienteDuplicado();
        }
    }

    @Transactional
    public Cliente guardarCliente(DatosNormalizados data, UserAccount actor) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNombreCompleto(data.nombreCompleto());
            cliente.setNombreNormalizado(data.nombreNormalizado());
            cliente.setContactoAlternativo(data.contactoAlternativo());
            cliente.setFechaNacimiento(data.fechaNacimiento());
            cliente.setTelefonoPersonalNormalizado(data.telefonoPersonal());
            cliente.setTelefonoTrabajoNormalizado(data.telefonoTrabajo());
            cliente.setCorreoPersonalNormalizado(data.emailPersonal());
            cliente.setCorreoTrabajoNormalizado(data.emailTrabajo());
            cliente.setCreadoPor(actor);
            cliente.setActualizadoPor(actor);
            cliente = clientes.saveAndFlush(cliente);

            DireccionCliente direccion = new DireccionCliente();
            direccion.setCliente(cliente);
            direccion.setCalleNumero(data.calleNumero());
            direccion.setColonia(data.colonia());
            direccion.setMunicipio(data.municipio());
            direccion.setEstado(data.estado());
            direccion.setCodigoPostal(data.codigoPostal());
            direccion.setCreadoPor(actor);
            direccion.setActualizadoPor(actor);
            direcciones.saveAndFlush(direccion);
            return cliente;
        } catch (DataIntegrityViolationException ex) {
            throw clienteDuplicado();
        }
    }

    @Transactional
    public Cliente guardarFotografia(Cliente cliente, String referencia, UserAccount actor) {
        cliente.setFotografiaReferencia(referencia);
        cliente.setActualizadoPor(actor);
        return clientes.saveAndFlush(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clientes.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CLIENTE_NO_ENCONTRADO", "El cliente solicitado no existe."));
    }

    public ClienteDtos.ClientResponse respuesta(Cliente cliente) {
        String fotografiaUrl = cliente.getFotografiaReferencia() == null ? null : "/api/clientes/" + cliente.getId() + "/fotografia";
        return new ClienteDtos.ClientResponse(cliente.getId(), cliente.getNombreCompleto(), cliente.getFechaNacimiento(), edadDe(cliente.getFechaNacimiento()), cliente.getTelefonoPersonalNormalizado(), cliente.getCorreoPersonalNormalizado(), cliente.getEstadoRegistro(), fotografiaUrl, "Cliente registrado correctamente.");
    }

    private ApiException clienteDuplicado() { return new ApiException(HttpStatus.CONFLICT, "CLIENTE_DUPLICADO", DUPLICATE_MESSAGE); }
    private void validarTelefono(String telefono, String code, String message) {
        if (telefono == null || !telefono.matches("\\d{10,15}")) throw new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }
    private String limpiarEspacios(String value) { return value == null ? null : value.trim().replaceAll("\\s+", " "); }
    private String limpiarOpcional(String value) { String result = limpiarEspacios(value); return result == null || result.isBlank() ? null : result; }
    private String normalizarCorreo(String value) { String result = limpiarOpcional(value); return result == null ? null : result.toLowerCase(Locale.ROOT); }
    private String normalizarTelefono(String value) { String result = limpiarOpcional(value); return result == null ? null : result.replaceAll("[\\s()\\-]", ""); }
    private String normalizarNombre(String value) {
        String cleaned = limpiarEspacios(value);
        String withoutAccents = Normalizer.normalize(cleaned, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }
    private int edadDe(LocalDate fechaNacimiento) { return Period.between(fechaNacimiento, LocalDate.now()).getYears(); }
}
