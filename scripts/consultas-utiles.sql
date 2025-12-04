-- ============================================================
-- CONSULTAS ÚTILES PARA MONITOREO Y DEBUGGING
-- ============================================================
-- Este archivo contiene consultas SQL útiles para monitorear
-- el estado del procesamiento batch y diagnosticar problemas.
-- ============================================================

USE spring_batch_db;

-- ============================================
-- 1. CONSULTAS DE CLIENTES
-- ============================================

-- Ver todos los clientes con su estado de procesamiento
SELECT 
    id,
    nombre,
    email,
    estado,
    procesado,
    fecha_creacion,
    fecha_actualizacion
FROM clientes
ORDER BY id;

-- Ver clientes pendientes de procesar
SELECT 
    id,
    nombre,
    email,
    estado
FROM clientes
WHERE procesado = FALSE OR procesado IS NULL;

-- Contar clientes por estado
SELECT 
    estado,
    COUNT(*) AS cantidad
FROM clientes
GROUP BY estado;

-- ============================================
-- 2. CONSULTAS DE CLIENTES PROCESADOS
-- ============================================

-- Ver todos los clientes procesados
SELECT 
    id,
    cliente_id,
    nombre_procesado,
    codigo_cliente,
    estado_final,
    job_execution_id,
    fecha_procesamiento,
    mensaje
FROM clientes_procesados
ORDER BY fecha_procesamiento DESC;

-- Ver procesamiento por job execution
SELECT 
    job_execution_id,
    COUNT(*) AS registros_procesados,
    MIN(fecha_procesamiento) AS inicio,
    MAX(fecha_procesamiento) AS fin
FROM clientes_procesados
GROUP BY job_execution_id
ORDER BY job_execution_id DESC;

-- ============================================
-- 3. CONSULTAS DE SPRING BATCH (METADATOS)
-- ============================================

-- Ver últimas ejecuciones de jobs
SELECT 
    je.JOB_EXECUTION_ID,
    ji.JOB_NAME,
    je.STATUS,
    je.EXIT_CODE,
    je.START_TIME,
    je.END_TIME,
    TIMESTAMPDIFF(SECOND, je.START_TIME, je.END_TIME) AS duracion_segundos
FROM BATCH_JOB_EXECUTION je
JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID
ORDER BY je.JOB_EXECUTION_ID DESC
LIMIT 10;

-- Ver estadísticas de steps
SELECT 
    se.STEP_EXECUTION_ID,
    se.STEP_NAME,
    se.STATUS,
    se.READ_COUNT,
    se.WRITE_COUNT,
    se.FILTER_COUNT,
    se.COMMIT_COUNT,
    se.ROLLBACK_COUNT,
    se.READ_SKIP_COUNT,
    se.WRITE_SKIP_COUNT
FROM BATCH_STEP_EXECUTION se
ORDER BY se.STEP_EXECUTION_ID DESC
LIMIT 10;

-- ============================================
-- 4. CONSULTAS DE DIAGNÓSTICO
-- ============================================

-- Verificar integridad: clientes procesados vs registros en tabla procesados
SELECT 
    'Clientes marcados como procesados' AS descripcion,
    COUNT(*) AS cantidad
FROM clientes WHERE procesado = TRUE
UNION ALL
SELECT 
    'Registros en clientes_procesados' AS descripcion,
    COUNT(*) AS cantidad
FROM clientes_procesados;

-- Encontrar clientes procesados sin registro en tabla procesados
SELECT c.*
FROM clientes c
LEFT JOIN clientes_procesados cp ON c.id = cp.cliente_id
WHERE c.procesado = TRUE AND cp.id IS NULL;

