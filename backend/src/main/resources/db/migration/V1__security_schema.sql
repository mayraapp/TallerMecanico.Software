CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL UNIQUE,
  description VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE permisos (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(80) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuarios (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre_completo VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  phone VARCHAR(30) NULL,
  password_hash VARCHAR(100) NOT NULL,
  status VARCHAR(12) NOT NULL,
  intentos_fallidos INT NOT NULL DEFAULT 0,
  bloqueado_hasta DATETIME(6) NULL,
  ultimo_acceso DATETIME(6) NULL,
  debe_cambiar_password BOOLEAN NOT NULL DEFAULT TRUE,
  version_sesion INT NOT NULL DEFAULT 1,
  es_admin_inicial BOOLEAN NOT NULL DEFAULT FALSE,
  creado_en DATETIME(6) NOT NULL,
  actualizado_en DATETIME(6) NOT NULL,
  CONSTRAINT chk_usuario_estado CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_usuarios_nombre ON usuarios(nombre_completo);
CREATE INDEX idx_usuarios_estado ON usuarios(status);

CREATE TABLE usuario_roles (
  usuario_id BIGINT NOT NULL,
  rol_id BIGINT NOT NULL,
  PRIMARY KEY (usuario_id, rol_id),
  CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE rol_permisos (
  rol_id BIGINT NOT NULL,
  permiso_id BIGINT NOT NULL,
  PRIMARY KEY (rol_id, permiso_id),
  CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (rol_id) REFERENCES roles(id),
  CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (permiso_id) REFERENCES permisos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE codigos_recuperacion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  codigo_hash CHAR(64) NOT NULL,
  expira_en DATETIME(6) NOT NULL,
  usado_en DATETIME(6) NULL,
  attempts INT NOT NULL DEFAULT 0,
  creado_en DATETIME(6) NOT NULL,
  CONSTRAINT fk_recuperacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_recuperacion_usuario_estado ON codigos_recuperacion(usuario_id, usado_en, expira_en);

CREATE TABLE intentos_acceso (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id BIGINT NULL,
  email VARCHAR(160) NOT NULL,
  success BOOLEAN NOT NULL,
  direccion_ip VARCHAR(64) NULL,
  creado_en DATETIME(6) NOT NULL,
  CONSTRAINT fk_intento_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_intentos_email_fecha ON intentos_acceso(email, creado_en);

CREATE TABLE auditoria (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  responsable_id BIGINT NULL,
  afectado_id BIGINT NULL,
  action VARCHAR(70) NOT NULL,
  direccion_ip VARCHAR(64) NULL,
  details VARCHAR(500) NOT NULL,
  creado_en DATETIME(6) NOT NULL,
  CONSTRAINT fk_auditoria_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios(id),
  CONSTRAINT fk_auditoria_afectado FOREIGN KEY (afectado_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_auditoria_fecha ON auditoria(creado_en);
CREATE INDEX idx_auditoria_accion ON auditoria(action);

CREATE TABLE sesiones_revocadas (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  jti VARCHAR(64) NOT NULL UNIQUE,
  expira_en DATETIME(6) NOT NULL,
  creado_en DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_sesion_expira ON sesiones_revocadas(expira_en);
