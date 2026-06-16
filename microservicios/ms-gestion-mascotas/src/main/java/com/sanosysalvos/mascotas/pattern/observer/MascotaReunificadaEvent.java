package com.sanosysalvos.mascotas.pattern.observer;

import com.sanosysalvos.mascotas.model.Mascota;
import lombok.Getter;

@Getter
public class MascotaReunificadaEvent {
    private final Mascota mascota;

    public MascotaReunificadaEvent(Mascota mascota) {
        this.mascota = mascota;
    }
}
