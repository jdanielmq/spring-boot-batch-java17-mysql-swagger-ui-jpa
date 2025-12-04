package com.ejemplo.springbatch.controller;

import com.ejemplo.springbatch.dto.ApiResponse;
import com.ejemplo.springbatch.dto.ClienteDTO;
import com.ejemplo.springbatch.dto.EstadisticasClienteResponse;
import com.ejemplo.springbatch.entity.EstadoCliente;
import com.ejemplo.springbatch.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar Clientes.
 * 
 * Este controlador expone endpoints HTTP para:
 * - Operaciones CRUD de clientes
 * - Consultas y búsquedas
 * - Estadísticas de clientes
 * - Operaciones de mantenimiento para pruebas
 * 
 * Base URL: /api/clientes
 * 
 * Endpoints disponibles:
 * - POST /api/clientes - Crear cliente
 * - GET /api/clientes - Listar clientes con paginación
 * - GET /api/clientes/{id} - Obtener cliente por ID
 * - PUT /api/clientes/{id} - Actualizar cliente
 * - DELETE /api/clientes/{id} - Eliminar cliente
 * - GET /api/clientes/estado/{estado} - Listar por estado
 * - GET /api/clientes/pendientes - Listar pendientes de procesar
 * - GET /api/clientes/estadisticas - Obtener estadísticas
 * - POST /api/clientes/datos-prueba - Crear datos de prueba
 * - POST /api/clientes/reiniciar-procesamiento - Reiniciar flags
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para gestionar clientes del sistema")
public class ClienteController {

    /**
     * Servicio de clientes.
     */
    private final ClienteService clienteService;

    /**
     * Constructor que inyecta el servicio de clientes.
     * 
     * @param clienteService Servicio de clientes
     */
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // ============================================
    // OPERACIONES CRUD
    // ============================================

