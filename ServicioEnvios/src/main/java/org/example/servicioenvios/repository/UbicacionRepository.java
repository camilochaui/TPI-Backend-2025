package org.example.servicioenvios.repository;

import org.example.servicioenvios.entity.TipoUbicacion;
import org.example.servicioenvios.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    Optional<Ubicacion> findByLatitudAndLongitud(Double latitud, Double longitud);

    List<Ubicacion> findByTipo(TipoUbicacion tipo);
}