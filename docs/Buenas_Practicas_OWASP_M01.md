# Buenas prácticas OWASP — AutoManager, Fase M01

**Proyecto:** AutoManager — Taller mecánico  
**Fase:** M01: seguridad, autenticación, autorización, roles y usuarios  
**Fecha de revisión:** 28 de septiembre de 2026  
**Referencia:** [OWASP Top 10:2025](https://owasp.org/Top10/2025/)

## 1. Propósito y alcance

Este documento relaciona las prácticas de seguridad aplicadas en la Fase M01 de AutoManager con los riesgos del **OWASP Top 10:2025**. Su alcance se limita a las funciones desarrolladas: registro público, autenticación, recuperación de contraseña, sesiones, usuarios, roles, permisos y auditoría.

No certifica que la aplicación esté libre de vulnerabilidades ni sustituye una prueba de penetración. Los módulos de clientes, vehículos, inventario, empleados, servicios, órdenes de trabajo, proveedores y reportes no forman parte de esta fase; deberán aplicar la misma matriz cuando sean desarrollados.

### Criterio de estado

- **✅ Aplicado:** existe una medida implementada en el código de esta fase.
- **◐ Parcial / siguiente paso:** existe una base, pero requiere configuración de producción, automatización o una validación adicional.
- **☐ Pendiente:** aún no se implementa porque está fuera del alcance de M01.

## 2. Matriz de cumplimiento

| Riesgo OWASP 2025 | Riesgo para AutoManager | Medida aplicada en M01 | Estado | Evidencia técnica |
|---|---|---|---|---|
| **A01: Control de acceso roto** | Que una persona consulte o modifique usuarios, roles, auditoría o datos que no le corresponden. | Control de acceso basado en roles y permisos (RBAC). Las rutas de API exigen autenticación y autorizaciones específicas; el router del frontend evita la navegación a pantallas no autorizadas. | ✅ | `SecurityConfig`, `UserController`, `RoleController`, `AuditController`, `frontend/src/router/index.js` |
| **A02: Configuración insegura** | Exponer credenciales, aceptar orígenes no autorizados o publicar parámetros de desarrollo. | Datos sensibles mediante variables de entorno; `backend/.env` está ignorado por Git. CORS se configura para el origen del frontend y existe un archivo de ejemplo sin secretos. | ✅ | `.gitignore`, `.env.example`, `backend/.env` *(local, no versionado)*, `AppSecurityProperties`, `SecurityConfig` |
| **A03: Fallos en la cadena de suministro de software** | Incorporar una dependencia con vulnerabilidades conocidas. | Dependencias declaradas y versionadas mediante Maven y npm. | ◐ | `backend/pom.xml`, `frontend/package.json`, archivos de bloqueo de dependencias |
| **A04: Fallos criptográficos** | Exponer contraseñas, sesiones o datos sensibles. | Contraseñas cifradas con BCrypt. JWT firmado con secreto externo. Sesión en cookie `HttpOnly` y `SameSite=Strict`; el modo seguro de cookie puede habilitarse para HTTPS. | ◐ | `AuthService`, `PasswordPolicy`, `JwtService`, `SecurityConfig`, `AppSecurityProperties` |
| **A05: Inyección** | Alterar consultas, comandos o datos mediante entradas de formularios. | Uso de Spring Data JPA, DTOs con validaciones y migraciones Flyway. Las operaciones de usuarios y roles se realizan por repositorios, no mediante SQL concatenado. | ✅ | `dto/`, `repository/`, `exception/GlobalExceptionHandler`, `resources/db/migration/` |
| **A06: Diseño inseguro** | Dar acceso automático a cuentas recién registradas o no definir reglas de privilegios. | El registro público crea cuentas con estado `PENDING`, inactivas y sin roles. Un superadministrador debe activar la cuenta y asignarle permisos. | ✅ | `PublicRegistrationController`, `UserService`, `UserStatus`, migración `V3__...sql` |
| **A07: Fallos de autenticación** | Suplantar cuentas, mantener sesiones inválidas o abusar de la recuperación de contraseña. | Inicio y cierre de sesión, JWT con expiración, cierre de sesiones revocadas, política de contraseña, recuperación con código temporal y bloqueo temporal tras intentos fallidos. | ✅ | `AuthController`, `AuthService`, `JwtAuthenticationFilter`, `RateLimitService`, `LoginAttempt`, `PasswordRecoveryCode`, `RevokedSession` |
| **A08: Fallos de integridad de software o datos** | Ejecutar cambios de esquema no controlados o aceptar datos alterados. | Esquema de base de datos versionado con Flyway; JWT firmado y DTOs validados en el servidor. | ◐ | `resources/db/migration/V1__...sql`, `V2__...sql`, `V3__...sql`, `JwtService`, DTOs |
| **A09: Fallos de registro y alertas de seguridad** | No detectar accesos, cambios administrativos o intentos fallidos. | Auditoría persistente de acciones y registro de intentos de inicio de sesión. Las pantallas administrativas permiten consultar auditoría según permiso. | ◐ | `AuditService`, `AuditLog`, `LoginAttempt`, `AuditController`, `frontend/src/views/AuditView.vue` |
| **A10: Manejo inseguro de condiciones excepcionales** | Mostrar trazas internas al usuario o procesar fallos de forma inconsistente. | Manejo global de excepciones, errores de validación controlados y respuestas API consistentes. | ✅ | `ApiException`, `GlobalExceptionHandler`, `UiAlert.vue` |

## 3. Controles implementados por componente

### Backend

1. **Autenticación y sesiones**
   - `AuthController` expone las operaciones de inicio/cierre de sesión, usuario autenticado y recuperación de contraseña.
   - `JwtService` emite y verifica JWT; `JwtAuthenticationFilter` obtiene la identidad para cada solicitud protegida.
   - Las sesiones cerradas se registran en `RevokedSession`, evitando que un token revocado continúe siendo aceptado.

2. **Autorización**
   - `SecurityConfig` establece las rutas públicas y las rutas protegidas.
   - Los controladores de usuarios, roles y auditoría aplican permisos de servidor. La interfaz no sustituye esta verificación.
   - `Role`, `Permission` y `UserAccount` permiten representar el modelo RBAC en la base de datos.

3. **Protección de credenciales**
   - `AuthService` usa `BCryptPasswordEncoder`; no se almacenan contraseñas en texto plano.
   - La política de contraseña se concentra en `PasswordPolicy`.
   - El secreto JWT, la contraseña de MySQL y otros valores configurables permanecen fuera del código versionado.

4. **Control de abuso y recuperación**
   - `RateLimitService` limita solicitudes sensibles.
   - `LoginAttempt` permite bloquear temporalmente después de intentos fallidos.
   - `PasswordRecoveryCode` usa códigos con vencimiento y límite de intentos.

5. **Validación, errores y auditoría**
   - Los DTOs validan entradas antes de procesarlas.
   - `GlobalExceptionHandler` evita respuestas improvisadas y estandariza los errores.
   - `AuditService` registra eventos relevantes de administración y seguridad.

### Frontend

1. `stores/auth.js` conserva el usuario autenticado, sus roles y permisos durante la sesión de la interfaz.
2. `router/index.js` protege rutas y redirige a la pantalla de acceso denegado cuando corresponde.
3. `api/client.js` centraliza las solicitudes HTTP y envía credenciales de sesión de forma controlada.
4. Las pantallas de inicio de sesión, registro, recuperación, usuarios, roles y auditoría consumen las validaciones y permisos definidos por la API.

## 4. Gestión segura de configuración

| Recurso | Ubicación | Regla de seguridad |
|---|---|---|
| Credenciales locales y secreto JWT | `backend/.env` | Es un archivo local. No debe subirse, copiarse en capturas ni enviarse por chat. |
| Plantilla de variables | `.env.example` | Puede versionarse porque no contiene valores reales. |
| Exclusiones de Git | `.gitignore` | Excluye archivos `.env` y otros artefactos locales. |
| Configuración de aplicación | `backend/src/main/resources/application.properties` | Lee los valores desde el entorno; no debe contener secretos reales. |

## 5. Recomendaciones obligatorias antes de producción

Estas acciones no se marcan como terminadas porque requieren infraestructura o automatización de despliegue:

- [ ] Publicar exclusivamente bajo HTTPS y establecer `COOKIE_SECURE=true`.
- [ ] Configurar `FRONTEND_ORIGIN` con el dominio real de producción; no usar comodines en CORS.
- [ ] Usar un gestor de secretos del proveedor de alojamiento, nunca un archivo `.env` publicado.
- [ ] Rotar el secreto JWT y las contraseñas si se exponen o cambia una persona responsable.
- [ ] Añadir análisis automatizado de dependencias (`mvn` y `npm`) al flujo de integración continua.
- [ ] Ejecutar pruebas de autorización: cada rol debe intentar acceder a cada recurso que no le corresponda y recibir `403`.
- [ ] Centralizar registros y crear alertas para bloqueos repetidos, cambios de rol, altas administrativas y fallos anómalos.
- [ ] Limitar las cuentas de base de datos al mínimo privilegio necesario; el usuario de producción no debe administrar otros esquemas.
- [ ] Hacer respaldo cifrado de la base de datos y probar la restauración.
- [ ] Realizar una revisión de seguridad o prueba de penetración antes de manejar datos reales de clientes y vehículos.

## 6. Pruebas y evidencia de la Fase M01

Durante la fase se verificó el arranque del contexto de Spring Boot y las migraciones de Flyway contra MySQL local. También se generó correctamente el build de producción del frontend con Vite. Estas verificaciones demuestran integración básica; no equivalen a una auditoría integral de seguridad.

Para una entrega académica se recomienda demostrar, con capturas o una prueba guiada, al menos los siguientes casos:

1. Un usuario no autenticado es redirigido a inicio de sesión al abrir una pantalla protegida.
2. Un usuario con rol insuficiente obtiene acceso denegado al solicitar una operación administrativa.
3. Una cuenta registrada públicamente permanece `PENDING` hasta que un superadministrador la active.
4. Una contraseña incorrecta incrementa el intento de acceso y se aplica el bloqueo configurado.
5. La recuperación de contraseña expira y no permite reutilizar un código ya usado.
6. Un cambio de usuario, rol o estado deja un registro de auditoría.

## 7. Conclusión

AutoManager M01 incorpora una base de seguridad alineada con OWASP para la identidad y el control de acceso: contraseñas protegidas, sesiones con JWT, roles, permisos, validaciones, auditoría, control de intentos y configuración sin secretos en Git. Las tareas pendientes se concentran en endurecimiento y operación en producción, no en sustituir los controles ya implementados.

La seguridad deberá revisarse nuevamente al desarrollar cada módulo futuro, especialmente antes de almacenar datos personales, vehículos, órdenes de trabajo o pagos.
