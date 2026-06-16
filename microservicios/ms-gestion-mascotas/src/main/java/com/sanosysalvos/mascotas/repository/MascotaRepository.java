package com.sanosysalvos.mascotas.repository;

import com.sanosysalvos.mascotas.model.EstadoMascota;
import com.sanosysalvos.mascotas.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, String> {

    List<Mascota> findByIdUsuarioOrderByFechaReporteDesc(String idUsuario);
    List<Mascota> findByIdUsuario(String idUsuario);

    @Query("SELECT COUNT(m) FROM Mascota m")
    long countTotal();

    @Query("SELECT COUNT(m) FROM Mascota m WHERE m.estado = :estado")
    long countByEstado(@Param("estado") EstadoMascota estado);

    @Query("SELECT m.tipoAnimal as tipo, COUNT(m) as total FROM Mascota m GROUP BY m.tipoAnimal ORDER BY COUNT(m) DESC")
    List<Object[]> countByTipoAnimal();

    @Query("SELECT COUNT(m) FROM Mascota m WHERE m.fechaReporte >= :dateLimit")
    long countNuevosDesde(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT m FROM Mascota m WHERE " +
           "(:estado IS NULL OR m.estado = :estado) AND " +
           "(:tipoAnimal IS NULL OR LOWER(m.tipoAnimal) = LOWER(:tipoAnimal)) AND " +
           "(:tamano IS NULL OR LOWER(CAST(m.tamano AS string)) = LOWER(:tamano)) AND " +
           "(:raza IS NULL OR LOWER(m.raza) LIKE LOWER(CONCAT('%', :raza, '%'))) " +
           "ORDER BY m.fechaReporte DESC")
    List<Mascota> findWithFilters(
            @Param("estado") EstadoMascota estado,
            @Param("tipoAnimal") String tipoAnimal,
            @Param("tamano") String tamano,
            @Param("raza") String raza);
}
