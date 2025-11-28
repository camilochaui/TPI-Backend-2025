package org.example.servicioflota.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Gestión de Camiones", description = "Endpoints para administrar camiones, asignación de contenedores y control de disponibilidad")
@SecurityRequirement(name = "bearerAuth")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @Autowired
    private org.example.servicioflota.repository.ContenedorRepository contenedorRepository;

    @Operation(
        summary = "Registrar nuevo camión",
        description = "Crea un nuevo camión con sus características, transportista asignado y contenedores iniciales. Valida que los contenedores no excedan la capacidad del camión."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Camión creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Validación fallida: contenedores exceden capacidad"),
        @ApiResponse(responseCode = "404", description = "Transportista o contenedor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createCamion(
            @Parameter(description = "Datos del camión a crear", required = true)
            @RequestBody CamionDTO camionDTO) {
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

    @Operation(
        summary = "Actualizar camión",
        description = "Modifica los datos de un camión existente. Permite reasignar contenedores validando que no excedan la capacidad."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Validación fallida: contenedores exceden capacidad"),
        @ApiResponse(responseCode = "404", description = "Camión, transportista o contenedor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{patente}")
    public ResponseEntity<?> updateCamion(
            @Parameter(description = "Patente del camión", required = true, example = "ABC123")
            @PathVariable String patente,
            @Parameter(description = "Datos actualizados del camión", required = true)
            @RequestBody CamionDTO camionDTO) {
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

    @Operation(
        summary = "Listar camiones",
        description = "Obtiene todos los camiones con filtros opcionales por disponibilidad, capacidad mínima de peso y volumen"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public List<CamionDTO> getAllCamiones(
            @Parameter(description = "Filtrar por disponibilidad", example = "true")
            @RequestParam(required = false) Boolean disponible,
            @Parameter(description = "Capacidad mínima de peso en kg", example = "5000.0")
            @RequestParam(required = false) Float minPeso,
            @Parameter(description = "Capacidad mínima de volumen en m³", example = "30.0")
            @RequestParam(required = false) Float minVolumen) {
        return camionService.getAllCamiones(disponible, minPeso, minVolumen).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Operation(
        summary = "Obtener camión por patente",
        description = "Consulta los detalles de un camión específico mediante su patente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión encontrado"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado")
    })
    @GetMapping("/{patente}")
    public ResponseEntity<CamionDTO> getCamionByPatente(
            @Parameter(description = "Patente del camión", required = true, example = "ABC123")
            @PathVariable String patente) {
        return camionService.getCamionById(patente)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ENDPOINTS PARA ASIGNAR Y LIBERAR CAMIONES

    @Operation(
        summary = "Asignar camión (marcar como ocupado)",
        description = "Marca un camión como ocupado/asignado para una ruta o envío"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Camión asignado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado")
    })
    @PostMapping("/{patente}/camion-asignado")
    public ResponseEntity<Map<String, String>> asignarCamion(
            @Parameter(description = "Patente del camión", required = true, example = "ABC123")
            @PathVariable String patente) {
        try {
            camionService.asignarCamion(patente);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("patente", patente, "estadoCamion", "OCUPADO"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Vincular contenedor a camión",
        description = "Asigna un contenedor específico a un camión. Valida que el contenedor esté disponible y que no exceda la capacidad del camión."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contenedor vinculado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Camión o contenedor no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto: contenedor ya asignado o capacidad excedida")
    })
    @PostMapping("/{patente}/vincular-contenedor/{idContenedor}")
    public ResponseEntity<Map<String, String>> vincularContenedorACamion(
            @Parameter(description = "Patente del camión", required = true, example = "ABC123")
            @PathVariable String patente,
            @Parameter(description = "ID del contenedor", required = true, example = "CONT001")
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

    @Operation(
        summary = "Liberar camión (marcar como disponible)",
        description = "Marca un camión como libre/disponible después de completar una ruta o envío"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión liberado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado")
    })
    @PostMapping("/{patente}/camion-libre")
    public ResponseEntity<Map<String, String>> liberarCamion(
            @Parameter(description = "Patente del camión", required = true, example = "ABC123")
            @PathVariable String patente) {
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
        if (camion.getContenedores() != null && !camion.getContenedores().isEmpty()) {
            dto.setContenedorIds(camion.getContenedores().stream()
                    .map(Contenedor::getIdContenedor)
                    .collect(Collectors.toList()));
        } else if (camion.getPatente() != null) {
            List<Contenedor> conts = contenedorRepository.findByCamion_Patente(camion.getPatente());
            if (conts != null && !conts.isEmpty()) {
                dto.setContenedorIds(conts.stream().map(Contenedor::getIdContenedor).collect(Collectors.toList()));
            }
        }
        return dto;
    }
}
