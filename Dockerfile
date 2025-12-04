# ============================================================
# DOCKERFILE - SPRING BATCH BOOT
# ============================================================
# Multi-stage build para crear una imagen optimizada
# de la aplicación Spring Batch 6.
# Actualizado para Java 25 LTS y Spring Boot 3.4.
# ============================================================

# ============================================
# ETAPA 1: BUILD
# ============================================
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven primero (para cache de dependencias)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación (sin tests)
RUN mvn clean package -DskipTests

# ============================================
# ETAPA 2: RUNTIME
# ============================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR desde etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Variables de entorno para optimización de JVM con Virtual Threads (Java 25 LTS)
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational"

# Comando de ejecución con opciones de JVM optimizadas
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

