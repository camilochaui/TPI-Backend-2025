package org.example.serviciotarifa.repository;

import org.example.serviciotarifa.entity.TarifaBaseKm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TarifaBaseKmRepository extends JpaRepository<TarifaBaseKm, Integer> {

    @Query("SELECT t FROM TarifaBaseKm t WHERE :volumen BETWEEN t.volumenMin AND t.volumenMax")
    Optional<TarifaBaseKm> findByVolumen(@Param("volumen") Float volumen);
}