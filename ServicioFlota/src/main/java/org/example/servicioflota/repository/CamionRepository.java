package org.example.servicioflota.repository;

import org.example.servicioflota.model.Camion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {
    
    @EntityGraph(attributePaths = {"contenedores"})
    Optional<Camion> findWithContenedoresByPatente(String patente);
}