    /**
     * Crea un nuevo cliente.
     * 
     * El cliente se crea con estado PENDIENTE para ser procesado
     * por el próximo job de batch.
     * 
     * @param clienteDTO Datos del cliente a crear
     * @return ResponseEntity con el cliente creado
     */
    @PostMapping
    @Operation(
        summary = "Crear Cliente",
        description = "Crea un nuevo cliente en el sistema con estado PENDIENTE"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Cliente creado exitosamente",
            content = @Content(schema = @Schema(implementation = ClienteDTO.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de cliente inválidos"
        )
    })
    public ResponseEntity<ApiResponse<ClienteDTO>> crearCliente(
            @Valid @RequestBody ClienteDTO clienteDTO) {
        
        log.info("Recibida solicitud para crear cliente: {}", clienteDTO.getEmail());

        try {
            ClienteDTO creado = clienteService.crearCliente(clienteDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(creado, "Cliente creado exitosamente"));
        } catch (IllegalArgumentException e) {
            log.warn("Error al crear cliente: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Obtiene todos los clientes con paginación.
     * 
     * @param pagina Número de página (0-based)
     * @param tamanio Tamaño de página
     * @return ResponseEntity con página de clientes
     */
    @GetMapping
    @Operation(
        summary = "Listar Clientes",
        description = "Obtiene todos los clientes con paginación"
    )
    public ResponseEntity<ApiResponse<Page<ClienteDTO>>> listarClientes(
            @Parameter(description = "Número de página (0-based)")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int tamanio) {
        
        log.debug("Listando clientes - Página: {}, Tamaño: {}", pagina, tamanio);

        Page<ClienteDTO> clientes = clienteService.obtenerTodos(pagina, tamanio);

        return ResponseEntity.ok(
            ApiResponse.exito(clientes, "Se encontraron " + clientes.getTotalElements() + " clientes")
        );
    }

    /**
     * Obtiene un cliente por su ID.
     * 
     * @param id ID del cliente a buscar
     * @return ResponseEntity con el cliente encontrado
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener Cliente por ID",
        description = "Busca y retorna un cliente por su identificador único"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Cliente encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Cliente no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<ClienteDTO>> obtenerPorId(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id) {
        
        log.debug("Buscando cliente por ID: {}", id);

        return clienteService.obtenerPorId(id)
            .map(cliente -> ResponseEntity.ok(
                ApiResponse.exito(cliente, "Cliente encontrado")))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Cliente no encontrado con ID: " + id, "NOT_FOUND")));
    }

    /**
     * Actualiza un cliente existente.
     * 
     * @param id ID del cliente a actualizar
     * @param clienteDTO Nuevos datos del cliente
     * @return ResponseEntity con el cliente actualizado
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar Cliente",
        description = "Actualiza los datos de un cliente existente"
    )
    public ResponseEntity<ApiResponse<ClienteDTO>> actualizarCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ClienteDTO clienteDTO) {
        
        log.info("Actualizando cliente ID: {}", id);

        try {
            ClienteDTO actualizado = clienteService.actualizarCliente(id, clienteDTO);
            return ResponseEntity.ok(
                ApiResponse.exito(actualizado, "Cliente actualizado exitosamente"));
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar cliente: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Elimina un cliente por su ID.
     * 
     * @param id ID del cliente a eliminar
     * @return ResponseEntity confirmando la eliminación
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar Cliente",
        description = "Elimina un cliente del sistema"
    )
    public ResponseEntity<ApiResponse<Void>> eliminarCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id) {
        
        log.info("Eliminando cliente ID: {}", id);

        try {
            clienteService.eliminarCliente(id);
            return ResponseEntity.ok(
                ApiResponse.exito("Cliente eliminado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        }
    }

    // ============================================
    // CONSULTAS ESPECIALIZADAS
    // ============================================

    /**
     * Obtiene clientes por estado.
     * 
     * @param estado Estado a filtrar (ACTIVO, INACTIVO, PENDIENTE, ERROR)
     * @return ResponseEntity con lista de clientes
     */
    @GetMapping("/estado/{estado}")
    @Operation(
        summary = "Listar por Estado",
        description = "Obtiene todos los clientes con un estado específico"
    )
    public ResponseEntity<ApiResponse<List<ClienteDTO>>> listarPorEstado(
            @Parameter(description = "Estado del cliente", required = true)
            @PathVariable EstadoCliente estado) {
        
        log.debug("Listando clientes por estado: {}", estado);

        List<ClienteDTO> clientes = clienteService.obtenerPorEstado(estado);

        return ResponseEntity.ok(
            ApiResponse.exito(clientes, "Se encontraron " + clientes.size() + " clientes con estado " + estado)
        );
    }

    /**
     * Obtiene clientes pendientes de procesar.
     * 
     * @return ResponseEntity con lista de clientes pendientes
     */
    @GetMapping("/pendientes")
    @Operation(
        summary = "Listar Pendientes",
        description = "Obtiene todos los clientes que aún no han sido procesados por el batch"
    )
    public ResponseEntity<ApiResponse<List<ClienteDTO>>> listarPendientes() {
        log.debug("Listando clientes pendientes de procesar");

        List<ClienteDTO> clientes = clienteService.obtenerPendientesProcesar();

        return ResponseEntity.ok(
            ApiResponse.exito(clientes, "Se encontraron " + clientes.size() + " clientes pendientes")
        );
    }

    /**
     * Busca clientes por nombre.
     * 
     * @param nombre Texto a buscar en el nombre
     * @return ResponseEntity con lista de clientes que coinciden
     */
    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar por Nombre",
        description = "Busca clientes cuyo nombre contenga el texto especificado"
    )
    public ResponseEntity<ApiResponse<List<ClienteDTO>>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre")
            @RequestParam String nombre) {
        
        log.debug("Buscando clientes por nombre: {}", nombre);

        List<ClienteDTO> clientes = clienteService.buscarPorNombre(nombre);

        return ResponseEntity.ok(
            ApiResponse.exito(clientes, "Se encontraron " + clientes.size() + " clientes")
        );
    }

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    /**
     * Obtiene estadísticas de clientes.
     * 
     * @return ResponseEntity con estadísticas
     */
    @GetMapping("/estadisticas")
    @Operation(
        summary = "Obtener Estadísticas",
        description = "Retorna estadísticas resumidas de los clientes del sistema"
    )
    public ResponseEntity<ApiResponse<EstadisticasClienteResponse>> obtenerEstadisticas() {
        log.debug("Obteniendo estadísticas de clientes");

        EstadisticasClienteResponse estadisticas = clienteService.obtenerEstadisticas();

        return ResponseEntity.ok(
            ApiResponse.exito(estadisticas, "Estadísticas obtenidas correctamente")
        );
    }

    // ============================================
    // OPERACIONES DE MANTENIMIENTO
    // ============================================

    /**
     * Crea datos de prueba para testing.
     * 
     * @param cantidad Número de clientes a crear (default: 10)
     * @return ResponseEntity con número de clientes creados
     */
    @PostMapping("/datos-prueba")
    @Operation(
        summary = "Crear Datos de Prueba",
        description = "Crea un número específico de clientes de prueba para testing del batch"
    )
    public ResponseEntity<ApiResponse<Integer>> crearDatosPrueba(
            @Parameter(description = "Número de clientes a crear")
            @RequestParam(defaultValue = "10") int cantidad) {
        
        log.info("Creando {} clientes de prueba", cantidad);

        int creados = clienteService.crearDatosPrueba(cantidad);

        return ResponseEntity.ok(
            ApiResponse.exito(creados, "Se crearon " + creados + " clientes de prueba")
        );
    }

    /**
     * Reinicia el estado de procesamiento de todos los clientes.
     * 
     * @return ResponseEntity con número de clientes reiniciados
     */
    @PostMapping("/reiniciar-procesamiento")
    @Operation(
        summary = "Reiniciar Procesamiento",
        description = "Marca todos los clientes como NO procesados para que el batch los vuelva a procesar"
    )
    public ResponseEntity<ApiResponse<Integer>> reiniciarProcesamiento() {
        log.info("Reiniciando estado de procesamiento de clientes");

        int actualizados = clienteService.reiniciarProcesamiento();

        return ResponseEntity.ok(
            ApiResponse.exito(actualizados, "Se reiniciaron " + actualizados + " clientes")
        );
    }
}

