package org.example.servicioflota.dto;

import java.util.List;

public class DepositoDTO {
    private Integer idDeposito;
    private String nombre;
    private String direccion;
    private String latitud;
    private String longitud;
    private List<String> contenedorIds;

    // Getters y Setters
    public Integer getIdDeposito() { return idDeposito; }
    public void setIdDeposito(Integer idDeposito) { this.idDeposito = idDeposito; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getLatitud() { return latitud; }
    public void setLatitud(String latitud) { this.latitud = latitud; }
    public String getLongitud() { return longitud; }
    public void setLongitud(String longitud) { this.longitud = longitud; }
    public List<String> getContenedorIds() { return contenedorIds; }
    public void setContenedorIds(List<String> contenedorIds) { this.contenedorIds = contenedorIds; }
}
