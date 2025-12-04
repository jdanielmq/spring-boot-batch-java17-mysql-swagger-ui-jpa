package com.ejemplo.springbatch.repository;

import com.ejemplo.springbatch.entity.ClienteProcesado;
import com.ejemplo.springbatch.entity.EstadoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad ClienteProcesado.
 * 
 * Esta interfaz maneja el acceso a datos de los registros
 * que han sido procesados por los jobs de Spring Batch.
 * 
 * Proporciona consultas para:
 * - Auditoría de procesamiento
 * - Reportes de ejecución
 * - Consultas por job execution
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Repository
public interface ClienteProcesadoRepository extends JpaRepository<ClienteProcesado, Long> {

    // ============================================
    // MÉTODOS DE BÚSQUEDA BÁSICOS
    // ============================================

    /**
     * Busca un registro procesado por el ID del cliente original.
     * 
     * @param clienteId ID del cliente original
     * @return Optional con el registro si existe
     */
    Optional<ClienteProcesado> findByClienteId(Long clienteId);

    /**
     * Busca un registro por el código de cliente generado.
     * 
     * @param codigoCliente Código único del cliente
     * @return Optional con el registro si existe
     */
    Optional<ClienteProcesado> findByCodigoCliente(String codigoCliente);

    /**
     * Obtiene todos los registros procesados por un job específico.
     * Útil para auditar los resultados de una ejecución particular.
     * 
     * @param jobExecutionId ID de la ejecución del job
     * @return Lista de registros procesados en esa ejecución
     */
    List<ClienteProcesado> findByJobExecutionId(Long jobExecutionId);

    /**
     * Obtiene registros por estado final con paginación.
     * 
     * @param estadoFinal Estado final del procesamiento
     * @param pageable Configuración de paginación
     * @return Página de registros procesados
     */
    Page<ClienteProcesado> findByEstadoFinal(EstadoCliente estadoFinal, Pageable pageable);

    // ============================================
    // CONSULTAS DE AUDITORÍA
    // ============================================

    /**
     * Cuenta registros procesados por una ejecución específica.
     * 
     * @param jobExecutionId ID de la ejecución del job
     * @return Número de registros procesados
     */
    Long countByJobExecutionId(Long jobExecutionId);

    /**
     * Cuenta registros por estado final.
     * 
     * @param estadoFinal Estado a contar
     * @return Número de registros en ese estado
     */
    Long countByEstadoFinal(EstadoCliente estadoFinal);

    /**
     * Obtiene registros procesados en un rango de fechas.
     * 
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de registros procesados en el rango
     */
    @Query("SELECT cp FROM ClienteProcesado cp WHERE cp.fechaProcesamiento BETWEEN :fechaInicio AND :fechaFin")
    List<ClienteProcesado> findByRangoFechaProcesamiento(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Verifica si un cliente ya fue procesado.
     * 
     * @param clienteId ID del cliente original
     * @return true si ya existe un registro procesado
     */
    boolean existsByClienteId(Long clienteId);

    // ============================================
    // ESTADÍSTICAS DE PROCESAMIENTO
    // ============================================

    /**
     * Obtiene el resumen de procesamiento por estado para un job.
     * 
     * @param jobExecutionId ID de la ejecución del job
     * @return Lista de arrays con [estado, cantidad]
     */
    @Query("SELECT cp.estadoFinal, COUNT(cp) FROM ClienteProcesado cp " +
           "WHERE cp.jobExecutionId = :jobExecutionId GROUP BY cp.estadoFinal")
    List<Object[]> obtenerResumenPorEstado(@Param("jobExecutionId") Long jobExecutionId);

    /**
     * Obtiene las últimas N ejecuciones de jobs con registros procesados.
     * 
     * @param pageable Configuración de paginación para limitar resultados
     * @return Lista de IDs de ejecuciones únicas
     */
    @Query("SELECT DISTINCT cp.jobExecutionId FROM ClienteProcesado cp " +
           "ORDER BY cp.jobExecutionId DESC")
    List<Long> findUltimasEjecuciones(Pageable pageable);
}

