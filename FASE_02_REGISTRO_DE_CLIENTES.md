# Fase 02 — Registro de clientes

> Documentación técnica detallada del código: [FASE_02_DOCUMENTACION_CODIGO.md](FASE_02_DOCUMENTACION_CODIGO.md).

> **Panel de avance:** ✅ Completado &nbsp;·&nbsp; 🟡 Parcial &nbsp;·&nbsp; ⏳ Pendiente

## 1. Información general

| Elemento | Descripción |
|---|---|
| Objetivo | Registrar un cliente autenticado, autorizado, único, con dirección y fotografía opcional privada. |
| Alcance | Caso de uso `Registro de clientes`; no incorpora otros módulos de negocio. |
| Precondiciones | Sesión activa, cuenta no bloqueada, rol operativo y permiso `CLIENTE_CREAR`. |
| Resultado esperado | Cliente, dirección y auditoría persistidos como una operación consistente; respuesta 201. |
| Tecnologías | Vue 3, Vite, Pinia, Axios, Tailwind, Java 26, Spring Boot, Spring Security, JPA, Flyway y MySQL 8. |

## 2. Trabajo realizado

- Se implementó el registro protegido de clientes con DTO, Facade, Service, Repository y transacción.
- Tras login, la navegación muestra únicamente **Registro de clientes**, información del usuario y cierre de sesión. Las rutas y módulos de M01 se conservaron, sin eliminarlos.
- El login redirige a `/clientes/registro`; `/clientes/nuevo` se conserva como alias.
- El backend permite crear clientes exclusivamente a `ADMIN` y `RECEPTIONIST` con `CLIENTE_CREAR`.
- Las cuentas operativas fueron aprovisionadas de forma idempotente mediante variables locales y migradas al dominio `.com`, sin persistir secretos en código o migraciones.
- La fotografía opcional acepta JPEG, PNG o WebP de hasta 15 MB, comprueba MIME y firma, usa UUID y no se expone como directorio público.
- Se aplican validaciones de Vue y Spring Boot, normalización, índices únicos y manejo de duplicados.
- Se reutilizaron auditoría, JWT, BCrypt, toasts, router guards y MySQL de M01.
- Login y usuarios adoptaron Facades sin convertir el registro interno en público.

## 3. Tabla general del trabajo

| ID | Módulo | Archivo | Acción | Descripción | Evidencia | Estado |
|---|---|---|---|---|---|---|
| F02-01 | Clientes | `ClienteController`, `ClienteFacade`, `ClienteService` | Modificado | Registro transaccional protegido. | 13 pruebas de cliente y HTTP 201. | ✅ Completado |
| F02-02 | Seguridad | `AuthenticatedUser`, migración V5 | Modificado/Creado | Requiere rol operativo y `CLIENTE_CREAR`. | Pruebas 401/403/201. | ✅ Completado |
| F02-03 | Cuentas | `InitialOperationalAccountsService`, V6 | Creado | Aprovisionamiento idempotente y migración a `.com`. | Flyway v6 y consulta MySQL. | ✅ Completado |
| F02-04 | Fotografía | `ClientePhotoStorage`, Facade Vue | Modificado | Límite 15 MB, MIME/firma, UUID y lectura protegida. | Pruebas y HTTP 201 con PNG. | ✅ Completado |
| F02-05 | Duplicados | Service/Repository/migración V4 | Reutilizado | Bloqueo por correo, teléfono y nombre-fecha. | HTTP 409 y restricciones únicas. | ✅ Completado |
| F02-06 | Login | AuthFacade frontend/backend | Creado | Flujo Facade sin cambiar reglas de M01. | Pruebas Spring y HTTP. | ✅ Completado |
| F02-07 | Usuarios | UsuarioFacade frontend/backend | Creado | Flujo interno por Facade. | Prueba Spring de alta interna. | ✅ Completado |
| F02-08 | Navegación | AppShell, router y LoginView | Modificado | Oculta módulos futuros y redirige a clientes. | Build Vue; inspección visual de login. | 🟡 Parcial |
| F02-09 | Notificaciones | notificacionFacade, registro cliente | Reutilizado/Modificado | Toast centralizado y errores de campo. | Prueba frontend; recorrido visual completo pendiente. | 🟡 Parcial |
| F02-10 | Documentación | Documentos de F02 | Creado/Modificado | Separa código de informe general. | Archivos Markdown presentes. | ✅ Completado |

