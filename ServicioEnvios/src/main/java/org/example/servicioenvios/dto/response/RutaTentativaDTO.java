package org.example.servicioenvios.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder

public class RutaTentativaDTO {
    private int idRutaTentativa;
    private String descripcion;
    private Double costoTotalEstimado;
    private Double distanciaTotalKm;
    private Integer cantidadTramos;
    private List<TramoResponseDTO> tramos;
}