package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoResponseDTO {
    private Double costoTotal;
    private Double detalleCostoGestion;
    private Double detalleCostoKilometro;
    private Double detalleCostoCombustible;
    private Double detalleCostoEstadia;
}