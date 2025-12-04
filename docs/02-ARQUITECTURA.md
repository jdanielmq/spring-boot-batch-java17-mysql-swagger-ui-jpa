# 🏛️ Arquitectura del Proyecto

## Visión General

Este proyecto implementa una **arquitectura empresarial por capas**, combinando Spring Batch para procesamiento por lotes con una API REST para control y monitoreo.

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                          │
│  ┌─────────────────────┐  ┌─────────────────────┐               │
│  │   JobController     │  │  ClienteController  │               │
│  │   /api/jobs/*       │  │  /api/clientes/*    │               │
│  └─────────────────────┘  └─────────────────────┘               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE SERVICIOS                             │
│  ┌─────────────────────┐  ┌─────────────────────┐               │
│  │ JobExecutionService │  │   ClienteService    │               │
│  │ (Ejecuta Jobs)      │  │   (CRUD Clientes)   │               │
│  └─────────────────────┘  └─────────────────────┘               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE BATCH                                 │
│  ┌────────────┐  ┌────────────────┐  ┌────────────┐             │
│  │   Reader   │→ │   Processor    │→ │   Writer   │             │
│  │ (Lee BD)   │  │ (Transforma)   │  │ (Guarda)   │             │
│  └────────────┘  └────────────────┘  └────────────┘             │
│                                                                  │
│  ┌─────────────────────────────────────────────────┐            │
│  │              Listeners (Monitoreo)               │            │
│  └─────────────────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE DATOS                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐               │
│  │ ClienteRepository   │  │ClienteProcesadoRepo │               │
│  └─────────────────────┘  └─────────────────────┘               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BASE DE DATOS MySQL                           │
│  ┌───────────┐  ┌────────────────────┐  ┌───────────────────┐   │
│  │ clientes  │  │ clientes_procesados│  │ BATCH_* (metadata)│   │
│  └───────────┘  └────────────────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## 📁 Estructura de Paquetes

```
com.ejemplo.springbatch/
│
├── SpringBatchApplication.java    # Punto de entrada
│
├── config/                        # Configuraciones
│   └── BatchConfig.java          # Define Job y Steps
│
├── controller/                    # Controladores REST
│   ├── JobController.java        # Endpoints de Jobs
│   └── ClienteController.java    # Endpoints de Clientes
│
├── service/                       # Lógica de negocio
│   ├── JobExecutionService.java  # Gestión de ejecuciones
│   └── ClienteService.java       # Operaciones de clientes
│
├── batch/                         # Componentes de Batch
│   ├── reader/
│   │   └── ClienteItemReader.java
│   ├── processor/
│   │   └── ClienteItemProcessor.java
│   ├── writer/
│   │   └── ClienteItemWriter.java
│   └── listener/
│       ├── JobCompletionListener.java
│       └── StepExecutionListener.java
│
├── entity/                        # Entidades JPA
│   ├── Cliente.java
│   ├── ClienteProcesado.java
│   └── EstadoCliente.java
│
├── repository/                    # Repositorios JPA
│   ├── ClienteRepository.java
│   └── ClienteProcesadoRepository.java
│
├── dto/                           # Objetos de transferencia
│   ├── ClienteDTO.java
│   ├── JobExecutionResponse.java
│   ├── JobStatusResponse.java
│   └── ApiResponse.java
│
└── exception/                     # Manejo de errores
    └── GlobalExceptionHandler.java
```

## 🔄 Flujo de Datos del Batch

### Entrada de Datos

```
┌─────────────────────────────────────────────────────────────┐
│                    TABLA: clientes                           │
├─────┬──────────────────┬────────────────────┬──────────────┤
│ ID  │ Nombre           │ Email              │ Procesado    │
├─────┼──────────────────┼────────────────────┼──────────────┤
│ 1   │ Juan Pérez       │ juan@ejemplo.com   │ FALSE        │
│ 2   │ María López      │ maria@ejemplo.com  │ FALSE        │
│ 3   │ Carlos Sánchez   │ carlos@ejemplo.com │ FALSE        │
└─────┴──────────────────┴────────────────────┴──────────────┘
```

### Transformaciones del Processor

```
ENTRADA (Cliente):
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "JUAN@ejemplo.COM",
  "estado": "PENDIENTE"
}

        ↓ Transformaciones ↓

1. Nombre a MAYÚSCULAS
2. Email a minúsculas
3. Generar código único (CLI-XXXXXXXX)
4. Determinar estado final

        ↓

SALIDA (ClienteProcesado):
{
  "clienteId": 1,
  "nombreProcesado": "JUAN PÉREZ",
  "emailProcesado": "juan@ejemplo.com",
  "codigoCliente": "CLI-A1B2C3D4",
  "estadoFinal": "ACTIVO"
}
```

### Salida de Datos

```
┌─────────────────────────────────────────────────────────────────────┐
│                    TABLA: clientes_procesados                        │
├─────┬───────────┬────────────────┬─────────────┬──────────┬─────────┤
│ ID  │ ClienteID │ NombreProcesado│ Código      │ Estado   │ JobExec │
├─────┼───────────┼────────────────┼─────────────┼──────────┼─────────┤
│ 1   │ 1         │ JUAN PÉREZ     │ CLI-A1B2C3D4│ ACTIVO   │ 1       │
│ 2   │ 2         │ MARÍA LÓPEZ    │ CLI-E5F6G7H8│ ACTIVO   │ 1       │
│ 3   │ 3         │ CARLOS SÁNCHEZ │ CLI-I9J0K1L2│ ACTIVO   │ 1       │
└─────┴───────────┴────────────────┴─────────────┴──────────┴─────────┘
```

## 📊 Modelo de Datos

### Entidad Cliente (Entrada)

```java
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
    private String telefono;
    
    @Enumerated(EnumType.STRING)
    private EstadoCliente estado;
    
    private Boolean procesado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
```

### Entidad ClienteProcesado (Salida)

```java
@Entity
@Table(name = "clientes_procesados")
public class ClienteProcesado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long clienteId;
    private String nombreProcesado;
    private String emailProcesado;
    private String codigoCliente;
    
    @Enumerated(EnumType.STRING)
    private EstadoCliente estadoFinal;
    
    private Long jobExecutionId;
    private LocalDateTime fechaProcesamiento;
    private String mensaje;
}
```

## 🎛️ Configuración del Job

```java
@Configuration
public class BatchConfig {

    @Bean
    public Job procesarClientesJob() {
        return new JobBuilder("procesarClientesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionListener)
                .start(procesarClientesStep())
                .build();
    }

    @Bean
    public Step procesarClientesStep() {
        return new StepBuilder("procesarClientesStep", jobRepository)
                .<Cliente, ClienteProcesado>chunk(100, transactionManager)
                .reader(clienteItemReader)
                .processor(clienteItemProcessor)
                .writer(clienteItemWriter)
                .listener(stepExecutionListener)
                .build();
    }
}
```

## 🔐 Patrón de Diseño: Responsabilidad Única

Cada componente tiene una única responsabilidad:

| Componente | Responsabilidad |
|------------|-----------------|
| Reader | Solo leer datos de la fuente |
| Processor | Solo transformar/validar datos |
| Writer | Solo persistir datos procesados |
| Listener | Solo monitorear eventos |
| Service | Solo lógica de negocio |
| Controller | Solo exponer endpoints |

## 🔗 Siguiente: [03-GUIA-USO.md](03-GUIA-USO.md)

