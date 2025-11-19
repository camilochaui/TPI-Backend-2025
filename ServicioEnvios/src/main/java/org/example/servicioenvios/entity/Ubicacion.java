package org.example.servicioenvios.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Long idUbicacion;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_ubicacion", nullable = false)
    private TipoUbicacion tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "num_solicitud", nullable = false)
    private Solicitud solicitud;

}