package org.example.serviciocliente.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.serviciocliente.dto.feign.SeguimientoDTO;
import org.example.serviciocliente.service.SolicitudClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// :
@Slf4j
@RestController
@RequestMapping("/api/v1/seguimiento")
@Tag(name = "Seguimiento", description = "Endpoints para que el Cliente consulte sus envíos")
@SecurityRequirement(name = "bearerAuth")
public class SeguimientoController {

    private final SolicitudClienteService solicitudClienteService;

    @Autowired
    public SeguimientoController(SolicitudClienteService solicitudClienteService) {
        this.solicitudClienteService = solicitudClienteService;
    }

    @Operation(summary = "Consultar seguimiento de un contenedor", description = "Obtiene la información unificada (estado, costo, tiempo) de un envío. "
            +
            "Requiere rol CLIENTE. Valida que el cliente {idCliente} sea el dueño del token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguimiento encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (no es el dueño)"),
            @ApiResponse(responseCode = "404", description = "Cliente o envío no encontrado"),
            @ApiResponse(responseCode = "503", description = "Servicio de Envíos no disponible")
    })
    @GetMapping("/cliente/{idCliente}/contenedor/{idContenedor}")
    @PreAuthorize("hasRole('CLIENTE') and #idCliente == #jwt.getClaim('id_cliente')")
    public ResponseEntity<SeguimientoDTO> consultarSeguimiento(
            @PathVariable Long idCliente,
            @PathVariable String idContenedor,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Cliente {} consultando seguimiento para contenedor {}", idCliente, idContenedor);

        SeguimientoDTO seguimiento = solicitudClienteService.consultarSeguimiento(idCliente, idContenedor);
        return ResponseEntity.ok(seguimiento);
    }
}