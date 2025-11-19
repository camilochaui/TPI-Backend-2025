package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO que representa una Ruta en las respuestas de la API.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaResponseDTO {
    private Long idRuta;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private List<TramoResponseDTO> tramos; // Lista de tramos anidada
}