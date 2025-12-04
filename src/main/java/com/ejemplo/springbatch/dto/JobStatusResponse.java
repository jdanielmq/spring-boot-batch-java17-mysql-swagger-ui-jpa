package com.ejemplo.springbatch.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para consultar el estado de una ejecución.
 * 
 * Similar a JobExecutionResponse pero más enfocado en consultas
 * de estado de ejecuciones existentes.
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusResponse {

    /**
     * Indica si se encontró la ejecución consultada.
     */
    private boolean encontrado;

    /**
     * ID de la ejecución.
     */
    private Long executionId;

    /**
     * Nombre del job.
     */
    private String jobName;

    /**
     * Estado actual de la ejecución.
     */
    private String estado;

    /**
     * Estado de salida del job.
     */
    private String exitStatus;

    /**
     * Fecha de inicio de la ejecución.
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha de fin de la ejecución.
     */
    private LocalDateTime fechaFin;

    /**
     * Mensaje o descripción adicional.
     */
    private String mensaje;
}

