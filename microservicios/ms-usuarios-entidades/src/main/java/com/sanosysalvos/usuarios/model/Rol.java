package com.sanosysalvos.usuarios.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Rol {
    DUENO(Arrays.asList("reportar", "ver_mascotas", "reportar_encuentro")),
    VETERINARIA(Arrays.asList("reportar", "ver_mascotas", "reportar_encuentro", "ver_historial")),
    REFUGIO(Arrays.asList("reportar", "ver_mascotas", "reportar_encuentro", "revisar_encuentros", "ver_estadisticas")),
    MUNICIPALIDAD(Arrays.asList("reportar", "ver_mascotas", "reportar_encuentro", "revisar_encuentros", "ver_estadisticas", "ver_zonas")),
    ADMIN(Arrays.asList("reportar", "ver_mascotas", "reportar_encuentro", "revisar_encuentros", "ver_estadisticas", "ver_zonas", "gestionar_usuarios", "cambiar_roles"));

    private final List<String> permisos;

    Rol(List<String> permisos) {
        this.permisos = permisos;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public boolean puedeHacer(String permiso) {
        return permisos.contains(permiso);
    }

    public boolean esModerador() {
        return this == REFUGIO || this == MUNICIPALIDAD || this == ADMIN;
    }

    public boolean esAdmin() {
        return this == ADMIN;
    }
}
