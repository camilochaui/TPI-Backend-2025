package org.example.servicioflota.service;

import org.example.servicioflota.model.Contenedor;
import org.example.servicioflota.model.Deposito;
import org.example.servicioflota.repository.ContenedorRepository;
import org.example.servicioflota.repository.DepositoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional; // Importar Optional

@Service
public class DepositoService {

    @Autowired
    private DepositoRepository depositoRepository;

    @Autowired
    private ContenedorRepository contenedorRepository;

    public Deposito saveDeposito(Deposito deposito) {
        return depositoRepository.save(deposito);
    }

    public List<Deposito> findAllDepositos() {
        return depositoRepository.findAll();
    }

    public Optional<Deposito> getDepositoById(Integer id) {
        return depositoRepository.findById(id);
    }

    public void checkInContenedor(Integer depositoId, String contenedorId) { // De Integer a String
        Deposito deposito = depositoRepository.findById(depositoId)
                .orElseThrow(() -> new EntityNotFoundException("Depósito no encontrado con id: " + depositoId));

        Contenedor contenedor = contenedorRepository.findById(contenedorId)
                .orElseThrow(() -> new EntityNotFoundException("Contenedor no encontrado con id: " + contenedorId));

        contenedor.setDeposito(deposito);
        contenedorRepository.save(contenedor);
    }

    public void checkOutContenedor(Integer depositoId, String contenedorId) { // De Integer a String
        depositoRepository.findById(depositoId)
                .orElseThrow(() -> new EntityNotFoundException("Depósito no encontrado con id: " + depositoId));


        Contenedor contenedor = contenedorRepository.findById(contenedorId)
                .orElseThrow(() -> new EntityNotFoundException("Contenedor no encontrado con id: " + contenedorId));

        contenedor.setDeposito(null);
        contenedorRepository.save(contenedor);
    }
}
