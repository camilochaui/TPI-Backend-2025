package org.example.servicioenvios.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO que representa la respuesta del servicio Depositos
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositoDTO {
    private Long idDeposito;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    // Este costo viene del DER de ServicioTarifas (idDeposito_ext)
    private Double costoEstadiaDiario;
}