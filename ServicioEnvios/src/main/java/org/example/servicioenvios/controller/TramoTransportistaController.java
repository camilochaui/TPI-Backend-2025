package org.example.servicioenvios.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.dto.response.TramoResponseDTO;
import org.example.servicioenvios.dto.response.TramoInicioResponseDTO;
import org.example.servicioenvios.dto.response.TramoFinResponseDTO;
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
@RequestMapping("/api/v1/transportista")
@Tag(name = "Gestión de Tramos (Transportista)", description = "Endpoints para que el Transportista gestione sus viajes")
@SecurityRequirement(name = "bearerAuth")
// ¡ADVERTENCIA! Esto solo valida el rol, no que el ID del token coincida con el
// parámetro.
// Esto permite que un transportista suplante a otro.
@PreAuthorize("hasRole('TRANSPORTISTA')")
public class TramoTransportistaController {

        private final TramoService tramoService;

        @Autowired
        public TramoTransportistaController(TramoService tramoService) {
                this.tramoService = tramoService;
        }

        @Operation(summary = "Obtener tramos de un transportista autenticado", description = "Devuelve los tramos asociados al transportista dueño del token JWT")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de tramos obtenida"),
                        @ApiResponse(responseCode = "403", description = "Token inválido o sin el rol requerido")
        })
        @GetMapping("/mis_tramos")
        public ResponseEntity<List<TramoResponseDTO>> obtenerTramosPorTransportista(
                        @AuthenticationPrincipal Jwt jwt) {
                // Debug: loguear claims del JWT para verificar qué llega en el token
                if (jwt != null) {
                        log.info("Claims del JWT: {}", jwt.getClaims());
                } else {
                        log.warn("JWT de autenticación es nulo al consultar mis_tramos");
                }

                // Extraer el ID del transportista desde el claim del JWT para evitar IDOR
                Integer idTransportista = extractIdFromJwt(jwt, "id_transportista");
                log.info("Transportista autenticado (claim) consultando sus tramos: {}", idTransportista);

                List<TramoResponseDTO> tramos = tramoService.obtenerTramosDelTransportista(idTransportista);
                return ResponseEntity.ok(tramos);
        }

        @Operation(summary = "Iniciar un tramo propio", description = "El transportista autenticado inicia un tramo asignado a uno de sus camiones.")
        @PostMapping("/tramo/{idTramo}/inicio")
        public ResponseEntity<TramoInicioResponseDTO> iniciarTramo(
                        @PathVariable Long idTramo,
                        @AuthenticationPrincipal Jwt jwt) {

                Integer idTransportista = extractIdFromJwt(jwt, "id_transportista");
                log.info("Transportista {} iniciando tramo {}", idTransportista, idTramo);

                TramoInicioResponseDTO tramoActualizado = tramoService.iniciarTramo(idTramo, idTransportista);
                return ResponseEntity.ok(tramoActualizado);
        }

        @Operation(summary = "Finalizar un tramo propio", description = "El transportista autenticado finaliza un tramo asignado a uno de sus camiones.")
        @PostMapping("/tramo/{idTramo}/fin")
        public ResponseEntity<TramoFinResponseDTO> finalizarTramo(
                        @PathVariable Long idTramo,
                        @AuthenticationPrincipal Jwt jwt) {

                Integer idTransportista = extractIdFromJwt(jwt, "id_transportista");
                log.info("Transportista {} finalizando tramo {}", idTransportista, idTramo);

                TramoFinResponseDTO tramoActualizado = tramoService.finalizarTramo(idTramo, idTransportista);
                return ResponseEntity.ok(tramoActualizado);
        }

        // Helper para extraer un ID entero desde los claims del JWT
        private Integer extractIdFromJwt(Jwt jwt, String claimName) {
                if (jwt == null)
                        return null;
                // Intentar extraer el claim solicitado
                Object claim = jwt.getClaim(claimName);
                // Si no está con el nombre en snake_case, intentar camelCase (id_transportista -> idTransportista)
                if (claim == null && claimName.contains("_")) {
                        String[] parts = claimName.split("_");
                        StringBuilder sb = new StringBuilder(parts[0]);
                        for (int i = 1; i < parts.length; i++) {
                                if (parts[i].length() > 0) {
                                        sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
                                }
                        }
                        String camel = sb.toString();
                        log.debug("Claim '{}' no encontrado; probando '{}' en el JWT", claimName, camel);
                        claim = jwt.getClaim(camel);
                }
                // Intentar variantes comunes si sigue sin encontrarse
                if (claim == null) {
                        log.debug("Claim '{}' no encontrado; probando 'id' y 'sub' como alternativas", claimName);
                        claim = jwt.getClaim("id");
                }
                if (claim == null) {
                        claim = jwt.getClaim("sub");
                }
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