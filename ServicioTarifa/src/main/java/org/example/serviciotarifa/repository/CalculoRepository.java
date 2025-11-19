package org.example.serviciotarifa.repository;

import org.example.serviciotarifa.entity.Calculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculoRepository extends JpaRepository<Calculo, Integer> {
}