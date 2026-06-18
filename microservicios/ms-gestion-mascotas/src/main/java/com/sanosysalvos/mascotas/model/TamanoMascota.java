package com.sanosysalvos.mascotas.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TamanoMascota {

    Pequeno("Pequeño"),
    Mediano("Mediano"),
    Grande("Grande");

    private final String display;

    TamanoMascota(String display) {
        this.display = display;
    }

    @JsonValue
    public String getDisplay() {
        return display;
    }

    @JsonCreator
    public static TamanoMascota fromString(String value) {
        if (value == null) return null;
        for (TamanoMascota t : values()) {
            // Acepta tanto "Pequeño" como "Pequeno" como "PEQUENO"
            if (t.display.equalsIgnoreCase(value)
                    || t.name().equalsIgnoreCase(value)
                    || t.name().equalsIgnoreCase(value.replace("ñ", "n").replace("Ñ", "N"))) {
                return t;
            }
        }
        throw new IllegalArgumentException("No enum constant TamanoMascota: " + value);
    }
}
