package org.example.serviciocliente.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.serviciocliente.dto.ClienteRequestDTO;
import org.example.serviciocliente.dto.ClienteResponseDTO;
import org.example.serviciocliente.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Gestión de Clientes", description = "Endpoints para el CRUD de Clientes (Admin) y registro (Servicio Envíos)")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Registrar un nuevo cliente", description = "Crea un nuevo cliente. Usado por Admin o internamente por ServicioEnvios. "
            +
            "Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto (DNI o Email ya existen)")
    })
    @PostMapping("/registro")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> registrarCliente(@Valid @RequestBody ClienteRequestDTO dto) {
        log.info("Solicitud de registro para cliente con DNI {}", dto.getDni());
        ClienteResponseDTO clienteCreado = clienteService.registrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteCreado);
    }

    @Operation(summary = "Listar todos los clientes", description = "Requiere rol ADMIN.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @Operation(summary = "Obtener un cliente por ID", description = "Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{idCliente}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(@PathVariable Long idCliente) {
        return ResponseEntity.ok(clienteService.obtenerClientePorId(idCliente));
    }

    @Operation(summary = "Editar un cliente", description = "Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto (DNI o Email ya en uso)")
    })
    @PutMapping("/{idCliente}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> editarCliente(
            @PathVariable Long idCliente,
            @Valid @RequestBody ClienteRequestDTO dto) {
        log.info("Solicitud de edición para cliente ID {}", idCliente);
        ClienteResponseDTO clienteActualizado = clienteService.editarCliente(idCliente, dto);
        return ResponseEntity.ok(clienteActualizado);
    }

    @Operation(summary = "Eliminar un cliente", description = "Requiere rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @DeleteMapping("/{idCliente}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long idCliente) {
        log.info("Solicitud de eliminación para cliente ID {}", idCliente);
        clienteService.eliminarCliente(idCliente);
        return ResponseEntity.noContent().build();
    }

    // "registrar el cliente si no existe"
    // Endpoint interno para ServicioEnvios

    @Operation(summary = "Registrar u obtener cliente (Para ServicioEnvios)", description = "Endpoint interno para ServicioEnvios. Registra cliente nuevo o retorna existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente obtenido/registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })

    @PostMapping("/solicitud")
    @PreAuthorize("hasRole('SERVICIO_CLIENTES') or hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> registrarOObtenerCliente(
            @Valid @RequestBody ClienteRequestDTO dto) {
        log.info("Solicitud de registro/obtención para cliente con DNI {}", dto.getDni());
        ClienteResponseDTO cliente = clienteService.registrarOObtenerCliente(dto);
        return ResponseEntity.ok(cliente);
    }
}
