package com.sanosysalvos.geolocalizacion.repository;

import com.sanosysalvos.geolocalizacion.dto.PuntoMapaDto;
import com.sanosysalvos.geolocalizacion.dto.ZonaCriticaDto;
import com.sanosysalvos.geolocalizacion.model.MascotaGeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeolocRepository extends JpaRepository<MascotaGeo, String> {

    @Query(value = "SELECT id_mascota AS id, nombre, tipo_animal AS tipoAnimal, estado, latitud AS lat, longitud AS lng, sector, comuna, foto_url AS fotoUrl, fecha_reporte AS fechaReporte " +
            "FROM mascotas WHERE latitud IS NOT NULL AND longitud IS NOT NULL ORDER BY fecha_reporte DESC", nativeQuery = true)
    List<PuntoMapaDto> obtenerPuntos();

    @Query(value = "SELECT COALESCE(NULLIF(TRIM(comuna), ''), 'Sin especificar') AS comuna, " +
            "COUNT(*) AS total, " +
            "SUM(estado = 'PERDIDA') AS perdidas, " +
            "SUM(estado = 'ENCONTRADA') AS encontradas, " +
            "SUM(estado = 'REUNIFICADA') AS reunificadas, " +
            "AVG(latitud) AS latCentro, " +
            "AVG(longitud) AS lngCentro " +
            "FROM mascotas WHERE latitud IS NOT NULL " +
            "GROUP BY COALESCE(NULLIF(TRIM(comuna), ''), 'Sin especificar') " +
            "ORDER BY total DESC", nativeQuery = true)
    List<ZonaCriticaDto> contarPorComuna();

    @Query(value = "SELECT latitud, longitud, estado, fecha_reporte FROM mascotas WHERE latitud IS NOT NULL AND longitud IS NOT NULL AND estado IN ('PERDIDA', 'ENCONTRADA')", nativeQuery = true)
    List<Object[]> zonasCalorBruto();

    @Query(value = "SELECT COUNT(*) FROM mascotas WHERE latitud IS NOT NULL", nativeQuery = true)
    Long countTotalConGeolocalizacion();

    @Query(value = "SELECT COUNT(DISTINCT NULLIF(TRIM(comuna), '')) FROM mascotas", nativeQuery = true)
    Long countComunasConReportes();

    @Query(value = "SELECT COUNT(*) FROM mascotas WHERE estado != 'REUNIFICADA'", nativeQuery = true)
    Long countCasosActivos();

    @Query(value = "SELECT comuna FROM mascotas WHERE fecha_reporte >= DATE_SUB(NOW(), INTERVAL 7 DAY) AND estado = 'PERDIDA' GROUP BY comuna ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    String getZonaMasActivaSemana();
}
