# 🎯 Preguntas Frecuentes en Entrevistas sobre Spring Batch 6

## Preguntas Básicas

### 1. ¿Qué es Spring Batch y para qué se utiliza?

**Respuesta:**
> Spring Batch es un framework para procesamiento de datos por lotes (batch processing). Se utiliza para procesar grandes volúmenes de datos de forma eficiente, como:
> - Migración de datos entre sistemas
> - ETL (Extract, Transform, Load)
> - Generación de reportes masivos
> - Procesamiento nocturno de transacciones

### 2. ¿Cuáles son los componentes principales de Spring Batch?

**Respuesta:**
> - **Job**: La unidad principal de trabajo, contiene uno o más Steps
> - **Step**: Una fase del Job con su propia lógica
> - **ItemReader**: Lee datos de una fuente
> - **ItemProcessor**: Transforma los datos
> - **ItemWriter**: Escribe los datos procesados
> - **JobRepository**: Almacena metadatos de ejecución
> - **JobLauncher**: Inicia la ejecución de Jobs

### 3. ¿Qué es un Chunk en Spring Batch?

**Respuesta:**
> Un Chunk es un grupo de elementos que se procesan juntos en una sola transacción. Por ejemplo, con chunk-size=100:
> 1. Se leen 100 elementos
> 2. Se procesan los 100 elementos
> 3. Se escriben los 100 elementos
> 4. Se hace COMMIT
>
> **Ventajas:**
> - Mejor rendimiento (menos commits)
> - Control de memoria
> - Rollback granular si hay errores

## Preguntas Intermedias

### 4. ¿Cómo manejas errores en Spring Batch?

**Respuesta:**
> Hay varias estrategias:
>
> 1. **Skip Policy**: Saltar registros con error
> ```java
> .faultTolerant()
> .skip(Exception.class)
> .skipLimit(10)
> ```
>
> 2. **Retry Policy**: Reintentar en caso de error temporal
> ```java
> .faultTolerant()
> .retry(TransientException.class)
> .retryLimit(3)
> ```
>
> 3. **Listeners**: Capturar errores y registrarlos
> ```java
> .listener(new SkipListener<>() {...})
> ```

### 5. ¿Qué es JobRepository y para qué sirve?

**Respuesta:**
> JobRepository es el componente que almacena información sobre:
> - Instancias de Jobs ejecutados
> - Estado de cada ejecución (COMPLETED, FAILED, etc.)
> - Parámetros de ejecución
> - Estadísticas de Steps (registros leídos, escritos, filtrados)
>
> Se almacena en tablas como:
> - BATCH_JOB_INSTANCE
> - BATCH_JOB_EXECUTION
> - BATCH_STEP_EXECUTION

### 6. ¿Cómo se previene que un Job se ejecute dos veces con los mismos parámetros?

**Respuesta:**
> Spring Batch identifica una instancia de Job única por:
> - Nombre del Job
> - JobParameters
>
> Si intentas ejecutar el mismo Job con los mismos parámetros y ya existe una ejecución COMPLETED, se lanzará una excepción.
>
> Para permitir múltiples ejecuciones, usar RunIdIncrementer:
> ```java
> new JobBuilder("miJob", jobRepository)
>     .incrementer(new RunIdIncrementer())
> ```

### 7. ¿Cuál es la diferencia entre Chunk-oriented y Tasklet?

**Respuesta:**
>
> **Chunk-oriented:**
> - Para procesar grandes volúmenes de datos
> - Usa Reader → Processor → Writer
> - Procesa en lotes (chunks)
> - Transacciones automáticas
>
> **Tasklet:**
> - Para tareas simples (limpiar archivos, enviar email)
> - Un solo método execute()
> - Sin procesamiento de items
>
> ```java
> // Tasklet
> @Bean
> public Step limpiarDirectorioStep() {
>     return new StepBuilder("limpiarDirectorio", jobRepository)
>         .tasklet((contribution, chunkContext) -> {
>             // Limpiar archivos
>             return RepeatStatus.FINISHED;
>         }, transactionManager)
>         .build();
> }
> ```

## Preguntas Avanzadas

### 8. ¿Cómo implementarías un Job con múltiples Steps que dependan entre sí?

**Respuesta:**
> Usando flujos condicionales:
>
> ```java
> @Bean
> public Job jobComplejo() {
>     return new JobBuilder("jobComplejo", jobRepository)
>         .start(step1())
>             .on("COMPLETED").to(step2())
>             .on("FAILED").to(stepError())
>         .from(step2())
>             .on("*").to(step3())
>         .end()
>         .build();
> }
> ```

### 9. ¿Cómo escalarías un Job de Spring Batch?

**Respuesta:**
> Hay varias técnicas:
>
> 1. **Multi-threaded Step**: Procesar chunks en paralelo
> ```java
> .taskExecutor(new SimpleAsyncTaskExecutor())
> .throttleLimit(4)
> ```
>
> 2. **Partitioning**: Dividir datos en particiones
> ```java
> .partitioner("step", partitioner)
> .gridSize(4)
> ```
>
> 3. **Remote Chunking**: Distribuir procesamiento entre nodos
>
> 4. **Parallel Steps**: Ejecutar Steps en paralelo
> ```java
> Flow flow1 = new FlowBuilder<>("flow1").start(step1()).build();
> Flow flow2 = new FlowBuilder<>("flow2").start(step2()).build();
> 
> .start(flow1)
> .split(taskExecutor)
> .add(flow2)
> ```

