package org.example.servicioenvios.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaSugerenciaRequestDTO {

    @NotEmpty(message = "Debe proveer una lista de IDs de depósitos (puede estar vacía para viaje directo)")
    private List<Long> idsDepositosIntermedios;
}