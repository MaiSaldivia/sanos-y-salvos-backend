package com.sanosysalvos.motor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "mascotas")
@Immutable
@Getter
public class Mascota {

    @Id
    @Column(name = "id_mascota")
    private String idMascota;

    @Column(name = "tipo_animal")
    private String tipoAnimal;

    private String nombre;
    private String raza;

    @Column(name = "color_primario")
    private String colorPrimario;

    private String tamano;
    private Double latitud;
    private Double longitud;
    private String estado;
}
