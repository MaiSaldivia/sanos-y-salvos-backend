package com.sanosysalvos.motor.controller;

import com.sanosysalvos.motor.dto.BusquedaResultDto;
import com.sanosysalvos.motor.dto.HistorialDto;
import com.sanosysalvos.motor.dto.ResumenDto;
import com.sanosysalvos.motor.service.MotorService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/motor")
@RequiredArgsConstructor
@Validated
public class MotorController {

    private final MotorService motorService;

    // Handler para errores de validación de parámetros
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath().toString().replaceFirst(".*\\.", "") + ": " + v.getMessage())
                .findFirst()
                .orElse("Parámetro inválido");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    // Handler para errores de negocio
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @GetMapping("/buscar/{idMascota}")
    public ResponseEntity<BusquedaResultDto> buscarCoincidencias(
            @PathVariable String idMascota,
            @RequestParam(required = false)
            @DecimalMin(value = "0.1", message = "La distancia máxima debe ser al menos 0.1 km")
            @DecimalMax(value = "500.0", message = "La distancia máxima no puede superar 500 km")
            Double maxKm,
            @RequestParam(required = false)
            @Min(value = 0, message = "El score mínimo debe ser 0 o mayor")
            @Max(value = 100, message = "El score mínimo no puede superar 100")
            Integer minScore,
            @RequestParam(required = false)
            @Min(value = 1, message = "El límite debe ser al menos 1")
            @Max(value = 50, message = "El límite no puede superar 50 resultados")
            Integer limite) {

        return ResponseEntity.ok(motorService.buscarCoincidencias(idMascota, maxKm, minScore, limite));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialDto>> getHistorial(
            @RequestParam(required = false, defaultValue = "20")
            @Min(value = 1, message = "El límite debe ser al menos 1")
            @Max(value = 100, message = "El límite no puede superar 100")
            Integer limite) {
        return ResponseEntity.ok(motorService.getHistorial(limite));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenDto> getResumen() {
        return ResponseEntity.ok(motorService.getResumen());
    }
}
