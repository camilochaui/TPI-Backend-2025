package org.example.servicioflota.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "contenedor")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "idContenedor")
public class Contenedor {

    @Id
    @Column(name = "id_contenedor")
    private String idContenedor;
    private Integer peso;
    private Integer volumen;

    @Column(name = "id_cliente_ext")
    private Integer idClienteExt;

    @ManyToOne
    @JoinColumn(name = "id_deposito_fk")
    private Deposito deposito;

    @ManyToOne
    @JoinColumn(name = "id_camion_fk")
    private Camion camion;

    @OneToMany(mappedBy = "contenedor", fetch = FetchType.EAGER)
    private List<CambioEstado> cambiosEstado;

    // --- Getters y Setters ---

    public String getEstadoActual() {
        if (this.cambiosEstado == null || this.cambiosEstado.isEmpty()) {
            return "Sin Estado";
        }
        return this.cambiosEstado.stream()
                .filter(cambio -> cambio.getFechaFin() == null)
                .findFirst()
                .map(CambioEstado::getEstado)
                .map(Estado::getNombre)
                .orElse("Histórico");
    }

    public String getIdContenedor() {
        return idContenedor;
    }

    public void setIdContenedor(String idContenedor) {
        this.idContenedor = idContenedor;
    }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public Integer getVolumen() {
        return volumen;
    }

    public void setVolumen(Integer volumen) {
        this.volumen = volumen;
    }

    public Integer getIdClienteExt() {
        return idClienteExt;
    }

    public void setIdClienteExt(Integer idClienteExt) {
        this.idClienteExt = idClienteExt;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public void setDeposito(Deposito deposito) {
        this.deposito = deposito;
    }

    public Camion getCamion() {
        return camion;
    }

    public void setCamion(Camion camion) {
        this.camion = camion;
    }

    public List<CambioEstado> getCambiosEstado() {
        return cambiosEstado;
    }

    public void setCambiosEstado(List<CambioEstado> cambiosEstado) {
        this.cambiosEstado = cambiosEstado;
    }
}