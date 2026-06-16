package com.sanosysalvos.mascotas.pattern.factory;

public class AlertaExtravio extends Alerta {

    private final String nombre;
    private final String tipoAnimal;
    private final String raza;

    public AlertaExtravio(String idMascota, String contacto, String sector, String nombre, String tipoAnimal, String raza) {
        super(idMascota, contacto, sector);
        this.nombre = nombre != null ? nombre : "Sin nombre";
        this.tipoAnimal = tipoAnimal != null ? tipoAnimal : "Animal";
        this.raza = raza != null ? raza : "Desconocida";
    }

    @Override
    public String getMensaje() {
        return String.format("🔴 PERDIDO: %s \"%s\" (%s) en %s. Contacto: %s", 
                tipoAnimal, nombre, raza, sector, contacto);
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoAnimal() {
        return tipoAnimal;
    }

    public String getRaza() {
        return raza;
    }
}
