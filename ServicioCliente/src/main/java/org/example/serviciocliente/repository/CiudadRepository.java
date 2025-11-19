package org.example.serviciocliente.repository;

import org.example.serviciocliente.entity.CiudadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CiudadRepository extends JpaRepository<CiudadEntity, Long> {

    List<CiudadEntity> findByProvinciaIdProvincia(Long idProvincia);

    boolean existsByNombreAndProvinciaIdProvincia(String nombre, Long idProvincia);
}
