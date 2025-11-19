package org.example.serviciotarifa.dto;

import java.util.List;

public class CalculoTarifaRequest {
    private Integer idSolicitud;
    private Float consumoCamionLitroKm;
    private String tipoCombustible;
    private Float distanciaTotalKm;
    private Float volumenContenedor;
    private List<EstadiaRequest> estadias;
    private Float tarifaGestion;
    private Integer cantidadTramos;

    public CalculoTarifaRequest() {}

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public Float getConsumoCamionLitroKm() { return consumoCamionLitroKm; }
    public void setConsumoCamionLitroKm(Float consumoCamionLitroKm) { this.consumoCamionLitroKm = consumoCamionLitroKm; }

    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }

    public Float getDistanciaTotalKm() { return distanciaTotalKm; }
    public void setDistanciaTotalKm(Float distanciaTotalKm) { this.distanciaTotalKm = distanciaTotalKm; }

    public Float getVolumenContenedor() { return volumenContenedor; }
    public void setVolumenContenedor(Float volumenContenedor) { this.volumenContenedor = volumenContenedor; }

    public List<EstadiaRequest> getEstadias() { return estadias; }
    public void setEstadias(List<EstadiaRequest> estadias) { this.estadias = estadias; }

    public Float getTarifaGestion() { return tarifaGestion; }
    public void setTarifaGestion(Float tarifaGestion) { this.tarifaGestion = tarifaGestion; }

    public Integer getCantidadTramos() { return cantidadTramos; }
    public void setCantidadTramos(Integer cantidadTramos) { this.cantidadTramos = cantidadTramos; }
}