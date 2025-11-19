package org.example.servicioflota.dto;

import java.util.List;

public class CamionDTO {
    private String patente;
    private Float capacidadPeso;
    private Float capacidadVolumen;
    private boolean disponibilidad;
    private Integer transportistaId;
    private List<String> contenedorIds;;

    // Getters y Setters
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }
    public Float getCapacidadPeso() { return capacidadPeso; }
    public void setCapacidadPeso(Float capacidadPeso) { this.capacidadPeso = capacidadPeso; }
    public Float getCapacidadVolumen() { return capacidadVolumen; }
    public void setCapacidadVolumen(Float capacidadVolumen) { this.capacidadVolumen = capacidadVolumen; }
    public boolean isDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(boolean disponibilidad) { this.disponibilidad = disponibilidad; }
    public Integer getTransportistaId() { return transportistaId; }
    public void setTransportistaId(Integer transportistaId) { this.transportistaId = transportistaId; }
    public List<String> getContenedorIds() { return contenedorIds; }
    public void setContenedorIds(List<String> contenedorIds) { this.contenedorIds = contenedorIds; }
}
