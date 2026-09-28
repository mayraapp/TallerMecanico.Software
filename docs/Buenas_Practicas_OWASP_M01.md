# Matriz de buenas prácticas OWASP para Spring Boot — AutoManager M01

- **Proyecto:** AutoManager — Taller mecánico
- **Fase evaluada:** M01: seguridad, autenticación, autorización, roles y usuarios
- **Tecnología evaluada:** Java 26, Spring Boot, Spring Security, JPA, Flyway, MySQL, Vue 3
- **Fecha de revisión:** 28 de septiembre de 2026

## Cómo leer este documento

OWASP no es una biblioteca que se instala. Es una guía para decidir qué controles de seguridad debe tener una aplicación. Spring Boot y Spring Security son las herramientas con las que esos controles se programan.

GitHub Markdown no permite colorear texto de forma fiable; por eso se usan indicadores de color:

| Indicador | Significado |
|---|---|
| 🟢 | Implementado y localizado en el código actual. |
| 🟡 | Existe una base, pero falta endurecerlo, configurarlo en producción o comprobarlo con pruebas. |
| 🔴 | No está implementado; se indica exactamente dónde incorporarlo. |

> Esta es una revisión de código de la Fase M01, no una certificación de seguridad ni una prueba de penetración. Los módulos de clientes, vehículos, órdenes de trabajo e inventario deberán revisarse cuando se desarrollen.

## Resumen ejecutivo

| Estado | Cantidad | Lectura rápida |
|---|---:|---|
| 🟢 Implementado | 8 | Base de identidad, roles, contraseñas, validación, auditoría y migraciones. |
| 🟡 Por endurecer | 9 | Requieren configuración de producción, crecimiento de la aplicación o una decisión de seguridad. |
| 🔴 Faltante | 5 | Deben priorizarse antes de publicar la aplicación en Internet. |

## Matriz principal: OWASP aplicado a Spring Boot

