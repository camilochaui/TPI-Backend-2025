package org.example.servicioenvios.feign;

import org.example.servicioenvios.dto.feign.ClienteInternoResponseDTO;
import org.example.servicioenvios.dto.feign.ClienteRegistroRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Cliente Feign para comunicarse con ServicioCliente
@FeignClient(
    name = "servicio-cliente",
    url = "${servicio.cliente.url}",
    path = "/api/v1/clientes"
    //configuration = FeignClientConfig.class
)
public interface ClienteFeignClient {

    // Llama al endpoint de ServicioCliente que valida si el cliente existe y, si no, lo crea.
    @PostMapping("/solicitud")
    ClienteInternoResponseDTO registrarOObtenerCliente(@RequestBody ClienteRegistroRequestDTO dto);

    @GetMapping("/{idCliente}")
    ClienteInternoResponseDTO obtenerClientePorId(@PathVariable("idCliente") Long idCliente);
}

