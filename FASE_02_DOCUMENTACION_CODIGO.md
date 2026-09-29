# Documentación del código — Fase 02: Registro de clientes

## 1. Objetivo

Este documento describe técnicamente el código existente y adaptado para el login, el registro interno de usuarios y el registro protegido de clientes. Abarca autenticación, autorización, roles y permisos, Facade, Repository, fotografía privada, validaciones, prevención de duplicados, auditoría y notificaciones. No documenta contraseñas, tokens, valores de entorno ni otros secretos.

## 2. Arquitectura del código

```text
Vista Vue → Facade frontend → Cliente HTTP → Controller → Facade backend
         → Service → Repository → MySQL
```

| Capa | Responsabilidad técnica |
|---|---|
| Vista Vue | Captura datos, presenta errores por campo y delega el caso de uso; no conoce SQL ni repositorios. |
| Facade frontend | Mantiene estado de carga, normaliza, valida, envía DTO/FormData y traduce errores a notificaciones. |
| Cliente HTTP | Axios comunica Vue con `/api`; conserva cookies HTTP-only y selecciona JSON o multipart según el cuerpo. |
| Controller | Recibe DTO validado, aplica `@PreAuthorize` y delega. No contiene lógica de persistencia. |
| Facade backend | Coordina el caso de uso, la transacción, la fotografía y la auditoría. |
| Service | Aplica reglas de negocio, normalización, duplicados, BCrypt o cálculo de edad. |
| Repository | Ejecuta consultas derivadas y persistencia JPA exclusivamente. |
| MySQL | Conserva integridad, índices únicos, relaciones y auditoría. |

## 3. Flujo del login

```text
LoginView.vue
→ useAuthFacade().iniciarSesion
→ api (Axios)
→ AuthController.login
→ AuthFacade.iniciarSesion
→ AuthService.login
→ UserRepository
→ MySQL
```

`AuthController` coloca el JWT solo en la cookie HTTP-only. `AuthService` conserva la comprobación BCrypt, estado de cuenta, bloqueo, límite de intentos y auditoría de M01. Vue guarda únicamente el DTO seguro en Pinia y redirige una sesión normal a `/clientes/registro`.

## 4. Flujo del registro interno de usuarios

```text
RegisterUserView.vue / UsersView.vue
→ usuarioFacade
→ api (Axios)
→ UserController
→ UsuarioFacade
→ UserService
→ UserRepository
→ MySQL
```

El registro sigue siendo interno: `UserController` exige permisos M01 y `UserService` conserva jerarquía de roles, política de contraseña, BCrypt y auditoría. Los DTO no devuelven hashes.

## 5. Flujo del registro de clientes

```text
ClientRegistrationView.vue
→ useClienteFacade().registrarCliente
→ api.post('/clientes', FormData)
→ ClienteController.registrar
→ ClienteFacade.registrarCliente
→ ClienteService
→ ClienteRepository + DireccionClienteRepository
→ MySQL
```

`ClienteFacade` valida la fotografía antes de guardar, coordina la transacción y registra `CLIENT_CREATED`. Si la transacción no confirma, elimina el archivo privado creado. `ClienteService` calcula la edad al responder; no persiste edad editable.

## 6. Archivos de código

