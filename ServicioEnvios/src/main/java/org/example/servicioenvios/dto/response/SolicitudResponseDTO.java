package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.servicioenvios.dto.feign.ClienteInternoResponseDTO;

import java.time.LocalDateTime;

// DTO que representa una Solicitud en las respuestas de la API.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResponseDTO {
    private Long numSolicitud;
    private String idContenedorExt;
    private ClienteInternoResponseDTO cliente;

    private Double peso;
    private Double volumen;

    private String estadoSolicitud;
    private LocalDateTime fechaCreacion;

    private Double costoEstimado;
    private String tiempoEstimado;
    private Double costoReal;
    private String tiempoReal;

    private RutaResponseDTO ruta;
}