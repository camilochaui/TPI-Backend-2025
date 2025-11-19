package org.example.serviciotarifa.entity;

import jakarta.persistence.*;
import java.util.Map;

@Entity
@Table(name = "tarifaestadia")
public class TarifaEstadia {

    @Id
    @Column(name = "idestadia")
    private Integer idEstadia;

    @Column(name = "costodiario")
    private Float costoDiario;

    @Column(name = "iddeposito_ext")
    private Integer idDepositoExt;

    @Column(name = "nombre")
    private String nombre;

    @Transient
    private String nombreDeposito;


    public TarifaEstadia() {}

    public TarifaEstadia(Integer idEstadia, Float costoDiario, Integer idDepositoExt, String nombre) {
        this.idEstadia = idEstadia;
        this.costoDiario = costoDiario;
        this.idDepositoExt = idDepositoExt;
        this.nombre = nombre;
        this.nombreDeposito = obtenerNombreDeposito(idDepositoExt);
    }

    // Getters y Setters
    public Integer getIdEstadia() { return idEstadia; }
    public void setIdEstadia(Integer idEstadia) { this.idEstadia = idEstadia; }

    public Float getCostoDiario() { return costoDiario; }
    public void setCostoDiario(Float costoDiario) { this.costoDiario = costoDiario; }

    public Integer getIdDepositoExt() { return idDepositoExt; }
    public void setIdDepositoExt(Integer idDepositoExt) {
        this.idDepositoExt = idDepositoExt;
        this.nombreDeposito = obtenerNombreDeposito(idDepositoExt);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNombreDeposito() { return nombreDeposito; }
    public void setNombreDeposito(String nombreDeposito) { this.nombreDeposito = nombreDeposito; }

    private String obtenerNombreDeposito(Integer idDeposito) {
        Map<Integer, String> depositos = Map.of(
                1, "Cordoba", 2, "San Francisco", 3, "Santa Fe",
                4, "Resistencia", 5, "Corrientes", 6, "Mendoza", 7, "San Luis"
        );
        return depositos.getOrDefault(idDeposito, "Desconocido");
    }
}