| Capa | Archivo | Clase o componente | Propósito | Dependencias | Estado |
|---|---|---|---|---|---|
| Backend | `controller/AuthController.java` | `AuthController` | Límite HTTP del login y ciclo de contraseña. | AuthFacade, AuthService, JWT, RateLimit. | Modificado |
| Backend | `service/AuthFacade.java` | `AuthFacade` | Delega el login a AuthService. | AuthService. | Creado |
| Backend | `controller/UserController.java` | `UserController` | Administración interna protegida. | UsuarioFacade, Spring Security. | Modificado |
| Backend | `service/UsuarioFacade.java` | `UsuarioFacade` | Coordina casos de uso de usuarios. | UserService. | Creado |
| Backend | `controller/ClienteController.java` | `ClienteController` | POST de cliente y GET privado de foto. | ClienteFacade, Spring Security. | Modificado |
| Backend | `service/ClienteFacade.java` | `ClienteFacade` | Transacción, foto y auditoría de cliente. | ClienteService, PhotoStorage, AuditService. | Modificado |
| Backend | `service/ClienteService.java` | `ClienteService` | Normalización, validación, duplicados y DTO. | Repositories de cliente/dirección. | Modificado |
| Backend | `service/ClientePhotoStorage.java` | `ClientePhotoStorage` | Valida y almacena imágenes privadas. | MultipartFile, NIO. | Modificado |
| Backend | `controller/ClienteRequestExceptionHandler.java` | Advice de cliente | Mapea errores de DTO y multipart a 400 seguro. | Spring MVC. | Modificado |
| Backend | `exception/GlobalExceptionHandler.java` | Advice global | Oculta detalles técnicos y registra fallos internos. | ApiException, SLF4J. | Modificado |
| Backend | `security/AuthenticatedUser.java` | Principal | Expone `ROLE_*` y permisos a Spring Security. | UserAccount, roles/permisos. | Modificado |
| Backend | `dto/ClienteDtos.java` | DTO de cliente | Contratos de entrada y salida. | Jakarta Validation. | Reutilizado |
| Backend | `entity/Cliente.java` | Entidad cliente | Mapea `clientes`. | JPA, UserAccount. | Reutilizado |
| Backend | `entity/DireccionCliente.java` | Entidad dirección | Mapea `direcciones_cliente`. | JPA, Cliente. | Reutilizado |
| Backend | `repository/ClienteRepository.java` | Repository | Consultas de duplicado. | Spring Data JPA. | Reutilizado |
| Backend | `repository/DireccionClienteRepository.java` | Repository | Persistencia de dirección. | Spring Data JPA. | Reutilizado |
| Backend | `service/InitialOperationalAccountsService.java` | Inicializador | Reutiliza o crea cuentas operativas con secreto de entorno. | BCrypt, roles, usuarios. | Creado |
| Frontend | `api/client.js` | `api` | Cliente Axios JSON/multipart. | Axios. | Modificado |
| Frontend | `facades/authFacade.js` | `useAuthFacade` | Flujo de login y estado de envío. | Axios, Pinia. | Creado |
| Frontend | `facades/usuarioFacade.js` | `usuarioFacade` | API interna de usuarios. | Axios. | Creado |
| Frontend | `facades/clienteFacade.js` | `useClienteFacade` | Formulario, foto, validación y registro. | Vue, Axios, toasts. | Modificado |
| Frontend | `facades/notificacionFacade.js` | `notificacionFacade` | Cola de toasts accesibles. | Vue reactive. | Modificado |
| Frontend | `views/LoginView.vue` | Login | Delega login y redirige a clientes. | AuthFacade, Router. | Modificado |
| Frontend | `views/RegisterUserView.vue` | Registro interno | Delega a UsuarioFacade. | UsuarioFacade. | Modificado |
| Frontend | `views/UsersView.vue` | Usuarios | Delega operaciones de usuario. | UsuarioFacade. | Modificado |
| Frontend | `views/ClientRegistrationView.vue` | Registro cliente | Presenta secciones, foto, modal y envío. | ClienteFacade, AppShell. | Modificado |
| Frontend | `router/index.js` | Guards | Protege autenticación, contraseña y permisos. | Pinia, Vue Router. | Modificado |
| Frontend | `components/AppShell.vue` | Panel | Oculta módulos futuros y conserva sesión/cierre. | Pinia, Router. | Modificado |

## 7. Clases

