package com.ejemplo.springbatch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO genérico para respuestas de la API REST.
 * 
 * Proporciona una estructura consistente para todas las respuestas,
 * incluyendo información sobre el éxito/fallo, mensajes y datos.
 * 
 * Uso típico:
 * - Respuestas exitosas con datos
 * - Respuestas de error con mensajes
 * - Respuestas de operaciones sin datos de retorno
 * 
 * @param <T> Tipo de datos contenidos en la respuesta
 * @author Ejemplo
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indica si la operación fue exitosa.
     */
    private boolean exitoso;

    /**
     * Mensaje descriptivo de la operación.
     */
    private String mensaje;

    /**
     * Datos de la respuesta (puede ser null).
     */
    private T datos;

    /**
     * Marca de tiempo de la respuesta.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Código de error (solo en caso de fallo).
     */
    private String codigoError;

    // ============================================
    // MÉTODOS ESTÁTICOS DE FÁBRICA
    // ============================================

    /**
     * Crea una respuesta exitosa con datos.
     * 
     * @param datos Datos a incluir en la respuesta
     * @param mensaje Mensaje descriptivo
     * @param <T> Tipo de los datos
     * @return ApiResponse configurada
     */
    public static <T> ApiResponse<T> exito(T datos, String mensaje) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }

    /**
     * Crea una respuesta exitosa sin datos.
     * 
     * @param mensaje Mensaje descriptivo
     * @param <T> Tipo de los datos
     * @return ApiResponse configurada
     */
    public static <T> ApiResponse<T> exito(String mensaje) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje(mensaje)
                .build();
    }

    /**
     * Crea una respuesta de error.
     * 
     * @param mensaje Mensaje de error
     * @param codigoError Código identificador del error
     * @param <T> Tipo de los datos
     * @return ApiResponse configurada
     */
    public static <T> ApiResponse<T> error(String mensaje, String codigoError) {
        return ApiResponse.<T>builder()
                .exitoso(false)
                .mensaje(mensaje)
                .codigoError(codigoError)
                .build();
    }

    /**
     * Crea una respuesta de error simple.
     * 
     * @param mensaje Mensaje de error
     * @param <T> Tipo de los datos
     * @return ApiResponse configurada
     */
    public static <T> ApiResponse<T> error(String mensaje) {
        return ApiResponse.<T>builder()
                .exitoso(false)
                .mensaje(mensaje)
                .build();
    }
}

