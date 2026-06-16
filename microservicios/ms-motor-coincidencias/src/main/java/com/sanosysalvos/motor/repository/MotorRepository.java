package com.sanosysalvos.motor.repository;

import com.sanosysalvos.motor.model.Coincidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotorRepository extends JpaRepository<Coincidencia, String> {

    Optional<Coincidencia> findByIdMascotaAAndIdMascotaB(String idMascotaA, String idMascotaB);

    @Query(value = "SELECT c.id_coincidencia as idCoincidencia, c.score, c.distancia_km as distanciaKm, c.notificado, c.created_at as createdAt, " +
            "ma.id_mascota AS idA, ma.nombre AS nombreA, ma.estado AS estadoA, ma.tipo_animal AS tipoA, " +
            "mb.id_mascota AS idB, mb.nombre AS nombreB, mb.estado AS estadoB, mb.tipo_animal AS tipoB " +
            "FROM coincidencias c " +
            "JOIN mascotas ma ON c.id_mascota_a = ma.id_mascota " +
            "JOIN mascotas mb ON c.id_mascota_b = mb.id_mascota " +
            "ORDER BY c.created_at DESC LIMIT ?1", nativeQuery = true)
    List<Object[]> getHistorial(int limite);

    @Query(value = "SELECT COUNT(*) FROM coincidencias", nativeQuery = true)
    Long countTotalProcesadas();

    @Query(value = "SELECT COUNT(*) FROM coincidencias WHERE score >= 70", nativeQuery = true)
    Long countAltas();

    @Query(value = "SELECT COUNT(*) FROM coincidencias WHERE score >= 50 AND score < 70", nativeQuery = true)
    Long countMedias();

    @Query(value = "SELECT COUNT(*) FROM coincidencias WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)", nativeQuery = true)
    Long countUltimaSemana();
}
