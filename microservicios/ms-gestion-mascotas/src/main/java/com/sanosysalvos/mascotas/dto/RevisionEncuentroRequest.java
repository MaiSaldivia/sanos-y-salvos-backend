package com.sanosysalvos.mascotas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionEncuentroRequest {

    @NotBlank(message = "La acción es obligatoria")
    @Pattern(
        regexp = "^(?i)(APROBAR|RECHAZAR)$",
        message = "La acción debe ser APROBAR o RECHAZAR"
    )
    private String accion;
}
