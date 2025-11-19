package org.example.servicioflota.controller;

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
public class TransportistaController {

    @Autowired
    private TransportistaService transportistaService;

    @GetMapping
    public List<TransportistaDTO> getAllTransportistas() {
        return transportistaService.getAllTransportistas().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportistaDTO> getTransportistaById(@PathVariable Integer id) {
        return transportistaService.getTransportistaById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<TransportistaDTO> createTransportista(@RequestBody TransportistaDTO transportistaDTO) {
        Transportista transportista = new Transportista();
        transportista.setNombre(transportistaDTO.getNombre());
        transportista.setApellido(transportistaDTO.getApellido());
        transportista.setDni(transportistaDTO.getDni());
        transportista.setTelefono(transportistaDTO.getTelefono());
        transportista.setDisponibilidad(transportistaDTO.isDisponibilidad());

        Transportista nuevoTransportista = transportistaService.saveTransportista(transportista);
        return new ResponseEntity<>(convertToDto(nuevoTransportista), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<TransportistaDTO> updateTransportista(@PathVariable Integer id, @RequestBody TransportistaDTO transportistaDTO) {
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

    @PostMapping("/{id}/transportista-asignado")
    public ResponseEntity<Map<String, String>> asignarTransportista(@PathVariable Integer id) {
        try {
            transportistaService.asignarTransportista(id);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("idTransportista", id.toString(), "estadoTransportista", "OCUPADO"));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/transportista-libre")
    public ResponseEntity<Map<String, String>> liberarTransportista(@PathVariable Integer id) {
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
            dto.setCamionPatente(transportista.getCamion().getPatente());
        }
        return dto;
    }
}