| Clase | Archivo | Capa | Responsabilidad | Clases relacionadas | Estado |
|---|---|---|---|---|---|
| `AuthController` | `controller/AuthController.java` | Controller | Recibe login y establece cookie segura. | AuthFacade, AuthService. | Modificado |
| `AuthFacade` | `service/AuthFacade.java` | Facade | Coordina el caso de uso de autenticación. | AuthService, UserRepository. | Creado |
| `UserController` | `controller/UserController.java` | Controller | Recibe gestión interna con permisos. | UsuarioFacade. | Modificado |
| `UsuarioFacade` | `service/UsuarioFacade.java` | Facade | Delega administración interna al servicio. | UserService, UserRepository. | Creado |
| `ClienteController` | `controller/ClienteController.java` | Controller | Protege registro/foto con rol y permiso. | ClienteFacade. | Modificado |
| `ClienteFacade` | `service/ClienteFacade.java` | Facade | Orquesta cliente, dirección, foto y auditoría. | ClienteService, AuditService. | Modificado |
| `ClienteService` | `service/ClienteService.java` | Service | Reglas de negocio de cliente. | ClienteRepository, DireccionClienteRepository. | Modificado |
| `ClientePhotoStorage` | `service/ClientePhotoStorage.java` | Service | Archivos privados con UUID. | NIO, MultipartFile. | Modificado |
| `ClienteRequestExceptionHandler` | `controller/...ExceptionHandler.java` | Advice | Respuestas 400 de transporte. | ClienteController. | Modificado |
| `GlobalExceptionHandler` | `exception/GlobalExceptionHandler.java` | Advice | Respuestas de error seguras. | ApiException, SLF4J. | Modificado |
| `AuthenticatedUser` | `security/AuthenticatedUser.java` | Security | Principal y autoridades. | UserAccount, Role, Permission. | Modificado |
| `InitialOperationalAccountsService` | `service/...AccountsService.java` | Service | Aprovisionamiento idempotente local. | UserRepository, RoleRepository. | Creado |
| `ClienteDtos` | `dto/ClienteDtos.java` | DTO | Entrada/salida de cliente. | ClienteController, ClienteService. | Reutilizado |
| `Cliente` | `entity/Cliente.java` | Entity | Cliente y claves normalizadas. | DireccionCliente, UserAccount. | Reutilizado |
| `DireccionCliente` | `entity/DireccionCliente.java` | Entity | Dirección inicial del cliente. | Cliente. | Reutilizado |

## 8. Métodos Java

