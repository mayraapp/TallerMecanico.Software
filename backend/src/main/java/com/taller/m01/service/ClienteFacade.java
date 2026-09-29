package com.taller.m01.service;

import com.taller.m01.dto.ClienteDtos;
import com.taller.m01.entity.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Component
/**
 * Coordinates the protected client-registration use case.
 *
 * <p>This facade keeps controller and file concerns out of {@link ClienteService}: it validates an
 * optional image before database work, runs the transaction, prevents orphan files and records a
 * non-sensitive audit event.</p>
 */
public class ClienteFacade {
    private final ClienteService clientes;
    private final ClientePhotoStorage fotografias;
    private final AuditService auditoria;

    public ClienteFacade(ClienteService clientes, ClientePhotoStorage fotografias, AuditService auditoria) {
        this.clientes = clientes;
        this.fotografias = fotografias;
        this.auditoria = auditoria;
    }

    @Transactional
    /**
     * Registers one client, its address and optional private photo as one audited transaction.
     *
     * @param request validated client DTO
     * @param fotografia optional multipart image
     * @param actor authenticated administrator or receptionist
     * @param ip source IP for audit only
     * @return safe client response with dynamically calculated age
     * @throws com.taller.m01.exception.ApiException for validation, duplicate, photo or storage failures
     */
    public ClienteDtos.ClientResponse registrarCliente(ClienteDtos.CreateClientRequest request, MultipartFile fotografia, UserAccount actor, String ip) {
        ClientePhotoStorage.FotografiaValidada fotografiaValidada = fotografias.validar(fotografia);
        ClienteService.DatosNormalizados datos = clientes.normalizarDatos(request);
        clientes.validarDatos(datos);
        clientes.verificarDuplicado(datos);
        Cliente cliente = clientes.guardarCliente(datos, actor);
        if (fotografiaValidada != null) {
            String referencia = fotografias.guardar(fotografiaValidada);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) fotografias.eliminarSiExiste(referencia);
                }
            });
            cliente = clientes.guardarFotografia(cliente, referencia, actor);
        }
        auditoria.record(actor, null, "CLIENT_CREATED", ip, "Cliente registrado con id " + cliente.getId() + '.');
        return clientes.respuesta(cliente);
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves a previously stored private image after controller authorization.
     *
     * @param clienteId client identifier
     * @return private resource metadata
     * @throws com.taller.m01.exception.ApiException when the client or photo does not exist
     */
    public ClientePhotoStorage.FotoLeida consultarFotografia(Long clienteId) {
        Cliente cliente = clientes.buscarPorId(clienteId);
        return fotografias.leer(cliente.getFotografiaReferencia());
    }
}
