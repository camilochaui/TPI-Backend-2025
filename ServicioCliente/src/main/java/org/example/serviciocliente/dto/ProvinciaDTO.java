package org.example.serviciocliente.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinciaDTO {
    private Long idProvincia;
    private String nombre;
}
