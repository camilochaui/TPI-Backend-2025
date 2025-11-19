package org.example.serviciocliente.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "ciudad")
public class ClienteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "El DNI es obligatorio")
    @Positive(message = "El DNI debe ser un número positivo")
    @Column(unique = true)
    private Long dni;

    @NotNull(message = "El teléfono es obligatorio")
    @Positive(message = "El teléfono debe ser un número positivo")
    private Long telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ingresar un correo válido")
    @Column(unique = true)
    private String mail;

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotNull(message = "La altura es obligatoria")
    @Positive(message = "La altura debe ser un número positivo")
    private Integer altura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_localidad", nullable = false)
    private CiudadEntity ciudad;
}