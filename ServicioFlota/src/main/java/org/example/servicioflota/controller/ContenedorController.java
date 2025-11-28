package org.example.servicioflota.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.servicioflota.dto.ContenedorDTO;
import org.example.servicioflota.model.CambioEstado;
import org.example.servicioflota.model.Contenedor;
import org.example.servicioflota.service.ContenedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/flota/contenedores")
@Tag(name = "Gestión de Contenedores", description = "Endpoints para administrar contenedores, sus estados y asignaciones")
@SecurityRequirement(name = "bearerAuth")
public class ContenedorController {

    @Autowired
    private ContenedorService contenedorService;

    @Operation(
        summary = "Registrar nuevo contenedor",
        description = "Crea un nuevo contenedor con sus características físicas. Requiere rol ADMINISTRADOR."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contenedor creado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: requiere rol ADMINISTRADOR")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ContenedorDTO> createContenedor(
            @Parameter(description = "Datos del contenedor a crear", required = true)
            @RequestBody ContenedorDTO contenedorDTO) {
        Contenedor contenedor = convertToEntity(contenedorDTO);
        Contenedor nuevoContenedor = contenedorService.saveContenedor(contenedor);
        return new ResponseEntity<>(convertToDto(nuevoContenedor), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Obtener contenedor por ID",
        description = "Consulta los detalles de un contenedor específico mediante su identificador"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contenedor encontrado"),
        @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContenedorDTO> getContenedor(
            @Parameter(description = "ID del contenedor", required = true, example = "CONT001")
            @PathVariable String id) {
        return contenedorService.getContenedorById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Listar contenedores",
        description = "Obtiene todos los contenedores con filtros opcionales por estado, depósito y contenedores pendientes de asignación"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<ContenedorDTO>> getContenedores(
            @Parameter(description = "Filtrar por estado del contenedor", example = "EN_DEPOSITO")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Filtrar por ID de depósito", example = "1")
            @RequestParam(required = false) Integer depositoId,
            @Parameter(description = "Mostrar solo contenedores pendientes", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean pendientes) {
        List<Contenedor> contenedores = contenedorService.getAllContenedores(estado, depositoId, pendientes);
        List<ContenedorDTO> contenedorDTOs = contenedores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(contenedorDTOs);
    }

    private ContenedorDTO convertToDto(Contenedor contenedor) {
        ContenedorDTO dto = new ContenedorDTO();
        dto.setIdContenedor(contenedor.getIdContenedor());
        dto.setPeso(contenedor.getPeso());
        dto.setVolumen(contenedor.getVolumen());
        dto.setIdClienteExt(contenedor.getIdClienteExt());
        if (contenedor.getDeposito() != null) {
            dto.setDepositoId(contenedor.getDeposito().getIdDeposito());
        }
        if (contenedor.getCamion() != null) {
            dto.setCamionPatente(contenedor.getCamion().getPatente());
        }
        if (contenedor.getCambiosEstado() != null) {
            dto.setCambiosEstadoIds(contenedor.getCambiosEstado().stream()
                .sorted(Comparator.comparing(CambioEstado::getFechaInicio))
                .map(CambioEstado::getIdCambioEstado)
                .collect(Collectors.toList()));
        }
        dto.setEstadoActual(contenedor.getEstadoActual());
        return dto;
    }

    private Contenedor convertToEntity(ContenedorDTO dto) {
        Contenedor contenedor = new Contenedor();
        contenedor.setIdContenedor(dto.getIdContenedor());
        contenedor.setPeso(dto.getPeso());
        contenedor.setVolumen(dto.getVolumen());
        contenedor.setIdClienteExt(dto.getIdClienteExt());

        return contenedor;
    }
}
