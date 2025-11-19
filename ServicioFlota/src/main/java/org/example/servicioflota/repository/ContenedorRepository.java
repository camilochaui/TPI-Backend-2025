package org.example.servicioflota.repository;

import org.example.servicioflota.model.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

// Asociado con el REQ 5) Filtrar contenedores por estado y depósito

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, String> {

    @Query("SELECT c FROM Contenedor c " +
            "LEFT JOIN c.deposito d " +
            "WHERE (:depositoId IS NULL OR d.idDeposito = :depositoId) " +
            "AND (:estado IS NULL OR c.idContenedor IN ( " +
            "  SELECT ce.contenedor.idContenedor " + // <-- Esto sigue funcionando
            "  FROM CambioEstado ce " +
            "  WHERE ce.estado.nombre = :estado AND ce.fechaFin IS NULL " +
            "))")
    List<Contenedor> findContenedoresByFiltros(
            @Param("estado") String estado,
            @Param("depositoId") Integer depositoId
    );
}