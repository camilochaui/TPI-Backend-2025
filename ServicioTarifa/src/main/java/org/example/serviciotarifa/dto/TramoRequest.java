package org.example.serviciotarifa.dto;

public class TramoRequest {
    private Float distanciaKm;
    private Integer idCamion;
    private Float consumoCamionLitroKm;
    private Float costoKmCamion;
    private Float capacidadPeso;
    private Float capacidadVolumen;

    public TramoRequest() {}

    public Float getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Float distanciaKm) { this.distanciaKm = distanciaKm; }

    public Integer getIdCamion() { return idCamion; }
    public void setIdCamion(Integer idCamion) { this.idCamion = idCamion; }

    public Float getConsumoCamionLitroKm() { return consumoCamionLitroKm; }
    public void setConsumoCamionLitroKm(Float consumoCamionLitroKm) { this.consumoCamionLitroKm = consumoCamionLitroKm; }

    public Float getCostoKmCamion() { return costoKmCamion; }
    public void setCostoKmCamion(Float costoKmCamion) { this.costoKmCamion = costoKmCamion; }

    public Float getCapacidadPeso() { return capacidadPeso; }
    public void setCapacidadPeso(Float capacidadPeso) { this.capacidadPeso = capacidadPeso; }

    public Float getCapacidadVolumen() { return capacidadVolumen; }
    public void setCapacidadVolumen(Float capacidadVolumen) { this.capacidadVolumen = capacidadVolumen; }
}
