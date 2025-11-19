package org.example.servicioflota.controller;

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
public class DepositoController {

    @Autowired
    private DepositoService depositoService;

    @GetMapping
    public ResponseEntity<List<DepositoDTO>> getAllDepositos() {
        List<Deposito> depositos = depositoService.findAllDepositos();
        List<DepositoDTO> depositosDto = depositos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(depositosDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositoDTO> getDepositoById(@PathVariable Integer id) {
        return depositoService.getDepositoById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DepositoDTO> createDeposito(@RequestBody DepositoDTO depositoDTO) {
        Deposito deposito = convertToEntity(depositoDTO);
        Deposito nuevoDeposito = depositoService.saveDeposito(deposito);
        return new ResponseEntity<>(convertToDto(nuevoDeposito), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<DepositoDTO> updateDeposito(
            @RequestParam Integer id,
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

    @PostMapping("/{id}/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload
    ) {

        depositoService.checkInContenedor(id, payload.get("contenedorId"));
        return new ResponseEntity<>(Map.of("mensaje", "Check-in realizado correctamente"), HttpStatus.OK);
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(
            @PathVariable Integer id,
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
        if (deposito.getContenedores() != null) {
            dto.setContenedorIds(deposito.getContenedores().stream()
                    .map(Contenedor::getIdContenedor)
                    .collect(Collectors.toList()));
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
