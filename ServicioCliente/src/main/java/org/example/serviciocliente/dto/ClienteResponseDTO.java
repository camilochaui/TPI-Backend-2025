package org.example.serviciocliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Este DTO representa la "respuesta" que damos cuando nos piden un cliente.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {
    private Long idCliente;
    private String nombre;
    private String apellido;
    private Long dni;
    private Long telefono;
    private String mail;
    private String calle;
    private Integer altura;

    // Damos la información completa de la ciudad y provincia
    private CiudadDTO ciudad;
}