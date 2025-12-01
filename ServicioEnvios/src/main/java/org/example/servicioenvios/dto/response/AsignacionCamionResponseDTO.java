package org.example.servicioenvios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionCamionResponseDTO {
    private Long idTramo;
    private String estadoTramo;
    private String patenteCamionExt;
}
