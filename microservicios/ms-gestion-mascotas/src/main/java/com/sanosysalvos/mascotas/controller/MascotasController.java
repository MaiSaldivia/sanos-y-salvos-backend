package com.sanosysalvos.mascotas.controller;

import com.sanosysalvos.mascotas.config.RequireAuth;
import com.sanosysalvos.mascotas.dto.MascotaRequest;
import com.sanosysalvos.mascotas.dto.MascotaResponse;
import com.sanosysalvos.mascotas.dto.ReporteEncuentroRequest;
import com.sanosysalvos.mascotas.dto.RevisionEncuentroRequest;
import com.sanosysalvos.mascotas.service.MascotasService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
@Validated
public class MascotasController {

    private final MascotasService mascotasService;

    // Handler de errores de validación Bean Validation (@Valid + @Validated)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Datos inválidos: " + fieldErrors.values().stream().findFirst().orElse("revisa el formulario"));
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // Handler de errores de validación de parámetros (@Validated en clase)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Parámetro inválido"));
        return ResponseEntity.badRequest().body(body);
    }

    // Handler de errores de negocio (ResponseStatusException — 404, 403, etc.)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @GetMapping
    public ResponseEntity<List<MascotaResponse>> obtenerTodas() {
        return ResponseEntity.ok(mascotasService.obtenerTodas());
    }

    // Alias que usa el frontend: /api/mascotas/busqueda — con filtros opcionales
    @GetMapping("/busqueda")
    public ResponseEntity<List<MascotaResponse>> busqueda(
            @RequestParam(required = false)
            @Size(max = 20, message = "El parámetro estado no puede superar los 20 caracteres")
            String estado,
            @RequestParam(required = false)
            @Size(max = 50, message = "El parámetro tipo_animal no puede superar los 50 caracteres")
            String tipo_animal,
            @RequestParam(required = false)
            @Size(max = 20, message = "El parámetro tamano no puede superar los 20 caracteres")
            String tamano,
            @RequestParam(required = false)
            @Size(max = 80, message = "El parámetro raza no puede superar los 80 caracteres")
            String raza) {
        return ResponseEntity.ok(mascotasService.buscarConFiltros(estado, tipo_animal, tamano, raza));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        return ResponseEntity.ok(mascotasService.getEstadisticas());
    }

    @RequireAuth
    @GetMapping("/usuario/mis-reportes")
    public ResponseEntity<List<MascotaResponse>> getMisMascotas(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(mascotasService.obtenerPorUsuario(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaResponse> obtenerPorId(
            @PathVariable @Size(max = 36, message = "El ID no es válido") String id) {
        return ResponseEntity.ok(mascotasService.obtenerPorId(id));
    }

    @RequireAuth
    @PostMapping
    public ResponseEntity<MascotaResponse> registrarMascota(
            @Valid @RequestBody MascotaRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mascotasService.registrarMascota(request, userId));
    }

    // Alias que usa el frontend: POST /api/mascotas/reportar
    @RequireAuth
    @PostMapping("/reportar")
    public ResponseEntity<MascotaResponse> reportarMascota(
            @Valid @RequestBody MascotaRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mascotasService.registrarMascota(request, userId));
    }

    @RequireAuth
    @PutMapping("/{id}")
    public ResponseEntity<MascotaResponse> actualizarMascota(
            @PathVariable String id,
            @Valid @RequestBody MascotaRequest request,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        String userRole = (String) httpRequest.getAttribute("userRole");
        // Pasamos userId|rol para que el service pueda distinguir moderadores
        String userIdConRol = userId + (userRole != null ? "|" + userRole : "");
        return ResponseEntity.ok(mascotasService.actualizarMascota(id, request, userIdConRol));
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        String userRole = (String) httpRequest.getAttribute("userRole");
        String userIdConRol = userId + (userRole != null ? "|" + userRole : "");
        mascotasService.eliminarMascota(id, userIdConRol);
        return ResponseEntity.noContent().build();
    }

    // ── Reportes de encuentro ───────────────────────────────────────────────

    @RequireAuth
    @PostMapping("/{id}/reportar-encuentro")
    public ResponseEntity<Map<String, Object>> reportarEncuentro(
            @PathVariable String id,
            @Valid @RequestBody ReporteEncuentroRequest request,
            HttpServletRequest httpRequest) {
        mascotasService.reportarEncuentro(id, request);
        return ResponseEntity.ok(Map.of("message", "Solicitud enviada a revisión"));
    }

    @GetMapping("/encuentros/revision")
    public ResponseEntity<List<Map<String, Object>>> getEncuentrosRevision() {
        return ResponseEntity.ok(mascotasService.getEncuentrosRevision());
    }

    @RequireAuth
    @PutMapping("/encuentros/revision/{id}")
    public ResponseEntity<Map<String, Object>> revisarEncuentro(
            @PathVariable String id,
            @Valid @RequestBody RevisionEncuentroRequest request) {
        mascotasService.revisarEncuentro(id, request.getAccion());
        return ResponseEntity.ok(Map.of("message", "Revisión procesada correctamente"));
    }
}
