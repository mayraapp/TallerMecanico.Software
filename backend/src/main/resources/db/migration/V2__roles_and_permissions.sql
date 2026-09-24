INSERT INTO roles (code, name, description) VALUES
 ('SUPERADMIN', 'Superadministrador', 'Control total de seguridad y usuarios'),
 ('ADMIN', 'Administrador', 'Acceso administrativo operativo'),
 ('RECEPTIONIST', 'Recepcionista', 'Recepción y órdenes de trabajo'),
 ('MECHANIC', 'Mecánico', 'Ejecución y edición de órdenes de trabajo');

INSERT INTO permisos (code, name, description) VALUES
 ('USERS_VIEW', 'Ver usuarios', 'Consultar usuarios'),
 ('USERS_CREATE', 'Crear usuarios', 'Registrar cuentas'),
 ('USERS_EDIT', 'Editar usuarios', 'Editar datos de cuentas'),
 ('USERS_ACTIVATE', 'Activar usuarios', 'Activar cuentas'),
 ('USERS_DEACTIVATE', 'Desactivar usuarios', 'Desactivar cuentas'),
 ('USERS_UNLOCK', 'Desbloquear usuarios', 'Desbloquear cuentas'),
 ('ROLES_ASSIGN', 'Asignar roles', 'Cambiar roles de cuentas'),
 ('PERMISSIONS_MANAGE', 'Administrar permisos', 'Modificar permisos de roles'),
 ('AUDIT_VIEW', 'Ver auditoría', 'Consultar eventos de seguridad'),
 ('ADMIN_PANEL_VIEW', 'Ver panel administrativo', 'Acceder al panel'),
 ('WORK_ORDERS_VIEW', 'Ver órdenes de trabajo', 'Consultar órdenes'),
 ('WORK_ORDERS_CREATE', 'Crear órdenes de trabajo', 'Registrar órdenes'),
 ('WORK_ORDERS_EDIT', 'Editar órdenes de trabajo', 'Modificar órdenes');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p WHERE r.code = 'SUPERADMIN';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.code IN ('ADMIN_PANEL_VIEW', 'WORK_ORDERS_VIEW', 'WORK_ORDERS_CREATE', 'WORK_ORDERS_EDIT') WHERE r.code = 'ADMIN';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.code IN ('WORK_ORDERS_VIEW', 'WORK_ORDERS_CREATE') WHERE r.code = 'RECEPTIONIST';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.code IN ('WORK_ORDERS_VIEW', 'WORK_ORDERS_EDIT') WHERE r.code = 'MECHANIC';
