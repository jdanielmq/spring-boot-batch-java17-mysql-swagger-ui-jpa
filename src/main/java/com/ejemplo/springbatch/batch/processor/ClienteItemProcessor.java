package com.ejemplo.springbatch.batch.processor;

import com.ejemplo.springbatch.entity.Cliente;
import com.ejemplo.springbatch.entity.ClienteProcesado;
import com.ejemplo.springbatch.entity.EstadoCliente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente ItemProcessor que transforma clientes durante el procesamiento batch.
 * 
 * Este componente es el segundo paso en el pipeline de Spring Batch.
 * Se encarga de aplicar la lógica de negocio y transformaciones a cada cliente.
 * 
 * Transformaciones aplicadas:
 * - Convierte el nombre a mayúsculas
 * - Normaliza el email a minúsculas
 * - Genera un código único de cliente
 * - Establece el estado final según reglas de negocio
 * - Agrega mensaje descriptivo del procesamiento
 * 
 * Reglas de negocio:
 * - Clientes con email válido se marcan como ACTIVO
 * - Clientes sin teléfono se marcan con advertencia
 * - El código se genera con formato CLI-XXXXXXXX
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Component
public class ClienteItemProcessor implements ItemProcessor<Cliente, ClienteProcesado> {

    /**
     * Procesa un cliente y lo transforma en un ClienteProcesado.
     * 
     * Este método contiene toda la lógica de negocio del procesamiento.
     * Puede retornar null para filtrar registros que no deben continuar.
     * 
     * @param cliente Cliente original leído por el ItemReader
     * @return ClienteProcesado con las transformaciones aplicadas, o null para filtrar
     * @throws Exception Si ocurre un error durante el procesamiento
     */
    @Override
    public ClienteProcesado process(Cliente cliente) throws Exception {
        log.info("----------------------------------------");
        log.info("PROCESANDO CLIENTE ID: {}", cliente.getId());
        log.info("----------------------------------------");

        try {
            // ============================================
            // PASO 1: Validaciones iniciales
            // ============================================
            if (!validarCliente(cliente)) {
                log.warn("Cliente ID {} no pasó las validaciones, será filtrado", cliente.getId());
                return null; // Retornar null filtra el registro
            }

            // ============================================
            // PASO 2: Aplicar transformaciones
            // ============================================
            
            // Transformar nombre a mayúsculas
            String nombreProcesado = transformarNombre(cliente.getNombre());
            log.debug("Nombre original: '{}' -> Procesado: '{}'", 
                    cliente.getNombre(), nombreProcesado);

            // Normalizar email a minúsculas
            String emailProcesado = normalizarEmail(cliente.getEmail());
            log.debug("Email original: '{}' -> Procesado: '{}'", 
                    cliente.getEmail(), emailProcesado);

            // Generar código único de cliente
            String codigoCliente = generarCodigoCliente(cliente.getId());
            log.debug("Código de cliente generado: {}", codigoCliente);

            // ============================================
            // PASO 3: Determinar estado final
            // ============================================
            EstadoCliente estadoFinal = determinarEstadoFinal(cliente);
            log.debug("Estado final determinado: {}", estadoFinal);

            // ============================================
            // PASO 4: Generar mensaje de procesamiento
            // ============================================
            String mensaje = generarMensajeProcesamiento(cliente, estadoFinal);

            // ============================================
            // PASO 5: Construir objeto procesado
            // ============================================
            ClienteProcesado procesado = ClienteProcesado.builder()
                    .clienteId(cliente.getId())
                    .nombreProcesado(nombreProcesado)
                    .emailProcesado(emailProcesado)
                    .codigoCliente(codigoCliente)
                    .estadoFinal(estadoFinal)
                    .fechaProcesamiento(LocalDateTime.now())
                    .mensaje(mensaje)
                    .build();

            log.info("Cliente ID {} procesado exitosamente con código {}", 
                    cliente.getId(), codigoCliente);

            return procesado;

        } catch (Exception e) {
            log.error("Error procesando cliente ID {}: {}", cliente.getId(), e.getMessage());
            throw e;
        }
    }

    // ============================================
    // MÉTODOS PRIVADOS DE PROCESAMIENTO
    // ============================================

    /**
     * Valida que el cliente tenga los datos mínimos requeridos.
     * 
     * @param cliente Cliente a validar
     * @return true si el cliente es válido, false en caso contrario
     */
    private boolean validarCliente(Cliente cliente) {
        if (cliente == null) {
            log.warn("El cliente es null");
            return false;
        }

        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            log.warn("El cliente ID {} no tiene nombre", cliente.getId());
            return false;
        }

        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            log.warn("El cliente ID {} no tiene email", cliente.getId());
            return false;
        }

        return true;
    }

    /**
     * Transforma el nombre del cliente.
     * Aplica: Mayúsculas y eliminación de espacios extra.
     * 
     * @param nombre Nombre original
     * @return Nombre transformado
     */
    private String transformarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }
        // Eliminar espacios extras y convertir a mayúsculas
        return nombre.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    /**
     * Normaliza el email del cliente.
     * Aplica: Minúsculas y eliminación de espacios.
     * 
     * @param email Email original
     * @return Email normalizado
     */
    private String normalizarEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    /**
     * Genera un código único para el cliente.
     * Formato: CLI-XXXXXXXX donde X son caracteres del UUID.
     * 
     * @param clienteId ID del cliente
     * @return Código único generado
     */
    private String generarCodigoCliente(Long clienteId) {
        // Generar UUID corto basado en el ID del cliente y timestamp
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return String.format("CLI-%s", uuid);
    }

    /**
     * Determina el estado final del cliente según reglas de negocio.
     * 
     * Reglas:
     * - Si tiene email válido y teléfono -> ACTIVO
     * - Si tiene email pero no teléfono -> ACTIVO (con nota)
     * - Si estaba INACTIVO -> permanece INACTIVO
     * 
     * @param cliente Cliente a evaluar
     * @return Estado final determinado
     */
    private EstadoCliente determinarEstadoFinal(Cliente cliente) {
        // Si el cliente ya estaba inactivo, mantener ese estado
        if (cliente.getEstado() == EstadoCliente.INACTIVO) {
            return EstadoCliente.INACTIVO;
        }

        // Si tiene datos completos, marcar como activo
        if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
            return EstadoCliente.ACTIVO;
        }

        // Por defecto, pendiente
        return EstadoCliente.PENDIENTE;
    }

    /**
     * Genera un mensaje descriptivo del procesamiento.
     * 
     * @param cliente Cliente procesado
     * @param estadoFinal Estado final asignado
     * @return Mensaje descriptivo
     */
    private String generarMensajeProcesamiento(Cliente cliente, EstadoCliente estadoFinal) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Procesado correctamente. ");
        mensaje.append("Estado final: ").append(estadoFinal.getDescripcion()).append(". ");
        
        if (cliente.getTelefono() == null || cliente.getTelefono().isEmpty()) {
            mensaje.append("Nota: Cliente sin teléfono registrado. ");
        }
        
        mensaje.append("Fecha de procesamiento: ").append(LocalDateTime.now());
        
        return mensaje.toString();
    }
}

