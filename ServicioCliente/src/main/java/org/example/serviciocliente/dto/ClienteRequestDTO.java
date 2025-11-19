package org.example.serviciocliente.dto;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "El DNI es obligatorio")
    private Long dni;

    @NotNull(message = "El teléfono es obligatorio")
    private Long telefono;

    @Email(message = "Debe ingresar un correo válido")
    private String mail;

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotNull(message = "La altura es obligatoria")
    private Integer altura;

    @NotNull(message = "La ciudad es obligatoria")
    private Long idCiudad;
}
