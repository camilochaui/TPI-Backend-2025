package org.example.serviciotarifa.repository;

import org.example.serviciotarifa.entity.Combustible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CombustibleRepository extends JpaRepository<Combustible, Integer> {
    Optional<Combustible> findByNombre(String nombre);
}