package com.taller.m01;

import com.taller.m01.dto.ClienteDtos;
import com.taller.m01.entity.*;
import com.taller.m01.repository.*;
import com.taller.m01.security.AuthenticatedUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.time.LocalDate;
import java.util.Set;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteControllerIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ClienteRepository clientes;
    @Autowired DireccionClienteRepository direcciones;

    @Test
    void rolesAutorizadosRegistranClienteYSeGuardaDireccion() throws Exception {
        long expectedClients = clientes.count();
        long expectedAddresses = direcciones.count();
        for (String role : new String[]{"ADMIN", "RECEPTIONIST"}) {
            UserAccount actor = actor(role);
            registrar(solicitud("María López " + nombrePara(role), "cliente." + role.toLowerCase() + "@correo.test", "555 123 45" + (role.equals("ADMIN") ? "11" : "12")), null, actor)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreCompleto").value("María López " + nombrePara(role)))
                .andExpect(jsonPath("$.edad").isNumber())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
            Assertions.assertEquals(++expectedClients, clientes.count());
            Assertions.assertEquals(++expectedAddresses, direcciones.count());
        }
    }

    @Test
    void solicitudSinSesionRecibe401() throws Exception {
        registrar(solicitud("María López", "sin.sesion@correo.test", "5551234567"), null, null)
            .andExpect(status().isUnauthorized());
    }

    @Test
    void mecanicoSinPermisoRecibe403() throws Exception {
        registrar(solicitud("María López", "mecanico@correo.test", "5551234567"), null, actor("MECHANIC"))
            .andExpect(status().isForbidden());
    }

    @Test
    void superadministradorSinPermisoOperativoRecibe403() throws Exception {
        registrar(solicitud("María López", "superadmin@correo.test", "5551234567"), null, actor("SUPERADMIN"))
            .andExpect(status().isForbidden());
    }

    @Test
    void nombreCorreoTelefonoYFechaInvalidosReciben400() throws Exception {
        UserAccount actor = actor("ADMIN");
        registrar(solicitud("María123", "correo-invalido", "123"), null, actor)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        registrar(solicitudConFecha("María López", "futuro@correo.test", "5551234567", LocalDate.now().plusDays(1)), null, actor)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FECHA_NACIMIENTO_INVALIDA"));
    }

    @Test
    void correoTrabajoVacioEsAceptadoYCorreoTrabajoInvalidoSeRechaza() throws Exception {
        UserAccount actor = actor("RECEPTIONIST");
        registrar(solicitud("María López", "sin.trabajo@correo.test", "5551234567"), null, actor).andExpect(status().isCreated());
        ClienteDtos.CreateClientRequest invalid = new ClienteDtos.CreateClientRequest("Ana Torres", "", LocalDate.of(1992, 2, 2), "555 123 4568", "", "trabajo.invalido@correo.test", "no-es-correo", direccion());
        registrar(invalid, null, actor).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void duplicadoPorCorreoYTelefonoRecibe409SinGuardarDireccionParcial() throws Exception {
        UserAccount actor = actor("ADMIN");
        registrar(solicitud("María López", "duplicado@correo.test", "5551234567"), null, actor).andExpect(status().isCreated());
        long clientsBefore = clientes.count();
        long addressesBefore = direcciones.count();
        registrar(solicitud("Ana Torres", "duplicado@correo.test", "5551234568"), null, actor)
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CLIENTE_DUPLICADO"));
        registrar(solicitud("Julia Torres", "otro@correo.test", "5551234567"), null, actor)
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CLIENTE_DUPLICADO"));
        Assertions.assertEquals(clientsBefore, clientes.count());
        Assertions.assertEquals(addressesBefore, direcciones.count());
    }

    @Test
    void fotografiaNoPermitidaRecibe400YNoGuardaCliente() throws Exception {
        UserAccount actor = actor("ADMIN");
        long before = clientes.count();
        MockMultipartFile invalidPhoto = new MockMultipartFile("fotografia", "archivo.png", MediaType.IMAGE_PNG_VALUE, "esto no es una imagen".getBytes());
        registrar(solicitud("María López", "foto.invalida@correo.test", "5551234567"), invalidPhoto, actor)
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("FOTOGRAFIA_INVALIDA"));
        Assertions.assertEquals(before, clientes.count());
    }

    @Test
    void fotografiaDemasiadoGrandeRecibe400YNoGuardaCliente() throws Exception {
        UserAccount actor = actor("RECEPTIONIST");
        long before = clientes.count();
        byte[] bytes = new byte[15 * 1024 * 1024 + 1];
        bytes[0] = (byte) 0x89; bytes[1] = 0x50; bytes[2] = 0x4E; bytes[3] = 0x47; bytes[4] = 0x0D; bytes[5] = 0x0A; bytes[6] = 0x1A; bytes[7] = 0x0A;
        MockMultipartFile tooLarge = new MockMultipartFile("fotografia", "foto.png", MediaType.IMAGE_PNG_VALUE, bytes);
        registrar(solicitud("María López", "foto.grande@correo.test", "5551234567"), tooLarge, actor)
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("FOTOGRAFIA_INVALIDA"));
        Assertions.assertEquals(before, clientes.count());
    }

    @Test
    void fotografiaValidaSeConsultaSoloConPermiso() throws Exception {
        UserAccount actor = actor("ADMIN");
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00};
        MockMultipartFile photo = new MockMultipartFile("fotografia", "cliente.png", MediaType.IMAGE_PNG_VALUE, png);
        registrar(solicitud("María López", "foto.valida@correo.test", "5551234567"), photo, actor).andExpect(status().isCreated()).andExpect(jsonPath("$.fotografiaUrl").exists());
        Long clientId = clientes.findByCorreoPersonalNormalizado("foto.valida@correo.test").orElseThrow().getId();
        mockMvc.perform(get("/api/clientes/{id}/fotografia", clientId).with(user(new AuthenticatedUser(actor))))
            .andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
        mockMvc.perform(get("/api/clientes/{id}/fotografia", clientId)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/clientes/{id}/fotografia", clientId).with(user(new AuthenticatedUser(actor("MECHANIC"))))).andExpect(status().isForbidden());
    }

    @Test
    void fotografiaVaciaRecibe400YNoGuardaCliente() throws Exception {
        UserAccount actor = actor("ADMIN");
        long before = clientes.count();
        MockMultipartFile empty = new MockMultipartFile("fotografia", "vacia.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);
        registrar(solicitud("María López", "foto.vacia@correo.test", "5551234567"), empty, actor)
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("FOTOGRAFIA_INVALIDA"));
        Assertions.assertEquals(before, clientes.count());
    }

    @Test
    void jpegPngYWebpValidosSeAceptan() throws Exception {
        UserAccount actor = actor("ADMIN");
        for (FotoPrueba foto : new FotoPrueba[]{
            new FotoPrueba("jpeg", MediaType.IMAGE_JPEG_VALUE, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}),
            new FotoPrueba("png", MediaType.IMAGE_PNG_VALUE, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00}),
            new FotoPrueba("webp", "image/webp", new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50})
        }) {
            MockMultipartFile archivo = new MockMultipartFile("fotografia", "cliente." + foto.extension(), foto.mimeType(), foto.contenido());
            registrar(solicitud("María López " + foto.extension(), "foto." + foto.extension() + "@correo.test", "55512345" + (foto.extension().equals("jpeg") ? "67" : foto.extension().equals("png") ? "68" : "69")), archivo, actor)
                .andExpect(status().isCreated());
        }
    }

    @Test
    void fotografiaExactamenteQuinceMbSeAcepta() throws Exception {
        UserAccount actor = actor("RECEPTIONIST");
        byte[] bytes = new byte[15 * 1024 * 1024];
        bytes[0] = (byte) 0x89; bytes[1] = 0x50; bytes[2] = 0x4E; bytes[3] = 0x47; bytes[4] = 0x0D; bytes[5] = 0x0A; bytes[6] = 0x1A; bytes[7] = 0x0A;
        MockMultipartFile photo = new MockMultipartFile("fotografia", "limite.png", MediaType.IMAGE_PNG_VALUE, bytes);
        registrar(solicitud("María López Límite", "foto.limite@correo.test", "5551234570"), photo, actor)
            .andExpect(status().isCreated());
    }

    private ResultActions registrar(ClienteDtos.CreateClientRequest request, MultipartFile photo, UserAccount actor) throws Exception {
        MockMultipartFile datos = new MockMultipartFile("datos", "datos.json", MediaType.APPLICATION_JSON_VALUE, solicitudJson(request).getBytes());
        var builder = multipart("/api/clientes").file(datos);
        if (photo != null) builder.file((MockMultipartFile) photo);
        if (actor != null) builder.with(user(new AuthenticatedUser(actor)));
        return mockMvc.perform(builder);
    }

    private ClienteDtos.CreateClientRequest solicitud(String nombre, String correo, String telefono) {
        return solicitudConFecha(nombre, correo, telefono, LocalDate.now().minusYears(30));
    }

    private ClienteDtos.CreateClientRequest solicitudConFecha(String nombre, String correo, String telefono, LocalDate fecha) {
        return new ClienteDtos.CreateClientRequest(nombre, "", fecha, telefono, "", correo, "", direccion());
    }

    private ClienteDtos.AddressRequest direccion() {
        return new ClienteDtos.AddressRequest("Avenida Motor 123", "Centro", "Monterrey", "Nuevo León", "64000");
    }

    private String solicitudJson(ClienteDtos.CreateClientRequest request) {
        ClienteDtos.AddressRequest address = request.direccion();
        return """
            {"nombreCompleto":"%s","contactoAlternativo":"%s","fechaNacimiento":"%s","telefonoPersonal":"%s","telefonoTrabajo":"%s","emailPersonal":"%s","emailTrabajo":"%s","direccion":{"calleNumero":"%s","colonia":"%s","municipio":"%s","estado":"%s","codigoPostal":"%s"}}
            """.formatted(request.nombreCompleto(), request.contactoAlternativo(), request.fechaNacimiento(), request.telefonoPersonal(), request.telefonoTrabajo(), request.emailPersonal(), request.emailTrabajo(), address.calleNumero(), address.colonia(), address.municipio(), address.estado(), address.codigoPostal());
    }

    private UserAccount actor(String roleCode) {
        Role role = roles.findByCode(roleCode).orElseThrow();
        UserAccount user = new UserAccount();
        user.setFullName("Prueba " + roleCode);
        user.setEmail("prueba." + roleCode.toLowerCase() + "@automanager.test");
        user.setPhone("5550000000");
        user.setPasswordHash("no-se-usa-en-prueba");
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(false);
        user.setRoles(Set.of(role));
        return users.saveAndFlush(user);
    }

    private String nombrePara(String role) { return role.equals("ADMIN") ? "Santos" : "Núñez"; }
    private record FotoPrueba(String extension, String mimeType, byte[] contenido) { }
}
