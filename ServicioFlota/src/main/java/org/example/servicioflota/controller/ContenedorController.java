package org.example.servicioflota.controller;

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
public class ContenedorController {

    @Autowired
    private ContenedorService contenedorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ContenedorDTO> createContenedor(@RequestBody ContenedorDTO contenedorDTO) {
        Contenedor contenedor = convertToEntity(contenedorDTO);
        Contenedor nuevoContenedor = contenedorService.saveContenedor(contenedor);
        return new ResponseEntity<>(convertToDto(nuevoContenedor), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenedorDTO> getContenedor(@PathVariable String id) {
        return contenedorService.getContenedorById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ContenedorDTO>> getContenedores(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer depositoId,
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
