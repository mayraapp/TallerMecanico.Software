INSERT INTO roles (code, name, description)
SELECT 'PENDING', 'Pendiente de aprobación', 'Solicitud pública sin permisos hasta autorización administrativa'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'PENDING');
