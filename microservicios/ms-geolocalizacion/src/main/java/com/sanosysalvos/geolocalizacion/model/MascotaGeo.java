package com.sanosysalvos.geolocalizacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
@Immutable
@Getter
public class MascotaGeo {

    @Id
    @Column(name = "id_mascota")
    private String idMascota;

    @Column(name = "tipo_animal")
    private String tipoAnimal;

    private String nombre;

    private Double latitud;
    private Double longitud;
    private String sector;
    private String comuna;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    private String estado;
}
