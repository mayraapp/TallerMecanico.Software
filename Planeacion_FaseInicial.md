# Planeación de la fase inicial — AutoManager

## 1. Propósito y alcance

**AutoManager** es el sistema web para la gestión de un taller mecánico. La primera entrega se denominó **M01: Seguridad, autenticación, autorización, roles y usuarios**. Su objetivo es identificar a cada persona que entra al sistema, conservar su sesión de forma segura y permitirle únicamente las pantallas y acciones autorizadas.

Esta fase no incorpora aún la operación del taller (clientes, vehículos, inventario, empleados, servicios, órdenes de trabajo, proveedores ni reportes). Los permisos relacionados con órdenes de trabajo existen como preparación del catálogo de permisos, pero el módulo de órdenes no existe todavía.

## 2. Estado por fases y módulos

| Fase o módulo | Estado | Descripción objetiva |
|---|:---:|---|
| M01 — Seguridad, autenticación, roles y usuarios | ✅ | Implementación local de acceso, sesión JWT, roles, permisos, gestión de usuarios, recuperación de contraseña, auditoría y registro público pendiente de aprobación. |
| M01 — Pruebas automatizadas de casos de negocio | ☐ | Existe una prueba de carga del contexto Spring; faltan pruebas unitarias e integrales para cada regla de autenticación, autorización y usuario. |
| M01 — Despliegue productivo | ☐ | La aplicación está preparada para configuración por variables de entorno, pero todavía no se ha publicado en un hosting público. |
| M02 — Clientes | ☐ | Pendiente. No hay pantallas, API ni tablas de clientes. |
| M02 — Vehículos | ☐ | Pendiente. No hay pantallas, API ni tablas de vehículos. |
| M03 — Inventario | ☐ | Pendiente. No hay pantallas, API ni tablas de inventario. |
| M03 — Empleados | ☐ | Pendiente. La administración de usuarios no sustituye un expediente de empleado. |
| M03 — Servicios | ☐ | Pendiente. No hay catálogo de servicios. |
| M03 — Órdenes de trabajo | ☐ | Pendiente. Sólo se sembraron permisos `WORK_ORDERS_*` para una fase posterior. |
| M03 — Proveedores | ☐ | Pendiente. No hay pantallas, API ni tablas de proveedores. |
| M04 — Reportes | ☐ | Pendiente. La auditoría de seguridad no es un módulo de reportes operativos. |

## 3. Elementos desarrollados en M01

### 3.1 Identidad y sesiones

- Inicio y cierre de sesión por correo y contraseña.
- Contraseñas almacenadas con `BCryptPasswordEncoder`; nunca se guardan en texto plano.
- JWT firmado que contiene el identificador del usuario, versión de sesión, identificador único de token y vencimiento.
- Cookie de sesión `TM_SESSION` con atributos `HttpOnly`, `SameSite=Strict`, ruta `/` y la opción `Secure` configurable mediante entorno.
- Revocación de token al cerrar sesión y aumento de versión de sesión al cambiar o restablecer la contraseña.
- Bloqueo temporal de 15 minutos después de cinco intentos fallidos en una cuenta activa.
- Límite de frecuencia en memoria para inicio de sesión, recuperación, verificación, restablecimiento y registro público.

### 3.2 Autorización y usuarios

- Roles iniciales: `SUPERADMIN`, `ADMIN`, `RECEPTIONIST`, `MECHANIC` y `PENDING`.
- Permisos independientes de los roles, validados en los controladores REST mediante `@PreAuthorize`.
- Administración de usuarios: consulta paginada, búsqueda, creación interna, edición, activación, desactivación, desbloqueo y cambio de rol.
- Protección de jerarquía: no se puede crear un Superadministrador desde la interfaz de usuarios, editar al administrador inicial, administrar a otro Superadministrador ni desactivar al último Superadministrador activo.
- Configuración de permisos por rol; los permisos del Superadministrador no son modificables.
- Registro público desde la pantalla de bienvenida. Crea una cuenta con rol `PENDING`, estado `INACTIVE` y sin permisos; un Superadministrador debe activarla y asignarle un rol antes de que pueda iniciar sesión.

### 3.3 Recuperación y auditoría

- Flujo de recuperación: solicitud, validación de código de seis dígitos y restablecimiento.
- Los códigos se guardan como hash SHA-256, vencen a los 10 minutos y admiten hasta cinco intentos.
- En modo de desarrollo el código se registra sólo en el log del backend; no hay envío de correo implementado todavía.
- Auditoría de inicio de sesión, intentos fallidos, bloqueos, cierres de sesión, cambios de contraseña, recuperación, creación y modificación de usuarios, cambios de rol y permisos.
- La vista de auditoría aclara que no debe mostrar contraseñas, hashes, códigos ni tokens.

