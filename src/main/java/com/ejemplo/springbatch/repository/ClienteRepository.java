package com.ejemplo.springbatch.repository;

import com.ejemplo.springbatch.entity.Cliente;
import com.ejemplo.springbatch.entity.EstadoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Cliente.
 * 
 * Esta interfaz proporciona métodos para acceder y manipular
 * los datos de clientes en la base de datos MySQL.
 * 
 * Spring Data JPA genera automáticamente la implementación
 * basándose en los nombres de los métodos y las anotaciones @Query.
 * 
 * Métodos disponibles:
 * - Métodos CRUD heredados de JpaRepository
 * - Consultas personalizadas por estado, email, etc.
 * - Consultas para el procesamiento batch
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // ============================================
    // MÉTODOS DE BÚSQUEDA BÁSICOS
    // ============================================

    /**
     * Busca un cliente por su email.
     * 
     * @param email Email del cliente a buscar
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca clientes por estado.
     * 
     * @param estado Estado a filtrar
     * @return Lista de clientes con el estado especificado
     */
    List<Cliente> findByEstado(EstadoCliente estado);

    /**
     * Busca clientes por estado con paginación.
     * 
     * @param estado Estado a filtrar
     * @param pageable Configuración de paginación
     * @return Página de clientes
     */
    Page<Cliente> findByEstado(EstadoCliente estado, Pageable pageable);

    /**
     * Busca clientes que contengan el texto en el nombre (ignorando mayúsculas).
     * 
     * @param nombre Texto a buscar en el nombre
     * @return Lista de clientes que coinciden
     */
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    // ============================================
    // MÉTODOS PARA PROCESAMIENTO BATCH
    // ============================================

    /**
     * Obtiene todos los clientes que no han sido procesados.
     * Utilizado por el ItemReader del batch para obtener registros pendientes.
     * 
     * @return Lista de clientes pendientes de procesar
     */
    @Query("SELECT c FROM Cliente c WHERE c.procesado = false OR c.procesado IS NULL")
    List<Cliente> findClientesSinProcesar();

    /**
     * Obtiene clientes sin procesar con paginación.
     * 
     * @param pageable Configuración de paginación
     * @return Página de clientes sin procesar
     */
    @Query("SELECT c FROM Cliente c WHERE c.procesado = false OR c.procesado IS NULL")
    Page<Cliente> findClientesSinProcesar(Pageable pageable);

    /**
     * Cuenta el número de clientes pendientes de procesar.
     * Útil para mostrar estadísticas antes de ejecutar el job.
     * 
     * @return Número de clientes sin procesar
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.procesado = false OR c.procesado IS NULL")
    Long contarClientesSinProcesar();

    /**
     * Marca un cliente como procesado.
     * Se ejecuta después de que el ItemWriter procesa exitosamente el registro.
     * 
     * @param clienteId ID del cliente a marcar
     * @return Número de registros actualizados
     */
    @Modifying
    @Query("UPDATE Cliente c SET c.procesado = true, c.fechaActualizacion = :fecha WHERE c.id = :clienteId")
    int marcarComoProcesado(@Param("clienteId") Long clienteId, @Param("fecha") LocalDateTime fecha);

    /**
     * Reinicia el estado de procesamiento de todos los clientes.
     * Útil para pruebas o reprocesamiento completo.
     * 
     * @return Número de registros actualizados
     */
    @Modifying
    @Query("UPDATE Cliente c SET c.procesado = false, c.fechaActualizacion = :fecha")
    int reiniciarEstadoProcesamiento(@Param("fecha") LocalDateTime fecha);

    // ============================================
    // CONSULTAS ESTADÍSTICAS
    // ============================================

    /**
     * Cuenta clientes por estado.
     * 
     * @param estado Estado a contar
     * @return Número de clientes en ese estado
     */
    Long countByEstado(EstadoCliente estado);

    /**
     * Verifica si existe un cliente con el email dado.
     * 
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Obtiene clientes creados en un rango de fechas.
     * 
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Lista de clientes creados en el rango
     */
    @Query("SELECT c FROM Cliente c WHERE c.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Cliente> findByRangoFechaCreacion(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}

