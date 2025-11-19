package org.example.servicioenvios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRequestDTO {

    // --- Datos del Cliente (Completos) ---
    @NotNull(message = "El DNI del cliente es obligatorio")
    @Positive(message = "El DNI debe ser un número positivo")
    private Long dniCliente;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    @NotBlank(message = "El apellido del cliente es obligatorio")
    private String apellidoCliente;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "Debe ingresar un correo válido")
    private String emailCliente;

    @NotNull(message = "El teléfono del cliente es obligatorio")
    private Long telefonoCliente;

    @NotBlank(message = "La calle del cliente es obligatoria")
    private String calleCliente;

    @NotNull(message = "La altura de la calle del cliente es obligatoria")
    @Positive
    private Integer alturaCliente;

    @NotNull(message = "El ID de la ciudad del cliente es obligatorio")
    private Long idCiudadCliente; // <-- ¡EL CAMPO QUE FALTABA!

    // --- Datos del Contenedor ---
    @NotBlank(message = "La identificación única del contenedor es obligatoria")
    private String idContenedor;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser positivo")
    private Double peso;

    @NotNull(message = "El volumen es obligatorio")
    @Positive(message = "El volumen debe ser positivo")
    private Double volumen;

    // --- Ubicación de Origen (Cliente) ---
    @NotBlank(message = "La dirección de origen es obligatoria")
    private String origenDireccion;
    @NotNull(message = "La latitud de origen es obligatoria")
    private Double origenLatitud;
    @NotNull(message = "La longitud de origen es obligatoria")
    private Double origenLongitud;

    // --- Ubicación de Destino (Cliente) ---
    @NotBlank(message = "La dirección de destino es obligatoria")
    private String destinoDireccion;
    @NotNull(message = "La latitud de destino es obligatoria")
    private Double destinoLatitud;
    @NotNull(message = "La longitud de destino es obligatoria")
    private Double destinoLongitud;
}