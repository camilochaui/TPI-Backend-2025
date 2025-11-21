package org.example.servicioenvios.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.dto.response.RutaTentativaDTO;
import org.example.servicioenvios.dto.response.SolicitudResponseDTO;
import org.example.servicioenvios.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Asociado con los:
//  Consultar rutas tentativas con todos los tramos sugeridos y el tiempo y costo estimados. (Operador / Administrador)
//  Seleccionar una ruta tentativa para asignarla a la solicitud. (Administrador)

@Slf4j
@RestController
@RequestMapping("/api/v1/rutas")
@Tag(name = "Gestión de Rutas (Admin)", description = "Endpoints para consultar y asignar rutas a solicitudes")
public class RutaController {

        private final RutaService rutaService;

        @Autowired
        public RutaController(RutaService rutaService) {
                this.rutaService = rutaService;
        }

        @Operation(summary = "Consultar rutas tentativas para una solicitud")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rutas tentativas generadas"),
                        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
        })
        @GetMapping("/tentativas/{numSolicitud}")
        public ResponseEntity<List<RutaTentativaDTO>> consultarRutasTentativas(
                        @PathVariable Long numSolicitud) {
                log.info("Consultando rutas tentativas para solicitud {}", numSolicitud);
                List<RutaTentativaDTO> rutas = rutaService.consultarRutasTentativas(numSolicitud);
                return ResponseEntity.ok(rutas);
        }

        @Operation(summary = "Asignar una ruta a una solicitud")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ruta asignada exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos")
        })
        @PostMapping("/asignacion/{numSolicitud}")
        public ResponseEntity<SolicitudResponseDTO> seleccionarRuta(
                        @PathVariable Long numSolicitud,
                        @RequestBody RutaTentativaDTO rutaSeleccionada) {

                log.info("Asignando ruta '{}' a solicitud {}",
                                rutaSeleccionada.getDescripcion(), numSolicitud);

                SolicitudResponseDTO solicitudActualizada = rutaService.seleccionarRuta(numSolicitud, rutaSeleccionada);

                return ResponseEntity.ok(solicitudActualizada);
        }

}
