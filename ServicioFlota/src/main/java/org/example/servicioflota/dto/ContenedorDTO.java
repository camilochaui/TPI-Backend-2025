package org.example.servicioflota.dto;

import java.util.List;

public class ContenedorDTO {
    private String idContenedor;
    private Integer peso;
    private Integer volumen;
    private Integer idClienteExt;
    private Integer depositoId;
    private String camionPatente;
    private List<Integer> cambiosEstadoIds;
    private String estadoActual;

    // Getters y Setters
    public String getIdContenedor() { return idContenedor; }
    public void setIdContenedor(String idContenedor) { this.idContenedor = idContenedor; }
    public Integer getPeso() { return peso; }
    public void setPeso(Integer peso) { this.peso = peso; }
    public Integer getVolumen() { return volumen; }
    public void setVolumen(Integer volumen) { this.volumen = volumen; }
    public Integer getIdClienteExt() { return idClienteExt; }
    public void setIdClienteExt(Integer idClienteExt) { this.idClienteExt = idClienteExt; }
    public Integer getDepositoId() { return depositoId; }
    public void setDepositoId(Integer depositoId) { this.depositoId = depositoId; }
    public String getCamionPatente() { return camionPatente; }
    public void setCamionPatente(String camionPatente) { this.camionPatente = camionPatente; }
    public List<Integer> getCambiosEstadoIds() { return cambiosEstadoIds; }
    public void setCambiosEstadoIds(List<Integer> cambiosEstadoIds) { this.cambiosEstadoIds = cambiosEstadoIds; }
    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }
}