| Clase | Método | Modificador | Parámetros | Retorno | Excepciones | Responsabilidad | Validaciones | Autorización |
|---|---|---|---|---|---|---|---|---|
| `AuthController` | `login` | public | LoginRequest, request | 200 + DTO/cookie | ApiException | Delega login a Facade. | `@Valid`, rate limit. | Pública; credenciales válidas. |
| `AppSecurityProperties` | `getInitialAdministratorPassword` | public | — | String | — | Lee la configuración de contraseña temporal del administrador. | Enlace de propiedades; el valor solo proviene del entorno local. | No aplica. |
| `AppSecurityProperties` | `setInitialAdministratorPassword` | public | String | void | — | Recibe la configuración de contraseña temporal del administrador. | Enlace de propiedades; no persiste ni registra el valor. | No aplica. |
| `AppSecurityProperties` | `getInitialReceptionistPassword` | public | — | String | — | Lee la configuración de contraseña temporal de recepción. | Enlace de propiedades; el valor solo proviene del entorno local. | No aplica. |
| `AppSecurityProperties` | `setInitialReceptionistPassword` | public | String | void | — | Recibe la configuración de contraseña temporal de recepción. | Enlace de propiedades; no persiste ni registra el valor. | No aplica. |
| `UserController` | `list` | public | filtros/paginación | UserPage | ApiException | Lista usuarios internos. | Parámetros HTTP. | `USERS_VIEW`. |
| `UserController` | `get` | public | id | UserResponse | ApiException | Consulta DTO seguro. | id ruta. | `USERS_VIEW`. |
| `UserController` | `create` | public | CreateUserRequest, request | 201 UserResponse | ApiException | Delega alta interna. | `@Valid`. | `USERS_CREATE`. |
| `UserController` | `update` | public | id, UpdateUserRequest | UserResponse | ApiException | Delega edición. | `@Valid`. | `USERS_EDIT`. |
| `UserController` | `status` | public | id, StatusRequest | UserResponse | ApiException | Delega estado. | `@Valid`. | Activar/desactivar. |
| `UserController` | `role` | public | id, RoleRequest | UserResponse | ApiException | Delega rol. | `@Valid`. | `ROLES_ASSIGN`. |
| `UserController` | `unlock` | public | id, request | UserResponse | ApiException | Delega desbloqueo. | id ruta. | `USERS_UNLOCK`. |
| `ClienteController` | `registrar` | public | CreateClientRequest, foto, principal | 201 ClientResponse | ApiException | Delega registro. | `@Valid`, multipart. | `CLIENTE_CREAR` + ADMIN/RECEPTIONIST. |
| `ClienteController` | `consultarFotografia` | public | id | Resource | ApiException | Devuelve foto privada. | id ruta. | `CLIENTE_CREAR` + ADMIN/RECEPTIONIST. |
| `AuthFacade` | `iniciarSesion` | public | LoginRequest, IP | LoginResult | ApiException | Delega autenticación. | AuthService. | No aplica antes de sesión. |
| `UsuarioFacade` | `listar` | public | filtros/página | UserPage | ApiException | Delega listado. | UserService. | Controller. |
| `UsuarioFacade` | `consultar` | public | id | UserResponse | ApiException | Delega consulta. | UserService. | Controller. |
| `UsuarioFacade` | `registrarInterno` | public | CreateUserRequest, IP | UserResponse | ApiException | Delega alta interna. | BCrypt/DTO en servicio. | Controller. |
| `UsuarioFacade` | `actualizar` | public | id, UpdateUserRequest, IP | UserResponse | ApiException | Delega edición. | UserService. | Controller. |
| `UsuarioFacade` | `actualizarEstado` | public | id, StatusRequest, IP | UserResponse | ApiException | Delega estado. | UserService. | Controller. |
| `UsuarioFacade` | `cambiarRol` | public | id, RoleRequest, IP | UserResponse | ApiException | Delega rol. | Jerarquía en servicio. | Controller. |
| `UsuarioFacade` | `desbloquear` | public | id, IP | UserResponse | ApiException | Delega desbloqueo. | UserService. | Controller. |
| `ClienteFacade` | `registrarCliente` | public | DTO, foto, actor, IP | ClientResponse | ApiException | Coordina transacción/auditoría. | Foto, datos, duplicados. | Controller ya autorizado. |
| `ClienteFacade` | `consultarFotografia` | public | id | FotoLeida | ApiException | Orquesta foto privada. | Referencia segura. | Controller ya autorizado. |
| `ClienteService` | `normalizarDatos` | public | CreateClientRequest | DatosNormalizados | — | Canoniza datos. | Espacios, correo, teléfono, nombre. | No aplica. |
| `ClienteService` | `validarDatos` | public | DatosNormalizados | void | ApiException | Aplica reglas finales. | Fecha, teléfono, CP. | No aplica. |
| `ClienteService` | `verificarDuplicado` | public | DatosNormalizados | void | 409 ApiException | Consulta las tres claves. | Correo/teléfono/nombre-fecha. | No aplica. |
| `ClienteService` | `guardarCliente` | public | DatosNormalizados, actor | Cliente | 409 ApiException | Guarda cliente/dirección. | Integridad MySQL. | No aplica. |
| `ClienteService` | `guardarFotografia` | public | Cliente, referencia, actor | Cliente | ApiException | Persiste referencia privada. | UUID validado antes. | No aplica. |
| `ClienteService` | `buscarPorId` | public | id | Cliente | 404 ApiException | Busca cliente protegido. | Existencia. | Controller. |
| `ClienteService` | `respuesta` | public | Cliente | ClientResponse | — | Construye DTO y edad. | Edad no persistida. | No aplica. |
| `ClientePhotoStorage` | `validar` | public | MultipartFile | FotografiaValidada | 400 ApiException | Comprueba imagen. | Vacío, MIME, firma, 15 MiB. | Controller protegido. |
| `ClientePhotoStorage` | `guardar` | public | Foto validada | referencia | 500 ApiException | Escribe UUID privado. | Ruta normalizada. | No expone directorio. |
| `ClientePhotoStorage` | `leer` | public | referencia | FotoLeida | 404 ApiException | Lee imagen privada. | UUID/extensión/ruta. | Controller protegido. |
| `ClientePhotoStorage` | `eliminarSiExiste` | public | referencia | void | — | Elimina huérfano tras rollback. | Patrón UUID. | Interna. |
| `InitialOperationalAccountsService` | `seedOperationalAccounts` | package | args | ApplicationRunner | IllegalStateException | Registra inicializador. | Rol existente. | Arranque local. |
| `InitialOperationalAccountsService` | `createIfConfigured` | public | — | void | IllegalStateException | Aprovisiona sin duplicar. | Variable no vacía, BCrypt. | No aplica. |
| `InitialOperationalAccountsService` | `provision` | private | correo, nombre, rol, contraseña temporal | void | IllegalStateException | Crea o reutiliza una cuenta operativa y le asigna rol. | Correo y contraseña configurados; hash BCrypt. | Arranque controlado. |
| `ClienteRequestExceptionHandler` | `datosInvalidos` | package | excepción, request | 400 ErrorResponse | — | Traduce Bean Validation. | Errores de campo. | No aplica. |
| `ClienteRequestExceptionHandler` | `solicitudInvalida` | package | excepción, request | 400 ErrorResponse | — | Rechaza multipart/JSON inválido. | Parte requerida. | No aplica. |
| `ClienteRequestExceptionHandler` | `fotografiaGrande` | package | excepción, request | 400 ErrorResponse | — | Traduce límite multipart. | 15 MB. | No aplica. |
| `ClienteRequestExceptionHandler` | `response` | private | estado, código, mensaje, request, campos | ResponseEntity | — | Construye la respuesta de error sin filtrar detalles internos. | Estructura de error estable. | No aplica. |
| `GlobalExceptionHandler` | `unknown` | package | excepción, request | 500 ErrorResponse | — | Registra internamente y oculta detalle. | Sin detalles técnicos. | No aplica. |
| `AuthenticatedUser` | `getAuthorities` | public | — | Collection | — | Genera roles/permisos Spring. | Elimina repetidos. | Fuente de `@PreAuthorize`. |
| `ClienteService` | `clienteDuplicado` | private | — | ApiException | 409 ApiException | Centraliza el error de conflicto de cliente. | Código y mensaje de duplicado seguros. | No aplica. |
| `ClienteService` | `validarTelefono` | private | teléfono, código, mensaje | void | 400 ApiException | Rechaza números con longitud o caracteres inválidos. | Dígitos normalizados y longitud. | No aplica. |
| `ClienteService` | `limpiarEspacios` | private | texto | String | — | Recorta y colapsa espacios internos. | Nulos permitidos. | No aplica. |
| `ClienteService` | `limpiarOpcional` | private | texto | String/null | — | Convierte texto vacío opcional en nulo. | Blancos no significativos. | No aplica. |
| `ClienteService` | `normalizarCorreo` | private | correo | String/null | — | Canoniza correo para consulta y almacenamiento. | Minúsculas y espacios. | No aplica. |
| `ClienteService` | `normalizarTelefono` | private | teléfono | String/null | — | Elimina separadores aceptados del teléfono. | Espacios, guiones y paréntesis. | No aplica. |
| `ClienteService` | `normalizarNombre` | private | nombre | String | — | Genera clave de nombre sin acentos para duplicado complementario. | Espacios y acentos. | No aplica. |
| `ClienteService` | `edadDe` | private | fechaNacimiento | int | — | Calcula la edad dinámica que se expone en DTO. | Fecha ya validada. | No aplica. |
| `ClientePhotoStorage` | `noEncontrada` | private | — | ApiException | 404 ApiException | Construye error seguro de fotografía inexistente. | Código seguro. | No aplica. |
| `ClientePhotoStorage` | `fotoInvalida` | private | — | ApiException | 400 ApiException | Construye mensaje único para fotografía inválida. | Tipo/tamaño no expuestos. | No aplica. |
| `ClientePhotoStorage` | `esMimePermitido` | private | MIME | boolean | — | Comprueba lista blanca de tipos declarados. | JPEG, PNG y WebP. | No aplica. |
| `ClientePhotoStorage` | `tipoPorReferencia` | private | referencia | TipoFotografia | — | Convierte referencia UUID validada a tipo de salida. | Extensión interna permitida. | No aplica. |
| `ClientePhotoStorage` | `detectarTipo` | private | bytes | TipoFotografia/null | — | Comprueba firma binaria de imagen. | Magic bytes JPEG/PNG/WebP. | No aplica. |
| `ClienteRepository` | `existsByCorreoPersonalNormalizado` | público interfaz | correo | boolean | — | Duplicado por correo. | Clave única. | No aplica. |
| `ClienteRepository` | `existsByTelefonoPersonalNormalizado` | público interfaz | teléfono | boolean | — | Duplicado por teléfono. | Clave única. | No aplica. |
| `ClienteRepository` | `existsByNombreNormalizadoAndFechaNacimiento` | público interfaz | nombre, fecha | boolean | — | Duplicado complementario. | Índice único. | No aplica. |
| `DireccionClienteRepository` | `existsByClienteId` | público interfaz | clienteId | boolean | — | Comprueba relación. | FK. | No aplica. |

