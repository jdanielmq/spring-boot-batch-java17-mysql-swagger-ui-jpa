package com.ejemplo.springbatch.dto;

import com.ejemplo.springbatch.entity.EstadoCliente;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad Cliente.
 * 
 * Este DTO se utiliza para transferir datos de clientes entre las capas
 * de la aplicación, especialmente entre el controlador y el servicio.
 * 
 * Ventajas de usar DTOs:
 * - Separa la representación interna (entidad) de la externa (API)
 * - Permite incluir validaciones específicas para la API
 * - Evita exponer campos sensibles de la entidad
 * - Facilita el versionado de la API
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {

    /**
     * Identificador único del cliente.
     * Solo se incluye en respuestas, no en creación.
     */
    private Long id;

    /**
     * Nombre completo del cliente.
     * Campo obligatorio con validación de longitud.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /**
     * Correo electrónico del cliente.
     * Debe ser único y tener formato válido.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    /**
     * Número de teléfono del cliente.
     * Campo opcional.
     */
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    /**
     * Estado actual del cliente.
     */
    private EstadoCliente estado;

    /**
     * Indica si el cliente ha sido procesado por batch.
     */
    private Boolean procesado;

    /**
     * Fecha de creación del registro.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización.
     */
    private LocalDateTime fechaActualizacion;
}

