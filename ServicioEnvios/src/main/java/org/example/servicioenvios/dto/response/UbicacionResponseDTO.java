package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que representa una Ubicación en las respuestas de la API.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionResponseDTO {
    private Long idUbicacion;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private String tipoUbicacion;
}