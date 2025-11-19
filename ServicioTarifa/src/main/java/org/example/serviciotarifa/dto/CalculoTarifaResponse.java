package org.example.serviciotarifa.dto;

import java.util.Map;

public class CalculoTarifaResponse {
    private Integer idCalculo;
    private Integer idSolicitud;
    private Float consumoPromedioGeneral;
    private Float costoTotal;
    private Map<String, Object> details;

    public CalculoTarifaResponse() {}

    public CalculoTarifaResponse(Integer idCalculo, Integer idSolicitud, Float consumoPromedioGeneral,
                                 Float costoTotal, Map<String, Object> details) {
        this.idCalculo = idCalculo;
        this.idSolicitud = idSolicitud;
        this.consumoPromedioGeneral = consumoPromedioGeneral;
        this.costoTotal = costoTotal;
        this.details = details;
    }

    public Integer getIdCalculo() { return idCalculo; }
    public void setIdCalculo(Integer idCalculo) { this.idCalculo = idCalculo; }

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public Float getConsumoPromedioGeneral() { return consumoPromedioGeneral; }
    public void setConsumoPromedioGeneral(Float consumoPromedioGeneral) { this.consumoPromedioGeneral = consumoPromedioGeneral; }

    public Float getCostoTotal() { return costoTotal; }
    public void setCostoTotal(Float costoTotal) { this.costoTotal = costoTotal; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}