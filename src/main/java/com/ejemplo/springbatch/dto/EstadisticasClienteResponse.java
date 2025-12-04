package com.ejemplo.springbatch.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para estadísticas de clientes.
 * 
 * Contiene métricas resumidas sobre el estado actual
 * de los clientes en el sistema.
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasClienteResponse {

    /**
     * Total de clientes en el sistema.
     */
    private long totalClientes;

    /**
     * Clientes pendientes de procesar.
     */
    private long clientesPendientes;

    /**
     * Clientes con estado ACTIVO.
     */
    private long clientesActivos;

    /**
     * Clientes con estado INACTIVO.
     */
    private long clientesInactivos;

    /**
     * Total de registros en la tabla de procesados.
     */
    private long registrosProcesados;

    /**
     * Fecha y hora de la consulta.
     */
    private LocalDateTime fechaConsulta;
}