| # | Práctica OWASP / Spring Boot | Estado | Dónde se usa hoy | Qué protege | Qué falta o dónde agregarlo |
|---:|---|:---:|---|---|---|
| 1 | **Denegar por defecto y autorizar en el servidor** | 🟢 | [SecurityConfig.java](../backend/src/main/java/com/taller/m01/config/SecurityConfig.java), [UserController.java](../backend/src/main/java/com/taller/m01/controller/UserController.java), [RoleController.java](../backend/src/main/java/com/taller/m01/controller/RoleController.java), [AuditController.java](../backend/src/main/java/com/taller/m01/controller/AuditController.java) | A01: evita que una persona sin permiso ejecute operaciones administrativas. | Mantener `anyRequest().authenticated()` y añadir `@PreAuthorize` a cada endpoint futuro. El control del frontend es solo visual; el backend debe seguir siendo la autoridad. |
| 2 | **Permisos por objeto, no solo por pantalla** | 🟡 | Permisos de usuarios, roles y auditoría se validan por autoridad en los controladores. | A01 / IDOR: evita que un usuario consulte o modifique el registro de otra persona por cambiar un ID en la URL. | Al crear clientes, vehículos u órdenes, añadir la comprobación de propietario/sucursal en el servicio, por ejemplo en `ClientService`, `VehicleService` y `WorkOrderService`; no basta con `hasAuthority`. |
| 3 | **Hash seguro de contraseñas** | 🟢 | [SecurityConfig.java](../backend/src/main/java/com/taller/m01/config/SecurityConfig.java) crea `BCryptPasswordEncoder`; [AuthService.java](../backend/src/main/java/com/taller/m01/service/AuthService.java) lo usa al crear o cambiar contraseñas. | A04/A07: impide almacenar contraseñas legibles en MySQL. | Conservar BCrypt o migrar deliberadamente a Argon2 si se toma esa decisión. Nunca guardar, registrar ni devolver una contraseña. |
| 4 | **Política de contraseña moderna** | 🟡 | [PasswordPolicy.java](../backend/src/main/java/com/taller/m01/service/PasswordPolicy.java) exige 8 caracteres, mayúscula, minúscula, número y símbolo. | A07: reduce contraseñas fáciles de adivinar. | Antes de producción, permitir frases largas (hasta 64 caracteres o más), preferir longitud sobre reglas de composición y bloquear contraseñas filtradas/comunes. Sin MFA, OWASP recomienda una longitud mínima mayor. |
| 5 | **Inicio de sesión resistente a ataques automatizados** | 🟢 | [AuthService.java](../backend/src/main/java/com/taller/m01/service/AuthService.java), [RateLimitService.java](../backend/src/main/java/com/taller/m01/service/RateLimitService.java), [LoginAttempt.java](../backend/src/main/java/com/taller/m01/entity/LoginAttempt.java) | A07: reduce fuerza bruta y credential stuffing. Se bloquea una cuenta activa tras cinco intentos y se limita la frecuencia de solicitudes. | Añadir MFA para superadministradores cuando exista un entorno real de producción. |
| 6 | **Recuperación de contraseña segura** | 🟡 | [AuthController.java](../backend/src/main/java/com/taller/m01/controller/AuthController.java), [AuthService.java](../backend/src/main/java/com/taller/m01/service/AuthService.java), [PasswordRecoveryCode.java](../backend/src/main/java/com/taller/m01/entity/PasswordRecoveryCode.java) | A07: usa código aleatorio, hash SHA-256, vencimiento de 10 minutos, un uso y límite de intentos. | El código se escribe en el log solo en modo desarrollo. Para producción: fijar `DEVELOPMENT_MODE=false`, enviar el código por correo seguro y no mostrarlo ni registrarlo. |
| 7 | **JWT firmado, corto y revocable** | 🟢 | [JwtService.java](../backend/src/main/java/com/taller/m01/security/JwtService.java), [JwtAuthenticationFilter.java](../backend/src/main/java/com/taller/m01/security/JwtAuthenticationFilter.java), [RevokedSession.java](../backend/src/main/java/com/taller/m01/entity/RevokedSession.java) | A04/A07: verifica firma, expiración, estado activo, versión de sesión y revocación al cerrar sesión. | Conservar un secreto Base64 de al menos 32 bytes fuera del repositorio y rotarlo si se expone. |
| 8 | **Cookie de sesión protegida** | 🟡 | [AuthController.java](../backend/src/main/java/com/taller/m01/controller/AuthController.java) crea `TM_SESSION` con `HttpOnly` y `SameSite=Strict`. | A04/A07: JavaScript no puede leer el token y el navegador limita envío entre sitios. | En producción configurar `COOKIE_SECURE=true` y servir solo HTTPS. Validar el dominio final del frontend y backend antes de cambiar la política `SameSite`. |
| 9 | **Protección CSRF para autenticación basada en cookies** | 🔴 | Actualmente [SecurityConfig.java](../backend/src/main/java/com/taller/m01/config/SecurityConfig.java) contiene `csrf(csrf -> csrf.disable())` y el JWT se lee desde la cookie `TM_SESSION`. | A01/A07: evita que otro sitio induzca al navegador autenticado a ejecutar una operación con su cookie. | En `SecurityConfig.java`, habilitar protección CSRF con `CookieCsrfTokenRepository`; en [client.js](../frontend/src/api/client.js), enviar el encabezado CSRF requerido. También añadir `X-XSRF-TOKEN` a los encabezados CORS permitidos. Este punto debe resolverse antes de producción. |
| 10 | **CORS de mínimo privilegio** | 🟡 | [SecurityConfig.java](../backend/src/main/java/com/taller/m01/config/SecurityConfig.java) permite un único `FRONTEND_ORIGIN`, métodos concretos y credenciales. | A02: evita que sitios ajenos llamen a la API con cookies del usuario. | En despliegue definir el dominio HTTPS real en `FRONTEND_ORIGIN`; no usar `*` cuando `allowCredentials` está activo. Ajustar los encabezados permitidos al habilitar CSRF. |
| 11 | **Validar datos en el límite de la API** | 🟢 | DTOs en [AuthDtos.java](../backend/src/main/java/com/taller/m01/dto/AuthDtos.java) y [UserDtos.java](../backend/src/main/java/com/taller/m01/dto/UserDtos.java); controladores con `@Valid`. | A05/A10: bloquea datos incompletos o malformados antes de llegar a la lógica o base de datos. | Todo DTO nuevo debe usar validaciones de Bean Validation; no usar entidades JPA como cuerpo directo de una petición. |
| 12 | **Evitar inyección SQL** | 🟢 | Repositorios de Spring Data en [repository/](../backend/src/main/java/com/taller/m01/repository/) y DTOs validados. | A05: evita que valores del usuario se interpreten como SQL. | Si algún módulo requiere SQL nativo, usar parámetros nombrados o preparados; nunca concatenar valores recibidos en una consulta. |
| 13 | **Errores controlados y sin trazas internas** | 🟢 | [GlobalExceptionHandler.java](../backend/src/main/java/com/taller/m01/exception/GlobalExceptionHandler.java), [application.properties](../backend/src/main/resources/application.properties) | A10: evita revelar clases, consultas, secretos o detalles de infraestructura. | Mantener `server.error.include-message=never` y registrar internamente la causa técnica sin enviarla al usuario. |
| 14 | **Cabeceras HTTP de seguridad** | 🔴 | No hay configuración explícita de CSP, `X-Content-Type-Options`, `Referrer-Policy`, anti-clickjacking u HSTS en el código revisado. | A02/A04: reduce XSS, clickjacking, MIME sniffing y conexiones HTTP inseguras. | Agregar `headers(...)` en `SecurityConfig.java`. Configurar CSP según los recursos reales; `frameOptions().deny()`, `contentTypeOptions()`, `referrerPolicy()` y HSTS solo bajo HTTPS. Comprobarlas también en el proxy o proveedor de hosting. |
| 15 | **Secretos y configuración fuera de Git** | 🟡 | [application.properties](../backend/src/main/resources/application.properties) lee variables; `.gitignore` excluye `.env`; [.env.example](../.env.example) es plantilla sin valores reales. | A02/A04: evita publicar contraseña MySQL y secreto JWT. | Para producción, cargar secretos desde el gestor del proveedor de hosting y no desde una copia de `backend/.env`. Cambiar inmediatamente cualquier secreto expuesto. |
| 16 | **Migraciones e integridad del esquema** | 🟢 | [db/migration/](../backend/src/main/resources/db/migration/) contiene `V1`, `V2` y `V3`; `spring.jpa.hibernate.ddl-auto=validate`. | A08: evita cambios manuales e inconsistentes en la base de datos. | Crear una nueva migración Flyway por cada cambio; no editar una migración ya aplicada en un ambiente compartido. |
| 17 | **Auditoría y registro de eventos** | 🟡 | [AuditService.java](../backend/src/main/java/com/taller/m01/service/AuditService.java), [AuditLog.java](../backend/src/main/java/com/taller/m01/entity/AuditLog.java), [LoginAttempt.java](../backend/src/main/java/com/taller/m01/entity/LoginAttempt.java) | A09: permite investigar accesos, cambios de rol, bloqueos y administración de cuentas. | En producción, centralizar logs, definir alertas y una retención. No registrar contraseñas, tokens JWT ni códigos de recuperación. |
| 18 | **Rate limiting apto para producción** | 🟡 | [RateLimitService.java](../backend/src/main/java/com/taller/m01/service/RateLimitService.java) limita por IP en memoria. | A07/A10: reduce abuso de login, recuperación y registro público. | Si la API se ejecuta en más de una instancia, trasladar el contador a Redis o a un servicio compartido. Definir limpieza/expiración de buckets para evitar crecimiento ilimitado en memoria. |
| 19 | **Dependencias sin vulnerabilidades conocidas** | 🔴 | Dependencias declaradas en [pom.xml](../backend/pom.xml) y [package.json](../frontend/package.json), sin análisis automatizado configurado. | A03: reduce el uso de bibliotecas vulnerables. | Agregar análisis de dependencias en CI: plugin OWASP Dependency-Check o Dependabot para Maven; `npm audit`/Dependabot para el frontend. Corregir o justificar cada hallazgo antes de publicar. |
| 20 | **Pruebas automáticas de seguridad** | 🔴 | El único test actual, [TallerSecurityApiApplicationTests.java](../backend/src/test/java/com/taller/m01/TallerSecurityApiApplicationTests.java), comprueba que el contexto inicia. | A01/A07/A10: evita regresiones que abran rutas, permisos o sesiones en futuros cambios. | Crear pruebas con `MockMvc` para 401 sin sesión, 403 sin permiso, 200 con permiso correcto, bloqueo tras cinco fallos, revocación de sesión, validación de DTOs y CSRF cuando se habilite. |
| 21 | **Transporte HTTPS y cookie segura** | 🔴 | El ambiente local usa HTTP y `COOKIE_SECURE` puede estar en `false`, lo cual es válido solo para desarrollo. | A04: impide que credenciales y cookies viajen sin cifrado en redes reales. | Configurar HTTPS en el proveedor de hosting, redirigir HTTP a HTTPS, activar `COOKIE_SECURE=true` y probar el flujo completo de sesión con dominios reales. |
| 22 | **Cuenta MySQL con mínimo privilegio** | 🟡 | La aplicación se conecta con el usuario `taller_app`, configurado por variables de entorno. | A01/A02: limita el daño si se compromete la API. | En producción, otorgar solo privilegios necesarios sobre `taller_mecanico_db`; no otorgar privilegios globales, de administración de usuarios o de otras bases de datos. |

