package org.example.serviciocliente.repository;

import org.example.serviciocliente.entity.ProvinciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinciaRepository extends JpaRepository<ProvinciaEntity, Long> {

    Optional<ProvinciaEntity> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}