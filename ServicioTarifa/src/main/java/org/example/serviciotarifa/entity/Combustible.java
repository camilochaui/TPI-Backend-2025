package org.example.serviciotarifa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "combustible")
public class Combustible {
    @Id
    @Column(name = "idcombustible")
    private Integer idCombustible;

    private String nombre;

    @Column(name = "precio_x_litro")
    private Float precioXLitro;

    public Combustible() {}

    public Combustible(Integer idCombustible, String nombre, Float precioXLitro) {
        this.idCombustible = idCombustible;
        this.nombre = nombre;
        this.precioXLitro = precioXLitro;
    }

    public Integer getIdCombustible() { return idCombustible; }
    public void setIdCombustible(Integer idCombustible) { this.idCombustible = idCombustible; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Float getPrecioXLitro() { return precioXLitro; }
    public void setPrecioXLitro(Float precioXLitro) { this.precioXLitro = precioXLitro; }
}