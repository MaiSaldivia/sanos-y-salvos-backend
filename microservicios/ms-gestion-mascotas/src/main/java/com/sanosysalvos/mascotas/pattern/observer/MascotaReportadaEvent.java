package com.sanosysalvos.mascotas.pattern.observer;

import com.sanosysalvos.mascotas.model.Mascota;
import lombok.Getter;

@Getter
public class MascotaReportadaEvent {
    private final Mascota mascota;

    public MascotaReportadaEvent(Mascota mascota) {
        this.mascota = mascota;
    }
}
