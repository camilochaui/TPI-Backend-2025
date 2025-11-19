package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que representa la respuesta del servicio Cliente Interno

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteInternoResponseDTO {
    private Long idCliente;
    private String nombre;
    private Long dni;

}