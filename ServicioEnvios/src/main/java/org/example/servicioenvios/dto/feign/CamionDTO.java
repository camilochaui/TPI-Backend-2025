package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// DTO que esperamos recibir de ServicioCamiones

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamionDTO {

    private String patente;
    private boolean disponibilidad;
    private Float capacidadPeso;
    private Float capacidadVolumen;
    private Integer transportistaId;
    private List<String> contenedorIds;
    private Float consumoXKm;
    private Integer idCombustible_ext;
    private Float costoBaseXKm;
}