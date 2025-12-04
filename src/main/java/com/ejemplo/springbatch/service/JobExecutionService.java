package com.ejemplo.springbatch.service;

import com.ejemplo.springbatch.batch.reader.ClienteItemReader;
import com.ejemplo.springbatch.dto.JobExecutionResponse;
import com.ejemplo.springbatch.dto.JobStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la ejecución de Jobs de Spring Batch.
 * 
 * Este servicio actúa como intermediario entre los controladores REST
 * y el framework de Spring Batch, proporcionando métodos para:
 * - Lanzar jobs de forma asíncrona o síncrona
 * - Consultar el estado de ejecuciones
 * - Obtener estadísticas de procesamiento
 * - Detener jobs en ejecución
 * 
 * Responsabilidades:
 * - Configurar parámetros de ejecución
 * - Validar pre-condiciones antes de lanzar jobs
 * - Transformar resultados a DTOs
 * - Manejar errores de ejecución
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Service
public class JobExecutionService {

    /**
     * Lanzador de jobs de Spring Batch.
     * Permite iniciar la ejecución de jobs.
     */
    private final JobLauncher jobLauncher;

    /**
     * Job de procesamiento de clientes configurado.
     */
    private final Job procesarClientesJob;

    /**
     * Explorador de jobs para consultar información de ejecuciones.
     */
    private final JobExplorer jobExplorer;

    /**
     * Repositorio de metadatos de Spring Batch.
     */
    private final JobRepository jobRepository;

    /**
     * Reader de clientes para reiniciar antes de cada ejecución.
     */
    private final ClienteItemReader clienteItemReader;

    /**
     * Constructor que inyecta las dependencias necesarias.
     * 
     * @param jobLauncher Lanzador de jobs
     * @param procesarClientesJob Job configurado
     * @param jobExplorer Explorador de jobs
     * @param jobRepository Repositorio de batch
     * @param clienteItemReader Reader de clientes
     */
    public JobExecutionService(
            JobLauncher jobLauncher,
            Job procesarClientesJob,
            JobExplorer jobExplorer,
            JobRepository jobRepository,
            ClienteItemReader clienteItemReader) {
        this.jobLauncher = jobLauncher;
        this.procesarClientesJob = procesarClientesJob;
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.clienteItemReader = clienteItemReader;
    }

    /**
     * Ejecuta el job de procesamiento de clientes.
     * 
     * Este método:
     * 1. Reinicia el reader para nueva ejecución
     * 2. Crea parámetros únicos para la ejecución
     * 3. Lanza el job de forma síncrona
     * 4. Retorna información de la ejecución
     * 
     * @return JobExecutionResponse con detalles de la ejecución
     */
    public JobExecutionResponse ejecutarJobProcesamiento() {
        log.info("========================================");
        log.info("INICIANDO EJECUCIÓN DEL JOB");
        log.info("========================================");

        try {
            // Reiniciar el reader para nueva ejecución
            clienteItemReader.reset();
            
            // Crear parámetros únicos para esta ejecución
            // El timestamp asegura que cada ejecución sea única
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("tiempo", System.currentTimeMillis())
                    .addString("ejecutadoPor", "API REST")
                    .addLocalDateTime("fechaEjecucion", LocalDateTime.now())
                    .toJobParameters();

            log.info("Parámetros del job: {}", jobParameters);

            // Ejecutar el job de forma síncrona
            JobExecution jobExecution = jobLauncher.run(procesarClientesJob, jobParameters);

            // Construir respuesta
            return construirRespuestaExitosa(jobExecution);

        } catch (Exception e) {
            log.error("Error al ejecutar el job: {}", e.getMessage(), e);
            return construirRespuestaError(e);
        }
    }

    /**
     * Obtiene el estado de una ejecución específica por su ID.
     * 
     * @param executionId ID de la ejecución a consultar
     * @return JobStatusResponse con el estado actual
     */
    public JobStatusResponse obtenerEstadoEjecucion(Long executionId) {
        log.info("Consultando estado de ejecución ID: {}", executionId);

        JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
        
        if (jobExecution == null) {
            log.warn("No se encontró la ejecución ID: {}", executionId);
            return JobStatusResponse.builder()
                    .encontrado(false)
                    .mensaje("No se encontró la ejecución con ID: " + executionId)
                    .build();
        }

        return construirEstadoEjecucion(jobExecution);
    }

    /**
     * Obtiene el historial de las últimas N ejecuciones del job.
     * 
     * @param limite Número máximo de ejecuciones a retornar
     * @return Lista de JobStatusResponse con el historial
     */
    public List<JobStatusResponse> obtenerHistorialEjecuciones(int limite) {
        log.info("Obteniendo historial de últimas {} ejecuciones", limite);

        List<JobInstance> instances = jobExplorer.getJobInstances(
                "procesarClientesJob", 0, limite);

        List<JobStatusResponse> historial = new ArrayList<>();

        for (JobInstance instance : instances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : executions) {
                historial.add(construirEstadoEjecucion(execution));
            }
        }

