package org.example.servicioenvios.repository;

import org.example.servicioenvios.entity.EstadoTramo;
import org.example.servicioenvios.entity.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Asociado con REQ 7) Gestión de Tramos

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {

    List<Tramo> findByRuta_IdRutaOrderByOrdenAsc(Long idRuta);

    List<Tramo> findByPatenteCamionExtAndEstadoTramo(String patenteCamionExt, EstadoTramo estado);

    List<Tramo> findByEstadoTramoAndDestino_Tipo_Nombre(org.example.servicioenvios.entity.EstadoTramo estado, String tipoNombre);

    Optional<Tramo> findByPatenteCamionExt(String patenteCamionExt);

    @Query("SELECT t FROM Tramo t WHERE t.patenteCamionExt IN :patentes AND t.estadoTramo IN (org.example.servicioenvios.entity.EstadoTramo.ASIGNADO, org.example.servicioenvios.entity.EstadoTramo.INICIADO)")
    List<Tramo> findTramosByTransportista(@Param("patentes") List<String> patentes);
}