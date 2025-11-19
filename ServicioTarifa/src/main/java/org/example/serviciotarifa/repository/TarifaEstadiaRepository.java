package org.example.serviciotarifa.repository;

import org.example.serviciotarifa.entity.TarifaEstadia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;


@Repository
public interface TarifaEstadiaRepository extends JpaRepository<TarifaEstadia, Integer> {

    Optional<TarifaEstadia> findByIdDepositoExt(Integer idDepositoExt);

}