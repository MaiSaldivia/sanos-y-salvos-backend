package com.sanosysalvos.motor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coincidencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coincidencia {

    @Id
    @Column(name = "id_coincidencia", length = 36)
    private String idCoincidencia;

    @Column(name = "id_mascota_a", length = 36, nullable = false)
    private String idMascotaA;

    @Column(name = "id_mascota_b", length = 36, nullable = false)
    private String idMascotaB;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(nullable = false)
    private Boolean notificado = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (notificado == null) {
            notificado = false;
        }
    }
}
