package com.sanosysalvos.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @Column(name = "id_usuario", length = 36)
    private String idUsuario;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
        if (rol == null) {
            rol = Rol.DUENO;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
