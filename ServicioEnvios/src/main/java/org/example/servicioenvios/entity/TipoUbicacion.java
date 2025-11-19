package org.example.servicioenvios.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_ubicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoUbicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_ubicacion")
    private Long idTipoUbicacion;

    @Column(nullable = false, unique = true)
    private String nombre;
}