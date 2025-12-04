# 📖 Guía de Uso del Proyecto

## 🚀 Configuración Inicial

### 1. Requisitos Previos

Asegúrate de tener instalado:
- Java 25 o superior (LTS)
- Maven 3.9 o superior
- MySQL 8.0 o superior

Verificar instalaciones:
```bash
java -version    # Debe mostrar Java 25+
mvn -version     # Debe mostrar Maven 3.9+
mysql --version  # Debe mostrar MySQL 8.0+
```

### 2. Configurar Base de Datos

```bash
# Conectar a MySQL como root
mysql -u root -p

# Ejecutar el script de creación
source /ruta/al/proyecto/scripts/crear-base-datos.sql
```

El script crea:
- Base de datos: `spring_batch_db`
- Tabla: `clientes`
- Tabla: `clientes_procesados`
- 10 clientes de prueba

### 3. Configurar Conexión

Editar `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_batch_db
    username: root
    password: TU_PASSWORD_AQUI
```

### 4. Ejecutar la Aplicación

```bash
# Opción 1: Con Maven
mvn spring-boot:run

# Opción 2: Compilar JAR y ejecutar
mvn clean package -DskipTests
java -jar target/spring-batch-boot-1.0.0.jar
```

## 🧪 Pruebas con Postman

### Colección de Endpoints

#### 1. Crear Datos de Prueba

```http
POST http://localhost:8080/api/clientes/datos-prueba?cantidad=20
```

**Respuesta:**
```json
{
    "exitoso": true,
    "mensaje": "Se crearon 20 clientes de prueba",
    "datos": 20,
    "timestamp": "2024-01-15T10:30:00"
}
```

#### 2. Ver Clientes Pendientes

```http
GET http://localhost:8080/api/clientes/pendientes
```

**Respuesta:**
```json
{
    "exitoso": true,
    "mensaje": "Se encontraron 30 clientes pendientes",
    "datos": [
        {
            "id": 1,
            "nombre": "Juan Pérez García",
            "email": "juan.perez@ejemplo.com",
            "estado": "PENDIENTE",
            "procesado": false
        },
        ...
    ]
}
```

#### 3. Ejecutar el Job de Procesamiento

```http
POST http://localhost:8080/api/jobs/ejecutar
```

**Respuesta:**
```json
{
    "exitoso": true,
    "mensaje": "Job ejecutado correctamente",
    "datos": {
        "exitoso": true,
        "executionId": 1,
        "jobName": "procesarClientesJob",
        "estado": "COMPLETED",
        "exitStatus": "COMPLETED",
        "fechaInicio": "2024-01-15T10:35:00",
        "fechaFin": "2024-01-15T10:35:05",
        "registrosLeidos": 30,
        "registrosEscritos": 30,
        "registrosFiltrados": 0
    }
}
```

#### 4. Ver Estado de una Ejecución

```http
GET http://localhost:8080/api/jobs/estado/1
```

#### 5. Ver Historial de Ejecuciones

```http
GET http://localhost:8080/api/jobs/historial?limite=5
```

#### 6. Ver Estadísticas del Job

```http
GET http://localhost:8080/api/jobs/estadisticas
```

**Respuesta:**
```json
{
    "exitoso": true,
    "datos": {
        "totalInstancias": 5,
        "ejecucionesCompletadas": 4,
        "ejecucionesFallidas": 1,
        "ejecucionesEnCurso": 0,
        "totalRegistrosProcesados": 150
    }
}
```

### CRUD de Clientes

#### Crear Cliente

```http
POST http://localhost:8080/api/clientes
Content-Type: application/json

{
    "nombre": "Nuevo Cliente",
    "email": "nuevo@cliente.com",
    "telefono": "555-1234"
}
```

#### Listar Clientes (paginado)

```http
GET http://localhost:8080/api/clientes?pagina=0&tamanio=10
```

#### Obtener Cliente por ID

```http
GET http://localhost:8080/api/clientes/1
```

#### Actualizar Cliente

