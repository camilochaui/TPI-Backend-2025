package org.example.servicioflota.controller;

import jakarta.persistence.EntityNotFoundException;
import org.example.servicioflota.dto.CamionDTO;
import org.example.servicioflota.model.Camion;
import org.example.servicioflota.model.Contenedor;
import org.example.servicioflota.service.CamionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/flota/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @PostMapping
    public ResponseEntity<?> createCamion(@RequestBody CamionDTO camionDTO) {
        try {
            Camion nuevoCamion = camionService.saveCamion(camionDTO);
            return new ResponseEntity<>(convertToDto(nuevoCamion), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Error de validación de capacidad
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            // Contenedor o transportista no encontrado
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Cualquier otro error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear el camión: " + e.getMessage()));
        }
    }

    @PutMapping("/{patente}")
    public ResponseEntity<?> updateCamion(@PathVariable String patente, @RequestBody CamionDTO camionDTO) {
        try {
            Camion camionActualizado = camionService.updateCamion(patente, camionDTO);
            return ResponseEntity.ok(convertToDto(camionActualizado));
        } catch (IllegalArgumentException e) {
            // Error de validación de capacidad
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            // Camión, contenedor o transportista no encontrado
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Cualquier otro error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el camión: " + e.getMessage()));
        }
    }

    @GetMapping
    public List<CamionDTO> getAllCamiones(@RequestParam(required = false) Boolean disponible,
                                          @RequestParam(required = false) Float minPeso,
                                          @RequestParam(required = false) Float minVolumen) {
        return camionService.getAllCamiones(disponible, minPeso, minVolumen).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{patente}")
    public ResponseEntity<CamionDTO> getCamionByPatente(@PathVariable String patente) {
        return camionService.getCamionById(patente)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ENDPOINTS PARA ASIGNAR Y LIBERAR CAMIONES

    @PostMapping("/{patente}/camion-asignado")
    public ResponseEntity<Map<String, String>> asignarCamion(@PathVariable String patente) {
        try {
            camionService.asignarCamion(patente);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("patente", patente, "estadoCamion", "OCUPADO"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{patente}/vincular-contenedor/{idContenedor}")
    public ResponseEntity<Map<String, String>> vincularContenedorACamion(
            @PathVariable String patente,
            @PathVariable String idContenedor) {
        try {
            // Llama a un nuevo método de servicio
            camionService.vincularContenedor(patente, idContenedor);
            return ResponseEntity.ok(Map.of(
                    "message", "Contenedor " + idContenedor + " vinculado a camión " + patente
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{patente}/camion-libre")
    public ResponseEntity<Map<String, String>> liberarCamion(@PathVariable String patente) {
        try {
            camionService.liberarCamion(patente);
            return ResponseEntity.ok(Map.of("patente", patente, "estadoCamion", "LIBRE"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private CamionDTO convertToDto(Camion camion) {
        CamionDTO dto = new CamionDTO();
        dto.setPatente(camion.getPatente());
        dto.setCapacidadPeso(camion.getCapacidadPeso());
        dto.setCapacidadVolumen(camion.getCapacidadVolumen());
        dto.setDisponibilidad(camion.isDisponibilidad());
        if (camion.getTransportista() != null) {
            dto.setTransportistaId(camion.getTransportista().getIdTransportista());
        }
        if (camion.getContenedores() != null) {
            dto.setContenedorIds(camion.getContenedores().stream()
                    .map(Contenedor::getIdContenedor)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
