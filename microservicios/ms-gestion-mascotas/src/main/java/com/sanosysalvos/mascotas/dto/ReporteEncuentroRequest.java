package com.sanosysalvos.mascotas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteEncuentroRequest {

    // URL o base64 de foto evidencia — opcional
    @JsonProperty("foto_evidencia_url")
    private String fotoEvidenciaUrl;

    @NotBlank(message = "La ubicación donde fue encontrada es obligatoria")
    @Size(max = 200, message = "La ubicación no puede superar los 200 caracteres")
    @JsonProperty("encontrada_en")
    private String encontradaEn;

    @NotBlank(message = "El nombre de contacto es obligatorio")
    @Size(max = 120, message = "El nombre de contacto no puede superar los 120 caracteres")
    @JsonProperty("contacto_nombre")
    private String contactoNombre;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Pattern(
        regexp = "^[+0-9\\s()\\-]{7,20}$",
        message = "El teléfono debe tener entre 7 y 20 caracteres y solo puede contener dígitos, +, -, ( ) y espacios"
    )
    @JsonProperty("contacto_telefono")
    private String contactoTelefono;
}
