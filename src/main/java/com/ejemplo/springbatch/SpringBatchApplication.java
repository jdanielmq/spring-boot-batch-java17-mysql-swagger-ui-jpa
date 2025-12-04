package com.ejemplo.springbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Spring Batch.
 * 
 * Esta clase es el punto de entrada del microservicio y configura
 * automáticamente todos los componentes de Spring Boot.
 * 
 * Anotaciones utilizadas:
 * - @SpringBootApplication: Combina @Configuration, @EnableAutoConfiguration
 *   y @ComponentScan para configurar automáticamente la aplicación.
 * - @EnableScheduling: Habilita la ejecución de tareas programadas (opcional).
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class SpringBatchApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     * 
     * Este método arranca el contenedor de Spring, configura todos los beans
     * y expone los endpoints REST para la ejecución de jobs de batch.
     * 
     * @param args Argumentos de línea de comandos (opcional)
     */
    public static void main(String[] args) {
        // Inicia la aplicación Spring Boot
        SpringApplication.run(SpringBatchApplication.class, args);
        
        // Mensaje de bienvenida en consola
        System.out.println("========================================");
        System.out.println("  SPRING BATCH BOOT - MICROSERVICIO");
        System.out.println("  Servidor iniciado en puerto 8080");
        System.out.println("  Swagger UI: http://localhost:8080/api/swagger-ui.html");
        System.out.println("========================================");
    }
}

