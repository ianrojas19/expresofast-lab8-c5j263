USE ExpresoFastC5J263_II2026;
GO

-- Contraseña plana para todos los usuarios: 'Password123!'
INSERT INTO usuario (username, password_hash, nombre_completo, email, activo)
VALUES 
('admin', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1Wn5cGBwA/7XgOymx1i86Kx5w5zK7y6', 'Carlos Alvarado', 'admin@expresofast.cr', 1),
('operador1', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1Wn5cGBwA/7XgOymx1i86Kx5w5zK7y6', 'Maria Solis', 'operador1@expresofast.cr', 1),
('conductor1', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1Wn5cGBwA/7XgOymx1i86Kx5w5zK7y6', 'Juan Perez', 'conductor1@expresofast.cr', 1);

INSERT INTO rol (nombre_rol)
VALUES ('ROLE_ADMIN'), ('ROLE_OPERADOR'), ('ROLE_CONDUCTOR');

-- Usuario 1 (admin) -> ROLE_ADMIN (1)
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (1, 1);
-- Usuario 2 (operador) -> ROLE_OPERADOR (2)
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (2, 2);
-- Usuario 3 (conductor) -> ROLE_CONDUCTOR (3)
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (3, 3);
GO
