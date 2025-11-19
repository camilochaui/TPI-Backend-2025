package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;


// DTO que representa la respuesta del servicio de Tarifa.

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionResponseDTO {
    private Integer idCalculo;
    private Integer idSolicitud;
    private Float consumoPromedioGeneral;
    private Double costoTotal;
    private Map<String, Object> details;

    // Constructor adicional para compatibilidad
    public CotizacionResponseDTO(Double costoTotal, String mensaje) {
        this.costoTotal = costoTotal;
        this.details = Map.of("mensaje", mensaje);
    }
}


