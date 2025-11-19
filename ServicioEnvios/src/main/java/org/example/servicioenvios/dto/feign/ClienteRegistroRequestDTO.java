package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que representa la solicitud de registro de un cliente al servicio Cliente Interno
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRegistroRequestDTO {

    private String nombre;
    private String apellido;
    private Long dni;
    private Long telefono;
    private String mail;
    private String calle;
    private Integer altura;
    private Long idCiudad;
}