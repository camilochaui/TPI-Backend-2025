package org.example.serviciotarifa.dto;

import java.time.LocalDate;

public class EstadiaRequest {
    private Long idDeposito;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;

    public EstadiaRequest() {}

    public EstadiaRequest(Long idDeposito, LocalDate fechaEntrada, LocalDate fechaSalida) {
        this.idDeposito = idDeposito;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
    }

    public Long getIdDeposito() { return idDeposito; }
    public void setIdDeposito(Long idDeposito) { this.idDeposito = idDeposito; }

    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }

    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
}