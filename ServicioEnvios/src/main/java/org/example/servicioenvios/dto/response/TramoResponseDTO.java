package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO que representa un Tramo en las respuestas de la API.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoResponseDTO {
    private Long idTramo;
    private Integer orden;
    private UbicacionResponseDTO origen;
    private UbicacionResponseDTO destino;
    private String estadoTramo;
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
    private String patenteCamionExt;
    private Double distanciaKmEstimada;
    private Double costoEstimado;
    private Double costoReal;
    private Double costoEstadiaDeposito;
}