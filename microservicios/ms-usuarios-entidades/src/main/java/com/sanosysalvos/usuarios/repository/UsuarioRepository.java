package com.sanosysalvos.usuarios.repository;

import com.sanosysalvos.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.rol as rol, COUNT(u) as total FROM Usuario u WHERE u.activo = true GROUP BY u.rol ORDER BY COUNT(u) DESC")
    List<Object[]> countByRol();
}
