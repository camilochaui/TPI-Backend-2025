package org.example.servicioenvios.feign;

//import org.example.servicioenvios.config.FeignClientConfig;
import org.example.servicioenvios.dto.feign.CamionDTO;
import org.example.servicioenvios.dto.feign.DepositoDTO;
import org.example.servicioenvios.dto.feign.TransportistaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.servicioenvios.dto.feign.contenedorRequestDTO;
import org.example.servicioenvios.dto.feign.ContenedorResponseDTO;
import java.util.List;
import java.util.Map;

// Cliente Feign para comunicarse con el Servicio de Flota

@FeignClient(
        name = "servicio-flota",
        path = "/api/v1/flota"
        // configuration = FeignClientConfig.class
)
public interface FlotaFeignClient {

    // CAMIONES
    @GetMapping("/camiones/{patente}")
    CamionDTO obtenerCamionPorPatente(@PathVariable("patente") String patente);

    @GetMapping("/camiones/disponibles")
    List<CamionDTO> buscarCamionesDisponibles(
            @RequestParam("pesoMin") Double peso,
            @RequestParam("volumenMin") Double volumen
    );

    @PostMapping("/camiones/{patente}/camion-asignado")
    ResponseEntity<Map<String, String>> asignarCamion(@PathVariable("patente") String patente);

    @PostMapping("/camiones/{patente}/vincular-contenedor/{idContenedor}")
    ResponseEntity<Map<String, String>> vincularContenedorACamion(
            @PathVariable("patente") String patente,
            @PathVariable("idContenedor") String idContenedor);

    @PostMapping("/camiones/{patente}/camion-libre")
    ResponseEntity<Map<String, String>> liberarCamion(@PathVariable("patente") String patente);


    // DEPOSITOS
    @GetMapping("/depositos")
    List<DepositoDTO> listarDepositos();

    @GetMapping("/depositos/{id}")
    DepositoDTO obtenerDepositoPorId(@PathVariable("id") Long id);


    // TRANSPORTISTAS
    @GetMapping("/transportistas/{id}")
    TransportistaDTO obtenerTransportistaPorId(@PathVariable("id") Integer id);

    @PostMapping("/transportistas/{id}/transportista-asignado")
    ResponseEntity<Map<String, String>> asignarTransportista(@PathVariable("id") Integer id);

    @PostMapping("/transportistas/{id}/transportista-libre")
    ResponseEntity<Map<String, String>> liberarTransportista(@PathVariable("id") Integer id);


    // CONTENEDORES
    @PostMapping("/contenedores")
    ContenedorResponseDTO crearContenedor(
            @RequestBody contenedorRequestDTO dto
    );
}