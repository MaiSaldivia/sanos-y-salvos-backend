package com.sanosysalvos.mascotas.pattern.factory;

public class AlertaReunificacion extends Alerta {

    private final String nombre;

    public AlertaReunificacion(String idMascota, String contacto, String sector, String nombre) {
        super(idMascota, contacto, sector);
        this.nombre = nombre != null ? nombre : "Sin nombre";
    }

    @Override
    public String getMensaje() {
        return String.format("✅ REUNIFICADO: \"%s\" fue reencontrado con su dueño. Gracias a la comunidad.", nombre);
    }

    public String getNombre() {
        return nombre;
    }
}
