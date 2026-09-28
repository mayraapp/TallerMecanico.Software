INSERT INTO permisos (code, name, description)
SELECT 'CLIENTE_CREAR', 'Registrar clientes', 'Crear registros de clientes'
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE code = 'CLIENTE_CREAR');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
JOIN permisos p ON p.code = 'CLIENTE_CREAR'
WHERE r.code IN ('SUPERADMIN', 'ADMIN', 'RECEPTIONIST')
  AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

CREATE TABLE clientes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre_completo VARCHAR(160) NOT NULL,
  nombre_normalizado VARCHAR(160) NOT NULL,
  contacto_alternativo VARCHAR(160) NULL,
  fecha_nacimiento DATE NOT NULL,
  telefono_personal_normalizado VARCHAR(15) NOT NULL,
  telefono_trabajo_normalizado VARCHAR(15) NULL,
  correo_personal_normalizado VARCHAR(160) NOT NULL,
  correo_trabajo_normalizado VARCHAR(160) NULL,
  fotografia_referencia VARCHAR(255) NULL,
  estado_registro VARCHAR(16) NOT NULL DEFAULT 'ACTIVO',
  creado_en DATETIME(6) NOT NULL,
  actualizado_en DATETIME(6) NOT NULL,
  creado_por_id BIGINT NOT NULL,
  actualizado_por_id BIGINT NOT NULL,
  CONSTRAINT uk_clientes_correo_personal UNIQUE (correo_personal_normalizado),
  CONSTRAINT uk_clientes_telefono_personal UNIQUE (telefono_personal_normalizado),
  CONSTRAINT uk_clientes_nombre_fecha UNIQUE (nombre_normalizado, fecha_nacimiento),
  CONSTRAINT fk_clientes_creado_por FOREIGN KEY (creado_por_id) REFERENCES usuarios(id),
  CONSTRAINT fk_clientes_actualizado_por FOREIGN KEY (actualizado_por_id) REFERENCES usuarios(id),
  INDEX idx_clientes_nombre_normalizado (nombre_normalizado),
  INDEX idx_clientes_estado (estado_registro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE direcciones_cliente (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cliente_id BIGINT NOT NULL,
  calle_numero VARCHAR(180) NOT NULL,
  colonia VARCHAR(120) NOT NULL,
  municipio VARCHAR(120) NOT NULL,
  estado VARCHAR(120) NOT NULL,
  codigo_postal VARCHAR(5) NOT NULL,
  estado_registro VARCHAR(16) NOT NULL DEFAULT 'ACTIVO',
  creado_en DATETIME(6) NOT NULL,
  actualizado_en DATETIME(6) NOT NULL,
  creado_por_id BIGINT NOT NULL,
  actualizado_por_id BIGINT NOT NULL,
  CONSTRAINT uk_direccion_cliente UNIQUE (cliente_id),
  CONSTRAINT fk_direccion_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
  CONSTRAINT fk_direccion_creado_por FOREIGN KEY (creado_por_id) REFERENCES usuarios(id),
  CONSTRAINT fk_direccion_actualizado_por FOREIGN KEY (actualizado_por_id) REFERENCES usuarios(id),
  INDEX idx_direcciones_codigo_postal (codigo_postal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
