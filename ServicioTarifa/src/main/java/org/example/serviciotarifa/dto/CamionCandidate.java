package org.example.serviciotarifa.dto;

public class CamionCandidate {
    private Integer idCamion;
    private Float capacidadPeso;
    private Float capacidadVolumen;
    private Float consumoPromedioLitroKm;
    private Float costoKm;

    public CamionCandidate() {}

    public Integer getIdCamion() { return idCamion; }
    public void setIdCamion(Integer idCamion) { this.idCamion = idCamion; }

    public Float getCapacidadPeso() { return capacidadPeso; }
    public void setCapacidadPeso(Float capacidadPeso) { this.capacidadPeso = capacidadPeso; }

    public Float getCapacidadVolumen() { return capacidadVolumen; }
    public void setCapacidadVolumen(Float capacidadVolumen) { this.capacidadVolumen = capacidadVolumen; }

    public Float getConsumoPromedioLitroKm() { return consumoPromedioLitroKm; }
    public void setConsumoPromedioLitroKm(Float consumoPromedioLitroKm) { this.consumoPromedioLitroKm = consumoPromedioLitroKm; }

    public Float getCostoKm() { return costoKm; }
    public void setCostoKm(Float costoKm) { this.costoKm = costoKm; }
}
