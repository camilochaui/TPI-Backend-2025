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
public class TramoFinResponseDTO {
    private Long idTramo;
    private Integer orden;
    private String estadoTramo;
    private LocalDateTime fechaHoraFinReal;
    private String patenteCamionExt;
}
