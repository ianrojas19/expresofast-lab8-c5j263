USE master;
GO
ALTER DATABASE ExpresoFastC5J263_II2026 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE ExpresoFastC5J263_II2026;
GO

CREATE DATABASE ExpresoFastC5J263_II2026;
GO

USE ExpresoFastC5J263_II2026;
GO

CREATE TABLE empresa_logistica (
    empresa_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    cedula_juridica VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    fecha_registro DATETIME NOT NULL
);

CREATE TABLE vehiculo (
    vehiculo_id INT IDENTITY(1,1) PRIMARY KEY,
    placa VARCHAR(15) NOT NULL UNIQUE,
    capacidad_kg DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('DISPONIBLE', 'EN_RUTA', 'MANTENIMIENTO')),
    empresa_id INT,
    FOREIGN KEY (empresa_id) REFERENCES empresa_logistica(empresa_id)
);

CREATE TABLE conductor (
    conductor_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    licencia VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL
);

CREATE TABLE envio (
    envio_id INT IDENTITY(1,1) PRIMARY KEY,
    codigo_rastreo VARCHAR(30) NOT NULL UNIQUE,
    direccion_destino VARCHAR(200) NOT NULL,
    peso_kg DECIMAL(10,2) NOT NULL,
    costo DECIMAL(10,2) NOT NULL,
    estado_envio VARCHAR(20) NOT NULL CHECK (estado_envio IN ('PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO')),
    vehiculo_id INT,
    conductor_id INT,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(vehiculo_id),
    FOREIGN KEY (conductor_id) REFERENCES conductor(conductor_id)
);
GO

-- Create user
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'expreso_user')
BEGIN
    CREATE LOGIN expreso_user WITH PASSWORD = 'Password123!';
END
GO
CREATE USER expreso_user FOR LOGIN expreso_user;
ALTER ROLE db_owner ADD MEMBER expreso_user;
GO

-- Insert test data
INSERT INTO empresa_logistica (nombre, cedula_juridica, telefono, fecha_registro) VALUES 
('Logistica Express', '3-101-123456', '2555-1000', GETDATE());

INSERT INTO vehiculo (placa, capacidad_kg, estado, empresa_id) VALUES 
('ABC-123', 5000.00, 'DISPONIBLE', 1),
('XYZ-987', 3000.00, 'DISPONIBLE', 1);

INSERT INTO conductor (nombre, apellidos, licencia, telefono) VALUES 
('Juan', 'Perez', 'B3-111', '8888-1111'),
('Maria', 'Gomez', 'B3-222', '8888-2222');
GO
