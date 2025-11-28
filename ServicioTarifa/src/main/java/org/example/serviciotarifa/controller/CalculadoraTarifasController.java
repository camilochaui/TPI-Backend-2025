package org.example.serviciotarifa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.serviciotarifa.dto.CalculoTarifaRequest;
import org.example.serviciotarifa.dto.CalculoTarifaResponse;
import org.example.serviciotarifa.service.CalculadoraTarifasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "Calculadora de Tarifas", description = "Endpoint alternativo para cálculo de tarifas de envío")
@SecurityRequirement(name = "bearerAuth")
public class CalculadoraTarifasController {

    private final CalculadoraTarifasService calculadoraTarifasService;

    public CalculadoraTarifasController(CalculadoraTarifasService calculadoraTarifasService) {
        this.calculadoraTarifasService = calculadoraTarifasService;
    }

    @Operation(
        summary = "Calcular tarifas de envío (endpoint alternativo)",
        description = "Calcula el costo total de un envío considerando distancia, combustible, estadía, y otros factores. "
                + "Retorna el desglose completo de costos y el total. Endpoint alternativo a /api/v1/tarifas/calculo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cálculo realizado exitosamente",
                content = @Content(schema = @Schema(implementation = CalculoTarifaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/calcular")
    public ResponseEntity<CalculoTarifaResponse> calcularTarifa(
            @Parameter(description = "Datos necesarios para el cálculo de tarifa", required = true)
            @RequestBody CalculoTarifaRequest request) {
        CalculoTarifaResponse response = calculadoraTarifasService.calcularTarifas(request);
        return ResponseEntity.ok(response);
    }
}
