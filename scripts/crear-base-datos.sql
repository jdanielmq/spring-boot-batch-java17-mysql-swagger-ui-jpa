-- ============================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS
-- ============================================================
-- Este script crea la base de datos y las tablas necesarias
-- para el proyecto Spring Batch Boot.
--
-- Ejecutar como usuario root o con privilegios de administrador.
-- ============================================================

-- 1. Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS spring_batch_db 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 2. Usar la base de datos
USE spring_batch_db;

-- 3. Crear tabla de clientes
-- Esta es la tabla de entrada para el procesamiento batch
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador único del cliente',
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre completo del cliente',
    email VARCHAR(150) NOT NULL UNIQUE COMMENT 'Correo electrónico único',
    telefono VARCHAR(20) COMMENT 'Número de teléfono',
    estado ENUM('PENDIENTE', 'ACTIVO', 'INACTIVO', 'ERROR') NOT NULL DEFAULT 'PENDIENTE' 
        COMMENT 'Estado actual del cliente',
    procesado BOOLEAN DEFAULT FALSE COMMENT 'Indica si el cliente ha sido procesado por batch',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del registro',
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP 
        COMMENT 'Fecha de última actualización',
    
    INDEX idx_estado (estado),
    INDEX idx_procesado (procesado),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de clientes pendientes de procesar';

-- 4. Crear tabla de clientes procesados
-- Esta tabla almacena los resultados del procesamiento batch
CREATE TABLE IF NOT EXISTS clientes_procesados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador del registro procesado',
    cliente_id BIGINT NOT NULL COMMENT 'Referencia al cliente original',
    nombre_procesado VARCHAR(100) NOT NULL COMMENT 'Nombre en mayúsculas',
    email_procesado VARCHAR(150) NOT NULL COMMENT 'Email normalizado',
    codigo_cliente VARCHAR(20) UNIQUE COMMENT 'Código único generado CLI-XXXXXXXX',
    estado_final ENUM('PENDIENTE', 'ACTIVO', 'INACTIVO', 'ERROR') NOT NULL 
        COMMENT 'Estado final después del procesamiento',
    job_execution_id BIGINT COMMENT 'ID de la ejecución del job que procesó',
    fecha_procesamiento DATETIME NOT NULL COMMENT 'Fecha y hora del procesamiento',
    mensaje VARCHAR(500) COMMENT 'Mensaje o notas del procesamiento',
    
    INDEX idx_cliente_id (cliente_id),
    INDEX idx_job_execution (job_execution_id),
    INDEX idx_estado_final (estado_final),
    INDEX idx_codigo_cliente (codigo_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de clientes ya procesados por el batch';

-- 5. Insertar datos de prueba
INSERT INTO clientes (nombre, email, telefono, estado, procesado) VALUES
    ('Juan Pérez García', 'juan.perez@ejemplo.com', '555-0001', 'PENDIENTE', FALSE),
    ('María López Rodríguez', 'maria.lopez@ejemplo.com', '555-0002', 'PENDIENTE', FALSE),
    ('Carlos Sánchez Martínez', 'carlos.sanchez@ejemplo.com', '555-0003', 'PENDIENTE', FALSE),
    ('Ana González Hernández', 'ana.gonzalez@ejemplo.com', '555-0004', 'PENDIENTE', FALSE),
    ('Pedro Ramírez Díaz', 'pedro.ramirez@ejemplo.com', NULL, 'PENDIENTE', FALSE),
    ('Laura Fernández Torres', 'laura.fernandez@ejemplo.com', '555-0006', 'PENDIENTE', FALSE),
    ('Miguel Ruiz Castro', 'miguel.ruiz@ejemplo.com', '555-0007', 'INACTIVO', FALSE),
    ('Carmen Moreno Jiménez', 'carmen.moreno@ejemplo.com', '555-0008', 'PENDIENTE', FALSE),
    ('Francisco Álvarez Romero', 'francisco.alvarez@ejemplo.com', NULL, 'PENDIENTE', FALSE),
    ('Isabel Muñoz Navarro', 'isabel.munoz@ejemplo.com', '555-0010', 'PENDIENTE', FALSE);

-- 6. Mostrar resumen
SELECT 'Tablas creadas exitosamente' AS Mensaje;
SELECT COUNT(*) AS 'Clientes insertados' FROM clientes;

