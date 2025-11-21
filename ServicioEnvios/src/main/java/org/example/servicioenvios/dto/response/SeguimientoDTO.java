package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que representa la información de seguimiento de un contenedor
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoDTO {
    private String idContenedor;

    // Consultar estado actual
    private String estadoActual;

    // Ver costo y tiempo estimado
    private Double costoEstimado;
    private String tiempoEstimado;
}