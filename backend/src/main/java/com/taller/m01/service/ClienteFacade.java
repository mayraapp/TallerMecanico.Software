package com.taller.m01.service;

import com.taller.m01.dto.ClienteDtos;
import com.taller.m01.entity.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Component
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
    public ClientePhotoStorage.FotoLeida consultarFotografia(Long clienteId) {
        Cliente cliente = clientes.buscarPorId(clienteId);
        return fotografias.leer(cliente.getFotografiaReferencia());
    }
}
