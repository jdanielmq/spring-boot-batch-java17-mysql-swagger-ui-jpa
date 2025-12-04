package com.ejemplo.springbatch.controller;

import com.ejemplo.springbatch.dto.ApiResponse;
import com.ejemplo.springbatch.dto.JobExecutionResponse;
import com.ejemplo.springbatch.dto.JobStatusResponse;
import com.ejemplo.springbatch.service.JobExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar la ejecución de Jobs de Spring Batch.
 * 
 * Este controlador expone endpoints HTTP para:
 * - Ejecutar el job de procesamiento de clientes
 * - Consultar el estado de ejecuciones
 * - Obtener historial y estadísticas de ejecuciones
 * 
 * Base URL: /api/jobs
 * 
 * Endpoints disponibles:
 * - POST /api/jobs/ejecutar - Ejecuta el job de procesamiento
 * - GET /api/jobs/estado/{id} - Obtiene estado de una ejecución
 * - GET /api/jobs/historial - Obtiene historial de ejecuciones
 * - GET /api/jobs/estadisticas - Obtiene estadísticas generales
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/jobs")
@Tag(name = "Jobs", description = "API para gestionar la ejecución de Jobs de Spring Batch")
public class JobController {

    /**
     * Servicio para ejecutar y consultar jobs.
     */
    private final JobExecutionService jobExecutionService;

    /**
     * Constructor que inyecta el servicio de ejecución de jobs.
     * 
     * @param jobExecutionService Servicio de jobs
     */
    public JobController(JobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    /**
     * Ejecuta el job de procesamiento de clientes.
     * 
     * Este endpoint inicia la ejecución del job que:
     * 1. Lee todos los clientes pendientes de procesar
     * 2. Aplica transformaciones (nombre en mayúsculas, email normalizado)
     * 3. Genera códigos únicos de cliente
     * 4. Guarda los resultados en la tabla de procesados
     * 
     * La ejecución es síncrona: el endpoint espera a que termine el job.
     * 
     * @return ResponseEntity con los detalles de la ejecución
     */
    @PostMapping("/ejecutar")
    @Operation(
        summary = "Ejecutar Job de Procesamiento",
        description = "Inicia la ejecución del job que procesa todos los clientes pendientes. " +
                      "La ejecución es síncrona y retorna las estadísticas al finalizar."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Job ejecutado correctamente",
            content = @Content(schema = @Schema(implementation = JobExecutionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error durante la ejecución del job"
        )
    })
    public ResponseEntity<ApiResponse<JobExecutionResponse>> ejecutarJob() {
        log.info("Recibida solicitud para ejecutar job de procesamiento");

        try {
            // Ejecutar el job de forma síncrona
            JobExecutionResponse resultado = jobExecutionService.ejecutarJobProcesamiento();

            // Evaluar resultado
            if (resultado.isExitoso()) {
                log.info("Job ejecutado exitosamente - Execution ID: {}", resultado.getExecutionId());
                return ResponseEntity.ok(
                    ApiResponse.exito(resultado, "Job ejecutado correctamente")
                );
            } else {
                log.warn("Job terminó con errores: {}", resultado.getMensaje());
                return ResponseEntity.ok(
                    ApiResponse.exito(resultado, "Job terminó con estado: " + resultado.getEstado())
                );
            }

        } catch (Exception e) {
            log.error("Error al ejecutar el job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error al ejecutar el job: " + e.getMessage(), "JOB_ERROR"));
        }
    }

    /**
     * Obtiene el estado de una ejecución específica.
     * 
     * Consulta la información de una ejecución por su ID,
     * incluyendo estado, tiempos y estadísticas.
     * 
     * @param executionId ID de la ejecución a consultar
     * @return ResponseEntity con el estado de la ejecución
     */
    @GetMapping("/estado/{executionId}")
    @Operation(
        summary = "Obtener Estado de Ejecución",
        description = "Consulta el estado y detalles de una ejecución específica del job"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estado de la ejecución obtenido",
            content = @Content(schema = @Schema(implementation = JobStatusResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Ejecución no encontrada"
        )
    })
    public ResponseEntity<ApiResponse<JobStatusResponse>> obtenerEstado(
            @Parameter(description = "ID de la ejecución del job", required = true)
            @PathVariable Long executionId) {
        
        log.info("Consultando estado de ejecución ID: {}", executionId);

        JobStatusResponse estado = jobExecutionService.obtenerEstadoEjecucion(executionId);

        if (estado.isEncontrado()) {
            return ResponseEntity.ok(
                ApiResponse.exito(estado, "Estado obtenido correctamente")
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene el historial de las últimas ejecuciones.
     * 
     * @param limite Número máximo de ejecuciones a retornar (default: 10)
     * @return ResponseEntity con lista de ejecuciones
     */
    @GetMapping("/historial")
    @Operation(
        summary = "Obtener Historial de Ejecuciones",
        description = "Retorna el historial de las últimas N ejecuciones del job"
    )
    public ResponseEntity<ApiResponse<List<JobStatusResponse>>> obtenerHistorial(
            @Parameter(description = "Número máximo de ejecuciones a retornar")
            @RequestParam(defaultValue = "10") int limite) {
        
        log.info("Consultando historial de últimas {} ejecuciones", limite);

        List<JobStatusResponse> historial = jobExecutionService.obtenerHistorialEjecuciones(limite);

        return ResponseEntity.ok(
            ApiResponse.exito(historial, "Se encontraron " + historial.size() + " ejecuciones")
        );
    }

    /**
     * Obtiene estadísticas generales de todas las ejecuciones.
     * 
     * @return ResponseEntity con mapa de estadísticas
     */
    @GetMapping("/estadisticas")
    @Operation(
        summary = "Obtener Estadísticas de Jobs",
        description = "Retorna estadísticas resumidas de todas las ejecuciones del job"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticas() {
        log.info("Consultando estadísticas de ejecuciones");

        Map<String, Object> estadisticas = jobExecutionService.obtenerEstadisticas();

        return ResponseEntity.ok(
            ApiResponse.exito(estadisticas, "Estadísticas obtenidas correctamente")
        );
    }
}

