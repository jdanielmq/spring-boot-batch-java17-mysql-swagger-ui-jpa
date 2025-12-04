-- ============================================================
-- SCRIPT PARA LIMPIAR DATOS Y REINICIAR TABLAS
-- ============================================================
-- Este script limpia los datos de las tablas del proyecto
-- sin eliminar las tablas de metadatos de Spring Batch.
--
-- Útil para reiniciar el ambiente de pruebas.
-- ============================================================

USE spring_batch_db;

-- 1. Limpiar tabla de clientes procesados
DELETE FROM clientes_procesados;
ALTER TABLE clientes_procesados AUTO_INCREMENT = 1;

-- 2. Reiniciar flag de procesamiento en clientes
UPDATE clientes SET procesado = FALSE, fecha_actualizacion = NOW();

-- 3. Opcional: Limpiar tablas de metadatos de Spring Batch
-- Descomentar las siguientes líneas si desea reiniciar completamente
-- el historial de ejecuciones de Spring Batch

-- DELETE FROM BATCH_STEP_EXECUTION_CONTEXT;
-- DELETE FROM BATCH_JOB_EXECUTION_CONTEXT;
-- DELETE FROM BATCH_STEP_EXECUTION;
-- DELETE FROM BATCH_JOB_EXECUTION_PARAMS;
-- DELETE FROM BATCH_JOB_EXECUTION;
-- DELETE FROM BATCH_JOB_INSTANCE;

SELECT 'Datos limpiados correctamente' AS Mensaje;
SELECT COUNT(*) AS 'Clientes reiniciados' FROM clientes WHERE procesado = FALSE;