## Prioridad de implementación

### 🔴 Prioridad 1 — Antes de publicar en Internet

1. **CSRF:** habilitarlo porque la sesión se transporta en cookie.
2. **HTTPS y `COOKIE_SECURE=true`:** sin HTTPS no debe existir un despliegue real con usuarios.
3. **Cabeceras de seguridad:** configurarlas en Spring Security y comprobarlas en el navegador.
4. **Pruebas de autorización:** demostrar automáticamente que cada rol obtiene solo los recursos permitidos.
5. **Análisis de dependencias:** incluirlo en el proceso de integración antes de cada publicación.

### 🟡 Prioridad 2 — Al preparar el ambiente productivo

1. Establecer `DEVELOPMENT_MODE=false` para que no se registren códigos de recuperación.
2. Definir el dominio exacto en `FRONTEND_ORIGIN` y comprobar CORS.
3. Trasladar el rate limit a almacenamiento compartido si se usan varias instancias.
4. Centralizar auditoría y alertas.
5. Dar al usuario MySQL únicamente los permisos imprescindibles.

### 🟡 Prioridad 3 — Al crear módulos futuros

1. Comprobar permiso **y pertenencia del registro** en cada cliente, vehículo u orden de trabajo.
2. Repetir validación de DTOs, migraciones Flyway y pruebas de autorización.
3. Aplicar el mismo modelo de auditoría a cambios sensibles de cada módulo.

