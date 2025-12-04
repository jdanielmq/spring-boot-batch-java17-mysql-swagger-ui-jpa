package com.ejemplo.springbatch.exception;

import com.ejemplo.springbatch.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST.
 * 
 * Esta clase intercepta las excepciones lanzadas por los controladores
 * y las convierte en respuestas HTTP apropiadas con formato consistente.
 * 
 * Excepciones manejadas:
 * - MethodArgumentNotValidException: Errores de validación
 * - IllegalArgumentException: Argumentos inválidos
 * - Exception: Cualquier otra excepción no controlada
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación de argumentos.
     * Se activa cuando falla la validación de @Valid en el controlador.
     * 
     * @param ex Excepción de validación
     * @return ResponseEntity con detalles de los campos inválidos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        log.warn("Error de validación: {}", ex.getMessage());

        // Construir mapa de errores por campo
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        return ResponseEntity.badRequest()
            .body(ApiResponse.<Map<String, String>>builder()
                .exitoso(false)
                .mensaje("Error de validación en los datos enviados")
                .datos(errores)
                .codigoError("VALIDATION_ERROR")
                .build());
    }

    /**
     * Maneja excepciones de argumentos inválidos.
     * Se activa cuando se lanza IllegalArgumentException desde servicios.
     * 
     * @param ex Excepción de argumento ilegal
     * @return ResponseEntity con mensaje de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        log.warn("Argumento inválido: {}", ex.getMessage());

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage(), "INVALID_ARGUMENT"));
    }

    /**
     * Maneja cualquier otra excepción no controlada.
     * 
     * @param ex Excepción genérica
     * @return ResponseEntity con error 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(
                "Error interno del servidor. Contacte al administrador.",
                "INTERNAL_ERROR"
            ));
    }
}