### 10. ¿Qué es un ItemStreamReader y cuándo lo usarías?

**Respuesta:**
> ItemStreamReader extiende ItemReader agregando:
> - `open()`: Inicializar recursos (conexiones, archivos)
> - `update()`: Guardar estado para restart
> - `close()`: Liberar recursos
>
> Se usa cuando necesitas:
> - Manejar recursos que requieren apertura/cierre
> - Soportar restart desde el punto de fallo
>
> Ejemplo: JdbcCursorItemReader, FlatFileItemReader

### 11. ¿Cómo reiniciarías un Job fallido desde donde se quedó?

**Respuesta:**
> Spring Batch soporta restart automático:
>
> 1. El Job debe ser restartable (por defecto lo es)
> 2. Usar ExecutionContext para guardar estado
> 3. El JobRepository guarda el último chunk procesado
>
> ```java
> // Configurar Job como restartable
> new JobBuilder("miJob", jobRepository)
>     .preventRestart() // Para deshabilitar restart
> ```
>
> Al ejecutar el mismo Job con los mismos parámetros después de un FAILED, continúa desde el último commit.

## Preguntas de Código

### 12. Escribe un ItemProcessor que filtre registros inválidos

```java
@Component
public class ValidacionProcessor implements ItemProcessor<Cliente, Cliente> {
    
    @Override
    public Cliente process(Cliente cliente) throws Exception {
        // Filtrar clientes sin email (retornar null)
        if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
            return null; // Se filtra, no pasa al Writer
        }
        
        // Validar formato de email
        if (!cliente.getEmail().contains("@")) {
            return null;
        }
        
        // Cliente válido, pasa al Writer
        return cliente;
    }
}
```

### 13. ¿Cómo pasarías datos entre Steps?

**Respuesta:**
```java
// En Step 1 - Guardar en ExecutionContext
@Override
public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
    context.getStepContext()
           .getStepExecution()
           .getJobExecution()
           .getExecutionContext()
           .put("totalRegistros", 100);
    return RepeatStatus.FINISHED;
}

// En Step 2 - Leer del ExecutionContext
@BeforeStep
public void beforeStep(StepExecution stepExecution) {
    int total = stepExecution.getJobExecution()
                            .getExecutionContext()
                            .getInt("totalRegistros");
}
```

## Consejos para la Entrevista

1. **Conoce el flujo completo**: Job → Step → Reader → Processor → Writer
2. **Entiende los estados**: COMPLETED, FAILED, STARTED, STOPPED
3. **Practica con el proyecto**: Ejecuta el job, revisa logs, modifica código
4. **Menciona transacciones**: Spring Batch maneja transacciones por chunk
5. **Habla de escalabilidad**: Menciona multi-threading, partitioning

## Preguntas sobre Spring Batch 6

### 14. ¿Qué novedades trae Spring Batch 6?

**Respuesta:**
> Spring Batch 6 introduce varias mejoras importantes:
>
> 1. **Requisito mínimo Java 17**: Compatible con versiones más recientes como Java 25
> 2. **Nuevo ChunkOrientedStepBuilder**: Configuración más fluida
> 3. **Método recover()**: Para recuperar jobs fallidos abruptamente
> 4. **APIs simplificadas**: Eliminación de métodos deprecados
> 5. **Mejor rendimiento**: Procesamiento de chunks optimizado
>
> ```java
> // Nuevo estilo con ChunkOrientedStepBuilder
> return new ChunkOrientedStepBuilder<Input, Output>(
>         "stepName", jobRepository, transactionManager, chunkSize)
>     .reader(reader)
>     .processor(processor)
>     .writer(writer)
>     .build();
> ```

### 15. ¿Cómo recuperas un Job que falló abruptamente en Spring Batch 6?

**Respuesta:**
> Spring Batch 6 introduce el método `recover()` en `JobOperator`:
>
> ```java
> @Autowired
> private JobOperator jobOperator;
>
> public void recuperarJob(Long executionId) throws Exception {
>     // Recupera la ejecución marcándola como fallida
>     // y permitiendo su reinicio
>     jobOperator.recover(executionId);
> }
> ```
>
> Esto es útil cuando un job termina de forma inesperada (crash del servidor,
> kill del proceso) y queda en estado STARTED o STOPPING.

### 16. ¿Qué versión de Java requiere Spring Batch 6?

**Respuesta:**
> Spring Batch 6 requiere **Java 17 como mínimo**, pero se recomienda usar
> **Java 25 (LTS)** que es la versión más reciente con soporte a largo plazo. 
> Esto permite aprovechar las nuevas características del lenguaje como:
> - Records y Pattern Matching
> - Sealed Classes
> - Virtual Threads (Java 21+)
> - Nuevos garbage collectors (ZGC Generational)
> - Mejoras de rendimiento de Java 25

## Recursos Adicionales

- [Documentación Oficial de Spring Batch 6](https://docs.spring.io/spring-batch/reference/)
- [Guía de Migración a Spring Batch 6](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide)
- [Spring Batch - Baeldung Tutorials](https://www.baeldung.com/spring-batch)
- [Spring Batch GitHub](https://github.com/spring-projects/spring-batch)