## 9. Funciones JavaScript y Vue

| Archivo o componente | Función | Parámetros | Retorno | Asíncrona | Responsabilidad | Validaciones | Errores |
|---|---|---|---|---|---|---|---|
| `authFacade.js` | `useAuthFacade` | — | estado/operación | No | Expone caso de login. | Backend definitivo. | Propaga Axios. |
| `authFacade.js` | `iniciarSesion` | payload | DTO usuario | Sí | POST login y actualiza Pinia. | Backend/HTTP. | Axios controlado. |
| `usuarioFacade.js` | `cargarRoles` | — | roles | Sí | GET roles. | Backend. | Axios. |
| `usuarioFacade.js` | `listarUsuarios` | filtros | página | Sí | GET usuarios. | Backend. | Axios. |
| `usuarioFacade.js` | `registrarUsuario` | DTO | usuario | Sí | POST interno. | Backend/BCrypt. | Axios. |
| `usuarioFacade.js` | `actualizarUsuario` | id, DTO | usuario | Sí | PUT interno. | Backend. | Axios. |
| `usuarioFacade.js` | `cambiarRol` | id, DTO | usuario | Sí | PATCH rol. | Backend. | Axios. |
| `usuarioFacade.js` | `cambiarEstado` | id, DTO | usuario | Sí | PATCH estado. | Backend. | Axios. |
| `usuarioFacade.js` | `desbloquearUsuario` | id | usuario | Sí | POST desbloqueo. | Backend. | Axios. |
| `clienteFacade.js` | `normalizarFormulario` | — | void | No | Limpia/canoniza formulario. | Espacios, correo, teléfono. | No aplica. |
| `clienteFacade.js` | `validarFormulario` | — | boolean | No | Valida antes del envío. | Nombre, fecha, correo, teléfono, dirección, foto. | Errores por campo. |
| `clienteFacade.js` | `seleccionarFotografia` | File | boolean | Sí | Valida y crea preview. | MIME, firma, vacío, 15 MiB. | Mensaje estándar. |
| `clienteFacade.js` | `quitarFotografia` | — | void | No | Libera preview y archivo local. | — | No aplica. |
| `clienteFacade.js` | `formatoTamano` | bytes | texto | No | Formatea tamaño visible. | Número finito. | Fallback 0 B. |
| `clienteFacade.js` | `registrarCliente` | — | boolean | Sí | Envía FormData, evita doble envío. | Revalida formulario. | Toast/errores API. |
| `clienteFacade.js` | `procesarError` | AxiosError | void | No | Traduce 400/401/403/409/500. | Código seguro. | Toast y campo. |
| `clienteFacade.js` | `limpiarFormulario` | — | void | No | Restaura formulario después de éxito. | — | No aplica. |
| `clienteFacade.js` | `cancelarCaptura` | — | void | No | Limpia tras modal confirmado. | — | Toast información. |
| `clienteFacade.js` | `validarArchivoFotografia` | File | mensaje | Sí | Lee firma binaria. | JPEG/PNG/WebP/15 MiB. | Mensaje estándar. |
| `clienteFacade.js` | `detectarTipoFotografia` | bytes | MIME | No | Detecta firma permitida. | Cabeceras binarias. | MIME vacío. |
| `clienteFacade.js` | `calcularEdad` | fecha ISO | número/null | No | Calcula edad visual. | Fecha válida. | null. |
| `notificacionFacade.js` | `mostrar` | tipo/título/mensaje | id | No | Crea toast con temporizador. | Duración. | No aplica. |
| `notificacionFacade.js` | `cerrar` | id | void | No | Elimina toast y timers. | Id existente. | No aplica. |
| `LoginView.vue` | `submit` | — | void | Sí | Delega login/redirección. | Correo/contraseña requeridos. | UiAlert. |
| `RegisterUserView.vue` | `resetForm` | — | void | No | Limpia alta interna. | Rol no privilegiado. | No aplica. |
| `RegisterUserView.vue` | `loadRoles` | — | void | Sí | Pide roles a facade. | Backend. | UiAlert. |
| `RegisterUserView.vue` | `submit` | — | void | Sí | Crea usuario interno. | Campos/política temporal. | UiAlert. |
| `UsersView.vue` | `load`, `loadRoles` | página | void | Sí | Lista datos por facade. | Backend. | Mensaje API. |
| `UsersView.vue` | `save` | — | void | Sí | Crea/edita usuario por facade. | Obligatorios/política. | Mensaje API. |
| `UsersView.vue` | `setStatus`, `unlock` | usuario | void | Sí | Delega estado/desbloqueo. | Confirmación existente. | Mensaje API. |
| `ClientRegistrationView.vue` | `seleccionarFoto`, `quitarFoto` | evento | boolean/void | Sí/No | Conecta control visual y facade. | Facade. | Mensaje de campo. |
| `ClientRegistrationView.vue` | `solicitarLimpieza`, `confirmar` | — | void | No | Controla modal accesible. | Cambios pendientes. | No aplica. |
| `TemporaryPasswordView.vue` | `submit` | — | void | Sí | Cambia contraseña y redirige. | Política M01. | UiAlert. |
| `AppShell.vue` | `logout` | — | void | Sí | Cierra sesión. | Cookie backend. | Ruta login. |
| `router/index.js` | guard global | destino | navegación | Sí | Carga sesión y protege rutas. | Sesión, cambio, permiso. | Login/Denied. |

