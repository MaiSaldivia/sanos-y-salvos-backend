package com.sanosysalvos.usuarios.controller;

import com.sanosysalvos.usuarios.config.RequireAuth;
import com.sanosysalvos.usuarios.config.RequireRole;
import com.sanosysalvos.usuarios.dto.*;
import com.sanosysalvos.usuarios.model.Rol;
import com.sanosysalvos.usuarios.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    // Handler para errores de validación Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        String firstMessage = fieldErrors.values().stream().findFirst().orElse("Datos inválidos");
        return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(false, "Datos inválidos: " + firstMessage));
    }

    // Handler para excepciones de negocio (duplicados, credenciales incorrectas, etc.)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ResponseWrapper<>(false, ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseWrapper<>(false, "Error interno del servidor: " + ex.getMessage()));
    }

    @PostMapping("/login")
    public ResponseWrapper<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse res = service.login(req.getEmail(), req.getPassword());
        return new ResponseWrapper<>(true, res);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<LoginResponse>> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse res = service.register(req);
        ResponseWrapper<LoginResponse> wrapper = new ResponseWrapper<>(true, res, "Cuenta creada exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapper);
    }

    @GetMapping("/me")
    @RequireAuth
    public ResponseWrapper<UserPublicDto> me(@RequestAttribute("userId") String userId) {
        UserPublicDto u = service.getPerfil(userId);
        return new ResponseWrapper<>(true, u);
    }

    @PutMapping("/me")
    @RequireAuth
    public ResponseWrapper<UserPublicDto> actualizarPerfil(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody ProfileRequest req) {
        UserPublicDto u = service.actualizarPerfil(userId, req);
        return new ResponseWrapper<>(true, u, "Perfil actualizado");
    }

    @PutMapping("/me/password")
    @RequireAuth
    public ResponseWrapper<Void> cambiarPassword(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody PasswordRequest req) {
        service.cambiarPassword(userId, req);
        return new ResponseWrapper<>(true, "Contraseña actualizada correctamente");
    }

    @GetMapping("/usuarios")
    @RequireRole(Rol.ADMIN)
    public ResponseWrapper<List<UserPublicDto>> listarUsuarios() {
        List<UserPublicDto> usuarios = service.listarUsuarios();
        return new ResponseWrapper<>(true, usuarios, usuarios.size());
    }

    @GetMapping("/usuarios/estadisticas")
    @RequireRole(Rol.ADMIN)
    public ResponseWrapper<Map<String, Object>> getEstadisticas() {
        Map<String, Object> stats = service.getEstadisticas();
        return new ResponseWrapper<>(true, stats);
    }

    @PutMapping("/usuarios/{id}/rol")
    @RequireRole(Rol.ADMIN)
    public ResponseWrapper<UserPublicDto> cambiarRol(
            @RequestAttribute("userId") String adminId,
            @PathVariable("id") String idUsuario,
            @Valid @RequestBody RolRequest req) {
        UserPublicDto u = service.cambiarRol(adminId, idUsuario, req.getRol());
        return new ResponseWrapper<>(true, u, "Rol actualizado");
    }

    @PutMapping("/usuarios/{id}/toggle-activo")
    @RequireRole(Rol.ADMIN)
    public ResponseWrapper<UserPublicDto> toggleActivo(
            @RequestAttribute("userId") String adminId,
            @PathVariable("id") String idUsuario) {
        UserPublicDto u = service.toggleActivo(adminId, idUsuario);
        return new ResponseWrapper<>(true, u);
    }
}
