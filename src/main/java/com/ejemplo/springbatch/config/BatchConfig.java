package com.ejemplo.springbatch.config;

import com.ejemplo.springbatch.batch.listener.JobCompletionListener;
import com.ejemplo.springbatch.batch.listener.StepExecutionListener;
import com.ejemplo.springbatch.batch.processor.ClienteItemProcessor;
import com.ejemplo.springbatch.batch.reader.ClienteItemReader;
import com.ejemplo.springbatch.batch.writer.ClienteItemWriter;
import com.ejemplo.springbatch.entity.Cliente;
import com.ejemplo.springbatch.entity.ClienteProcesado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración principal del Job de Spring Batch.
 * 
 * Esta clase define la estructura completa del job de procesamiento de clientes:
 * - Definición del Job y sus parámetros
 * - Definición de los Steps (pasos) del job
 * - Configuración del chunk (tamaño de lote)
 * - Asignación de listeners para monitoreo
 * 
 * Arquitectura del Job:
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    procesarClientesJob                      │
 * ├─────────────────────────────────────────────────────────────┤
 * │  ┌─────────────────────────────────────────────────────┐   │
 * │  │              procesarClientesStep                    │   │
 * │  │  ┌───────────┐  ┌───────────┐  ┌───────────┐       │   │
 * │  │  │  Reader   │→ │ Processor │→ │  Writer   │       │   │
 * │  │  │(DB→Cliente│  │(Transform)│  │(Cliente→DB│       │   │
 * │  │  └───────────┘  └───────────┘  └───────────┘       │   │
 * │  └─────────────────────────────────────────────────────┘   │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class BatchConfig {

    /**
     * Repositorio de metadatos de Spring Batch.
     * Almacena información de ejecuciones, parámetros y estado de jobs.
     */
    private final JobRepository jobRepository;

    /**
     * Gestor de transacciones para operaciones de base de datos.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Componente que lee clientes de la base de datos.
     */
    private final ClienteItemReader clienteItemReader;

    /**
     * Componente que procesa y transforma los clientes.
     */
    private final ClienteItemProcessor clienteItemProcessor;

    /**
     * Componente que escribe los clientes procesados.
     */
    private final ClienteItemWriter clienteItemWriter;

    /**
     * Listener del ciclo de vida del job.
     */
    private final JobCompletionListener jobCompletionListener;

    /**
     * Listener del ciclo de vida de cada step.
     */
    private final StepExecutionListener stepExecutionListener;

    /**
     * Tamaño del chunk (número de registros por transacción).
     * Configurado en application.yml
     */
    @Value("${batch.config.chunk-size:100}")
    private int chunkSize;

    /**
     * Constructor que inyecta todas las dependencias necesarias.
     * 
     * @param jobRepository Repositorio de metadatos de batch
     * @param transactionManager Gestor de transacciones
     * @param clienteItemReader Reader de clientes
     * @param clienteItemProcessor Processor de clientes
     * @param clienteItemWriter Writer de clientes
     * @param jobCompletionListener Listener del job
     * @param stepExecutionListener Listener del step
     */
    public BatchConfig(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ClienteItemReader clienteItemReader,
            ClienteItemProcessor clienteItemProcessor,
            ClienteItemWriter clienteItemWriter,
            JobCompletionListener jobCompletionListener,
            StepExecutionListener stepExecutionListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.clienteItemReader = clienteItemReader;
        this.clienteItemProcessor = clienteItemProcessor;
        this.clienteItemWriter = clienteItemWriter;
        this.jobCompletionListener = jobCompletionListener;
        this.stepExecutionListener = stepExecutionListener;
    }

    /**
     * Define el Job principal de procesamiento de clientes.
     * 
     * Características del Job:
     * - Nombre: "procesarClientesJob"
     * - Incrementador: RunIdIncrementer para permitir múltiples ejecuciones
     * - Listener: JobCompletionListener para monitoreo
     * - Steps: Un único step de procesamiento
     * 
     * El RunIdIncrementer permite ejecutar el mismo job múltiples veces
     * generando un nuevo run.id para cada ejecución.
     * 
     * @return Job configurado y listo para ejecutar
     */
    @Bean
    public Job procesarClientesJob() {
        log.info("Configurando Job: procesarClientesJob");
        
        return new JobBuilder("procesarClientesJob", jobRepository)
                // Incrementador para permitir múltiples ejecuciones del mismo job
                .incrementer(new RunIdIncrementer())
                // Listener para monitorear inicio/fin del job
                .listener(jobCompletionListener)
                // Definir el primer (y único) step
                .start(procesarClientesStep())
                // Construir el job
                .build();
    }

    /**
     * Define el Step de procesamiento de clientes.
     * 
     * Configuración del Step:
     * - Nombre: "procesarClientesStep"
     * - Chunk Size: Configurable (default 100)
     * - Reader: ClienteItemReader (lee de BD)
     * - Processor: ClienteItemProcessor (transforma)
     * - Writer: ClienteItemWriter (escribe a BD)
     * - Listener: StepExecutionListener para estadísticas
     * 
     * ¿Qué es un Chunk?
     * Un chunk es un grupo de registros que se procesan juntos en una
     * transacción. Si el chunk-size es 100, se leen 100 registros,
     * se procesan, y luego se escriben en una sola transacción.
     * 
     * Beneficios del procesamiento por chunks:
     * - Mejor rendimiento por menos commits a BD
     * - Control de memoria (no carga todo en memoria)
     * - Rollback granular en caso de error
     * 
     * @return Step configurado
     */
    @Bean
    public Step procesarClientesStep() {
        log.info("Configurando Step: procesarClientesStep con chunk-size: {}", chunkSize);
        
        return new StepBuilder("procesarClientesStep", jobRepository)
                // Configurar el chunk: <TipoEntrada, TipoSalida>
                // TipoEntrada: Cliente (lo que lee el Reader)
                // TipoSalida: ClienteProcesado (lo que produce el Processor)
                .<Cliente, ClienteProcesado>chunk(chunkSize, transactionManager)
                // Asignar el Reader
                .reader(clienteItemReader)
                // Asignar el Processor
                .processor(clienteItemProcessor)
                // Asignar el Writer
                .writer(clienteItemWriter)
                // Listener para estadísticas del step
                .listener(stepExecutionListener)
                // Construir el step
                .build();
    }
}