## Ubicación de los controles más importantes

| Necesidad | Archivo principal | Responsabilidad |
|---|---|---|
| Reglas globales de seguridad, CORS, CSRF y cabeceras | [SecurityConfig.java](../backend/src/main/java/com/taller/m01/config/SecurityConfig.java) | Define qué rutas son públicas, qué peticiones necesitan sesión y qué defensas HTTP están activas. |
| Creación y validación de JWT | [JwtService.java](../backend/src/main/java/com/taller/m01/security/JwtService.java) | Firma tokens, establece expiración y los valida. |
| Lectura de sesión en cada petición | [JwtAuthenticationFilter.java](../backend/src/main/java/com/taller/m01/security/JwtAuthenticationFilter.java) | Lee la cookie, comprueba token, estado del usuario y revocación. |
| Inicio, cierre y recuperación de contraseña | [AuthService.java](../backend/src/main/java/com/taller/m01/service/AuthService.java) | Aplica BCrypt, bloqueo, recuperación y auditoría. |
| Política de contraseña | [PasswordPolicy.java](../backend/src/main/java/com/taller/m01/service/PasswordPolicy.java) | Define requisitos de contraseña; aquí se mejorará longitud/lista de contraseñas filtradas. |
| Permisos de cada API | Controladores en [controller/](../backend/src/main/java/com/taller/m01/controller/) | Cada operación administrativa usa `@PreAuthorize`. |
| Datos recibidos por API | [AuthDtos.java](../backend/src/main/java/com/taller/m01/dto/AuthDtos.java) y [UserDtos.java](../backend/src/main/java/com/taller/m01/dto/UserDtos.java) | Aplican validación antes de procesar la petición. |
| Peticiones del frontend y futuro token CSRF | [client.js](../frontend/src/api/client.js) | Configura Axios, credenciales y deberá enviar la cabecera CSRF. |
| Configuración y secretos | [application.properties](../backend/src/main/resources/application.properties), `backend/.env` | Lee variables del entorno; el archivo `.env` real no se versiona. |
| Pruebas de seguridad | [backend/src/test/](../backend/src/test/) | Aquí se agregarán pruebas con MockMvc y Spring Security Test. |

## Reglas que deben mantenerse en todo código nuevo

- No confiar en permisos ocultos en Vue: todo permiso importante se valida otra vez en Spring Boot.
- No concatenar SQL, comandos, URLs ni HTML con datos del usuario.
- No escribir secretos, tokens, contraseñas ni códigos temporales en commits, capturas, logs o respuestas de API.
- Validar cada cuerpo de petición con DTOs y `@Valid`.
- Usar una migración Flyway nueva para cada cambio de base de datos.
- Incluir pruebas de 401, 403 y 200 para cada nueva operación protegida.
- Revisar esta matriz cuando se añada un módulo o se cambie el modelo de sesión.

## Referencias oficiales

- [OWASP Top 10:2025](https://owasp.org/Top10/2025/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- [OWASP Cross-Site Request Forgery Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
- [Spring Security: autenticación con usuario y contraseña](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html)
- [Spring Security: protección CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

## Conclusión

La Fase M01 cuenta con una buena base de seguridad de Spring Boot: autenticación, JWT, roles, permisos, BCrypt, validación, auditoría, recuperación y migraciones. No obstante, el estado correcto no es “todo terminado”: CSRF para las cookies, cabeceras HTTP, HTTPS, análisis de dependencias y pruebas automatizadas de autorización son los faltantes prioritarios. Esta matriz permite explicar al profesor qué está hecho, dónde está implementado y qué pasos concretos siguen.
