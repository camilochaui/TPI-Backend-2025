package org.example.serviciocliente.repository;

import org.example.serviciocliente.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByDni(Long dni);

    boolean existsByMail(String mail);

    boolean existsByDni(Long dni);
}
