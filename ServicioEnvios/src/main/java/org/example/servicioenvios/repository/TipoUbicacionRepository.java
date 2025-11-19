package org.example.servicioenvios.repository;

import org.example.servicioenvios.entity.TipoUbicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoUbicacionRepository extends JpaRepository<TipoUbicacion, Long> {

    Optional<TipoUbicacion> findByNombre(String nombre);
}