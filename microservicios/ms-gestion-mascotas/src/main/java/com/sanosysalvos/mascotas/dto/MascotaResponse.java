package com.sanosysalvos.mascotas.dto;

import com.sanosysalvos.mascotas.model.EstadoMascota;
import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.model.TamanoMascota;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MascotaResponse {

    private String idMascota;
    private String tipoAnimal;
    private String raza;
    private String nombre;
    private String colorPrimario;
    private TamanoMascota tamano;
    private String sexo;
    private String edad;
    private String fotoUrl;
    private Double latitud;
    private Double longitud;
    private String sector;
    private String comuna;
    private String direccion;
    private LocalDateTime fechaReporte;
    private EstadoMascota estado;
    private String descripcion;
    private String contacto;
    private String telefono;
    private String idUsuario;

    public MascotaResponse(Mascota mascota) {
        this.idMascota = mascota.getIdMascota();
        this.tipoAnimal = mascota.getTipoAnimal();
        this.raza = mascota.getRaza();
        this.nombre = mascota.getNombre();
        this.colorPrimario = mascota.getColorPrimario();
        this.tamano = mascota.getTamano();
        this.sexo = mascota.getSexo();
        this.edad = mascota.getEdad();
        this.fotoUrl = mascota.getFotoUrl();
        this.latitud = mascota.getLatitud();
        this.longitud = mascota.getLongitud();
        this.sector = mascota.getSector();
        this.comuna = mascota.getComuna();
        this.direccion = mascota.getDireccion();
        this.fechaReporte = mascota.getFechaReporte();
        this.estado = mascota.getEstado();
        this.descripcion = mascota.getDescripcion();
        this.contacto = mascota.getContacto();
        this.telefono = mascota.getTelefono();
        this.idUsuario = mascota.getIdUsuario();
    }
}
