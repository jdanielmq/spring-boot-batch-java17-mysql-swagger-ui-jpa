package com.ejemplo.springbatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Cliente después de ser procesado por el batch.
 * 
 * Esta tabla almacena el resultado del procesamiento de cada cliente,
 * incluyendo transformaciones aplicadas y el estado final.
 * 
 * Se utiliza como destino del ItemWriter en el job de Spring Batch,
 * permitiendo tener un historial de procesamiento separado de la
 * tabla original de clientes.
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
@Entity
@Table(name = "clientes_procesados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteProcesado {

    /**
     * Identificador único del registro procesado.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Referencia al ID del cliente original.
     */
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    /**
     * Nombre del cliente en mayúsculas (transformación aplicada).
     */
    @Column(name = "nombre_procesado", nullable = false, length = 100)
    private String nombreProcesado;

    /**
     * Email del cliente normalizado (transformación aplicada).
     */
    @Column(name = "email_procesado", nullable = false, length = 150)
    private String emailProcesado;

    /**
     * Código único generado durante el procesamiento.
     * Formato: CLI-XXXXXX
     */
    @Column(name = "codigo_cliente", unique = true, length = 20)
    private String codigoCliente;

    /**
     * Estado resultante después del procesamiento.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_final", nullable = false, length = 20)
    private EstadoCliente estadoFinal;

    /**
     * Identificador de la ejecución del job que procesó este registro.
     */
    @Column(name = "job_execution_id")
    private Long jobExecutionId;

    /**
     * Fecha y hora del procesamiento.
     */
    @Column(name = "fecha_procesamiento", nullable = false)
    private LocalDateTime fechaProcesamiento;

    /**
     * Mensaje o notas del procesamiento.
     */
    @Column(name = "mensaje", length = 500)
    private String mensaje;

    /**
     * Establece la fecha de procesamiento antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        if (this.fechaProcesamiento == null) {
            this.fechaProcesamiento = LocalDateTime.now();
        }
    }
}