## 4. Tabla de archivos

| Capa | Archivo | Acción realizada | Propósito | Estado |
|---|---|---|---|---|
| Frontend | `frontend/src/facades/authFacade.js` | Creado | Facade de login. | ✅ Completado |
| Frontend | `frontend/src/facades/usuarioFacade.js` | Creado | Facade de usuarios internos. | ✅ Completado |
| Frontend | `frontend/src/facades/clienteFacade.js` | Modificado | Validación, foto y FormData. | ✅ Completado |
| Frontend | `frontend/src/api/client.js` | Modificado | JSON/multipart correcto. | ✅ Completado |
| Frontend | `frontend/src/components/AppShell.vue` | Modificado | Menú temporalmente limitado. | ✅ Completado |
| Frontend | `frontend/src/views/ClientRegistrationView.vue` | Modificado | Formulario de cliente. | ✅ Completado |
| Backend | `backend/.../service/AuthFacade.java` | Creado | Facade de login. | ✅ Completado |
| Backend | `backend/.../service/UsuarioFacade.java` | Creado | Facade de usuarios. | ✅ Completado |
| Backend | `backend/.../service/ClienteFacade.java` | Modificado | Orquestación de cliente. | ✅ Completado |
| Backend | `backend/.../service/ClientePhotoStorage.java` | Modificado | Foto privada. | ✅ Completado |
| Backend | `backend/.../controller/ClienteController.java` | Modificado | API protegida. | ✅ Completado |
| Base de datos | `backend/.../V5__cliente_crear_operational_roles.sql` | Creado | Asignación de permiso. | ✅ Completado |
| Base de datos | `backend/.../V6__operational_account_emails_com.sql` | Creado | Migración de correos operativos. | ✅ Completado |
| Documentación | `FASE_02_DOCUMENTACION_CODIGO.md` | Creado | Código, clases y métodos. | ✅ Completado |
| Documentación | `FASE_02_REGISTRO_DE_CLIENTES.md` | Modificado | Informe y evidencia general. | ✅ Completado |

## 5. Tabla de pruebas

| ID | Prueba | Resultado esperado | Resultado obtenido | Evidencia | Estado |
|---|---|---|---|---|---|
| T-01 | Compilación backend | Sin errores. | Correcta. | `./mvnw test`. | ✅ Completado |
| T-02 | Pruebas Spring | Sin fallos. | 16 aprobadas, 0 fallos/errores. | Surefire. | ✅ Completado |
| T-03 | Compilación/pruebas frontend | Sin fallos. | Build correcto; 4 pruebas aprobadas. | Vite/Vitest. | ✅ Completado |
| T-04 | Login Administrador | 200 para cuenta válida. | Verificado antes de actualizar identificador; cuenta migrada conserva hash. | HTTP/MySQL. | ✅ Completado |
| T-05 | Login Recepcionista | 200 para cuenta válida. | 200 con identificador `.com`. | HTTP. | ✅ Completado |
| T-06 | Autorización ADMIN/RECEPTIONIST | 201. | Pruebas e inserciones reales aprobadas. | Spring/HTTP. | ✅ Completado |
| T-07 | Sin autenticación | 401. | 401. | HTTP/prueba. | ✅ Completado |
| T-08 | Rol no autorizado | 403. | SUPERADMIN y MECHANIC rechazados. | Prueba Spring. | ✅ Completado |
| T-09 | Cliente duplicado | 409. | 409. | HTTP/prueba. | ✅ Completado |
| T-10 | Foto | MIME/firma/15 MB. | Válidas aceptadas; falsa, vacía o grande rechazadas. | 13 pruebas de cliente. | ✅ Completado |
| T-11 | Persistencia | Cliente + dirección + auditoría. | Verificados en MySQL. | Consultas directas. | ✅ Completado |
| T-12 | Facades | Login/usuarios/clientes delegan correctamente. | Pruebas y compilación correctas. | 2 pruebas de Facade. | ✅ Completado |
| T-13 | Toast/modal/vista previa visual | Avisos y controles visibles. | Implementados; recorrido manual completo pendiente. | Código/build. | 🟡 Parcial |
| T-14 | Solicitudes simultáneas | Una sola inserción. | No ejecutada. | — | ⏳ Pendiente |

## 6. Respuestas HTTP

