# FASE 02 — Gestión de clientes

- **Proyecto:** AutoManager — Taller mecánico
- **Caso de uso:** F02-C01 — Registro de clientes
- **Estado del documento:** Implementación y pruebas automatizadas completadas; la validación visual con una sesión real queda indicada en la sección de pruebas.
- **Fecha:** 28 de septiembre de 2026

## 1. Objetivo

Incorporar un registro de clientes protegido al sistema existente. El registro guarda datos personales, contacto y dirección en MySQL, detecta duplicados y permite una fotografía opcional sin exponer el directorio de archivos.

## 2. Alcance

Esta fase implementa exclusivamente `POST /api/clientes`, el formulario protegido correspondiente y la consulta protegida de fotografía. Conserva sin cambios funcionales el inicio y cierre de sesión, JWT, Spring Security, BCrypt, recuperación de contraseña, auditoría, usuarios, roles y permisos de M01.

No implementa vehículos, inventario, órdenes de trabajo, servicios, empleados, proveedores, reportes ni el módulo de talleres.

## 3. Caso de uso 01: registrar cliente

Un usuario autenticado con el permiso `CLIENTE_CREAR` captura los datos de un cliente. Vue valida los datos para orientar a la persona usuaria; Spring Boot los normaliza y valida de forma definitiva. La operación se ejecuta dentro de una transacción y crea el cliente y su dirección. Después registra un evento de auditoría seguro.

## 4. Precondiciones

1. Existe una sesión JWT válida y la cuenta está activa.
2. La cuenta no se encuentra bloqueada; el filtro JWT de M01 no autentica cuentas bloqueadas o inactivas.
3. El usuario posee `CLIENTE_CREAR`.
4. Los campos pasan la validación de Vue y Spring Boot.
5. No existe coincidencia por correo personal, teléfono personal o nombre normalizado con fecha de nacimiento.

## 5. Actores autorizados y permiso

| Rol | `CLIENTE_CREAR` | Resultado |
|---|---:|---|
| Superadministrador | Sí | Puede registrar y consultar fotografía privada. |
| Administrador | Sí | Puede registrar y consultar fotografía privada. |
| Recepcionista | Sí | Puede registrar y consultar fotografía privada. |
| Mecánico | No | Recibe `403 Forbidden`. |
| Sin sesión | No aplica | Recibe `401 Unauthorized`. |

La migración V4 inserta el permiso y lo asigna a SUPERADMIN, ADMIN y RECEPTIONIST. La autorización efectiva está en el backend mediante `@PreAuthorize("hasAuthority('CLIENTE_CREAR')")`; ocultar una ruta en Vue no concede ni sustituye permisos.

## 6. Flujo de registro

```text
Vista ClientRegistrationView.vue
        ↓
Facade de frontend clienteFacade.registrarCliente()
        ↓
Axios / POST multipart a /api/clientes
        ↓
ClienteController.registrar()
        ↓
ClienteFacade.registrarCliente()
        ↓
ClienteService (normalizar, validar, duplicado, guardar)
        ↓
ClienteRepository + DireccionClienteRepository
        ↓
MySQL mediante Flyway y JPA
```

La fotografía se valida antes de crear registros. Si la transacción no confirma, un sincronizador de transacción elimina el archivo privado que pudiera haberse escrito.

## 7. Reglas de validación

| Dato | Implementación |
|---|---|
| Nombre completo y contacto alternativo | Se eliminan extremos, se compactan espacios y solo se permiten letras Unicode, espacios, apóstrofes y guiones. El nombre completo es obligatorio. |
| Fecha de nacimiento | Debe ser anterior al día actual y no mayor a 120 años. La edad se calcula con `Period.between`; no existe columna `edad`. |
| Teléfonos | Vue y Spring eliminan espacios, paréntesis y guiones. El resultado debe tener entre 10 y 15 dígitos. El personal es obligatorio y único. |
| Correos | Se recortan y convierten a minúsculas. El correo personal es obligatorio, se valida con Bean Validation y es único. El correo de trabajo es opcional. |
| Dirección | Calle/número y colonia no aceptan valores vacíos; municipio y estado usan validación de texto; código postal requiere exactamente cinco dígitos. |
| Fotografía | Solo JPEG, PNG o WebP con máximo 5 MiB. El backend valida firma de archivo, no confía en nombre ni MIME enviado. |

