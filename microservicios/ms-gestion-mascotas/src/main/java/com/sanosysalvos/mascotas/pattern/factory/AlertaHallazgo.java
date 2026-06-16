package com.sanosysalvos.mascotas.pattern.factory;

public class AlertaHallazgo extends Alerta {

    private final String tipoAnimal;
    private final String descripcion;

    public AlertaHallazgo(String idMascota, String contacto, String sector, String tipoAnimal, String descripcion) {
        super(idMascota, contacto, sector);
        this.tipoAnimal = tipoAnimal != null ? tipoAnimal : "Animal";
        this.descripcion = descripcion != null ? descripcion : "";
    }

    @Override
    public String getMensaje() {
        return String.format("🟢 ENCONTRADO: %s en %s. %s Contacto: %s", 
                tipoAnimal, sector, descripcion, contacto);
    }

    public String getTipoAnimal() {
        return tipoAnimal;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
