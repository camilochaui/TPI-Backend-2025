package org.example.serviciotarifa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tarifabasekm")
public class TarifaBaseKm {
    @Id
    @Column(name = "idtarifakm")
    private Integer idTarifaKm;

    @Column(name = "volumenmin")
    private Float volumenMin;

    @Column(name = "volumenmax")
    private Float volumenMax;

    @Column(name = "costobasekm")
    private Float costoBaseKm;

    public TarifaBaseKm() {}

    public TarifaBaseKm(Integer idTarifaKm, Float volumenMin, Float volumenMax, Float costoBaseKm) {
        this.idTarifaKm = idTarifaKm;
        this.volumenMin = volumenMin;
        this.volumenMax = volumenMax;
        this.costoBaseKm = costoBaseKm;
    }

    public Integer getIdTarifaKm() { return idTarifaKm; }
    public void setIdTarifaKm(Integer idTarifaKm) { this.idTarifaKm = idTarifaKm; }

    public Float getVolumenMin() { return volumenMin; }
    public void setVolumenMin(Float volumenMin) { this.volumenMin = volumenMin; }

    public Float getVolumenMax() { return volumenMax; }
    public void setVolumenMax(Float volumenMax) { this.volumenMax = volumenMax; }

    public Float getCostoBaseKm() { return costoBaseKm; }
    public void setCostoBaseKm(Float costoBaseKm) { this.costoBaseKm = costoBaseKm; }
}