package com.sanosysalvos.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @JsonProperty("password_actual")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La nueva contraseña debe tener entre 6 y 100 caracteres")
    @JsonProperty("password_nuevo")
    private String passwordNuevo;
}
