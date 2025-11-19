package org.example.servicioenvios.repository;

import org.example.servicioenvios.entity.EstadoSolicitud;
import org.example.servicioenvios.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByIdContenedorExt(String idContenedorExt);

    List<Solicitud> findByEstadoSolicitudIn(List<EstadoSolicitud> estados);

    List<Solicitud> findByEstadoSolicitud(EstadoSolicitud estadoSolicitud);

    List<Solicitud> findByIdClienteExt(Long idClienteExt);

    @Query("SELECT s FROM Solicitud s LEFT JOIN FETCH s.ubicaciones u LEFT JOIN FETCH u.tipo WHERE s.numSolicitud = :id")
    Optional<Solicitud> findByIdWithUbicacionesAndTipos(@Param("id") Long id);
}