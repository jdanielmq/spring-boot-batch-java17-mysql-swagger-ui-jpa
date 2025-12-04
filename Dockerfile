# ============================================================
# DOCKERFILE - SPRING BATCH BOOT
# ============================================================
# Multi-stage build para crear una imagen optimizada
# de la aplicación Spring Batch.
# ============================================================

# ============================================
# ETAPA 1: BUILD
# ============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

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
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR desde etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "app.jar"]

