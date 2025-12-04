package com.ejemplo.springbatch.batch.writer;

import com.ejemplo.springbatch.entity.ClienteProcesado;
import com.ejemplo.springbatch.repository.ClienteProcesadoRepository;
import com.ejemplo.springbatch.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Componente ItemWriter que persiste los clientes procesados.
 * 
 * Este componente es el tercer y último paso en el pipeline de Spring Batch.
 * Se encarga de guardar los resultados del procesamiento en la base de datos.
 * 
 * Responsabilidades:
 * - Guardar los ClienteProcesado en la tabla de procesados
 * - Actualizar el flag de procesado en la tabla original
 * - Registrar estadísticas de escritura
 * - Manejar transacciones de forma atómica
 * 
 * Características técnicas:
 * - Recibe un Chunk (lote) de registros para mejor rendimiento
 * - Operaciones transaccionales para consistencia
 * - Logging detallado para auditoría
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Component
public class ClienteItemWriter implements ItemWriter<ClienteProcesado> {

    /**
     * Repositorio para guardar clientes procesados.
     */
    private final ClienteProcesadoRepository clienteProcesadoRepository;

    /**
     * Repositorio para actualizar el estado del cliente original.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Constructor que inyecta las dependencias necesarias.
     * 
     * @param clienteProcesadoRepository Repositorio de clientes procesados
     * @param clienteRepository Repositorio de clientes originales
     */
    public ClienteItemWriter(
            ClienteProcesadoRepository clienteProcesadoRepository,
            ClienteRepository clienteRepository) {
        this.clienteProcesadoRepository = clienteProcesadoRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Escribe un chunk de clientes procesados en la base de datos.
     * 
     * Este método recibe un lote (chunk) de registros para procesar
     * en una sola transacción, mejorando el rendimiento.
     * 
     * Proceso:
     * 1. Recorre cada cliente procesado del chunk
     * 2. Guarda el registro en la tabla de procesados
     * 3. Marca el cliente original como procesado
     * 4. Registra estadísticas de la operación
     * 
     * @param chunk Lote de ClienteProcesado a escribir
     * @throws Exception Si ocurre un error durante la escritura
     */
    @Override
    @Transactional
    public void write(Chunk<? extends ClienteProcesado> chunk) throws Exception {
        log.info("========================================");
        log.info("INICIANDO ESCRITURA DE CHUNK");
        log.info("Tamaño del chunk: {} registros", chunk.size());
        log.info("========================================");

        int exitosos = 0;
        int fallidos = 0;

        for (ClienteProcesado clienteProcesado : chunk) {
            try {
                // ============================================
                // PASO 1: Guardar cliente procesado
                // ============================================
                log.debug("Guardando cliente procesado - Cliente ID: {}", 
                        clienteProcesado.getClienteId());

                // Verificar si ya existe un registro para este cliente
                if (clienteProcesadoRepository.existsByClienteId(clienteProcesado.getClienteId())) {
                    log.warn("El cliente ID {} ya fue procesado anteriormente, se omite", 
                            clienteProcesado.getClienteId());
                    continue;
                }

                // Guardar el registro procesado
                ClienteProcesado guardado = clienteProcesadoRepository.save(clienteProcesado);
                log.debug("Cliente procesado guardado con ID: {}", guardado.getId());

                // ============================================
                // PASO 2: Actualizar cliente original
                // ============================================
                int actualizados = clienteRepository.marcarComoProcesado(
                        clienteProcesado.getClienteId(),
                        LocalDateTime.now()
                );

                if (actualizados > 0) {
                    log.debug("Cliente original ID {} marcado como procesado", 
                            clienteProcesado.getClienteId());
                    exitosos++;
                } else {
                    log.warn("No se pudo marcar como procesado el cliente ID {}", 
                            clienteProcesado.getClienteId());
                    fallidos++;
                }

            } catch (Exception e) {
                log.error("Error escribiendo cliente procesado ID {}: {}", 
                        clienteProcesado.getClienteId(), e.getMessage());
                fallidos++;
                // Re-lanzar para que Spring Batch maneje el error
                throw e;
            }
        }

        // ============================================
        // RESUMEN DE ESCRITURA
        // ============================================
        log.info("----------------------------------------");
        log.info("RESUMEN DE ESCRITURA DEL CHUNK");
        log.info("----------------------------------------");
        log.info("Total procesados: {}", chunk.size());
        log.info("Exitosos: {}", exitosos);
        log.info("Fallidos: {}", fallidos);
        log.info("----------------------------------------");
    }
}

