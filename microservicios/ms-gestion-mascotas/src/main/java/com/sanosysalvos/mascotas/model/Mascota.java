package com.sanosysalvos.mascotas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {

    @Id
    @Column(name = "id_mascota", length = 36)
    private String idMascota;

    @Column(name = "tipo_animal", nullable = false, length = 50)
    private String tipoAnimal;

    @Column(length = 80)
    private String raza;

    @Column(length = 80)
    private String nombre;

    @Column(name = "color_primario", length = 60)
    private String colorPrimario;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TamanoMascota tamano;

    @Column(length = 20)
    private String sexo;

    @Column(length = 30)
    private String edad;

    @Column(name = "foto_url", columnDefinition = "LONGTEXT")
    private String fotoUrl;

    private Double latitud;
    private Double longitud;

    @Column(length = 120)
    private String sector;

    @Column(length = 120)
    private String comuna;

    @Column(length = 200)
    private String direccion;

    @Column(name = "fecha_reporte", insertable = false, updatable = false)
    private LocalDateTime fechaReporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMascota estado;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 120)
    private String contacto;

    @Column(length = 30)
    private String telefono;

    @Column(name = "id_usuario", length = 36)
    private String idUsuario;

    @PrePersist
    protected void onCreate() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoMascota.PERDIDA;
        }
    }
}
