-- CLIENTE_CREAR is intentionally exclusive to the two operational roles in this phase.
DELETE rp
FROM rol_permisos rp
JOIN roles r ON r.id = rp.rol_id
JOIN permisos p ON p.id = rp.permiso_id
WHERE p.code = 'CLIENTE_CREAR'
  AND r.code NOT IN ('ADMIN', 'RECEPTIONIST');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
JOIN permisos p ON p.code = 'CLIENTE_CREAR'
WHERE r.code IN ('ADMIN', 'RECEPTIONIST')
  AND NOT EXISTS (
    SELECT 1
    FROM rol_permisos rp
    WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
