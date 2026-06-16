package com.sanosysalvos.usuarios.service;

import com.sanosysalvos.usuarios.config.JwtUtil;
import com.sanosysalvos.usuarios.dto.*;
import com.sanosysalvos.usuarios.model.Rol;
import com.sanosysalvos.usuarios.model.Usuario;
import com.sanosysalvos.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@[\\w-\\.]+\\.[a-z]{2,4}$", Pattern.CASE_INSENSITIVE);

    private UserPublicDto mapToPublicDto(Usuario u) {
        return UserPublicDto.builder()
                .idUsuario(u.getIdUsuario())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .activo(u.getActivo())
                .permisos(u.getRol().getPermisos())
                .createdAt(u.getCreatedAt())
                .build();
    }

    // ── Login ──────────────────────────────────────────────────
    public LoginResponse login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es requerido");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es requerida");
        }

        Usuario u = repo.findByEmailAndActivoTrue(email.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos"));

        // En producción se usaría bcrypt, aquí se usa texto plano igual que el original
        if (!u.getPasswordHash().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }

        String token = jwtUtil.generarToken(u.getIdUsuario(), u.getNombre(), u.getEmail(), u.getRol());

        return LoginResponse.builder()
                .token(token)
                .usuario(mapToPublicDto(u))
                .build();
    }

    // ── Registro ───────────────────────────────────────────────
    public LoginResponse register(RegisterRequest req) {
        if (req.getNombre() == null || req.getNombre().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre debe tener al menos 2 caracteres");
        }
        if (req.getEmail() == null || !EMAIL_PATTERN.matcher(req.getEmail()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email no es válido");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
        }

        boolean existe = repo.existsByEmail(req.getEmail().toLowerCase().trim());
        if (existe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }

        Rol rolFinal = (req.getRol() != null && req.getRol() != Rol.ADMIN) ? req.getRol() : Rol.DUENO;

        Usuario nuevo = Usuario.builder()
                .idUsuario(UUID.randomUUID().toString())
                .nombre(req.getNombre().trim())
                .email(req.getEmail().toLowerCase().trim())
                .passwordHash(req.getPassword()) // texto plano
                .rol(rolFinal)
                .activo(true)
                .build();

        Usuario guardado = repo.save(nuevo);
        String token = jwtUtil.generarToken(guardado.getIdUsuario(), guardado.getNombre(), guardado.getEmail(), guardado.getRol());

        // Recargamos el usuario para tener createdAt seteado por BD (o PrePersist)
        guardado = repo.findById(guardado.getIdUsuario()).orElse(guardado);

        return LoginResponse.builder()
                .token(token)
                .usuario(mapToPublicDto(guardado))
                .build();
    }

    // ── Obtener perfil propio ──────────────────────────────────
    public UserPublicDto getPerfil(String idUsuario) {
        Usuario u = repo.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return mapToPublicDto(u);
    }

    // ── Cambiar contraseña ─────────────────────────────────────
    public void cambiarPassword(String idUsuario, PasswordRequest req) {
        if (req.getPasswordActual() == null || req.getPasswordActual().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es requerida");
        }
        if (req.getPasswordNuevo() == null || req.getPasswordNuevo().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 6 caracteres");
        }
        if (req.getPasswordActual().equals(req.getPasswordNuevo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser diferente a la actual");
        }

        Usuario u = repo.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (!u.getPasswordHash().equals(req.getPasswordActual())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña actual incorrecta");
        }

        u.setPasswordHash(req.getPasswordNuevo());
        repo.save(u);
    }

    // ── Actualizar nombre ──────────────────────────────────────
    public UserPublicDto actualizarPerfil(String idUsuario, ProfileRequest req) {
        if (req.getNombre() == null || req.getNombre().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre debe tener al menos 2 caracteres");
        }

        Usuario u = repo.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        u.setNombre(req.getNombre().trim());
        Usuario guardado = repo.save(u);
        return mapToPublicDto(guardado);
    }

    // ── Admin: listar todos los usuarios ──────────────────────
    public List<UserPublicDto> listarUsuarios() {
        return repo.findAll().stream()
                .map(this::mapToPublicDto)
                .collect(Collectors.toList());
    }

    // ── Admin: cambiar rol ─────────────────────────────────────
    public UserPublicDto cambiarRol(String idAdmin, String idUsuario, Rol nuevoRol) {
        if (idAdmin.equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol");
        }

        Usuario u = repo.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        u.setRol(nuevoRol);
        Usuario guardado = repo.save(u);
        return mapToPublicDto(guardado);
    }

    // ── Admin: activar/desactivar usuario ─────────────────────
    public UserPublicDto toggleActivo(String idAdmin, String idUsuario) {
        if (idAdmin.equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta");
        }

        Usuario u = repo.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        u.setActivo(!u.getActivo());
        Usuario guardado = repo.save(u);
        return mapToPublicDto(guardado);
    }

    // ── Admin: estadísticas ────────────────────────────────────
    public Map<String, Object> getEstadisticas() {
        List<Object[]> counts = repo.countByRol();
        long total = 0;
        List<Map<String, Object>> porRol = new ArrayList<>();

        for (Object[] row : counts) {
            Rol rol = (Rol) row[0];
            long count = (long) row[1];
            total += count;

            Map<String, Object> rMap = new HashMap<>();
            rMap.put("rol", rol.name());
            rMap.put("total", count);
            porRol.add(rMap);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("por_rol", porRol);
        return stats;
    }
}
