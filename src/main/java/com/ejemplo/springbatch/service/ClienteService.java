package com.ejemplo.springbatch.service;

import com.ejemplo.springbatch.dto.ClienteDTO;
import com.ejemplo.springbatch.dto.EstadisticasClienteResponse;
import com.ejemplo.springbatch.entity.Cliente;
import com.ejemplo.springbatch.entity.ClienteProcesado;
import com.ejemplo.springbatch.entity.EstadoCliente;
import com.ejemplo.springbatch.repository.ClienteProcesadoRepository;
import com.ejemplo.springbatch.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar las operaciones de Clientes.
 * 
 * Este servicio proporciona la lógica de negocio para:
 * - CRUD de clientes
 * - Consultas y filtros
 * - Estadísticas de clientes
 * - Operaciones de mantenimiento
 * 
 * Utiliza transacciones para garantizar la consistencia de datos
 * y convierte entre entidades y DTOs para la capa de presentación.
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Service
public class ClienteService {

    /**
     * Repositorio para acceso a datos de clientes.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Repositorio para acceso a datos de clientes procesados.
     */
    private final ClienteProcesadoRepository clienteProcesadoRepository;

    /**
     * Constructor que inyecta los repositorios necesarios.
     * 
     * @param clienteRepository Repositorio de clientes
     * @param clienteProcesadoRepository Repositorio de clientes procesados
     */
    public ClienteService(
            ClienteRepository clienteRepository,
            ClienteProcesadoRepository clienteProcesadoRepository) {
        this.clienteRepository = clienteRepository;
        this.clienteProcesadoRepository = clienteProcesadoRepository;
    }

    // ============================================
    // OPERACIONES CRUD
    // ============================================

    /**
     * Crea un nuevo cliente en el sistema.
     * 
     * El cliente se crea con estado PENDIENTE y procesado=false
     * para que sea recogido por el próximo job de batch.
     * 
     * @param clienteDTO Datos del cliente a crear
     * @return Cliente creado convertido a DTO
     */
    @Transactional
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        log.info("Creando nuevo cliente: {}", clienteDTO.getEmail());

