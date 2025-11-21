package org.example.servicioflota.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "camion")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "patente")
public class Camion {

    @Id
    @Column(name = "patente")
    private String patente;

    @Column(name = "capacidad_peso")
    private Float capacidadPeso;

    @Column(name = "capacidad_volumen")
    private Float capacidadVolumen;

    private boolean disponibilidad;

    @Column(name = "consumo_xkm")
    private Float consumoXKm;

    @Column(name = "costo_base_xkm")
    private Float costoBaseXKm;

    @Column(name = "id_combustible_ext")
    private Integer idCombustibleExt;

    @OneToOne
    @JoinColumn(name = "id_transportista_fk")
    private Transportista transportista;

    @OneToMany(mappedBy = "camion")
    private List<Contenedor> contenedores;

    // ---- Getters & Setters ----

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public Float getCapacidadPeso() {
        return capacidadPeso;
    }

    public void setCapacidadPeso(Float capacidadPeso) {
        this.capacidadPeso = capacidadPeso;
    }

    public Float getCapacidadVolumen() {
        return capacidadVolumen;
    }

    public void setCapacidadVolumen(Float capacidadVolumen) {
        this.capacidadVolumen = capacidadVolumen;
    }

    public boolean isDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public Float getConsumoXKm() {
        return consumoXKm;
    }

    public void setConsumoXKm(Float consumoXKm) {
        this.consumoXKm = consumoXKm;
    }

    public Float getCostoBaseXKm() {
        return costoBaseXKm;
    }

    public void setCostoBaseXKm(Float costoBaseXKm) {
        this.costoBaseXKm = costoBaseXKm;
    }

    public Integer getIdCombustibleExt() {
        return idCombustibleExt;
    }

    public void setIdCombustibleExt(Integer idCombustibleExt) {
        this.idCombustibleExt = idCombustibleExt;
    }

    public Transportista getTransportista() {
        return transportista;
    }

    public void setTransportista(Transportista transportista) {
        this.transportista = transportista;
    }

    public List<Contenedor> getContenedores() {
        return contenedores;
    }

    public void setContenedores(List<Contenedor> contenedores) {
        this.contenedores = contenedores;
    }
}
