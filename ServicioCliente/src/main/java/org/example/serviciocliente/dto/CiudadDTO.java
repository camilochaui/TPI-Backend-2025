package org.example.serviciocliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiudadDTO {
    private Long idCiudad;
    private String nombre;
    private ProvinciaDTO provincia;
}