### 3.4 Experiencia de usuario

- Interfaz Vue 3 con marca **AutoManager**, diseño adaptable y elementos visuales de `lucide-vue-next`.
- Pantallas públicas: acceso, registro público, recuperación, verificación de código y nueva contraseña.
- Pantallas protegidas: panel, registro interno de usuarios, usuarios, roles/permisos y auditoría.
- Guardas de Vue Router que redirigen al inicio de sesión y comprueban permisos antes de mostrar las rutas privadas.
- Pinia conserva el perfil autenticado, los roles, los permisos y el estado de carga de sesión.

## 4. Roles y datos de autorización

| Rol | Estado | Datos o capacidades asignadas |
|---|:---:|---|
| `SUPERADMIN` | ✅ | Todos los permisos de M01. Consulta y administra usuarios, roles, permisos y auditoría. |
| `ADMIN` | ✅ | Permisos de panel y de órdenes de trabajo preparados para fases posteriores. No posee permisos de gestión de usuarios de M01. |
| `RECEPTIONIST` | ✅ | Permisos preparados para consultar y crear órdenes de trabajo futuras. |
| `MECHANIC` | ✅ | Permisos preparados para consultar y editar órdenes de trabajo futuras. |
| `PENDING` | ✅ | Solicitud de registro público sin permisos y con cuenta inactiva hasta autorización. |

Los permisos persistidos son: `USERS_VIEW`, `USERS_CREATE`, `USERS_EDIT`, `USERS_ACTIVATE`, `USERS_DEACTIVATE`, `USERS_UNLOCK`, `ROLES_ASSIGN`, `PERMISSIONS_MANAGE`, `AUDIT_VIEW`, `ADMIN_PANEL_VIEW`, `WORK_ORDERS_VIEW`, `WORK_ORDERS_CREATE` y `WORK_ORDERS_EDIT`.

## 5. Datos y modelo de base de datos

Las migraciones reales las ejecuta Flyway desde `backend/src/main/resources/db/migration`. El archivo `database/reference-schema.sql` es una guía humana y no crea información por sí mismo.

| Tabla | Estado | Datos que resguarda |
|---|:---:|---|
| `usuarios` | ✅ | Identidad, correo único, teléfono, hash BCrypt, estado, bloqueos, último acceso, obligación de cambio de contraseña y versión de sesión. |
| `roles` | ✅ | Código, nombre y descripción de cada rol. |
| `permisos` | ✅ | Código, nombre y descripción de una capacidad autorizable. |
| `usuario_roles` | ✅ | Relación entre cada usuario y sus roles. |
| `rol_permisos` | ✅ | Relación entre cada rol y sus permisos. |
| `codigos_recuperacion` | ✅ | Hash, vencimiento, uso e intentos de códigos de recuperación. |
| `intentos_acceso` | ✅ | Intentos de inicio de sesión, resultado, correo e IP. |
| `auditoria` | ✅ | Responsable, afectado, acción, IP, detalle seguro y fecha. |
| `sesiones_revocadas` | ✅ | Identificador JWT revocado y su vencimiento. |
| Datos de clientes, vehículos, inventario, empleados, servicios, órdenes, proveedores y reportes | ☐ | Aún no se crean tablas para esos módulos. |

Las migraciones son incrementales:

1. `V1__security_schema.sql` crea las nueve tablas de seguridad y sus índices.
2. `V2__roles_and_permissions.sql` registra los cuatro roles operativos y los permisos iniciales.
3. `V3__public_registration_pending_role.sql` agrega el rol `PENDING` sin permisos.

## 6. Documentación del código generado

### 6.1 Backend — `backend/`

