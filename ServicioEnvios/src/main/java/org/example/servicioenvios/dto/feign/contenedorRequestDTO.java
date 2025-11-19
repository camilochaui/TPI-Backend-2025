package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que representa el request para crear un contenedor en el servicio de Flota

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class contenedorRequestDTO {
    private String idContenedor;
    private Integer peso;
    private Integer volumen;
    private Integer idClienteExt;
}

// Nota: El DTO original de Envios usa Double para peso/volumen.
