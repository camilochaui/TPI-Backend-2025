package org.example.servicioflota.repository;

import org.example.servicioflota.model.CambioEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CambioEstadoRepository extends JpaRepository<CambioEstado, Integer> {
}