## 10. DTO

| DTO | Dirección | Campos | Validaciones | Utilizado por |
|---|---|---|---|---|
| `CreateClientRequest` | Entrada | Nombre, contacto, fecha, teléfonos, correos, dirección. | `@NotBlank`, `@Email`, `@Pattern`, `@Valid`. | ClienteController, ClienteService. |
| `AddressRequest` | Entrada | Calle, colonia, municipio, estado, CP. | Obligatorios; CP de cinco dígitos. | CreateClientRequest. |
| `ClientResponse` | Salida | id, nombre, fecha, edad, contacto, estado, URL privada, mensaje. | Edad calculada; no entidad. | ClienteFacade/Controller/Vue. |
| `LoginRequest` / `LoginResponse` | Entrada/Salida | Credenciales y usuario seguro. | `@Valid` en controller. | AuthController/AuthFacade. |
| `CreateUserRequest`, `UpdateUserRequest`, `RoleRequest`, `StatusRequest` | Entrada | Datos internos y cambios permitidos. | Jakarta Validation y servicio. | UserController/UsuarioFacade. |
| `UserResponse`, `UserPage` | Salida | Usuario seguro y paginación. | Sin hash ni secreto. | UsuarioFacade/Vue. |

## 11. Repositories

| Repository | Método | Consulta o propósito | Entidad | Retorno |
|---|---|---|---|---|
| `ClienteRepository` | `existsByCorreoPersonalNormalizado` | Detecta correo personal duplicado. | Cliente | boolean |
| `ClienteRepository` | `existsByTelefonoPersonalNormalizado` | Detecta teléfono personal duplicado. | Cliente | boolean |
| `ClienteRepository` | `existsByNombreNormalizadoAndFechaNacimiento` | Comprobación complementaria de identidad. | Cliente | boolean |
| `ClienteRepository` | `findByCorreoPersonalNormalizado` | Busca por correo normalizado. | Cliente | Optional |
| `DireccionClienteRepository` | `existsByClienteId` | Comprueba dirección asociada. | DireccionCliente | boolean |
| `UserRepository` | `findByEmailIgnoreCase` | Autenticación y aprovisionamiento. | UserAccount | Optional |

