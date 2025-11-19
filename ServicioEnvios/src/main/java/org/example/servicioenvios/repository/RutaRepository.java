package org.example.servicioenvios.repository;

import org.example.servicioenvios.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {

    Optional<Ruta> findBySolicitud_NumSolicitud(Long numSolicitud);
}