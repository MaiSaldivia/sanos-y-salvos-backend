package com.sanosysalvos.usuarios.dto;

import com.sanosysalvos.usuarios.model.Rol;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolRequest {

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}
