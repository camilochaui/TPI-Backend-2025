package org.example.serviciotarifa.controller;

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
public class TarifaController {

    private final CalculadoraTarifasService tarifaService;

    public TarifaController(CalculadoraTarifasService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping("/combustible/{tipo}")
    public ResponseEntity<Float> getPrecioCombustible(@PathVariable String tipo) {
        try {
            Float precio = tarifaService.obtenerPrecioCombustible(tipo);
            return ResponseEntity.ok(precio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/contenedor/{volumen}")
    public ResponseEntity<Float> getTarifaBaseKm(@PathVariable Float volumen) {
        try {
            Float tarifa = tarifaService.obtenerTarifaBaseKm(volumen);
            return ResponseEntity.ok(tarifa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/estadia/{idDeposito}")
    public ResponseEntity<Float> getCostoEstadia(@PathVariable Long idDeposito) {
        try {
            Float costo = tarifaService.obtenerCostoEstadia(idDeposito);
            return ResponseEntity.ok(costo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    // Registrar y actualizar depósitos y tarifas.
    @PutMapping("/estadia/{idDeposito}")
    public ResponseEntity<Void> actualizarTarifaEstadia(
            @PathVariable Long idDeposito,
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

    @PutMapping("/combustible/{tipo}")
    public ResponseEntity<Void> actualizarPrecioCombustible(
            @PathVariable String tipo,
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

    @PostMapping("/calculo")
    public ResponseEntity<CalculoTarifaResponse> calcularTarifas(@RequestBody CalculoTarifaRequest request) {
        try {
            CalculoTarifaResponse response = tarifaService.calcularTarifas(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calculo")
    public ResponseEntity<List<Calculo>> getAllCalculos() {
        try {
            List<Calculo> calculos = tarifaService.obtenerTodosLosCalculos();
            return ResponseEntity.ok(calculos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calculo/{id}")
    public ResponseEntity<Calculo> getCalculoById(@PathVariable Integer id) {
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