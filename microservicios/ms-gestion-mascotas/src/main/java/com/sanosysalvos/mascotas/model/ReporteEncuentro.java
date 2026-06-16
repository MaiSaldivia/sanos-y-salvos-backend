package com.sanosysalvos.mascotas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_encuentro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEncuentro {

    @Id
    @Column(name = "id_reporte_encuentro", length = 36)
    private String idReporteEncuentro;

    @Column(name = "id_mascota", nullable = false, length = 36)
    private String idMascota;

    @Column(name = "foto_evidencia_url", columnDefinition = "LONGTEXT")
    private String fotoEvidenciaUrl;

    @Column(name = "encontrada_en", length = 200)
    private String encontradaEn;

    @Column(name = "contacto_nombre", length = 120)
    private String contactoNombre;

    @Column(name = "contacto_telefono", length = 30)
    private String contactoTelefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision", length = 20)
    private EstadoRevision estadoRevision;

    @Column(name = "fecha_reporte", insertable = false, updatable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @PrePersist
    protected void onCreate() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now();
        }
        if (estadoRevision == null) {
            estadoRevision = EstadoRevision.EN_REVISION;
        }
    }
}