## 8. Prevención de duplicados

### Nivel de aplicación

`ClienteService.verificarDuplicado(...)` consulta el Repository por correo personal normalizado, teléfono personal normalizado y nombre normalizado junto con fecha de nacimiento. Si encuentra una coincidencia responde `409 Conflict` con código `CLIENTE_DUPLICADO` y un mensaje seguro.

### Nivel MySQL

La migración V4 crea tres restricciones únicas en `clientes`:

1. `uk_clientes_correo_personal`
2. `uk_clientes_telefono_personal`
3. `uk_clientes_nombre_fecha`

`ClienteService.guardarCliente(...)` fuerza el guardado con `saveAndFlush()` y convierte una eventual violación simultánea de restricción en `409 Conflict`. No se muestran mensajes técnicos de MySQL.

## 9. Patrones Facade y Repository

| Patrón | Implementación | Responsabilidad |
|---|---|---|
| Facade de frontend | `frontend/src/facades/clienteFacade.js` | Controla formulario, normalización, validación, carga, envío multipart, previsualización y tratamiento de errores. |
| Facade de notificaciones | `frontend/src/facades/notificacionFacade.js` | Centraliza toasts de éxito, error, advertencia e información. |
| Facade de backend | `ClienteFacade` | Coordina validación de foto, datos, duplicados, transacción, almacenamiento privado y auditoría. |
| Service | `ClienteService` y `ClientePhotoStorage` | Reglas de negocio, normalización, persistencia coordinada y archivos privados. |
| Repository | `ClienteRepository` y `DireccionClienteRepository` | Consultas de duplicado y persistencia JPA; no contienen lógica de negocio. |

Vue no usa SQL ni repositories. Los DTOs son la frontera de la API y las entidades JPA no se devuelven al frontend.

## 10. Modelo de datos y relaciones

### Tabla `clientes`

Contiene identificador, nombre completo y normalizado, contacto alternativo, fecha de nacimiento, teléfonos normalizados, correos normalizados, referencia de fotografía, estado, fechas de creación/actualización y usuarios responsable de crear/modificar.

### Tabla `direcciones_cliente`

Contiene una dirección inicial por cliente: calle/número, colonia, municipio, estado, código postal, estado, fechas y responsables de auditoría. `cliente_id` es llave foránea y única, por lo que la relación actual es uno a uno.

No se almacena edad. No existe `taller_id` en `clientes`.

## 11. Migraciones

La migración aditiva [V4__clientes_registro.sql](backend/src/main/resources/db/migration/V4__clientes_registro.sql) fue aplicada en MySQL local por Flyway. Crea tablas, llaves foráneas, índices, restricciones únicas y el permiso `CLIENTE_CREAR`, sin borrar ni reemplazar tablas de M01.

El esquema de referencia se actualizó en [database/reference-schema.sql](database/reference-schema.sql).

## 12. API REST

| Método y ruta | Seguridad | Respuesta |
|---|---|---|
| `POST /api/clientes` | Sesión activa y `CLIENTE_CREAR` | `201 Created` con cliente, edad calculada y mensaje. |
| `GET /api/clientes/{id}/fotografia` | Sesión activa y `CLIENTE_CREAR` | Imagen privada o `404` si no existe. |

El POST recibe `multipart/form-data` con la parte JSON `datos` y, opcionalmente, la parte `fotografia`.

| Situación | HTTP | Código principal |
|---|---:|---|
| Datos de solicitud inválidos | 400 | `VALIDATION_ERROR`, `FECHA_NACIMIENTO_INVALIDA`, `FOTOGRAFIA_INVALIDA` u otro código específico. |
| Sin autenticación | 401 | `UNAUTHENTICATED` |
| Sin permiso | 403 | `FORBIDDEN` |
| Duplicado | 409 | `CLIENTE_DUPLICADO` |
| Error interno | 500 | Error controlado sin detalles de MySQL. |

## 13. Manejo de fotografía

