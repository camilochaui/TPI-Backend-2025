package org.example.servicioenvios.feign;

import org.example.servicioenvios.dto.feign.CotizacionRequestDTO;
import org.example.servicioenvios.dto.feign.CotizacionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "servicio-tarifa", url = "${servicio.tarifas.url}", path = "/api/v1/tarifas")
public interface TarifasFeignClient {

    @PostMapping("/calculo")
    CotizacionResponseDTO calcularTarifas(@RequestBody CotizacionRequestDTO request);

    @GetMapping("/combustible/{tipo}")
    Float obtenerPrecioCombustible(@PathVariable String tipo);

    @GetMapping("/contenedor/{volumen}")
    Float obtenerTarifaBaseKm(@PathVariable Float volumen);

    @GetMapping("/estadia/{deposito}")
    Float obtenerCostoEstadia(@PathVariable String deposito);
}