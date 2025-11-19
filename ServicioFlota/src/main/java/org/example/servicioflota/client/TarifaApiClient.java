package org.example.servicioflota.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "servicio-tarifa")
public interface TarifaApiClient {

    @GetMapping("/api/tarifas/combustible/{tipo}")
    Float getPrecioCombustible(@PathVariable("tipo") String tipo);

    @GetMapping("/api/tarifas/contenedor/{volumen}")
    Float getTarifaBaseKm(@PathVariable("volumen") Float volumen);

    @GetMapping("/api/tarifas/estadia/{deposito}")
    Float getCostoEstadia(@PathVariable("deposito") String deposito);
}
