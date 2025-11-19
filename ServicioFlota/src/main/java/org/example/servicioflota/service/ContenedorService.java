package org.example.servicioflota.service;

import org.example.servicioflota.model.Contenedor;
import org.example.servicioflota.repository.ContenedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContenedorService {

    @Autowired
    private ContenedorRepository contenedorRepository;

    public Contenedor saveContenedor(Contenedor contenedor) {
        return contenedorRepository.save(contenedor);
    }

    public Optional<Contenedor> getContenedorById(String id) {
        return contenedorRepository.findById(id);
    }

    public List<Contenedor> getAllContenedores(String estado, Integer depositoId) {
        return contenedorRepository.findContenedoresByFiltros(estado, depositoId);
    }
}
