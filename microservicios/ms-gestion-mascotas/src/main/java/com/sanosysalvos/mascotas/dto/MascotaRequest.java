package com.sanosysalvos.mascotas.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.sanosysalvos.mascotas.model.EstadoMascota;
import com.sanosysalvos.mascotas.model.TamanoMascota;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MascotaRequest {

    // @JsonProperty fuerza el nombre snake_case en la deserialización (porque SNAKE_CASE
    // global convierte el campo Java "tipoAnimal" → busca "tipo_animal" en el JSON).
    // @JsonAlias permite que el frontend también envíe el nombre camelCase sin romper nada.

    @NotBlank(message = "El tipo de animal es obligatorio")
    @Size(max = 50, message = "El tipo de animal no puede superar los 50 caracteres")
    @JsonProperty("tipo_animal")
    @JsonAlias("tipoAnimal")
    private String tipoAnimal;

    @Size(max = 80, message = "La raza no puede superar los 80 caracteres")
    private String raza;

    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    private String nombre;

    @Size(max = 60, message = "El color no puede superar los 60 caracteres")
    @JsonProperty("color_primario")
    @JsonAlias("colorPrimario")
    private String colorPrimario;

    // Nullable — el usuario puede no seleccionar tamaño
    @JsonSetter(nulls = Nulls.SKIP)
    private TamanoMascota tamano;

    @Size(max = 20, message = "El sexo no puede superar los 20 caracteres")
    private String sexo;

    @Size(max = 30, message = "La edad no puede superar los 30 caracteres")
    private String edad;

    // fotoUrl puede ser base64 (LONGTEXT), no se limita en tamaño aquí
    @JsonProperty("foto_url")
    @JsonAlias("fotoUrl")
    private String fotoUrl;

    // Coordenadas validadas por rango geográfico de Chile
    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0",  message = "La latitud debe ser menor o igual a 90")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0",  message = "La longitud debe ser menor o igual a 180")
    private Double longitud;

    @Size(max = 120, message = "El sector no puede superar los 120 caracteres")
    private String sector;

    @Size(max = 120, message = "La comuna no puede superar los 120 caracteres")
    private String comuna;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String direccion;

    @NotNull(message = "El estado es obligatorio")
    private EstadoMascota estado;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @Size(max = 120, message = "El contacto no puede superar los 120 caracteres")
    private String contacto;

    @Pattern(
        regexp = "^[+0-9\\s()\\-]{0,20}$",
        message = "El teléfono solo puede contener dígitos, espacios, +, -, ( ) y máximo 20 caracteres"
    )
    private String telefono;
}
