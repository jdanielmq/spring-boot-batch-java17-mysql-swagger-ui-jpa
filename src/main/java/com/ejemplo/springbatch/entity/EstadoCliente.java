package com.ejemplo.springbatch.entity;

/**
 * Enumeración que define los posibles estados de un Cliente.
 * 
 * Esta enumeración se utiliza para controlar el ciclo de vida
 * de los clientes en el sistema y durante el procesamiento batch.
 * 
 * Estados disponibles:
 * - PENDIENTE: Cliente recién creado, pendiente de procesamiento
 * - ACTIVO: Cliente procesado y activo en el sistema
 * - INACTIVO: Cliente desactivado o dado de baja
 * - ERROR: Cliente con errores durante el procesamiento
 * 
 * @author Ejemplo
 * @version 1.0.0
 */
public enum EstadoCliente {
    
    /**
     * Estado inicial de un cliente.
     * Indica que el cliente está pendiente de ser procesado por el batch.
     */
    PENDIENTE("Pendiente de procesar"),
    
    /**
     * Estado de cliente activo.
     * El cliente ha sido procesado exitosamente y está activo.
     */
    ACTIVO("Cliente activo"),
    
    /**
     * Estado de cliente inactivo.
     * El cliente ha sido desactivado del sistema.
     */
    INACTIVO("Cliente inactivo"),
    
    /**
     * Estado de error.
     * Hubo un problema durante el procesamiento del cliente.
     */
    ERROR("Error en procesamiento");

    /**
     * Descripción legible del estado.
     */
    private final String descripcion;

    /**
     * Constructor del enumerado.
     * 
     * @param descripcion Descripción del estado
     */
    EstadoCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la descripción del estado.
     * 
     * @return Descripción legible del estado
     */
    public String getDescripcion() {
        return descripcion;
    }
}

