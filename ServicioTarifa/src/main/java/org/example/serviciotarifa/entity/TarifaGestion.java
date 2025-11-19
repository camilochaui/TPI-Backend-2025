package org.example.serviciotarifa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "tarifagestion")
public class TarifaGestion {
    @Id
    @Column(name = "idtarifagestion")
    private Integer idTarifaGestion;

    @Column(name = "costofijotramo")
    private Float costoFijoTramo;

    // Constructores
    public TarifaGestion() {}

    public TarifaGestion(Integer idTarifaGestion, Float costoFijoTramo) {
        this.idTarifaGestion = idTarifaGestion;
        this.costoFijoTramo = costoFijoTramo;
    }

    // Getters y Setters
    public Integer getIdTarifaGestion() { return idTarifaGestion; }
    public void setIdTarifaGestion(Integer idTarifaGestion) { this.idTarifaGestion = idTarifaGestion; }

    public Float getCostoFijoTramo() { return costoFijoTramo; }
    public void setCostoFijoTramo(Float costoFijoTramo) { this.costoFijoTramo = costoFijoTramo; }
}