## 12. Validaciones

| Campo | Frontend | Backend | Normalización | Mensaje de error |
|---|---|---|---|---|
| Nombre | Patrón Unicode. | DTO + regla negocio. | Recorta/compacta; clave sin acentos/minúscula. | Nombre válido sin números. |
| Contacto alternativo | Opcional con patrón de nombre. | DTO. | Recorta/compacta. | Formato inválido. |
| Fecha | Visual, no futura. | Anterior a hoy y máximo 120 años. | ISO LocalDate. | Fecha inválida. |
| Edad | Visual calculada. | Calculada en `ClientResponse`. | No se persiste. | No aplica. |
| Teléfonos | 10–15 dígitos. | DTO + service. | Elimina espacios, guiones y paréntesis. | Teléfono inválido. |
| Correos | Regex formal. | `@Email`. | Trim y minúsculas. | Correo inválido. |
| Dirección | Requeridos. | DTO y FK. | Trim/espacios internos. | Campo obligatorio. |
| Código postal | Cinco dígitos. | DTO + service. | Trim. | CP inválido. |
| Fotografía | MIME, firma, vacío, 15 MiB. | Multipart 15 MB + MIME/firma/ruta. | UUID; no nombre original. | Mensaje estándar de foto. |

## 13. Seguridad

