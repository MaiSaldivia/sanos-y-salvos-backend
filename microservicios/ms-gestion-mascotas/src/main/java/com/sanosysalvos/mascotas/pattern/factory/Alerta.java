package com.sanosysalvos.mascotas.pattern.factory;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class Alerta {

    @JsonProperty("id_alerta")
    protected String idAlerta;

    @JsonProperty("id_mascota")
    protected String idMascota;

    protected String contacto;
    protected String sector;
    protected String fecha;

    public Alerta(String idMascota, String contacto, String sector) {
        this.idAlerta = UUID.randomUUID().toString();
        this.idMascota = idMascota;
        this.contacto = contacto;
        this.sector = sector;
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public abstract String getMensaje();

    public String getIdAlerta() {
        return idAlerta;
    }

    public String getIdMascota() {
        return idMascota;
    }

    public String getContacto() {
        return contacto;
    }

    public String getSector() {
        return sector;
    }

    public String getFecha() {
        return fecha;
    }

    // Para simular la salida del tipo en JSON
    @JsonProperty("tipo")
    public String getTipo() {
        return this.getClass().getSimpleName();
    }
}
