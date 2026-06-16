package com.sanosysalvos.mascotas.pattern.observer;

import com.sanosysalvos.mascotas.model.EstadoMascota;
import lombok.Getter;

@Getter
public class MascotaEstadoCambiadoEvent {
    private final String idMascota;
    private final EstadoMascota estadoAnterior;
    private final EstadoMascota estadoNuevo;

    public MascotaEstadoCambiadoEvent(String idMascota, EstadoMascota estadoAnterior, EstadoMascota estadoNuevo) {
        this.idMascota = idMascota;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }
}