```http
PUT http://localhost:8080/api/clientes/1
Content-Type: application/json

{
    "nombre": "Nombre Actualizado",
    "email": "actualizado@cliente.com",
    "telefono": "555-5678"
}
```

#### Eliminar Cliente

```http
DELETE http://localhost:8080/api/clientes/1
```

## 📊 Swagger UI

Accede a la documentación interactiva:

**URL:** http://localhost:8080/api/swagger-ui.html

![Swagger UI](https://via.placeholder.com/800x400?text=Swagger+UI)

Desde Swagger puedes:
- Ver todos los endpoints disponibles
- Probar endpoints directamente
- Ver esquemas de request/response
- Descargar especificación OpenAPI

## 🔄 Flujo de Trabajo Típico

### Escenario: Procesamiento de Nuevos Clientes

```
1. Crear clientes de prueba
   POST /api/clientes/datos-prueba?cantidad=50

2. Verificar clientes pendientes
   GET /api/clientes/pendientes
   → Debe mostrar 50 clientes

3. Ejecutar el job de procesamiento
   POST /api/jobs/ejecutar
   → Procesa los 50 clientes

4. Verificar resultados
   GET /api/clientes/estadisticas
   → Clientes pendientes: 0
   → Registros procesados: 50

5. Ver historial de ejecución
   GET /api/jobs/historial
```

### Escenario: Reprocesamiento

```
1. Reiniciar estado de procesamiento
   POST /api/clientes/reiniciar-procesamiento
   → Todos los clientes marcados como no procesados

2. Ejecutar job nuevamente
   POST /api/jobs/ejecutar
```

## 🔍 Monitoreo en Consola

Al ejecutar el job, verás logs detallados:

```
========================================
  SPRING BATCH BOOT - MICROSERVICIO
  Servidor iniciado en puerto 8080
  Swagger UI: http://localhost:8080/api/swagger-ui.html
========================================

╔════════════════════════════════════════════════════════════╗
║             INICIO DE EJECUCIÓN DEL JOB                    ║
╠════════════════════════════════════════════════════════════╣
║ Job Name: procesarClientesJob
║ Job ID: 1
║ Execution ID: 1
╚════════════════════════════════════════════════════════════╝

┌────────────────────────────────────────────────────────────┐
│ INICIANDO STEP: procesarClientesStep
└────────────────────────────────────────────────────────────┘

========================================
INICIANDO LECTURA DE CLIENTES
========================================
Total de clientes pendientes encontrados: 30

----------------------------------------
PROCESANDO CLIENTE ID: 1
----------------------------------------
Nombre original: 'Juan Pérez' -> Procesado: 'JUAN PÉREZ'
Email original: 'juan@ejemplo.com' -> Procesado: 'juan@ejemplo.com'
Código de cliente generado: CLI-A1B2C3D4

╔════════════════════════════════════════════════════════════╗
║               FIN DE EJECUCIÓN DEL JOB                     ║
╠════════════════════════════════════════════════════════════╣
║ Estado Final: COMPLETED
║ Duración: 5 segundos
║ ✓ JOB COMPLETADO EXITOSAMENTE
║   - Leídos: 30
║   - Procesados: 30
║   - Filtrados: 0
╚════════════════════════════════════════════════════════════╝
```

## ⚠️ Solución de Problemas

### Error: "Access denied for user 'root'"

```
Causa: Password incorrecto o usuario sin permisos
Solución: Verificar credenciales en application.yml
```

### Error: "Communications link failure"

```
Causa: MySQL no está corriendo
Solución: Iniciar MySQL
  - macOS: brew services start mysql
  - Linux: sudo systemctl start mysql
  - Windows: net start mysql
```

### Error: "Job instance already exists"

```
Causa: Intentando ejecutar el mismo job con los mismos parámetros
Solución: El proyecto usa RunIdIncrementer, no debería ocurrir.
          Si persiste, reiniciar tablas de metadatos de Spring Batch.
```

## 🔗 Siguiente: [04-PREGUNTAS-ENTREVISTA.md](04-PREGUNTAS-ENTREVISTA.md)

