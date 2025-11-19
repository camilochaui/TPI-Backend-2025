package org.example.servicioenvios.dto.response;

import lombok.Builder;
import lombok.Data;

// DTO que representa un Tramo Tentativo en las respuestas de la API.
@Data
@Builder
public class TramoTentativoDTO {
    private Integer orden;
    private UbicacionResponseDTO origen;
    private UbicacionResponseDTO destino;
    private Double distanciaKmEstimada;
    private Double costoEstimado;
    private Double costoEstadiaDeposito;
}