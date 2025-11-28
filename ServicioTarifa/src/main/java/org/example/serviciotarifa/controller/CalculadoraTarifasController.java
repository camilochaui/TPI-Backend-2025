package org.example.serviciotarifa.controller;

import org.example.serviciotarifa.dto.CalculoTarifaRequest;
import org.example.serviciotarifa.dto.CalculoTarifaResponse;
import org.example.serviciotarifa.service.CalculadoraTarifasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tarifas")
public class CalculadoraTarifasController {

    private final CalculadoraTarifasService calculadoraTarifasService;

    public CalculadoraTarifasController(CalculadoraTarifasService calculadoraTarifasService) {
        this.calculadoraTarifasService = calculadoraTarifasService;
    }

    @PostMapping("/calcular")
    public ResponseEntity<CalculoTarifaResponse> calcular(@RequestBody CalculoTarifaRequest request) {
        CalculoTarifaResponse resp = calculadoraTarifasService.calcularTarifas(request);
        return ResponseEntity.ok(resp);
    }
}
