package org.example.servicioflota.service;

import org.example.servicioflota.model.Transportista;
import org.example.servicioflota.repository.TransportistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TransportistaService {

    @Autowired
    private TransportistaRepository transportistaRepository;

    public List<Transportista> getAllTransportistas() {
        return transportistaRepository.findAll();
    }

    public Transportista saveTransportista(Transportista transportista) {
        return transportistaRepository.save(transportista);
    }

    public Optional<Transportista> getTransportistaById(Integer id) {
        return transportistaRepository.findById(id);
    }

    public Transportista updateTransportista(Integer id, Transportista transportistaDetails) {
        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transportista no encontrado con id: " + id));

        transportista.setNombre(transportistaDetails.getNombre());
        transportista.setApellido(transportistaDetails.getApellido());
        transportista.setDni(transportistaDetails.getDni());
        transportista.setTelefono(transportistaDetails.getTelefono());
        transportista.setDisponibilidad(transportistaDetails.isDisponibilidad());

        return transportistaRepository.save(transportista);
    }

    @Transactional
    public void asignarTransportista(Integer id) {
        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transportista no encontrado con id: " + id));
        transportista.setDisponibilidad(false);
        transportistaRepository.save(transportista);
    }


    @Transactional
    public void liberarTransportista(Integer id) {
        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transportista no encontrado con id: " + id));
        transportista.setDisponibilidad(true);
        transportistaRepository.save(transportista);
    }
}
