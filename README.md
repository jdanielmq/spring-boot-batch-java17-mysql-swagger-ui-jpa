# Spring Batch Boot - Microservicio de Procesamiento por Lotes

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-5.x-green)

## 📋 Descripción

Este proyecto es un **microservicio de ejemplo** que demuestra cómo implementar **Spring Batch** con **Spring Boot 3** siguiendo una arquitectura empresarial. El servicio procesa clientes de forma masiva aplicando transformaciones de negocio.

### 🎯 Objetivo

Proyecto diseñado para aprender y prepararse para **pruebas técnicas** de Java, demostrando:

- ✅ Arquitectura empresarial (Controllers, Services, Components)
- ✅ Spring Batch con Jobs, Steps, Reader/Processor/Writer
- ✅ Conexión a MySQL con JPA/Hibernate
- ✅ API REST para ejecutar y monitorear Jobs
- ✅ Documentación Swagger/OpenAPI
- ✅ Código completamente documentado en español

## 🏗️ Arquitectura del Proyecto

```
spring-batch-boot/
├── src/main/java/com/ejemplo/springbatch/
│   ├── SpringBatchApplication.java      # Clase principal
│   ├── config/
│   │   └── BatchConfig.java             # Configuración del Job
│   ├── controller/
│   │   ├── JobController.java           # API para ejecutar Jobs
│   │   └── ClienteController.java       # API CRUD de Clientes
│   ├── service/
│   │   ├── JobExecutionService.java     # Lógica de ejecución de Jobs
│   │   └── ClienteService.java          # Lógica de negocio de Clientes
│   ├── batch/
│   │   ├── reader/
│   │   │   └── ClienteItemReader.java   # Lee clientes de BD
│   │   ├── processor/
│   │   │   └── ClienteItemProcessor.java # Transforma clientes
│   │   ├── writer/
│   │   │   └── ClienteItemWriter.java   # Escribe resultados
│   │   └── listener/
│   │       ├── JobCompletionListener.java
│   │       └── StepExecutionListener.java
│   ├── entity/
│   │   ├── Cliente.java                 # Entidad de entrada
│   │   ├── ClienteProcesado.java        # Entidad de salida
│   │   └── EstadoCliente.java           # Enum de estados
│   ├── repository/
│   │   ├── ClienteRepository.java
│   │   └── ClienteProcesadoRepository.java
│   ├── dto/
│   │   ├── ClienteDTO.java
│   │   ├── JobExecutionResponse.java
│   │   └── ApiResponse.java
│   └── exception/
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   └── application.yml                  # Configuración
├── scripts/
│   ├── crear-base-datos.sql            # Script de inicialización
│   └── consultas-utiles.sql            # Queries de monitoreo
├── docs/
│   ├── 01-CONCEPTOS-SPRING-BATCH.md
│   ├── 02-ARQUITECTURA.md
│   ├── 03-GUIA-USO.md
│   └── 04-PREGUNTAS-ENTREVISTA.md
└── pom.xml
```

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Configurar Base de Datos

```bash
# Conectar a MySQL y ejecutar:
mysql -u root -p < scripts/crear-base-datos.sql
```

### 2. Configurar Conexión

Editar `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_batch_db
    username: root
    password: tu_password
```

### 3. Ejecutar la Aplicación

```bash
# Con Maven
mvn spring-boot:run

# O compilar y ejecutar JAR
mvn clean package
java -jar target/spring-batch-boot-1.0.0.jar
```

### 4. Probar la API

```bash
# Crear datos de prueba
curl -X POST "http://localhost:8080/api/clientes/datos-prueba?cantidad=20"

# Ejecutar el Job de procesamiento
curl -X POST "http://localhost:8080/api/jobs/ejecutar"

# Ver estadísticas
curl "http://localhost:8080/api/jobs/estadisticas"
```

## 📖 Documentación de la API

Una vez iniciada la aplicación, accede a:

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v3/api-docs

### Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/jobs/ejecutar` | Ejecuta el Job de procesamiento |
| GET | `/api/jobs/estado/{id}` | Estado de una ejecución |
| GET | `/api/jobs/historial` | Historial de ejecuciones |
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/clientes` | Listar clientes |
| GET | `/api/clientes/pendientes` | Clientes sin procesar |

## 📚 Documentación Detallada

Para entender en profundidad el proyecto, consulta los documentos en la carpeta `docs/`:

1. **[01-CONCEPTOS-SPRING-BATCH.md](docs/01-CONCEPTOS-SPRING-BATCH.md)** - Fundamentos teóricos
2. **[02-ARQUITECTURA.md](docs/02-ARQUITECTURA.md)** - Arquitectura del proyecto
3. **[03-GUIA-USO.md](docs/03-GUIA-USO.md)** - Guía práctica de uso
4. **[04-PREGUNTAS-ENTREVISTA.md](docs/04-PREGUNTAS-ENTREVISTA.md)** - Preguntas frecuentes en entrevistas

## 🔧 Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Spring Boot | 3.2.0 | Framework base |
| Spring Batch | 5.x | Procesamiento por lotes |
| Spring Data JPA | 3.x | Acceso a datos |
| MySQL | 8.0 | Base de datos |
| Lombok | 1.18.x | Reducir boilerplate |
| SpringDoc OpenAPI | 2.3.0 | Documentación API |

## 👨‍💻 Autor

Proyecto de ejemplo para aprendizaje de Spring Batch.

## 📄 Licencia

Este proyecto es de uso educativo y libre para aprendizaje.

