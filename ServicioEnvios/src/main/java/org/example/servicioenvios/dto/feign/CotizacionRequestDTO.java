package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// DTO que representa el request para obtener una cotizacion del servicio de tarifas
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class CotizacionRequestDTO {
    private Integer idSolicitud;
    private Float consumoCamionLitroKm;
    private String tipoCombustible;
    private Float distanciaTotalKm;
    private Float volumenContenedor;
    private List<EstadiaDTO> estadias;
    private Float tarifaGestion;
    private Integer cantidadTramos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadiaDTO {
        private Long idDeposito;
        private String fechaEntrada;
        private String fechaSalida;
    }
}


