package org.example.servicioflota.repository;

import org.example.servicioflota.model.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

//Filtrar contenedores por estado y depósito

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, String> {

        List<Contenedor> findByCamionPatente(String patente);

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
                        @Param("depositoId") Integer depositoId);

        @Query("SELECT c FROM Contenedor c " +
                        "LEFT JOIN c.deposito d " +
                        "WHERE (:depositoId IS NULL OR d.idDeposito = :depositoId) " +
                        // Debe existir un cambio de estado ACTUAL (fechaFin IS NULL) y ese estado
                        // no debe ser 'Entregado'. De este modo excluimos también contenedores
                        // que no tienen ningún cambio de estado (que se muestran como 'Sin Estado').
                        "AND EXISTS ( " +
                        "  SELECT ce FROM CambioEstado ce " +
                        "  WHERE ce.contenedor.idContenedor = c.idContenedor " +
                        "    AND ce.fechaFin IS NULL " +
                        "    AND ce.estado.nombre <> 'Entregado' " +
                        ")")
        List<Contenedor> findContenedoresPendientes(@Param("depositoId") Integer depositoId);

        // Buscar contenedores por camión (patente)
        List<Contenedor> findByCamion_Patente(String patente);

        // Buscar contenedores por depósito (id)
        List<Contenedor> findByDeposito_IdDeposito(Integer depositoId);
}