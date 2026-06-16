package com.sanosysalvos.motor.repository;

import com.sanosysalvos.motor.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, String> {
    List<Mascota> findByEstadoAndIdMascotaNot(String estado, String idMascota);
}
