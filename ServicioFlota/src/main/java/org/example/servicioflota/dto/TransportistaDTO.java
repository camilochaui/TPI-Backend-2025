package org.example.servicioflota.dto;

import java.util.List;

public class TransportistaDTO {
    private Integer idTransportista;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private boolean disponibilidad;
    private List<String> camionesPatentes;

    // Getters y Setters
    public Integer getIdTransportista() { return idTransportista; }
    public void setIdTransportista(Integer idTransportista) { this.idTransportista = idTransportista; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public boolean isDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(boolean disponibilidad) { this.disponibilidad = disponibilidad; }
    public List<String> getCamionesPatentes() { return camionesPatentes; }
    public void setCamionesPatentes(List<String> camionesPatentes) { this.camionesPatentes = camionesPatentes; }
}
