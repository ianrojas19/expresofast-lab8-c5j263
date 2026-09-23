USE ExpresoFastC5J263_II2026;
GO

INSERT INTO EmpresaLogistica (nombre, cedula_juridica, telefono, fecha_registro) VALUES 
('Logistica Express', '3-101-123456', '2555-1000', GETDATE());

INSERT INTO Vehiculo (placa, capacidad_kg, estado, empresa_id) VALUES 
('ABC-123', 5000.00, 'DISPONIBLE', 1),
('XYZ-987', 3000.00, 'DISPONIBLE', 1);

INSERT INTO Conductor (nombre, apellidos, licencia, telefono) VALUES 
('Juan', 'Perez', 'B3-111', '8888-1111'),
('Maria', 'Gomez', 'B3-222', '8888-2222');
GO
