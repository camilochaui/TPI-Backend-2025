package org.example.servicioflota.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.servicioflota.dto.TransportistaDTO;
import org.example.servicioflota.model.Camion;
import org.example.servicioflota.model.Transportista;
import org.example.servicioflota.service.TransportistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/flota/transportistas")
@Tag(name = "Gestión de Transportistas", description = "Endpoints para administrar transportistas y su disponibilidad")
@SecurityRequirement(name = "bearerAuth")
public class TransportistaController {

    @Autowired
    private TransportistaService transportistaService;

    @Operation(
        summary = "Listar todos los transportistas",
        description = "Obtiene la lista completa de transportistas registrados en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public List<TransportistaDTO> getAllTransportistas() {
        return transportistaService.getAllTransportistas().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Operation(
        summary = "Obtener transportista por ID",
        description = "Consulta los detalles de un transportista específico mediante su identificador"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transportista encontrado"),
        @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransportistaDTO> getTransportistaById(
            @Parameter(description = "ID del transportista", required = true, example = "1")
            @PathVariable Integer id) {
        return transportistaService.getTransportistaById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Registrar nuevo transportista",
        description = "Crea un nuevo transportista con sus datos personales y de contacto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transportista creado exitosamente")
    })
    @PostMapping()
    public ResponseEntity<TransportistaDTO> createTransportista(
            @Parameter(description = "Datos del transportista a crear", required = true)
            @RequestBody TransportistaDTO transportistaDTO) {
        Transportista transportista = new Transportista();
        transportista.setNombre(transportistaDTO.getNombre());
        transportista.setApellido(transportistaDTO.getApellido());
        transportista.setDni(transportistaDTO.getDni());
        transportista.setTelefono(transportistaDTO.getTelefono());
        transportista.setDisponibilidad(transportistaDTO.isDisponibilidad());

        Transportista nuevoTransportista = transportistaService.saveTransportista(transportista);
        return new ResponseEntity<>(convertToDto(nuevoTransportista), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Actualizar transportista",
        description = "Modifica los datos de un transportista existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transportista actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    })
    @PutMapping
    public ResponseEntity<TransportistaDTO> updateTransportista(
            @Parameter(description = "ID del transportista", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Datos actualizados del transportista", required = true)
            @RequestBody TransportistaDTO transportistaDTO) {
        try {
            Transportista transportistaDetails = new Transportista();
            transportistaDetails.setNombre(transportistaDTO.getNombre());
            transportistaDetails.setApellido(transportistaDTO.getApellido());
            transportistaDetails.setDni(transportistaDTO.getDni());
            transportistaDetails.setTelefono(transportistaDTO.getTelefono());
            transportistaDetails.setDisponibilidad(transportistaDTO.isDisponibilidad());

            Transportista updatedTransportista = transportistaService.updateTransportista(id, transportistaDetails);
            return ResponseEntity.ok(convertToDto(updatedTransportista));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Asignar transportista (marcar como ocupado)",
        description = "Marca un transportista como ocupado/asignado para una ruta o envío"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transportista asignado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    })
    @PostMapping("/{id}/transportista-asignado")
    public ResponseEntity<Map<String, String>> asignarTransportista(
            @Parameter(description = "ID del transportista", required = true, example = "1")
            @PathVariable Integer id) {
        try {
            transportistaService.asignarTransportista(id);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("idTransportista", id.toString(), "estadoTransportista", "OCUPADO"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Liberar transportista (marcar como disponible)",
        description = "Marca un transportista como libre/disponible después de completar una ruta o envío"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transportista liberado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Transportista no encontrado")
    })
    @PostMapping("/{id}/transportista-libre")
    public ResponseEntity<Map<String, String>> liberarTransportista(
            @Parameter(description = "ID del transportista", required = true, example = "1")
            @PathVariable Integer id) {
        try {
            transportistaService.liberarTransportista(id);

            return ResponseEntity.ok(Map.of("idTransportista", id.toString(), "estadoTransportista", "LIBRE"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    private TransportistaDTO convertToDto(Transportista transportista) {
        TransportistaDTO dto = new TransportistaDTO();
        dto.setIdTransportista(transportista.getIdTransportista());
        dto.setNombre(transportista.getNombre());
        dto.setApellido(transportista.getApellido());
        dto.setDni(transportista.getDni());
        dto.setTelefono(transportista.getTelefono());
        dto.setDisponibilidad(transportista.isDisponibilidad());
        if (transportista.getCamion() != null) {
            dto.setCamionesPatentes(java.util.List.of(transportista.getCamion().getPatente()));
        } else {
            dto.setCamionesPatentes(java.util.List.of());
        }
        return dto;
    }
}