| Ubicación | Responsabilidad implementada |
|---|---|
| `pom.xml` | Proyecto Maven con Java 26, Spring Boot 4.1.1, Spring Web MVC, Security, Data JPA, Validation, Flyway MySQL, MySQL Connector/J, JJWT y dependencias de pruebas. |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` | Maven Wrapper para construir el backend sin depender de una instalación global de Maven. |
| `TallerSecurityApiApplication.java` | Punto de entrada de Spring Boot. |
| `config/AppSecurityProperties.java` | Mapea las variables de JWT, cookie, administrador inicial y origen permitido del frontend. |
| `config/SecurityConfig.java` | Registra BCrypt, seguridad sin sesión de servidor, filtro JWT, CORS de un origen configurable, rutas públicas y respuesta JSON para 401. |
| `security/AuthenticatedUser.java` | Convierte el usuario persistido en principal de Spring Security y sus autoridades. |
| `security/JwtService.java` | Genera y valida JWT firmados con secreto Base64. |
| `security/JwtAuthenticationFilter.java` | Lee la cookie, valida token, estado de usuario, versión de sesión y revocación antes de autenticar la petición. |
| `controller/AuthController.java` | Expone login, logout, perfil actual, recuperación, verificación, restablecimiento y cambio de contraseña. |
| `controller/PublicRegistrationController.java` | Expone `POST /api/public/register` sin sesión para solicitar una cuenta pendiente. |
| `controller/UserController.java` | Expone el CRUD administrativo limitado de usuarios y exige permisos por operación. |
| `controller/RoleController.java` | Lista roles/permisos y actualiza permisos de roles autorizados. |
| `controller/AuditController.java` | Lista eventos de auditoría para quien tenga `AUDIT_VIEW`. |
| `controller/RequestInfo.java` | Obtiene la IP disponible de la petición para auditoría. |
| `dto/AuthDtos.java`, `dto/UserDtos.java` | Define contratos REST y validaciones de correo, teléfono, campos obligatorios, códigos y listas de permisos. |
| `entity/` | Contiene las entidades JPA `UserAccount`, `Role`, `Permission`, `AuditLog`, `LoginAttempt`, `PasswordRecoveryCode`, `RevokedSession` y el enum `UserStatus`. |
| `repository/` | Interfaces JPA para lectura y persistencia de usuarios, roles, permisos, auditoría, intentos, códigos y sesiones revocadas. |
| `service/AuthService.java` | Reglas de login, bloqueo, logout, recuperación y cambio de contraseña. |
| `service/UserService.java` | Reglas de usuarios internos, registro público, estados, roles y protección de jerarquía. |
| `service/RoleService.java` | Consulta y modificación segura de permisos por rol. |
| `service/AuditService.java` | Crea y pagina eventos de auditoría. |
| `service/CurrentUserService.java` | Obtiene el usuario autenticado y comprueba Superadministrador. |
| `service/InitialAdminService.java` | Crea el administrador inicial desde variables de entorno si aún no existe. |
| `service/PasswordPolicy.java` | Exige mínimo ocho caracteres, mayúscula, minúscula, número, símbolo y confirmación igual. |
| `service/RateLimitService.java` | Limita operaciones sensibles por identidad en memoria del proceso. |
| `service/DtoMapper.java` | Convierte entidades a respuestas API sin exponer hash de contraseña. |
| `exception/ApiException.java`, `exception/GlobalExceptionHandler.java` | Estructura errores de negocio, validación y errores no controlados como JSON. |
| `src/main/resources/application.properties` | Configura puerto, importación opcional de entorno, datasource, JPA en modo `validate`, Flyway y propiedades de seguridad. |
| `src/main/resources/db/migration/` | Contiene las tres migraciones Flyway descritas en la sección anterior. |
| `src/test/java/com/taller/m01/TallerSecurityApiApplicationTests.java` | Prueba `contextLoads`: verifica que el contexto de Spring Boot puede iniciar con la configuración local. |

### 6.2 Frontend — `frontend/`

| Ubicación | Responsabilidad implementada |
|---|---|
| `package.json`, `package-lock.json`, `vite.config.js` | Configuración reproducible de Vue 3, Vite, Tailwind CSS, Pinia, Vue Router, Axios, iconos y herramientas de pruebas. |
| `index.html`, `src/main.js`, `src/App.vue` | Entrada HTML y arranque de Vue con Pinia y Router; `App.vue` entrega la vista a `RouterView`. |
| `src/api/client.js` | Cliente Axios con cookies (`withCredentials`) y URL configurable por `VITE_API_URL`; localmente usa `http://localhost:8080/api`. |
| `src/stores/auth.js` | Estado central de usuario, permisos y sesión; consulta `/auth/me`, inicia y cierra sesión. |
| `src/router/index.js` | Rutas públicas y privadas, redirección de acceso, comprobación de permiso y flujo de cambio de contraseña. |
| `src/components/AppShell.vue` | Estructura del panel con marca AutoManager, navegación adaptativa filtrada por permisos y cierre de sesión. |
| `src/components/UiAlert.vue` | Mensajes reutilizables de éxito y error. |
| `src/views/LoginView.vue` | Inicio de sesión, visibilidad de contraseña, enlace a recuperación y a registro público. |
| `src/views/PublicRegistrationView.vue` | Solicitud pública de usuario; no inicia sesión ni concede un rol. |
| `src/views/ForgotPasswordView.vue` | Solicitud de recuperación de contraseña. |
| `src/views/VerifyCodeView.vue` | Validación del código de recuperación y avance al cambio de contraseña. |
| `src/views/ResetPasswordView.vue` | Captura y confirma una contraseña restablecida. |
| `src/views/TemporaryPasswordView.vue` | Cambio de contraseña temporal y salida al inicio de sesión. |
| `src/views/DashboardView.vue` | Panel inicial de acceso y resumen de seguridad. |
| `src/views/RegisterUserView.vue` | Registro interno de usuarios para quien tenga `USERS_CREATE`. |
| `src/views/UsersView.vue` | Consulta, filtros, alta, edición, cambio de estado y desbloqueo de usuarios autorizados. |
| `src/views/RolesView.vue` | Consulta y actualización de la matriz rol-permiso para Superadministrador. |
| `src/views/AuditView.vue` | Consulta paginada de auditoría. |
| `src/views/DeniedView.vue`, `NotFoundView.vue` | Respuestas visuales para permiso insuficiente y ruta inexistente. |
| `src/style.css` | Estilos globales, componentes de formulario, tarjetas, navegación y tema visual de AutoManager. |
| `public/favicon.svg` | Ícono cargado por `index.html`. |
| `components/HelloWorld.vue`, `assets/hero.png`, `assets/vite.svg`, `assets/vue.svg`, `public/icons.svg` | Recursos residuales de la plantilla de Vite; no son importados por la aplicación AutoManager ni representan un módulo funcional. |

