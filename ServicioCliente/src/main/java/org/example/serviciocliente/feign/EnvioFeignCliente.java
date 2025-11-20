package org.example.serviciocliente.feign;

import org.example.serviciocliente.config.FeignClientConfig;
import org.example.serviciocliente.dto.feign.SeguimientoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Cliente Feign para comunicarse con ServicioEnvios.

@FeignClient(
    name = "servicio-envios",
    url = "${servicio.envios.url}",
    path = "/api/v1/envios",
    configuration = FeignClientConfig.class
)
public interface EnvioFeignCliente {

    @GetMapping("/{idContenedor}/seguimiento")
    SeguimientoDTO obtenerSeguimiento(@PathVariable("idContenedor") String idContenedor);

}