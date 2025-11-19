package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaResponseDTO {
    private Long numSolicitud;
    private String idContenedor;
    private String origen;
    private String destino;
    private Double distanciaKm;
    private String tiempoEstimado;
    private String mensaje;
}