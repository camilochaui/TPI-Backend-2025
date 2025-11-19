package org.example.servicioenvios.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UbicacionTentativaDTO {
    private String direccion;
    private String tipoUbicacion;
}
