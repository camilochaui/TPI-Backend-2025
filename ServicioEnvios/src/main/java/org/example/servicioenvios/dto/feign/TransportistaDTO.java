package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO que representa la información del transportista obtenida vía Feign

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportistaDTO {
    private Integer idTransportista;
    private String nombre;
    private String apellido;
    private String dni;
    private boolean disponibilidad;
    // La lista de patentes es clave para la validación de seguridad
    private List<String> camionesPatentes;
}