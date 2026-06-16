package com.sanosysalvos.geolocalizacion.controller;

import com.sanosysalvos.geolocalizacion.dto.EstadisticasGeoDto;
import com.sanosysalvos.geolocalizacion.dto.PuntoMapaDto;
import com.sanosysalvos.geolocalizacion.dto.ZonaCalorDto;
import com.sanosysalvos.geolocalizacion.dto.ZonaCriticaDto;
import com.sanosysalvos.geolocalizacion.service.GeolocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geolocalizacion")
@RequiredArgsConstructor
public class GeolocController {

    private final GeolocService geolocService;

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason() != null ? ex.getReason() : ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error al obtener datos de geolocalización: " + ex.getMessage()));
    }

    @GetMapping("/puntos")
    public ResponseEntity<List<PuntoMapaDto>> obtenerPuntos() {
        return ResponseEntity.ok(geolocService.obtenerPuntos());
    }

    @GetMapping("/zonas-criticas")
    public ResponseEntity<List<ZonaCriticaDto>> contarPorComuna() {
        return ResponseEntity.ok(geolocService.contarPorComuna());
    }

    @GetMapping("/mapa-calor")
    public ResponseEntity<List<ZonaCalorDto>> zonasCalor() {
        return ResponseEntity.ok(geolocService.zonasCalor());
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasGeoDto> getEstadisticasGeo() {
        return ResponseEntity.ok(geolocService.getEstadisticasGeo());
    }
}
