package com.ejemplo.springbatch.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Listener que monitorea el ciclo de vida de cada Step del Job.
 * 
 * Este componente se ejecuta al inicio y fin de cada step,
 * proporcionando visibilidad detallada del proceso de cada paso.
 * 
 * Uso típico:
 * - Monitorear progreso de steps individuales
 * - Validar pre-condiciones antes del step
 * - Realizar limpieza después del step
 * - Personalizar el estado de salida
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Component
public class StepExecutionListener implements org.springframework.batch.core.StepExecutionListener {

    /**
     * Se ejecuta antes de que el step comience.
     * 
     * @param stepExecution Información de la ejecución del step
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("┌────────────────────────────────────────────────────────────┐");
        log.info("│ INICIANDO STEP: {}", stepExecution.getStepName());
        log.info("├────────────────────────────────────────────────────────────┤");
        log.info("│ Job Execution ID: {}", stepExecution.getJobExecutionId());
        log.info("│ Step Execution ID: {}", stepExecution.getId());
        log.info("│ Inicio: {}", stepExecution.getStartTime());
        log.info("└────────────────────────────────────────────────────────────┘");
    }

    /**
     * Se ejecuta después de que el step termina.
     * 
     * Puede modificar el ExitStatus para personalizar el flujo del job.
     * Por ejemplo, marcar como fallido si no se procesaron suficientes registros.
     * 
     * @param stepExecution Información de la ejecución del step completada
     * @return ExitStatus final del step (puede ser modificado)
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("┌────────────────────────────────────────────────────────────┐");
        log.info("│ FINALIZANDO STEP: {}", stepExecution.getStepName());
        log.info("├────────────────────────────────────────────────────────────┤");
        log.info("│ Estado: {}", stepExecution.getStatus());
        log.info("│ Exit Status: {}", stepExecution.getExitStatus().getExitCode());
        log.info("├────────────────────────────────────────────────────────────┤");
        log.info("│ ESTADÍSTICAS:");
        log.info("│   Registros leídos: {}", stepExecution.getReadCount());
        log.info("│   Registros escritos: {}", stepExecution.getWriteCount());
        log.info("│   Registros filtrados: {}", stepExecution.getFilterCount());
        log.info("│   Commits realizados: {}", stepExecution.getCommitCount());
        log.info("│   Rollbacks: {}", stepExecution.getRollbackCount());
        log.info("│   Saltados en lectura: {}", stepExecution.getReadSkipCount());
        log.info("│   Saltados en proceso: {}", stepExecution.getProcessSkipCount());
        log.info("│   Saltados en escritura: {}", stepExecution.getWriteSkipCount());
        
        // Calcular duración de forma segura
        long duracionMs = 0;
        if (stepExecution.getEndTime() != null && stepExecution.getStartTime() != null) {
            duracionMs = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis();
        }
        log.info("│ Duración: {} ms", duracionMs);
        log.info("└────────────────────────────────────────────────────────────┘");

        // Ejemplo: Personalizar el exit status si no se procesaron registros
        if (stepExecution.getReadCount() == 0) {
            log.warn("ADVERTENCIA: No se leyeron registros en este step");
            return new ExitStatus("SIN_DATOS", "No había registros para procesar");
        }

        return stepExecution.getExitStatus();
    }
}
