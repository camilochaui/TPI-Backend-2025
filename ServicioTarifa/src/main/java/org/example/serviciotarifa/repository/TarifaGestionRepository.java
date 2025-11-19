
package org.example.serviciotarifa.repository;

import org.example.serviciotarifa.entity.TarifaGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaGestionRepository extends JpaRepository<TarifaGestion, Integer> {
}