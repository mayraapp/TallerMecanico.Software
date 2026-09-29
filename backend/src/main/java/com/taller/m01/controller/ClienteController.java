package com.taller.m01.controller;

import com.taller.m01.dto.ClienteDtos;
import com.taller.m01.security.AuthenticatedUser;
import com.taller.m01.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clientes")
/**
 * Protected HTTP boundary for the client-registration use case.
 * Controllers validate transport data and delegate all orchestration to {@link ClienteFacade}.
 */
public class ClienteController {
    private final ClienteFacade clientes;

    public ClienteController(ClienteFacade clientes) { this.clientes = clientes; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CLIENTE_CREAR') and hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPTIONIST')")
    /**
     * Registers a client only for active administrator or receptionist accounts with {@code CLIENTE_CREAR}.
     *
     * @param datos validated client and address DTO
     * @param fotografia optional image checked by the facade before persistence
     * @param principal authenticated account; missing authentication is rejected as 401 by Spring Security
     * @param request servlet request used only for the audit IP
     * @return 201 with a safe client DTO; duplicate and validation exceptions are mapped globally
     */
    public ResponseEntity<ClienteDtos.ClientResponse> registrar(
        @Valid @RequestPart("datos") ClienteDtos.CreateClientRequest datos,
        @RequestPart(value = "fotografia", required = false) MultipartFile fotografia,
        @AuthenticationPrincipal AuthenticatedUser principal,
        HttpServletRequest request
    ) {
        ClienteDtos.ClientResponse response = clientes.registrarCliente(datos, fotografia, principal.account(), RequestInfo.ip(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/fotografia")
    @PreAuthorize("hasAuthority('CLIENTE_CREAR') and hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPTIONIST')")
    /**
     * Reads a private photo after the same role-and-permission authorization as registration.
     *
     * @param id client identifier
     * @return private image resource or a controlled 404
     */
    public ResponseEntity<Resource> consultarFotografia(@PathVariable Long id) {
        ClientePhotoStorage.FotoLeida fotografia = clientes.consultarFotografia(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(fotografia.mediaType())).body(fotografia.resource());
    }
}
