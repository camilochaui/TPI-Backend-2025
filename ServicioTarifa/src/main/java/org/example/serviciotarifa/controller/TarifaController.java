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
import org.example.serviciotarifa.entity.Calculo;
import org.example.serviciotarifa.service.CalculadoraTarifasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tarifas")
@Tag(name = "Gestión de Tarifas", description = "Endpoints para consultar y actualizar tarifas de combustible, estadía, y calcular costos de envíos")
@SecurityRequirement(name = "bearerAuth")
public class TarifaController {

    private final CalculadoraTarifasService tarifaService;

    public TarifaController(CalculadoraTarifasService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @Operation(
        summary = "Obtener precio de combustible por tipo",
        description = "Consulta el precio por litro de un tipo específico de combustible (Gasoil, Nafta, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Precio obtenido exitosamente"),
        @ApiResponse(responseCode = "400", description = "Tipo de combustible inválido")
    })
    @GetMapping("/combustible/{tipo}")
    public ResponseEntity<Float> getPrecioCombustible(
            @Parameter(description = "Tipo de combustible (ej: Gasoil, Nafta)", required = true)
            @PathVariable String tipo) {
        try {
            Float precio = tarifaService.obtenerPrecioCombustible(tipo);
            return ResponseEntity.ok(precio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Obtener tarifa base por kilómetro según volumen",
        description = "Calcula la tarifa base por kilómetro recorrido en función del volumen del contenedor/carga"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarifa calculada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Volumen inválido")
    })
    @GetMapping("/contenedor/{volumen}")
    public ResponseEntity<Float> getTarifaBaseKm(
            @Parameter(description = "Volumen del contenedor en m³", required = true, example = "50.0")
            @PathVariable Float volumen) {
        try {
            Float tarifa = tarifaService.obtenerTarifaBaseKm(volumen);
            return ResponseEntity.ok(tarifa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Obtener costo de estadía por depósito",
        description = "Consulta el costo diario de estadía/almacenamiento en un depósito específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Costo de estadía obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado")
    })
    @GetMapping("/estadia/{idDeposito}")
    public ResponseEntity<Float> getCostoEstadia(
            @Parameter(description = "ID del depósito", required = true, example = "1")
            @PathVariable Long idDeposito) {
        try {
            Float costo = tarifaService.obtenerCostoEstadia(idDeposito);
            return ResponseEntity.ok(costo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @Operation(
        summary = "Actualizar tarifa de estadía de un depósito",
        description = "Actualiza el costo diario de estadía/almacenamiento para un depósito específico. Requiere permisos de administrador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarifa actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/estadia/{idDeposito}")
    public ResponseEntity<Void> actualizarTarifaEstadia(
            @Parameter(description = "ID del depósito", required = true, example = "1")
            @PathVariable Long idDeposito,
            @Parameter(description = "Nuevo costo diario de estadía", required = true, example = "150.50")
            @RequestBody Float nuevoCostoDiario) {
        try {
            tarifaService.actualizarCostoEstadia(idDeposito, nuevoCostoDiario);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Actualizar precio de combustible",
        description = "Actualiza el precio por litro de un tipo específico de combustible. Requiere permisos de administrador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Precio actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tipo de combustible no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/combustible/{tipo}")
    public ResponseEntity<Void> actualizarPrecioCombustible(
            @Parameter(description = "Tipo de combustible (ej: Gasoil, Nafta)", required = true)
            @PathVariable String tipo,
            @Parameter(description = "Nuevo precio por litro", required = true, example = "250.75")
            @RequestBody Float nuevoPrecioLitro) {
        try {
            tarifaService.actualizarPrecioCombustible(tipo, nuevoPrecioLitro);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Calcular tarifas de envío",
        description = "Calcula el costo total de un envío considerando distancia, combustible, estadía, y otros factores. "
                + "Retorna el desglose completo de costos y el total."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cálculo realizado exitosamente",
                content = @Content(schema = @Schema(implementation = CalculoTarifaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/calculo")
    public ResponseEntity<CalculoTarifaResponse> calcularTarifas(
            @Parameter(description = "Datos necesarios para el cálculo de tarifa", required = true)
            @RequestBody CalculoTarifaRequest request) {
        try {
            CalculoTarifaResponse response = tarifaService.calcularTarifas(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Listar todos los cálculos realizados",
        description = "Obtiene el historial completo de cálculos de tarifas realizados en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/calculo")
    public ResponseEntity<List<Calculo>> getAllCalculos() {
        try {
            List<Calculo> calculos = tarifaService.obtenerTodosLosCalculos();
            return ResponseEntity.ok(calculos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Obtener cálculo por ID",
        description = "Consulta los detalles de un cálculo de tarifa específico mediante su identificador"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cálculo encontrado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cálculo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/calculo/{id}")
    public ResponseEntity<Calculo> getCalculoById(
            @Parameter(description = "ID del cálculo", required = true, example = "1")
            @PathVariable Integer id) {
        try {
            Calculo calculo = tarifaService.obtenerCalculoPorId(id);
            if (calculo != null) {
                return ResponseEntity.ok(calculo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}