## 7. Rutas API implementadas

| Grupo | Rutas |
|---|---|
| Autenticación pública | `POST /api/auth/login`, `POST /api/auth/forgot-password`, `POST /api/auth/verify-code`, `POST /api/auth/reset-password` |
| Autenticación con sesión | `POST /api/auth/logout`, `GET /api/auth/me`, `POST /api/auth/change-temporary-password` |
| Registro público | `POST /api/public/register` |
| Usuarios | `GET/POST /api/users`, `GET/PUT /api/users/{id}`, `PATCH /api/users/{id}/status`, `PATCH /api/users/{id}/role`, `POST /api/users/{id}/unlock` |
| Roles y permisos | `GET /api/roles`, `GET /api/permissions`, `PUT /api/roles/{id}/permissions` |
| Auditoría | `GET /api/audit` |

## 8. Credenciales y protección de secretos

Las credenciales reales locales para conectar con MySQL están en:

```text
backend/.env
```

Ese archivo contiene, entre otros, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y los datos del administrador inicial. **No se debe abrir, copiar al repositorio ni publicar su contenido.** La regla `**/.env` de `.gitignore` impide incluirlo en Git.

La plantilla segura que sí se versiona es:

```text
.env.example
```

Para preparar otro equipo, se copia esa plantilla como `backend/.env`, se reemplazan los valores de ejemplo localmente y se conservan permisos restrictivos. El backend la carga porque `application.properties` importa `./.env` cuando se ejecuta desde `backend/`.

## 9. Ejecución y evidencia local

| Comprobación | Resultado |
|---|:---:|
| `npm run build` desde `frontend/` | ✅ Compila la aplicación Vite a `frontend/dist/`. |
| `./mvnw test` desde `backend/` | ✅ Ejecuta correctamente la prueba de carga del contexto Spring. |
| Flyway | ✅ Las tres migraciones de M01 son reconocidas al iniciar el backend con la base configurada. |
| Navegación local | ✅ Se verificó la ruta de registro público y la pantalla de acceso en `http://localhost:5173`. |
| Validación API de registro | ✅ Una solicitud inválida responde con `422 VALIDATION_ERROR`; no se crearon cuentas de prueba para esa validación. |

Para la demostración local:

```bash
# Terminal 1
cd backend
./mvnw spring-boot:run

# Terminal 2
cd frontend
npm run dev
```

Abrir `http://localhost:5173/login`. El botón **Registrarme** abre `http://localhost:5173/register` sin requerir una sesión de Superadministrador. La aprobación posterior sí requiere administración autorizada.

## 10. Publicación recomendada

La publicación pública no forma parte de M01 todavía. Para cuando se realice, la separación recomendada es la siguiente:

