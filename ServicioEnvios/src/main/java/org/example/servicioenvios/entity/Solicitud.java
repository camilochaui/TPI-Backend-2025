package org.example.servicioenvios.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_solicitud")
    private Long numSolicitud;

    @Column(name = "id_contenedor_ext", nullable = false, unique = true)
    private String idContenedorExt;

    @Column(name = "id_cliente_ext", nullable = false)
    private Long idClienteExt;

    @Column(nullable = false)
    private Double peso;

    @Column(nullable = false)
    private Double volumen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false)
    private EstadoSolicitud estadoSolicitud;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "costo_estimado")
    private Double costoEstimado;

    @Column(name = "tiempo_estimado")
    private String tiempoEstimado;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "tiempo_real")
    private String tiempoReal;

    // Una solicitud tiene una ruta
    @OneToOne(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Ruta ruta;

    // Una solicitud puede tener muchas ubicaciones
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Ubicacion> ubicaciones;
}