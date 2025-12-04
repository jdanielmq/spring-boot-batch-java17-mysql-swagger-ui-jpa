package com.ejemplo.springbatch.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Listener que monitorea el ciclo de vida del Job de Spring Batch.
 * 
 * Este componente se ejecuta al inicio y fin de cada ejecución del job,
 * permitiendo realizar acciones como:
 * - Registrar el inicio del procesamiento
 * - Calcular tiempo de ejecución
 * - Generar reportes de resultados
 * - Notificar sobre el estado final
 * 
 * Métodos del ciclo de vida:
 * - beforeJob(): Se ejecuta antes de iniciar el job
 * - afterJob(): Se ejecuta después de completar el job (éxito o error)
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Component
public class JobCompletionListener implements JobExecutionListener {

    /**
     * Marca de tiempo del inicio del job para calcular duración.
     */
    private LocalDateTime tiempoInicio;

    /**
     * Se ejecuta antes de que el job comience.
     * 
     * Uso típico:
     * - Inicializar recursos
     * - Registrar inicio del procesamiento
     * - Configurar contexto del job
     * 
     * @param jobExecution Objeto que contiene información de la ejecución
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        this.tiempoInicio = LocalDateTime.now();
        
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║             INICIO DE EJECUCIÓN DEL JOB                    ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║ Job Name: {}", jobExecution.getJobInstance().getJobName());
        log.info("║ Job ID: {}", jobExecution.getJobId());
        log.info("║ Execution ID: {}", jobExecution.getId());
        log.info("║ Inicio: {}", tiempoInicio);
        log.info("║ Parámetros: {}", jobExecution.getJobParameters());
        log.info("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Se ejecuta después de que el job termina (éxito o error).
     * 
     * Uso típico:
     * - Calcular estadísticas de ejecución
     * - Generar reportes
     * - Enviar notificaciones
     * - Limpiar recursos
     * 
     * @param jobExecution Objeto con información de la ejecución completada
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime tiempoFin = LocalDateTime.now();
        Duration duracion = Duration.between(tiempoInicio, tiempoFin);
        
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║               FIN DE EJECUCIÓN DEL JOB                     ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║ Job Name: {}", jobExecution.getJobInstance().getJobName());
        log.info("║ Execution ID: {}", jobExecution.getId());
        log.info("║ Estado Final: {}", jobExecution.getStatus());
        log.info("║ Inicio: {}", tiempoInicio);
        log.info("║ Fin: {}", tiempoFin);
        log.info("║ Duración: {} segundos", duracion.getSeconds());
        log.info("╠════════════════════════════════════════════════════════════╣");

        // Mostrar información detallada según el estado
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            mostrarResumenExitoso(jobExecution);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            mostrarResumenFallido(jobExecution);
        }

        log.info("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Muestra el resumen cuando el job completa exitosamente.
     * 
     * @param jobExecution Ejecución del job
     */
    private void mostrarResumenExitoso(JobExecution jobExecution) {
        log.info("║ ✓ JOB COMPLETADO EXITOSAMENTE");
        log.info("╠════════════════════════════════════════════════════════════╣");
        
        // Mostrar estadísticas de cada step
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info("║ Step: {}", stepExecution.getStepName());
            log.info("║   - Leídos: {}", stepExecution.getReadCount());
            log.info("║   - Procesados: {}", stepExecution.getWriteCount());
            log.info("║   - Filtrados: {}", stepExecution.getFilterCount());
            log.info("║   - Saltados (lectura): {}", stepExecution.getReadSkipCount());
            log.info("║   - Saltados (escritura): {}", stepExecution.getWriteSkipCount());
            log.info("║   - Commits: {}", stepExecution.getCommitCount());
            log.info("║   - Rollbacks: {}", stepExecution.getRollbackCount());
        });
    }

    /**
     * Muestra el resumen cuando el job falla.
     * 
     * @param jobExecution Ejecución del job fallida
     */
    private void mostrarResumenFallido(JobExecution jobExecution) {
        log.error("║ ✗ JOB FALLÓ");
        log.error("╠════════════════════════════════════════════════════════════╣");
        log.error("║ Exit Status: {}", jobExecution.getExitStatus().getExitCode());
        log.error("║ Exit Description: {}", jobExecution.getExitStatus().getExitDescription());
        
        // Mostrar excepciones si las hay
        jobExecution.getAllFailureExceptions().forEach(exception -> {
            log.error("║ Excepción: {}", exception.getMessage());
        });
    }
}