| Capa | Software recomendado | Por qué |
|---|---|---|
| Frontend Vue/Vite | **Vercel** | Publica el sitio estático construido en `dist` e integra despliegues desde GitHub. |
| Backend Spring Boot | **Railway** | Permite desplegar un proyecto Spring Boot desde GitHub o CLI y configurar secretos como variables de entorno. |
| Base de datos productiva | MySQL administrado, no el MySQL local | Un hosting no puede acceder a `localhost` de la computadora del desarrollador. Debe usarse una instancia MySQL privada y respaldada. |

### 10.1 Subir el frontend con Vercel

1. Crear una cuenta en [Vercel](https://vercel.com/) y elegir **Add New → Project**.
2. Importar el repositorio de GitHub `TallerMecanico.Software`.
3. Configurar **Root Directory** como `frontend`.
4. Elegir el preset **Vite**. Usar `npm run build` como Build Command y `dist` como Output Directory si Vercel no los detecta automáticamente.
5. Crear la variable pública `VITE_API_URL` con el valor `https://DOMINIO-DEL-BACKEND/api`. Esta URL no es un secreto; Vite la incorpora al build. No crear allí variables de contraseña ni JWT.
6. Desplegar y guardar la URL pública generada por Vercel.
7. Antes de producción, configurar una redirección SPA hacia `index.html` para que al actualizar una ruta de Vue Router, por ejemplo `/register`, no genere 404.

Vercel documenta que detecta el comando de build y el directorio de salida de los proyectos, y que la raíz se configura por proyecto en repositorios con varias carpetas. Consulte [configuración de build de Vercel](https://vercel.com/docs/builds/configure-a-build) y [Vite en Vercel](https://vercel.com/docs/frameworks/frontend/vite).

### 10.2 Subir el backend con Railway

1. Crear una cuenta en [Railway](https://railway.com/) y seleccionar **New Project → Deploy from GitHub repo**.
2. Elegir el repositorio `TallerMecanico.Software` y establecer **Root Directory** como `backend`.
3. Configurar el build con `./mvnw clean package` y el inicio con `java -jar target/taller-security-api-0.0.1-SNAPSHOT.jar` si Railway no los detecta.
4. Crear o enlazar una instancia MySQL administrada. No usar `localhost` ni las credenciales de desarrollo. Definir `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con los datos privados de esa instancia.
5. Cargar en Railway como variables privadas: `JWT_SECRET` (Base64 nuevo de al menos 32 bytes), `INITIAL_ADMIN_NAME`, `INITIAL_ADMIN_EMAIL`, `INITIAL_ADMIN_PASSWORD`, `JWT_EXPIRATION_MINUTES`, `COOKIE_SECURE=true`, `DEVELOPMENT_MODE=false` y `FRONTEND_ORIGIN` con la URL exacta de Vercel.
6. Verificar que el entorno de build proporcione **Java 26**. No disminuir la versión de Java para publicar; si la plataforma no ofrece Java 26, usar un proveedor o una imagen de ejecución compatible antes de continuar.
7. Generar el dominio público del backend, actualizar `VITE_API_URL` en Vercel con `https://DOMINIO-RAILWAY/api` y volver a desplegar el frontend.
8. Probar login, registro público, CORS, logout y acceso restringido desde la URL final. Mantener `COOKIE_SECURE=true` en producción.

Railway documenta el despliegue de Spring Boot desde GitHub y mediante CLI, y su pestaña **Variables** mantiene la configuración fuera del repositorio. Consulte [guía Spring Boot de Railway](https://docs.railway.com/guides/spring-boot), [variables de Railway](https://docs.railway.com/variables/reference) y [despliegue desde CLI](https://docs.railway.com/cli/up).

> Nota de despliegue: el archivo actual usa `SERVER_PORT` para configurar el puerto de Spring Boot. Antes de publicar, se debe confirmar el puerto de escucha requerido por Railway y configurarlo en las variables del servicio o ajustar esa propiedad para consumir el puerto que asigne el proveedor. Esta validación no se ha realizado porque no hay un despliegue productivo creado.

## 11. Próximo trabajo ordenado

1. Ampliar las pruebas automatizadas de M01 para autenticación, permisos, bloqueo, recuperación, auditoría y registro público.
2. Preparar la configuración específica de producción (dominios, CORS, cookies seguras, MySQL administrado y puerto de hosting).
3. Iniciar M02 con el modelo de datos y pantallas de clientes y vehículos, reutilizando la autorización ya creada.
4. Añadir M03 para operación de servicios, inventario, empleados, proveedores y órdenes de trabajo.
5. Construir M04 con reportes operativos y controles de auditoría complementarios.
