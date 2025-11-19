package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para recibir la respuesta del Contenedor creado
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorResponseDTO {
    private String idContenedor;
    private Integer peso;
    private Integer volumen;
    private Integer idClienteExt;
    private String estadoActual;
}