        // Ordenar por ID descendente (más recientes primero)
        historial.sort((a, b) -> Long.compare(b.getExecutionId(), a.getExecutionId()));

        // Limitar resultados
        return historial.stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas generales de todas las ejecuciones.
     * 
     * @return Mapa con estadísticas resumidas
     */
    public Map<String, Object> obtenerEstadisticas() {
        log.info("Calculando estadísticas de ejecuciones");

        Map<String, Object> estadisticas = new HashMap<>();

        // Obtener todas las instancias del job
        long totalInstances = 0;
        try {
            totalInstances = jobExplorer.getJobInstanceCount("procesarClientesJob");
        } catch (Exception e) {
            log.debug("No hay instancias previas del job: {}", e.getMessage());
        }
        estadisticas.put("totalInstancias", totalInstances);

        // Obtener ejecuciones recientes para estadísticas
        List<JobInstance> instances = jobExplorer.getJobInstances(
                "procesarClientesJob", 0, 100);

        int completadas = 0;
        int fallidas = 0;
        int enEjecucion = 0;
        long totalRegistrosProcesados = 0;

        for (JobInstance instance : instances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : executions) {
                BatchStatus status = execution.getStatus();
                if (status == BatchStatus.COMPLETED) {
                    completadas++;
                } else if (status == BatchStatus.FAILED) {
                    fallidas++;
                } else if (status == BatchStatus.STARTED || status == BatchStatus.STARTING) {
                    enEjecucion++;
                }

                // Sumar registros procesados
                for (StepExecution step : execution.getStepExecutions()) {
                    totalRegistrosProcesados += step.getWriteCount();
                }
            }
        }

        estadisticas.put("ejecucionesCompletadas", completadas);
        estadisticas.put("ejecucionesFallidas", fallidas);
        estadisticas.put("ejecucionesEnCurso", enEjecucion);
        estadisticas.put("totalRegistrosProcesados", totalRegistrosProcesados);
        estadisticas.put("fechaConsulta", LocalDateTime.now());

        return estadisticas;
    }

    // ============================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ============================================

    /**
     * Construye la respuesta cuando el job se ejecuta exitosamente.
     * 
     * @param jobExecution Ejecución completada
     * @return JobExecutionResponse con los detalles
     */
    private JobExecutionResponse construirRespuestaExitosa(JobExecution jobExecution) {
        // Obtener estadísticas del step
        int registrosLeidos = 0;
        int registrosEscritos = 0;
        int registrosFiltrados = 0;

        for (StepExecution step : jobExecution.getStepExecutions()) {
            registrosLeidos += step.getReadCount();
            registrosEscritos += step.getWriteCount();
            registrosFiltrados += step.getFilterCount();
        }

        return JobExecutionResponse.builder()
                .exitoso(jobExecution.getStatus() == BatchStatus.COMPLETED)
                .executionId(jobExecution.getId())
                .jobName(jobExecution.getJobInstance().getJobName())
                .estado(jobExecution.getStatus().toString())
                .exitStatus(jobExecution.getExitStatus().getExitCode())
                .fechaInicio(jobExecution.getStartTime())
                .fechaFin(jobExecution.getEndTime())
                .registrosLeidos(registrosLeidos)
                .registrosEscritos(registrosEscritos)
                .registrosFiltrados(registrosFiltrados)
                .mensaje("Job ejecutado correctamente")
                .build();
    }

    /**
     * Construye la respuesta cuando ocurre un error.
     * 
     * @param excepcion Excepción que causó el error
     * @return JobExecutionResponse con información del error
     */
    private JobExecutionResponse construirRespuestaError(Exception excepcion) {
        return JobExecutionResponse.builder()
                .exitoso(false)
                .estado("ERROR")
                .mensaje("Error al ejecutar el job: " + excepcion.getMessage())
                .fechaInicio(LocalDateTime.now())
                .build();
    }

    /**
     * Construye el estado detallado de una ejecución.
     * 
     * @param jobExecution Ejecución a consultar
     * @return JobStatusResponse con el estado actual
     */
    private JobStatusResponse construirEstadoEjecucion(JobExecution jobExecution) {
        return JobStatusResponse.builder()
                .encontrado(true)
                .executionId(jobExecution.getId())
                .jobName(jobExecution.getJobInstance().getJobName())
                .estado(jobExecution.getStatus().toString())
                .exitStatus(jobExecution.getExitStatus().getExitCode())
                .fechaInicio(jobExecution.getStartTime())
                .fechaFin(jobExecution.getEndTime())
                .mensaje(jobExecution.getExitStatus().getExitDescription())
                .build();
    }

}
