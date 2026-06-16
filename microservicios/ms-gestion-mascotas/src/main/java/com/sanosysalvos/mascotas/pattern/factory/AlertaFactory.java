package com.sanosysalvos.mascotas.pattern.factory;

import com.sanosysalvos.mascotas.model.EstadoMascota;
import com.sanosysalvos.mascotas.model.Mascota;

public class AlertaFactory {

    public static Alerta crear(EstadoMascota estado, Mascota m) {
        if (estado == null) {
            throw new IllegalArgumentException("Estado desconocido: null");
        }
        switch (estado) {
            case PERDIDA:
                return new AlertaExtravio(m.getIdMascota(), m.getContacto(), m.getSector(), m.getNombre(), m.getTipoAnimal(), m.getRaza());
            case ENCONTRADA:
                return new AlertaHallazgo(m.getIdMascota(), m.getContacto(), m.getSector(), m.getTipoAnimal(), m.getDescripcion());
            case REUNIFICADA:
                return new AlertaReunificacion(m.getIdMascota(), m.getContacto(), m.getSector(), m.getNombre());
            default:
                throw new IllegalArgumentException("Estado desconocido: " + estado);
        }
    }
}