Las imágenes se almacenan en el directorio configurado por `CLIENT_PHOTO_STORAGE_DIRECTORY` (por defecto `./uploads/clientes` respecto al backend). El directorio no es público. Cada archivo usa UUID y extensión detectada por su firma; nunca conserva el nombre original. La referencia, no el binario, se guarda en MySQL.

La ruta de foto se sirve únicamente desde el controlador protegido. El límite de Spring es 5 MB por archivo y 6 MB por solicitud; la validación de código repite el límite como defensa adicional.

## 14. Auditoría

`ClienteFacade` reutiliza `AuditService` de M01 y registra `CLIENT_CREATED` con el usuario responsable, IP, fecha y el identificador del cliente en una descripción segura. No registra contraseñas, JWT, teléfonos, correos ni contenido de fotografía.

## 15. Interfaz, alertas y confirmaciones

La nueva ruta protegida es `/clientes/nuevo`. Mantiene la paleta azul oscuro, grafito, blanco y acento ámbar/cian de AutoManager. Incluye secciones personales, contacto, dirección, foto con vista previa, edad calculada, mensajes bajo campos inválidos y diseño responsivo.

`ToastNotifications.vue` muestra avisos con icono, título, descripción, color por tipo, cierre manual, temporizador visual, animación y atributos ARIA. `ConfirmationModal.vue` reemplaza `confirm()` para limpiar el formulario, admite Escape y establece foco inicial en Cancelar.

Mientras la petición está en curso, `isSubmitting` deshabilita el botón y lo cambia a **Registrando cliente…**. El formulario solo se limpia después de una respuesta exitosa.

## 16. Pruebas ejecutadas y resultados reales

| Comando | Resultado real |
|---|---|
| `systemctl is-active mysql` | `active` |
| `cd backend && ./mvnw test` | **10 pruebas aprobadas**, 0 fallos, 0 errores. Flyway validó y dejó el esquema en versión 4. |
| `cd frontend && npm run test` | **2 pruebas aprobadas** para la Facade de notificaciones. |
| `cd frontend && npm run build` | Build de Vite generado correctamente. |

Las pruebas de integración `ClienteControllerIntegrationTests` verifican:

1. Registro `201` para SUPERADMIN, ADMIN y RECEPTIONIST.
2. `401` sin sesión y `403` para MECHANIC.
3. Nombre, correo, teléfono y fecha futura inválidos con `400`.
4. Correo de trabajo vacío aceptado y correo de trabajo inválido rechazado.
5. Duplicados por correo y por teléfono con `409` sin dirección parcial.
6. Fotografía con tipo inválido y mayor a 5 MB rechazadas con `400` sin cliente parcial.
7. Fotografía PNG válida, consulta protegida y respuestas `401`/`403` al no cumplir seguridad.
8. Persistencia de cliente y dirección durante la transacción de prueba.

Las pruebas se ejecutan dentro de transacciones de prueba y se revierten al terminar; por ello no dejan clientes de prueba persistentes en MySQL.

## 17. Verificaciones pendientes o parciales

| Prueba | Estado | Motivo y comando posterior |
|---|---|---|
| Registro visual completo con una sesión real | Parcial | El formulario compila y la pantalla de acceso se abrió. Falta iniciar sesión manualmente con un rol autorizado y registrar el cliente de demostración. Después se comprobará con `SELECT` en MySQL. |
| Prevención con dos solicitudes HTTP simultáneas reales | Pendiente | La doble protección está implementada (consulta más índices únicos), pero falta una prueba concurrente aislada que no deje datos de prueba en la base local compartida. |
| Vista previa de fotografía en navegador | Parcial | Implementada; requiere la prueba visual manual con un archivo JPEG, PNG o WebP local. |
| Respuesta visual de todos los toasts | Parcial | La Facade tiene pruebas unitarias; falta verificar manualmente cada variante contra backend en la sesión real. |

## 18. Preparación para varios talleres

El cliente se mantiene como entidad independiente. No se creó `taller_id`, talleres ficticios ni módulo de talleres. En una fase futura podrá añadirse una tabla intermedia `cliente_taller` para resolver una relación muchos a muchos entre clientes y talleres sin rediseñar `clientes`.