        // Verificar que el email no exista
        if (clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + clienteDTO.getEmail());
        }

        // Convertir DTO a entidad
        Cliente cliente = convertirAEntidad(clienteDTO);
        
        // Establecer valores por defecto
        cliente.setEstado(EstadoCliente.PENDIENTE);
        cliente.setProcesado(false);

        // Guardar en base de datos
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente creado con ID: {}", guardado.getId());

        return convertirADTO(guardado);
    }

    /**
     * Obtiene un cliente por su ID.
     * 
     * @param id ID del cliente a buscar
     * @return Optional con el cliente si existe
     */
    public Optional<ClienteDTO> obtenerPorId(Long id) {
        log.debug("Buscando cliente por ID: {}", id);
        return clienteRepository.findById(id)
                .map(this::convertirADTO);
    }

    /**
     * Obtiene un cliente por su email.
     * 
     * @param email Email del cliente a buscar
     * @return Optional con el cliente si existe
     */
    public Optional<ClienteDTO> obtenerPorEmail(String email) {
        log.debug("Buscando cliente por email: {}", email);
        return clienteRepository.findByEmail(email)
                .map(this::convertirADTO);
    }

    /**
     * Obtiene todos los clientes con paginación.
     * 
     * @param pagina Número de página (0-based)
     * @param tamanio Tamaño de la página
     * @return Página de clientes
     */
    public Page<ClienteDTO> obtenerTodos(int pagina, int tamanio) {
        log.debug("Obteniendo clientes - Página: {}, Tamaño: {}", pagina, tamanio);
        
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("id").descending());
        return clienteRepository.findAll(pageable)
                .map(this::convertirADTO);
    }

    /**
     * Actualiza un cliente existente.
     * 
     * @param id ID del cliente a actualizar
     * @param clienteDTO Nuevos datos del cliente
     * @return Cliente actualizado
     */
    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        log.info("Actualizando cliente ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Actualizar campos
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setTelefono(clienteDTO.getTelefono());
        
        // Si cambia el email, verificar que no exista
        if (!cliente.getEmail().equals(clienteDTO.getEmail())) {
            if (clienteRepository.existsByEmail(clienteDTO.getEmail())) {
                throw new IllegalArgumentException("El email ya está en uso: " + clienteDTO.getEmail());
            }
            cliente.setEmail(clienteDTO.getEmail());
        }

        Cliente actualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado correctamente");

        return convertirADTO(actualizado);
    }

    /**
     * Elimina un cliente por su ID.
     * 
     * @param id ID del cliente a eliminar
     */
    @Transactional
    public void eliminarCliente(Long id) {
        log.info("Eliminando cliente ID: {}", id);

        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado con ID: " + id);
        }

        clienteRepository.deleteById(id);
        log.info("Cliente eliminado correctamente");
    }

    // ============================================
    // CONSULTAS DE CLIENTES
    // ============================================

    /**
     * Obtiene clientes por estado.
     * 
     * @param estado Estado a filtrar
     * @return Lista de clientes con ese estado
     */
    public List<ClienteDTO> obtenerPorEstado(EstadoCliente estado) {
        log.debug("Buscando clientes por estado: {}", estado);
        return clienteRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene clientes pendientes de procesar.
     * 
     * @return Lista de clientes sin procesar
     */
    public List<ClienteDTO> obtenerPendientesProcesar() {
        log.debug("Obteniendo clientes pendientes de procesar");
        return clienteRepository.findClientesSinProcesar().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca clientes por nombre (búsqueda parcial).
     * 
     * @param nombre Texto a buscar en el nombre
     * @return Lista de clientes que coinciden
     */
    public List<ClienteDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando clientes por nombre: {}", nombre);
        return clienteRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // OPERACIONES DE MANTENIMIENTO
    // ============================================

    /**
     * Reinicia el estado de procesamiento de todos los clientes.
     * Útil para reprocesar todos los registros.
     * 
     * @return Número de clientes actualizados
     */
    @Transactional
    public int reiniciarProcesamiento() {
        log.info("Reiniciando estado de procesamiento de todos los clientes");
        int actualizados = clienteRepository.reiniciarEstadoProcesamiento(LocalDateTime.now());
        log.info("Se reiniciaron {} clientes", actualizados);
        return actualizados;
    }

    /**
     * Crea datos de prueba en el sistema.
     * 
     * @param cantidad Número de clientes a crear
     * @return Número de clientes creados
     */
    @Transactional
    public int crearDatosPrueba(int cantidad) {
        log.info("Creando {} clientes de prueba", cantidad);

        int creados = 0;
        for (int i = 1; i <= cantidad; i++) {
            Cliente cliente = Cliente.builder()
                    .nombre("Cliente de Prueba " + i)
                    .email("cliente" + i + "_" + System.currentTimeMillis() + "@ejemplo.com")
                    .telefono(String.format("555-%04d", i))
                    .estado(EstadoCliente.PENDIENTE)
                    .procesado(false)
                    .build();
            
            clienteRepository.save(cliente);
            creados++;
        }

        log.info("Se crearon {} clientes de prueba", creados);
        return creados;
    }

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    /**
     * Obtiene estadísticas generales de clientes.
     * 
     * @return Objeto con estadísticas resumidas
     */
    public EstadisticasClienteResponse obtenerEstadisticas() {
        log.debug("Calculando estadísticas de clientes");

        long total = clienteRepository.count();
        long pendientes = clienteRepository.contarClientesSinProcesar();
        long activos = clienteRepository.countByEstado(EstadoCliente.ACTIVO);
        long inactivos = clienteRepository.countByEstado(EstadoCliente.INACTIVO);
        long procesados = clienteProcesadoRepository.count();

        return EstadisticasClienteResponse.builder()
                .totalClientes(total)
                .clientesPendientes(pendientes)
                .clientesActivos(activos)
                .clientesInactivos(inactivos)
                .registrosProcesados(procesados)
                .fechaConsulta(LocalDateTime.now())
                .build();
    }

    // ============================================
    // MÉTODOS DE CONVERSIÓN
    // ============================================

    /**
     * Convierte una entidad Cliente a DTO.
     * 
     * @param cliente Entidad a convertir
     * @return ClienteDTO equivalente
     */
    private ClienteDTO convertirADTO(Cliente cliente) {
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())
                .procesado(cliente.getProcesado())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaActualizacion(cliente.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte un DTO a entidad Cliente.
     * 
     * @param dto DTO a convertir
     * @return Entidad Cliente equivalente
     */
    private Cliente convertirAEntidad(ClienteDTO dto) {
        return Cliente.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .estado(dto.getEstado())
                .build();
    }
}

