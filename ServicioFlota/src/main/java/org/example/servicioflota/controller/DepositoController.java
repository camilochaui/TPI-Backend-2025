package org.example.servicioflota.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.servicioflota.dto.DepositoDTO;
import org.example.servicioflota.model.Contenedor;
import org.example.servicioflota.model.Deposito;
import org.example.servicioflota.service.DepositoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/flota/depositos")
@Tag(name = "Gestión de Depósitos", description = "Endpoints para administrar depósitos y realizar check-in/check-out de contenedores")
@SecurityRequirement(name = "bearerAuth")
public class DepositoController {

    @Autowired
    private DepositoService depositoService;

    @Autowired
    private org.example.servicioflota.repository.ContenedorRepository contenedorRepository;

    @Operation(
        summary = "Listar todos los depósitos",
        description = "Obtiene la lista completa de depósitos registrados con sus ubicaciones y contenedores asignados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<DepositoDTO>> getAllDepositos() {
        List<Deposito> depositos = depositoService.findAllDepositos();
        List<DepositoDTO> depositosDto = depositos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(depositosDto);
    }

    @Operation(
        summary = "Obtener depósito por ID",
        description = "Consulta los detalles de un depósito específico incluyendo su ubicación y contenedores almacenados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Depósito encontrado"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepositoDTO> getDepositoById(
            @Parameter(description = "ID del depósito", required = true, example = "1")
            @PathVariable Integer id) {
        return depositoService.getDepositoById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Registrar nuevo depósito",
        description = "Crea un nuevo depósito con su ubicación geográfica. Opcionalmente puede asignar contenedores iniciales."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Depósito creado exitosamente")
    })
    @PostMapping
    public ResponseEntity<DepositoDTO> createDeposito(
            @Parameter(description = "Datos del depósito a crear", required = true)
            @RequestBody DepositoDTO depositoDTO) {
        Deposito deposito = convertToEntity(depositoDTO);
        Deposito nuevoDeposito = depositoService.saveDeposito(deposito);

        // Si el DTO trae contenedorIds, asignarlos al depósito recién creado
        if (depositoDTO.getContenedorIds() != null && !depositoDTO.getContenedorIds().isEmpty()) {
            nuevoDeposito = depositoService.assignContenedoresToDeposito(nuevoDeposito.getIdDeposito(), depositoDTO.getContenedorIds());
        }

        return new ResponseEntity<>(convertToDto(nuevoDeposito), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Actualizar depósito",
        description = "Modifica los datos de un depósito existente, incluyendo nombre, dirección y coordenadas"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Depósito actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado")
    })
    @PutMapping
    public ResponseEntity<DepositoDTO> updateDeposito(
            @Parameter(description = "ID del depósito", required = true, example = "1")
            @RequestParam Integer id,
            @Parameter(description = "Datos actualizados del depósito", required = true)
            @RequestBody DepositoDTO depositoDTO) {
        return depositoService.getDepositoById(id)
                .map(existingDeposito -> {
                    existingDeposito.setNombre(depositoDTO.getNombre());
                    existingDeposito.setDireccion(depositoDTO.getDireccion());
                    existingDeposito.setLatitud(depositoDTO.getLatitud());
                    existingDeposito.setLongitud(depositoDTO.getLongitud());
                    Deposito updatedDeposito = depositoService.saveDeposito(existingDeposito);
                    return new ResponseEntity<>(convertToDto(updatedDeposito), HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Check-in de contenedor",
        description = "Registra la llegada de un contenedor a un depósito. Actualiza el estado del contenedor a EN_DEPOSITO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-in realizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Depósito o contenedor no encontrado")
    })
    @PostMapping("/{id}/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(
            @Parameter(description = "ID del depósito", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Payload con contenedorId", required = true)
            @RequestBody Map<String, String> payload
    ) {

        depositoService.checkInContenedor(id, payload.get("contenedorId"));
        return new ResponseEntity<>(Map.of("mensaje", "Check-in realizado correctamente"), HttpStatus.OK);
    }

    @Operation(
        summary = "Check-out de contenedor",
        description = "Registra la salida de un contenedor de un depósito. Actualiza el estado del contenedor a EN_TRANSITO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-out realizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Depósito o contenedor no encontrado")
    })
    @PostMapping("/{id}/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(
            @Parameter(description = "ID del depósito", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Payload con contenedorId", required = true)
            @RequestBody Map<String, String> payload
    ) {

        depositoService.checkOutContenedor(id, payload.get("contenedorId"));
        return new ResponseEntity<>(Map.of("mensaje", "Check-out realizado correctamente"), HttpStatus.OK);
    }

    private DepositoDTO convertToDto(Deposito deposito) {
        DepositoDTO dto = new DepositoDTO();
        dto.setIdDeposito(deposito.getIdDeposito());
        dto.setNombre(deposito.getNombre());
        dto.setDireccion(deposito.getDireccion());
        dto.setLatitud(deposito.getLatitud());
        dto.setLongitud(deposito.getLongitud());
        if (deposito.getContenedores() != null && !deposito.getContenedores().isEmpty()) {
            dto.setContenedorIds(deposito.getContenedores().stream()
                    .map(Contenedor::getIdContenedor)
                    .collect(Collectors.toList()));
        } else if (deposito.getIdDeposito() != null) {
            // Si la colección no está inicializada, consultamos el repositorio
            List<Contenedor> conts = contenedorRepository.findByDeposito_IdDeposito(deposito.getIdDeposito());
            if (conts != null && !conts.isEmpty()) {
                dto.setContenedorIds(conts.stream().map(Contenedor::getIdContenedor).collect(Collectors.toList()));
            }
        }
        return dto;
    }

    private Deposito convertToEntity(DepositoDTO dto) {
        Deposito deposito = new Deposito();
        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setLatitud(dto.getLatitud());
        deposito.setLongitud(dto.getLongitud());

        return deposito;
    }
}
