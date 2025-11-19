package org.example.servicioenvios.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servicioenvios.entity.EstadoTramo;
import java.time.LocalDateTime;


@Entity
@Table(name = "tramo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "ruta")
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramo")
    private Long idTramo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;

    @Column(nullable = false)
    private Integer orden;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origen_id", nullable = false)
    private Ubicacion origen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destino_id", nullable = false)
    private Ubicacion destino;


    @Enumerated(EnumType.STRING)
    @Column(name = "estado_tramo", nullable = false)
    private EstadoTramo estadoTramo;

    @Column(name = "fecha_hora_inicio_estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @Column(name = "fecha_hora_fin_estimada")
    private LocalDateTime fechaHoraFinEstimada;

    @Column(name = "fecha_hora_inicio_real")
    private LocalDateTime fechaHoraInicioReal;

    @Column(name = "fecha_hora_fin_real")
    private LocalDateTime fechaHoraFinReal;

    @Column(name = "patente_camion_ext")
    private String patenteCamionExt;

    @Column(name = "distancia_km_estimada")
    private Double distanciaKmEstimada;

    @Column(name = "costo_estimado")
    private Double costoEstimado;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "costo_estadia_deposito")
    private Double costoEstadiaDeposito;
}