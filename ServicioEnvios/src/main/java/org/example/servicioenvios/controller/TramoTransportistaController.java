package org.example.servicioenvios.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.dto.response.TramoResponseDTO;
import org.example.servicioenvios.service.TramoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transportista/tramos")
@Tag(name = "Gestión de Tramos (Transportista)", description = "Endpoints para que el Transportista gestione sus viajes")
@SecurityRequirement(name = "bearerAuth")
// ¡ADVERTENCIA! Esto solo valida el rol, no que el ID del token coincida con el parámetro.
// Esto permite que un transportista suplante a otro.
@PreAuthorize("hasRole('TRANSPORTISTA')")
public class
TramoTransportistaController {

    private final TramoService tramoService;

    @Autowired
    public TramoTransportistaController(TramoService tramoService) {
        this.tramoService = tramoService;
    }

    @Operation(summary = "Obtener tramos de un transportista (¡INSEGURO!)",
            description = "Devuelve tramos para un ID de transportista específico pasado por parámetro. " +
                    "ADVERTENCIA: Un transportista autenticado puede ver tramos de OTRO transportista.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tramos obtenida"),
            @ApiResponse(responseCode = "403", description = "Token inválido o sin el rol requerido")
    })
    @GetMapping("") // Path cambiado de "/mis-tramos"
    public ResponseEntity<List<TramoResponseDTO>> obtenerTramosPorTransportista(
            @AuthenticationPrincipal Jwt jwt
    ) {
        // Extraer el ID del transportista desde el claim del JWT para evitar IDOR
        Integer idTransportista = extractIdFromJwt(jwt, "id_transportista");
        log.info("Transportista autenticado (claim) consultando sus tramos: {}", idTransportista);

        List<TramoResponseDTO> tramos = tramoService.obtenerTramosDelTransportista(idTransportista);
        return ResponseEntity.ok(tramos);
    }


    @Operation(summary = "Iniciar un tramo (¡INSEGURO!)",
            description = "Inicia un tramo para un ID de transportista específico. " +
                    "ADVERTENCIA: Un transportista autenticado puede iniciar tramos de OTRO transportista.")
    @PostMapping("/{idTramo}/inicio")
    public ResponseEntity<TramoResponseDTO> iniciarTramo(
            @PathVariable Long idTramo,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer idTransportista = extractIdFromJwt(jwt, "id_transportista");
        log.info("Transportista {} iniciando tramo {}", idTransportista, idTramo);

        TramoResponseDTO tramoActualizado = tramoService.iniciarTramo(idTramo, idTransportista);

        return ResponseEntity.ok(tramoActualizado);
    }

    @Operation(summary = "Finalizar un tramo (¡INSEGURO!)",
            description = "Finaliza un tramo para un ID de transportista específico. " +
                    "ADVERTENCIA: Un transportista autenticado puede finalizar tramos de OTRO transportista.")
    @PostMapping("/{idTramo}/fin")
        public ResponseEntity<TramoResponseDTO> finalizarTramo(
                        @PathVariable Long idTramo,
                        @AuthenticationPrincipal Jwt jwt
        ) {

                Integer idTransportista = extractIdFromJwt(jwt, "id_transportista");
                log.info("Transportista {} finalizando tramo {}", idTransportista, idTramo);

                TramoResponseDTO tramoActualizado = tramoService.finalizarTramo(idTramo, idTransportista);

                return ResponseEntity.ok(tramoActualizado);
        }

        // Helper para extraer un ID entero desde los claims del JWT
        private Integer extractIdFromJwt(Jwt jwt, String claimName) {
                if (jwt == null) return null;
                Object claim = jwt.getClaim(claimName);
                if (claim instanceof Number) {
                        return ((Number) claim).intValue();
                }
                if (claim instanceof String) {
                        try {
                                return Integer.parseInt((String) claim);
                        } catch (NumberFormatException e) {
                                log.warn("Claim {} no es numérico: {}", claimName, claim);
                        }
                }
                log.warn("No se encontró claim {} en el token", claimName);
                return null;
        }
}