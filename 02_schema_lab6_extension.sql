USE ExpresoFastC5J263_II2026;
GO

CREATE TABLE usuario (
    usuario_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    activo BIT NOT NULL
);

CREATE TABLE rol (
    rol_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(30) NOT NULL UNIQUE CHECK (nombre_rol IN ('ROLE_ADMIN', 'ROLE_OPERADOR', 'ROLE_CONDUCTOR'))
);

CREATE TABLE usuario_rol (
    usuario_id INT,
    rol_id INT,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id),
    FOREIGN KEY (rol_id) REFERENCES rol(rol_id)
);

CREATE TABLE bitacora_envio (
    bitacora_id INT IDENTITY(1,1) PRIMARY KEY,
    envio_id INT NOT NULL,
    estado_anterior VARCHAR(20) NOT NULL,
    estado_nuevo VARCHAR(20) NOT NULL,
    fecha_cambio DATETIME NOT NULL,
    usuario_id INT NOT NULL,
    observaciones VARCHAR(250),
    FOREIGN KEY (envio_id) REFERENCES envio(envio_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
);
GO
