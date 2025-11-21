package org.example.serviciocliente.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para el endpoint: GET /api/seguimiento/{idContenedor} (Req. del Cliente)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoDTO {

    private String idContenedor;
    // Consultar estado actual.
    private String estadoActual;

    // Ver costo y tiempo estimado.
    private Double costoEstimado;
    private String tiempoEstimado;
}