package com.ejemplo.springbatch.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para la ejecución de un Job de Spring Batch.
 * 
 * Este objeto contiene toda la información relevante sobre
 * la ejecución de un job, incluyendo:
 * - Estado de la ejecución
 * - Estadísticas de procesamiento
 * - Tiempos de ejecución
 * - Mensajes informativos
 * 
 * Se retorna como respuesta de los endpoints que ejecutan jobs.
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionResponse {

    /**
     * Indica si el job terminó exitosamente.
     */
    private boolean exitoso;

    /**
     * ID único de la ejecución del job.
     */
    private Long executionId;

    /**
     * Nombre del job ejecutado.
     */
    private String jobName;

    /**
     * Estado actual de la ejecución.
     * Valores: COMPLETED, FAILED, STARTED, STOPPED, etc.
     */
    private String estado;

    /**
     * Estado de salida con código de terminación.
     * Valores: COMPLETED, FAILED, UNKNOWN, etc.
     */
    private String exitStatus;

    /**
     * Fecha y hora de inicio de la ejecución.
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de fin de la ejecución.
     */
    private LocalDateTime fechaFin;

    /**
     * Número total de registros leídos.
     */
    private int registrosLeidos;

    /**
     * Número total de registros escritos exitosamente.
     */
    private int registrosEscritos;

    /**
     * Número de registros filtrados (no escritos).
     */
    private int registrosFiltrados;

    /**
     * Mensaje descriptivo de la ejecución.
     */
    private String mensaje;
}

