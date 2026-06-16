package com.sanosysalvos.mascotas.repository;

import com.sanosysalvos.mascotas.model.EstadoRevision;
import com.sanosysalvos.mascotas.model.ReporteEncuentro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteEncuentroRepository extends JpaRepository<ReporteEncuentro, String> {

    long countByEstadoRevision(EstadoRevision estadoRevision);

    @Query("SELECT re.idReporteEncuentro as idReporteEncuentro, re.idMascota as idMascota, re.fotoEvidenciaUrl as fotoEvidenciaUrl, " +
           "re.encontradaEn as encontradaEn, re.contactoNombre as contactoNombre, re.contactoTelefono as contactoTelefono, " +
           "re.estadoRevision as estadoRevision, re.fechaReporte as fechaReporte, re.fechaRevision as fechaRevision, " +
           "m.nombre as mascotaNombre, m.tipoAnimal as tipoAnimal, m.fotoUrl as mascotaFoto " +
           "FROM ReporteEncuentro re LEFT JOIN Mascota m ON re.idMascota = m.idMascota " +
           "WHERE (:estadoRevision IS NULL OR re.estadoRevision = :estadoRevision) " +
           "ORDER BY re.fechaReporte DESC")
    List<Object[]> findReportesJoined(@Param("estadoRevision") EstadoRevision estadoRevision);
}
