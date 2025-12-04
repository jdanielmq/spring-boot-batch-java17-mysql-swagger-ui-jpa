package com.ejemplo.springbatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Cliente en el sistema.
 * 
 * Esta clase mapea la tabla 'clientes' en la base de datos MySQL.
 * Se utiliza como ejemplo para demostrar el procesamiento por lotes
 * con Spring Batch.
 * 
 * Campos principales:
 * - id: Identificador único del cliente
 * - nombre: Nombre completo del cliente
 * - email: Correo electrónico único
 * - telefono: Número de teléfono
 * - estado: Estado del cliente (ACTIVO, INACTIVO, PENDIENTE)
 * - fechaCreacion: Fecha de creación del registro
 * - fechaActualizacion: Fecha de última actualización
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    /**
     * Identificador único del cliente.
     * Se genera automáticamente usando la estrategia IDENTITY de MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Nombre completo del cliente.
     * Campo obligatorio con longitud máxima de 100 caracteres.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Correo electrónico del cliente.
     * Debe ser único en la base de datos y tener formato válido.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Número de teléfono del cliente.
     * Campo opcional con longitud máxima de 20 caracteres.
     */
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Estado actual del cliente en el sistema.
     * Valores posibles: ACTIVO, INACTIVO, PENDIENTE
     */
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCliente estado;

    /**
     * Indica si el cliente ha sido procesado por el batch.
     * Se utiliza para controlar qué registros ya fueron procesados.
     */
    @Column(name = "procesado")
    @Builder.Default
    private Boolean procesado = false;

    /**
     * Fecha y hora de creación del registro.
     * Se asigna automáticamente al persistir por primera vez.
     */
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última actualización.
     * Se actualiza automáticamente en cada modificación.
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Método de callback que se ejecuta antes de persistir.
     * Establece la fecha de creación y actualización inicial.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoCliente.PENDIENTE;
        }
        if (this.procesado == null) {
            this.procesado = false;
        }
    }

    /**
     * Método de callback que se ejecuta antes de actualizar.
     * Actualiza la fecha de última modificación.
     */
    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

