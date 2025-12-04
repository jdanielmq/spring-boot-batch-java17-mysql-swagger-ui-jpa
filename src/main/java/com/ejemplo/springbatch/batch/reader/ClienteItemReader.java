package com.ejemplo.springbatch.batch.reader;

import com.ejemplo.springbatch.entity.Cliente;
import com.ejemplo.springbatch.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Componente ItemReader personalizado para leer clientes de la base de datos.
 * 
 * Este componente es el primer paso en el pipeline de Spring Batch.
 * Se encarga de obtener los registros de clientes que necesitan ser procesados.
 * 
 * Características:
 * - Lee clientes que no han sido procesados (procesado = false)
 * - Implementa la interfaz ItemReader de Spring Batch
 * - Utiliza un Iterator para retornar un cliente a la vez
 * - Thread-safe mediante sincronización
 * 
 * Flujo de datos:
 * 1. Al iniciar el job, obtiene todos los clientes pendientes
 * 2. En cada llamada a read(), retorna el siguiente cliente
 * 3. Retorna null cuando no hay más clientes (señal de fin)
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Component
public class ClienteItemReader implements ItemReader<Cliente> {

    /**
     * Repositorio para acceder a los datos de clientes.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Iterador sobre la lista de clientes pendientes.
     * Se inicializa de forma perezosa en la primera llamada a read().
     */
    private Iterator<Cliente> clienteIterator;

    /**
     * Bandera para controlar la inicialización del iterador.
     */
    private boolean initialized = false;

    /**
     * Constructor que inyecta el repositorio de clientes.
     * 
     * @param clienteRepository Repositorio JPA de clientes
     */
    public ClienteItemReader(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Lee el siguiente cliente de la base de datos.
     * 
     * Este método es llamado repetidamente por Spring Batch hasta que
     * retorne null, indicando que no hay más elementos por procesar.
     * 
     * Proceso:
     * 1. En la primera llamada, inicializa el iterador con todos los clientes pendientes
     * 2. Retorna el siguiente cliente del iterador
     * 3. Retorna null cuando no hay más clientes
     * 
     * @return El siguiente Cliente a procesar, o null si no hay más
     * @throws Exception Si ocurre un error al leer los datos
     * @throws UnexpectedInputException Si hay datos inesperados
     * @throws ParseException Si hay error al parsear datos
     * @throws NonTransientResourceException Si hay error de recurso no transitorio
     */
    @Override
    public synchronized Cliente read() throws Exception, UnexpectedInputException, 
            ParseException, NonTransientResourceException {
        
        // Inicialización perezosa del iterador
        if (!initialized) {
            log.info("========================================");
            log.info("INICIANDO LECTURA DE CLIENTES");
            log.info("========================================");
            
            // Obtener todos los clientes pendientes de procesar
            List<Cliente> clientesPendientes = clienteRepository.findClientesSinProcesar();
            
            log.info("Total de clientes pendientes encontrados: {}", clientesPendientes.size());
            
            // Crear iterador sobre la lista
            this.clienteIterator = clientesPendientes.iterator();
            this.initialized = true;
        }

        // Retornar el siguiente cliente o null si no hay más
        if (clienteIterator != null && clienteIterator.hasNext()) {
            Cliente cliente = clienteIterator.next();
            log.debug("Leyendo cliente ID: {} - Nombre: {}", cliente.getId(), cliente.getNombre());
            return cliente;
        }

        log.info("No hay más clientes por leer");
        return null;
    }

    /**
     * Reinicia el reader para una nueva ejecución.
     * 
     * Este método debe llamarse antes de reutilizar el reader
     * en una nueva ejecución del job.
     */
    public synchronized void reset() {
        log.info("Reiniciando ClienteItemReader para nueva ejecución");
        this.clienteIterator = null;
        this.initialized = false;
    }
}

