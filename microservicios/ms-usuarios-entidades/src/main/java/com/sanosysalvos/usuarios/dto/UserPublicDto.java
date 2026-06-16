package com.sanosysalvos.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanosysalvos.usuarios.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPublicDto {

    @JsonProperty("id_usuario")
    private String idUsuario;

    private String nombre;
    private String email;
    private Rol rol;
    private Boolean activo;
    private List<String> permisos;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
