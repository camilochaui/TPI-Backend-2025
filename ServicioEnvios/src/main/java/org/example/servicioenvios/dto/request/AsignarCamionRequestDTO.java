package org.example.servicioenvios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para asignar un camión a un envío
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarCamionRequestDTO {
    @NotBlank(message = "La patente del camión es obligatoria")
    private String patenteCamion;
}