| Código | Situación | Resultado esperado | Verificado |
|---|---|---|---|
| 201 | Cliente válido. | DTO seguro. | Sí |
| 400 | DTO, foto o datos inválidos. | Error seguro por campo/tipo. | Sí |
| 401 | Sin sesión. | No autenticado. | Sí |
| 403 | Rol/permiso insuficiente. | Sin autorización. | Sí |
| 409 | Cliente duplicado. | `CLIENTE_DUPLICADO`. | Sí |
| 500 | Error inesperado. | Sin detalle técnico al navegador. | Parcial: manejador documentado; no se forzó un error interno. |

## 7. Base de datos

| Tabla | Propósito | Relaciones | Restricciones | Estado |
|---|---|---|---|---|
| `usuarios` | Cuentas y estado. | Roles, auditoría. | Correo único, estado. | ✅ Completado |
| `roles` / `permisos` | RBAC. | Tablas puente. | Códigos únicos. | ✅ Completado |
| `clientes` | Cliente y claves normalizadas. | Usuarios, dirección. | Tres claves únicas. | ✅ Completado |
| `direcciones_cliente` | Dirección inicial. | FK a cliente. | NOT NULL y FK. | ✅ Completado |
| `auditoria` | Eventos seguros. | Responsable/afectado. | Acción, fecha. | ✅ Completado |
| `rol_permisos` | Asignación de permiso. | Role/Permission. | PK compuesta. | ✅ Completado |

## 8. Roles y permisos

| Rol | CLIENTE_CREAR | Acceso al formulario | Resultado |
|---|---:|---:|---|
| ADMIN | Sí | Sí | Puede registrar y leer fotografías privadas. |
| RECEPTIONIST | Sí | Sí | Puede registrar y leer fotografías privadas. |
| SUPERADMIN | No | No | 403 para clientes. |
| MECHANIC | No | No | 403 para clientes. |
| Sin sesión | No aplica | No | 401. |

## 9. Estado de la fase

| Avance | Cantidad | Lectura rápida |
|---|---:|---|
| ✅ Completado | 6 | Funciones terminadas y con la evidencia indicada. |
| 🟡 Parcial | 3 | Implementadas, pero con una verificación manual o controlada pendiente. |
| ⏳ Pendiente | 3 | Fuera de alcance o aún no ejecutadas. |

### ✅ Funcionalidades completadas

| ID | Funcionalidad | Evidencia |
|---|---|---|
| C-01 | Registro protegido de clientes. | HTTP 201, cliente/dirección/auditoría en MySQL. |
| C-02 | Duplicados y transacción. | Pruebas, 409 e índices únicos. |
| C-03 | Foto privada hasta 15 MB. | Validaciones MIME/firma, pruebas y PNG real. |
| C-04 | Roles/permiso backend. | 201, 401 y 403 probados. |
| C-05 | Facade/Repository. | Código y pruebas de integración. |
| C-06 | Identificadores operativos `.com`. | Flyway v6, MySQL y login Recepcionista. |

### 🟡 Funcionalidades parciales

| ID | Funcionalidad | Parte terminada | Parte faltante |
|---|---|---|---|
| P-01 | Recorrido visual de cliente. | Formulario, preview, modal y toast implementados. | Prueba manual completa con captura real. |
| P-02 | Navegación posterior al login. | Redirección y menú compilados. | Recorrido visual de todos los tamaños de pantalla. |
| P-03 | Error 500. | Handler seguro y log local. | Prueba controlada de una falla interna real. |

### ⏳ Funcionalidades pendientes

| ID | Funcionalidad | Motivo | Acción necesaria |
|---|---|---|---|
| PE-01 | Prueba concurrente. | No se ejecutó. | Enviar dos solicitudes equivalentes en paralelo. |
| PE-02 | Módulo de talleres. | Fuera de alcance. | Fase futura. |
| PE-03 | Relación cliente-taller. | Fuera de alcance. | Diseñar tabla intermedia futura. |

## 10. Preparación futura

No se implementó el módulo de talleres ni se agregó `taller_id` a `clientes`. El cliente es independiente; cuando exista el módulo podrá incorporarse una relación muchos-a-muchos mediante `cliente_taller`. Esa tabla no existe todavía.

## 11. Referencias

- [Documentación del código de Fase 02](FASE_02_DOCUMENTACION_CODIGO.md)
- [README del proyecto](README.md)
- [Buenas prácticas OWASP de M01](docs/Buenas_Practicas_OWASP_M01.md)

No existe un diagrama de componentes separado en el proyecto al momento de esta documentación.