## 19. Tabla general de estado

| ID | Componente | Funcionalidad | Estado | Evidencia | Pendiente |
|---|---|---|---|---|---|
| F02-C01-01 | Frontend | Formulario protegido de cliente | Parcial | `ClientRegistrationView.vue`, `npm run build` | Prueba visual con sesión real. |
| F02-C01-02 | Backend | Registro transaccional de cliente | Completo | `ClienteFacade`, `ClienteService`, 10 pruebas backend | Ninguno para el caso de uso implementado. |
| F02-C01-03 | Seguridad | Autorización `CLIENTE_CREAR` | Completo | V4 y pruebas 201/401/403 | Ninguno. |
| F02-C01-04 | Base de datos | Prevención de duplicados | Completo | Tres índices únicos V4 y pruebas 409 | Falta únicamente prueba concurrente aislada. |
| F02-C01-05 | Interfaz | Toasts y confirmación visual | Parcial | Componentes y 2 pruebas Vitest | Validación manual de todas las variantes. |
| F02-C01-06 | Fotografía | Validación, almacenamiento privado y endpoint protegido | Completo | `ClientePhotoStorage` y pruebas de tipo/tamaño/acceso | Vista previa visual pendiente. |
| F02-C01-07 | Arquitectura | Facade y Repository | Completo | Facades y repositories creados | Ninguno. |
| F02-C01-08 | Talleres | Relación cliente-taller | No aplica | Documentado en esta fase | Se implementará solo en una fase futura autorizada. |

## 20. Archivos creados

```text
FASE_02.md
backend/src/main/resources/db/migration/V4__clientes_registro.sql
backend/src/main/java/com/taller/m01/controller/ClienteController.java
backend/src/main/java/com/taller/m01/controller/ClienteRequestExceptionHandler.java
backend/src/main/java/com/taller/m01/dto/ClienteDtos.java
backend/src/main/java/com/taller/m01/entity/Cliente.java
backend/src/main/java/com/taller/m01/entity/DireccionCliente.java
backend/src/main/java/com/taller/m01/repository/ClienteRepository.java
backend/src/main/java/com/taller/m01/repository/DireccionClienteRepository.java
backend/src/main/java/com/taller/m01/service/ClienteFacade.java
backend/src/main/java/com/taller/m01/service/ClientePhotoStorage.java
backend/src/main/java/com/taller/m01/service/ClienteService.java
backend/src/test/java/com/taller/m01/ClienteControllerIntegrationTests.java
frontend/src/components/ConfirmationModal.vue
frontend/src/components/ToastNotifications.vue
frontend/src/facades/clienteFacade.js
frontend/src/facades/notificacionFacade.js
frontend/src/facades/notificacionFacade.test.js
frontend/src/views/ClientRegistrationView.vue
```

## 21. Archivos modificados

```text
.env.example
backend/src/main/resources/application.properties
database/reference-schema.sql
frontend/package.json
frontend/src/App.vue
frontend/src/components/AppShell.vue
frontend/src/router/index.js
frontend/src/style.css
```

## 22. Tabla de métodos y funciones de Fase 02