- Spring Security autentica con `AuthenticatedUser`, que expone permisos y `ROLE_*`.
- `ClienteController` exige simultáneamente `CLIENTE_CREAR` y rol `ADMIN` o `RECEPTIONIST`; ocultar una ruta Vue no sustituye ese control.
- Sin autenticación, Spring devuelve 401; con sesión sin autorización, devuelve 403.
- Las contraseñas se comprueban o generan mediante BCrypt en los servicios existentes; ni DTO, auditoría, frontend ni respuesta API reciben hashes.
- Los router guards cargan `/auth/me`, exigen sesión, cambio de contraseña pendiente y permiso de ruta.
- La fotografía no es estática: su GET requiere la misma autorización del registro.

## 14. Manejo de errores

| Código HTTP | Excepción o condición | Componente que la genera | Respuesta |
|---|---|---|---|
| 201 | Cliente válido y único. | ClienteController/Facade. | DTO seguro y mensaje de éxito. |
| 400 | DTO, JSON/multipart, foto, fecha, teléfono o CP inválido. | Advice/Service/PhotoStorage. | Código seguro y errores por campo cuando aplican. |
| 401 | Sin cookie o sesión inválida. | Spring Security. | No autenticado. |
| 403 | Sin permiso o rol operativo. | `@PreAuthorize`. | No tiene permiso. |
| 409 | Correo, teléfono o nombre-fecha duplicado. | ClienteService/MySQL. | `CLIENTE_DUPLICADO`. |
| 500 | Fallo inesperado o almacenamiento no disponible. | GlobalExceptionHandler/PhotoStorage. | Mensaje genérico; detalle solo en log local. |

## 15. Base de datos utilizada por el código

- `usuarios`, `roles`, `permisos` y tablas de relación abastecen el principal de seguridad, Spring Security, login y administración interna.
- `clientes` conserva datos normalizados, auditoría de creación/actualización y la referencia de fotografía; sus índices únicos respaldan las consultas Repository.
- `direcciones_cliente` usa FK a `clientes`; el servicio la persiste en la misma transacción.
- `auditoria` recibe `CLIENT_CREATED` con responsable, IP, acción e identificador, sin binarios, contraseñas ni tokens.

## 16. Documentación dentro del código fuente

Se añadieron JavaDoc/JSDoc a Facades, límites HTTP, servicios de fotografía, repositorios de clientes, entidades de cliente/dirección, configuración operativa, manejo de errores, guard de rutas y funciones Vue adaptadas. Los comentarios describen responsabilidad, parámetros, retorno, validación y autorización sin alterar lógica.
