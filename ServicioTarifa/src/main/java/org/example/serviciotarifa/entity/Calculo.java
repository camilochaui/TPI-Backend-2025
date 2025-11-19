package org.example.serviciotarifa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "calculo")
public class Calculo {
    @Id
    @Column(name = "idcalculo")
    private Integer idCalculo;

    @Column(name = "idsolicitud_ext")
    private Integer idSolicitudExt;

    @Column(name = "tipocalculo")
    private String tipoCalculo;

    @Column(name = "consumopromediogeneral")
    private Float consumoPromedioGeneral;

    @Column(name = "costototal")
    private Float costoTotal;

    @Column(columnDefinition = "json")
    private String detalle;

    public Calculo() {}

    public Calculo(Integer idCalculo, Integer idSolicitudExt, String tipoCalculo,
                   Float consumoPromedioGeneral, Float costoTotal, String detalle) {
        this.idCalculo = idCalculo;
        this.idSolicitudExt = idSolicitudExt;
        this.tipoCalculo = tipoCalculo;
        this.consumoPromedioGeneral = consumoPromedioGeneral;
        this.costoTotal = costoTotal;
        this.detalle = detalle;
    }

    public Integer getIdCalculo() { return idCalculo; }
    public void setIdCalculo(Integer idCalculo) { this.idCalculo = idCalculo; }

    public Integer getIdSolicitudExt() { return idSolicitudExt; }
    public void setIdSolicitudExt(Integer idSolicitudExt) { this.idSolicitudExt = idSolicitudExt; }

    public String getTipoCalculo() { return tipoCalculo; }
    public void setTipoCalculo(String tipoCalculo) { this.tipoCalculo = tipoCalculo; }

    public Float getConsumoPromedioGeneral() { return consumoPromedioGeneral; }
    public void setConsumoPromedioGeneral(Float consumoPromedioGeneral) { this.consumoPromedioGeneral = consumoPromedioGeneral; }

    public Float getCostoTotal() { return costoTotal; }
    public void setCostoTotal(Float costoTotal) { this.costoTotal = costoTotal; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}