| Capa | Archivo o clase | Método o función | Parámetros | Retorno | Responsabilidad | Validaciones | Estado |
|---|---|---|---|---|---|---|---|
| Backend | `ClienteController` | `registrar` | DTO, foto, usuario, petición | `201` + DTO | Recibe multipart y delega a Facade. | `@Valid`, `CLIENTE_CREAR` | Completo |
| Backend | `ClienteController` | `consultarFotografia` | id | Recurso de imagen | Sirve foto privada. | Autenticación y permiso | Completo |
| Backend | `ClienteFacade` | `registrarCliente` | DTO, foto, actor, IP | `ClientResponse` | Coordina caso de uso y auditoría. | Transacción y limpieza al rollback | Completo |
| Backend | `ClienteFacade` | `consultarFotografia` | id | `FotoLeida` | Coordina acceso de foto. | Cliente existente | Completo |
| Backend | `ClienteService` | `normalizarDatos` | DTO | `DatosNormalizados` | Limpia nombre, correo, teléfono y dirección. | Espacios, minúsculas y acentos | Completo |
| Backend | `ClienteService` | `validarDatos` | Datos normalizados | `void` | Reglas de fecha, teléfono y CP. | Rango de edad, 10–15 dígitos, 5 dígitos | Completo |
| Backend | `ClienteService` | `verificarDuplicado` | Datos normalizados | `void` | Consulta duplicados. | Correo, teléfono, nombre+fecha | Completo |
| Backend | `ClienteService` | `guardarCliente` | Datos, actor | `Cliente` | Guarda cliente y dirección. | Transacción, índices únicos | Completo |
| Backend | `ClienteService` | `guardarFotografia` | Cliente, referencia, actor | `Cliente` | Guarda referencia privada. | Solo referencia UUID validada | Completo |
| Backend | `ClientePhotoStorage` | `validar` | `MultipartFile` | `FotografiaValidada` | Comprueba antes de persistir. | Firma JPEG/PNG/WebP y 5 MB | Completo |
| Backend | `ClientePhotoStorage` | `guardar` | Foto validada | referencia UUID | Escribe archivo privado. | Ruta normalizada y nombre seguro | Completo |
| Backend | `ClientePhotoStorage` | `leer` | referencia | `FotoLeida` | Lee foto privada existente. | Expresión UUID y ruta segura | Completo |
| Backend | `ClientePhotoStorage` | `eliminarSiExiste` | referencia | `void` | Limpia archivo cuando hay rollback. | Ruta segura | Completo |
| Repository | `ClienteRepository` | `existsBy...` | Valores normalizados | `boolean` | Detecta duplicados. | Datos normalizados | Completo |
| Repository | `DireccionClienteRepository` | `existsByClienteId` | id cliente | `boolean` | Verifica relación en prueba. | ID de cliente | Completo |
| Frontend | `clienteFacade` | `registrarCliente` | Sin parámetros; usa formulario | `Promise<boolean>` | Construye multipart, envía y trata resultado. | Doble envío con `isSubmitting` | Completo |
| Frontend | `clienteFacade` | `validarFormulario` | Formulario | `boolean` | Valida antes del envío. | Nombre, fecha, teléfono, correo, CP y foto | Completo |
| Frontend | `clienteFacade` | `normalizarFormulario` | Formulario | `void` | Normaliza valores visibles. | Espacios, correo y teléfono | Completo |
| Frontend | `clienteFacade` | `procesarError` | Error HTTP | `void` | Mapea errores a campos y toast. | 400, 401, 403, 409 y red | Completo |
| Frontend | `clienteFacade` | `seleccionarFotografia` | Archivo | `void` | Crea vista previa segura. | Tipo y tamaño local | Completo |
| Frontend | `notificacionFacade` | `mostrarExito/Error/Advertencia/Informacion` | Mensaje, título opcional | id | Centraliza notificaciones. | Duración y cierre | Completo |
| Frontend | `notificacionFacade` | `cerrar` | id | `void` | Cierra y limpia temporizadores. | ID existente | Completo |

## 23. Funcionalidades completas

- Registro de cliente autenticado y autorizado.
- Roles solicitados con permiso nuevo `CLIENTE_CREAR`.
- Validaciones backend y frontend.
- Prevención de duplicados en aplicación y MySQL.
- Cliente y dirección dentro de transacción.
- Edad calculada sin columna persistida.
- Fotografía opcional, privada y protegida.
- Auditoría de alta.
- Facades de cliente y notificación.
- Toasts, modal de limpieza y prevención de doble envío.
- Migración Flyway, pruebas backend y build/frontend test ejecutados.

## 24. Funcionalidades parciales

- Demostración visual con credenciales reales, foto local y registro persistente por navegador.
- Validación manual de cada toast y del diseño responsivo en teléfono/tableta.
- Prueba concurrente HTTP aislada contra MySQL compartido.

## 25. Funcionalidades pendientes fuera de alcance

- Listado, edición, baja y consulta general de clientes.
- Relación `cliente_taller` y módulo de talleres.
- Vehículos, inventario, servicios, órdenes de trabajo, empleados, proveedores y reportes.
