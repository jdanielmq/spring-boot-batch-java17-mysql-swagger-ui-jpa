# 📘 Conceptos Fundamentales de Spring Batch 6

## ¿Qué es Spring Batch?

**Spring Batch 6** es la versión más reciente del framework de código abierto para el procesamiento de grandes volúmenes de datos por lotes (batch processing). Esta versión requiere **Java 17+** (recomendado Java 25 LTS) y viene integrada con **Spring Boot 3.4**. Es ideal para:

- Procesar millones de registros de forma eficiente
- ETL (Extract, Transform, Load)
- Migración de datos entre sistemas
- Generación de reportes masivos
- Procesamiento nocturno de transacciones

## 🏗️ Arquitectura de Spring Batch

```
┌─────────────────────────────────────────────────────────────────┐
│                         JOB                                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                       STEP 1                               │  │
│  │   ┌─────────┐    ┌───────────┐    ┌─────────┐            │  │
│  │   │ Reader  │ →  │ Processor │ →  │ Writer  │            │  │
│  │   └─────────┘    └───────────┘    └─────────┘            │  │
│  │                                                           │  │
│  │   [Item] → [Item] → [Item] ... → [Chunk] → [BD]          │  │
│  └───────────────────────────────────────────────────────────┘  │
│                            ↓                                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                       STEP 2 (opcional)                    │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 📦 Componentes Principales

### 1. Job (Trabajo)

El **Job** es la unidad principal de procesamiento. Representa una tarea completa de batch.

```java
@Bean
public Job procesarClientesJob() {
    return new JobBuilder("procesarClientesJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobCompletionListener)
            .start(procesarClientesStep())
            .build();
}
```

**Características:**
- Tiene un nombre único
- Puede tener múltiples Steps
- Tiene un estado de ejecución (COMPLETED, FAILED, etc.)
- Puede recibir parámetros (JobParameters)

### 2. Step (Paso)

Un **Step** es una fase independiente del Job. Puede ser:

- **Chunk-oriented**: Lee, procesa y escribe en lotes
- **Tasklet**: Ejecuta una tarea simple

```java
@Bean
public Step procesarClientesStep() {
    return new StepBuilder("procesarClientesStep", jobRepository)
            .<Cliente, ClienteProcesado>chunk(100, transactionManager)
            .reader(clienteItemReader)
            .processor(clienteItemProcessor)
            .writer(clienteItemWriter)
            .build();
}
```

### 3. ItemReader (Lector)

El **ItemReader** lee datos de una fuente, un elemento a la vez.

```java
@Component
public class ClienteItemReader implements ItemReader<Cliente> {
    
    @Override
    public Cliente read() throws Exception {
        // Retorna el siguiente elemento o null si no hay más
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null; // Señal de fin de datos
    }
}
```

**Fuentes de datos comunes:**
- Base de datos (JdbcCursorItemReader)
- Archivos CSV/XML (FlatFileItemReader)
- APIs REST
- Colas de mensajes

### 4. ItemProcessor (Procesador)

El **ItemProcessor** transforma cada elemento leído.

```java
@Component
public class ClienteItemProcessor implements ItemProcessor<Cliente, ClienteProcesado> {
    
    @Override
    public ClienteProcesado process(Cliente cliente) throws Exception {
        // Transformar el cliente
        return ClienteProcesado.builder()
                .nombreProcesado(cliente.getNombre().toUpperCase())
                .emailProcesado(cliente.getEmail().toLowerCase())
                .build();
    }
}
```

**Importante:**
- Retornar `null` filtra el elemento (no pasa al Writer)
- Puede lanzar excepciones para manejo de errores

### 5. ItemWriter (Escritor)

El **ItemWriter** persiste los elementos procesados en lotes (chunks).

```java
@Component
public class ClienteItemWriter implements ItemWriter<ClienteProcesado> {
    
    @Override
    public void write(Chunk<? extends ClienteProcesado> chunk) throws Exception {
        // Recibe un chunk completo para mejor rendimiento
        for (ClienteProcesado item : chunk) {
            repository.save(item);
        }
    }
}
```

## 🔄 Flujo de Ejecución

```
1. JobLauncher.run(job, parameters)
       ↓
2. Job comienza (beforeJob)
       ↓
3. Step 1 comienza (beforeStep)
       ↓
4. LOOP:
   ├── Reader.read() → Item1, Item2, ... ItemN (chunk-size)
   ├── Processor.process(Item1) → ProcessedItem1
   ├── Processor.process(Item2) → ProcessedItem2
   ├── ...
   ├── Writer.write([ProcessedItem1, ProcessedItem2, ...])
   └── COMMIT transacción
       (repetir hasta read() retorne null)
       ↓
5. Step 1 termina (afterStep)
       ↓
6. Job termina (afterJob)
```

## 📊 Procesamiento por Chunks

El **chunk-size** define cuántos elementos se procesan en una transacción:

```yaml
batch:
  config:
    chunk-size: 100
```

**Ventajas:**
- ✅ Mejor rendimiento (menos commits a BD)
- ✅ Control de memoria (no carga todo en RAM)
- ✅ Rollback granular (solo el chunk falla)

**Ejemplo con chunk-size=100:**
```
Lectura:    [1][2][3]...[100]  →  Commit
Lectura:    [101][102]...[200] →  Commit
Lectura:    [201][202]...[300] →  Commit
...
```

## 🔒 Metadatos de Spring Batch

Spring Batch almacena información de ejecuciones en tablas de metadatos:

| Tabla | Propósito |
|-------|-----------|
| BATCH_JOB_INSTANCE | Instancias únicas de jobs |
| BATCH_JOB_EXECUTION | Cada ejecución de un job |
| BATCH_JOB_EXECUTION_PARAMS | Parámetros de ejecución |
| BATCH_STEP_EXECUTION | Ejecuciones de steps |
| BATCH_STEP_EXECUTION_CONTEXT | Contexto de steps |

**Consulta útil:**
```sql
SELECT * FROM BATCH_JOB_EXECUTION 
ORDER BY JOB_EXECUTION_ID DESC 
LIMIT 10;
```

## 🎯 Estados de un Job

| Estado | Descripción |
|--------|-------------|
| STARTING | Job está iniciando |
| STARTED | Job en ejecución |
| STOPPING | Job deteniéndose |
| STOPPED | Job detenido manualmente |
| COMPLETED | Job terminó exitosamente |
| FAILED | Job terminó con error |
| ABANDONED | Job abandonado |
| UNKNOWN | Estado desconocido |

## 🔧 JobParameters

Los **JobParameters** permiten pasar valores al Job:

```java
JobParameters params = new JobParametersBuilder()
        .addLong("tiempo", System.currentTimeMillis())
        .addString("usuario", "admin")
        .addLocalDate("fecha", LocalDate.now())
        .toJobParameters();

jobLauncher.run(job, params);
```

**Importante:**
- Los parámetros identifican una ejecución única
- El mismo Job con los mismos parámetros no puede ejecutarse dos veces
- Usar `RunIdIncrementer` para permitir re-ejecuciones

## 🆕 Novedades en Spring Batch 6

### Principales Cambios

| Característica | Descripción |
|---------------|-------------|
| **Java 17+ requerido** | Mínimo Java 17, recomendado Java 25 LTS |
| **Nuevo ChunkOrientedStepBuilder** | Configuración más clara y fluida de Steps |
| **Método recover()** | Recuperación de Jobs fallidos abruptamente |
| **APIs simplificadas** | Eliminación de métodos deprecados |
| **Mejor rendimiento** | Procesamiento de chunks optimizado |
| **Virtual Threads** | Soporte para hilos virtuales de Java 21+ |

### Ejemplo con ChunkOrientedStepBuilder

```java
@Bean
public Step chunkOrientedStep(JobRepository jobRepository, 
        PlatformTransactionManager transactionManager,
        ItemReader<Cliente> itemReader, 
        ItemProcessor<Cliente, ClienteProcesado> itemProcessor, 
        ItemWriter<ClienteProcesado> itemWriter) {
    
    return new ChunkOrientedStepBuilder<Cliente, ClienteProcesado>(
            "procesarClientesStep", jobRepository, transactionManager, 100)
        .reader(itemReader)
        .processor(itemProcessor)
        .writer(itemWriter)
        .build();
}
```

### Recuperación de Jobs Fallidos

```java
@Autowired
private JobOperator jobOperator;

// Recuperar ejecución fallida abruptamente
public void recuperarJobFallido(Long executionId) throws Exception {
    jobOperator.recover(executionId);
}
```

## 📚 Recursos Adicionales

- [Documentación Oficial de Spring Batch 6](https://docs.spring.io/spring-batch/reference/)
- [Guía de Migración a Spring Batch 6](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide)
- [Spring Batch - Baeldung Tutorials](https://www.baeldung.com/spring-batch)
- [Spring Batch GitHub](https://github.com/spring-projects/spring-batch)

## 🔗 Siguiente: [02-ARQUITECTURA.md](02-ARQUITECTURA.md)

