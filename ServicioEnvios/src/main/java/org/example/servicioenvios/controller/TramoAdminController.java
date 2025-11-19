package org.example.servicioenvios.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.dto.request.AsignarCamionRequestDTO;
import org.example.servicioenvios.dto.response.TramoResponseDTO;
import org.example.servicioenvios.service.TramoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controlador para la gestión de tramos por parte del Admin
// Asociado con el REQ 6) Asignar camión a un tramo de traslado

@Slf4j
@RestController
@RequestMapping("/api/v1/tramos")
@Tag(name = "Gestión de Tramos (Admin)", description = "Endpoints para que el Admin gestione los tramos")
public class TramoAdminController {

    private final TramoService tramoService;

    @Autowired
    public TramoAdminController(TramoService tramoService) {
        this.tramoService = tramoService;
    }

    @Operation(summary = "Asignar un camión a un tramo", description = "Valida y asigna un camión (por patente) a un tramo específico. " + "Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Camión asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Tramo o Camión no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto (Camión no disponible o no cumple requisitos)"),
            @ApiResponse(responseCode = "503", description = "ServicioFlota no disponible")
    })
    @PostMapping("/{idTramo}/camion-asignacion")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TramoResponseDTO> asignarCamion(
            @PathVariable Long idTramo,
            @Valid @RequestBody AsignarCamionRequestDTO requestDTO
    ) {
        log.info("Recibida solicitud de Admin para asignar camión {} al tramo {}",
                requestDTO.getPatenteCamion(), idTramo);

        TramoResponseDTO tramoActualizado = tramoService.asignarCamionATramo(idTramo, requestDTO);

        return ResponseEntity.ok(tramoActualizado);
    }
}