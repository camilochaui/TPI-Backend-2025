package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoSeguimientoDTO {
    private String tipo; // e.g. SOLICITUD_CREADA, TRAMO_INICIO_REAL, TRAMO_FINALIZADO, CAMION_ASIGNADO
    private String descripcion; // Texto amigable para mostrar
    private LocalDateTime fecha; // Fecha y hora del evento
}
