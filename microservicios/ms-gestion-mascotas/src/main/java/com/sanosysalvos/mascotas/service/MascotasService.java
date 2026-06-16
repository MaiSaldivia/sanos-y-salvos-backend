package com.sanosysalvos.mascotas.service;

import com.sanosysalvos.mascotas.dto.MascotaRequest;
import com.sanosysalvos.mascotas.dto.MascotaResponse;
import com.sanosysalvos.mascotas.dto.ReporteEncuentroRequest;
import com.sanosysalvos.mascotas.model.EstadoMascota;
import com.sanosysalvos.mascotas.model.EstadoRevision;
import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.model.ReporteEncuentro;
import com.sanosysalvos.mascotas.pattern.observer.MascotaReportadaEvent;
import com.sanosysalvos.mascotas.pattern.observer.MascotaReunificadaEvent;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import com.sanosysalvos.mascotas.repository.ReporteEncuentroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MascotasService {

    private final MascotaRepository mascotaRepository;
    private final ReporteEncuentroRepository reporteEncuentroRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<MascotaResponse> obtenerTodas() {
        return mascotaRepository.findAll().stream()
                .map(MascotaResponse::new)
                .collect(Collectors.toList());
    }

    public List<MascotaResponse> buscarConFiltros(String estadoStr, String tipoAnimal, String tamano, String raza) {
        EstadoMascota estado = null;
        if (estadoStr != null && !estadoStr.isBlank()) {
            try {
                estado = EstadoMascota.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Estado inválido: '" + estadoStr + "'. Valores permitidos: PERDIDA, ENCONTRADA, REUNIFICADA");
            }
        }
        String tipoFinal  = (tipoAnimal != null && !tipoAnimal.isBlank()) ? tipoAnimal : null;
        String tamanoFinal= (tamano != null && !tamano.isBlank()) ? tamano : null;
        String razaFinal  = (raza != null && !raza.isBlank()) ? raza : null;

        return mascotaRepository.findWithFilters(estado, tipoFinal, tamanoFinal, razaFinal)
                .stream().map(MascotaResponse::new).collect(Collectors.toList());
    }

    public List<MascotaResponse> obtenerPorUsuario(String idUsuario) {
        return mascotaRepository.findByIdUsuario(idUsuario).stream()
                .map(MascotaResponse::new)
                .collect(Collectors.toList());
    }

    public MascotaResponse obtenerPorId(String idMascota) {
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
        return new MascotaResponse(mascota);
    }

    public java.util.Map<String, Object> getEstadisticas() {
        long total = mascotaRepository.countTotal();
        long perdidas = mascotaRepository.countByEstado(EstadoMascota.PERDIDA);
        long encontradas = mascotaRepository.countByEstado(EstadoMascota.ENCONTRADA);
        long reunificadas = mascotaRepository.countByEstado(EstadoMascota.REUNIFICADA);
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("perdidas", perdidas);
        stats.put("encontradas", encontradas);
        stats.put("reunificadas", reunificadas);
        return stats;
    }

    @Transactional
    public MascotaResponse registrarMascota(MascotaRequest request, String userId) {
        Mascota mascota = Mascota.builder()
                .idMascota(UUID.randomUUID().toString())
                .idUsuario(userId)
                .tipoAnimal(request.getTipoAnimal())
                .raza(request.getRaza())
                .nombre(request.getNombre())
                .colorPrimario(request.getColorPrimario())
                .tamano(request.getTamano())
                .sexo(request.getSexo())
                .edad(request.getEdad())
                .fotoUrl(request.getFotoUrl())
                .latitud(request.getLatitud() != null ? request.getLatitud() : -36.8201)
                .longitud(request.getLongitud() != null ? request.getLongitud() : -73.0444)
                .sector(request.getSector())
                .comuna(request.getComuna())
                .direccion(request.getDireccion())
                .estado(request.getEstado())
                .descripcion(request.getDescripcion())
                .contacto(request.getContacto())
                .telefono(request.getTelefono())
                .fechaReporte(LocalDateTime.now())
                .build();

        Mascota saved = mascotaRepository.save(mascota);
        eventPublisher.publishEvent(new MascotaReportadaEvent(saved));
        return new MascotaResponse(saved);
    }

    @Transactional
    public MascotaResponse actualizarMascota(String idMascota, MascotaRequest request, String userId) {
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        // Admins, refugios y municipalidad pueden modificar cualquier mascota
        // Solo los dueños normales están restringidos a sus propias mascotas
        boolean esModerador = false;
        try {
            // El rol viene en el JWT y se inyecta como atributo de request en el interceptor
            // Para simplicidad verificamos si el usuario es el dueño; si no, asumimos que
            // el controller ya validó el rol antes de llamar aquí.
            // El controller pasa el rol en userId como "userId|rol"
            if (userId != null && userId.contains("|")) {
                String rol = userId.split("\\|")[1];
                esModerador = rol.equals("ADMIN") || rol.equals("REFUGIO") || rol.equals("MUNICIPALIDAD");
                userId = userId.split("\\|")[0];
            }
        } catch (Exception ignored) {}

        if (!esModerador && !mascota.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para modificar esta mascota");
        }

        mascota.setTipoAnimal(request.getTipoAnimal());
        mascota.setRaza(request.getRaza());
        mascota.setNombre(request.getNombre());
        mascota.setColorPrimario(request.getColorPrimario());
        mascota.setTamano(request.getTamano());
        mascota.setSexo(request.getSexo());
        mascota.setEdad(request.getEdad());
        mascota.setFotoUrl(request.getFotoUrl());
        mascota.setLatitud(request.getLatitud());
        mascota.setLongitud(request.getLongitud());
        mascota.setSector(request.getSector());
        mascota.setComuna(request.getComuna());
        mascota.setDireccion(request.getDireccion());
        mascota.setDescripcion(request.getDescripcion());
        mascota.setContacto(request.getContacto());
        mascota.setTelefono(request.getTelefono());

        if (request.getEstado() != null && mascota.getEstado() != request.getEstado()) {
            mascota.setEstado(request.getEstado());
        }

        Mascota updated = mascotaRepository.save(mascota);
        return new MascotaResponse(updated);
    }

    @Transactional
    public void eliminarMascota(String idMascota, String userId) {
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        boolean esModerador = false;
        try {
            if (userId != null && userId.contains("|")) {
                String rol = userId.split("\\|")[1];
                esModerador = rol.equals("ADMIN") || rol.equals("REFUGIO") || rol.equals("MUNICIPALIDAD");
                userId = userId.split("\\|")[0];
            }
        } catch (Exception ignored) {}

        if (!esModerador && !mascota.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permisos para eliminar esta mascota");
        }

        mascotaRepository.delete(mascota);
    }

    // ── Reportes de encuentro ───────────────────────────────────────────────

    @Transactional
    public void reportarEncuentro(String idMascota, ReporteEncuentroRequest request) {
        mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        ReporteEncuentro reporte = ReporteEncuentro.builder()
                .idReporteEncuentro(UUID.randomUUID().toString())
                .idMascota(idMascota)
                .fotoEvidenciaUrl(request.getFotoEvidenciaUrl())
                .encontradaEn(request.getEncontradaEn())
                .contactoNombre(request.getContactoNombre())
                .contactoTelefono(request.getContactoTelefono())
                .estadoRevision(EstadoRevision.EN_REVISION)
                .build();

        reporteEncuentroRepository.save(reporte);
    }

    public List<Map<String, Object>> getEncuentrosRevision() {
        List<Object[]> rows = reporteEncuentroRepository.findReportesJoined(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("idReporteEncuentro", row[0]);
            m.put("idMascota",          row[1]);
            m.put("fotoEvidenciaUrl",   row[2]);
            m.put("encontradaEn",       row[3]);
            m.put("contactoNombre",     row[4]);
            m.put("contactoTelefono",   row[5]);
            m.put("estadoRevision",     row[6] != null ? row[6].toString() : null);
            m.put("fechaReporte",       row[7]);
            m.put("fechaRevision",      row[8]);
            m.put("mascotaNombre",      row[9]);
            m.put("tipoAnimal",         row[10]);
            m.put("mascotaFoto",        row[11]);
            result.add(m);
        }
        return result;
    }

    @Transactional
    public void revisarEncuentro(String idReporte, String accion) {
        ReporteEncuentro reporte = reporteEncuentroRepository.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));

        EstadoRevision nuevoEstado = "APROBAR".equalsIgnoreCase(accion)
                ? EstadoRevision.APROBADO : EstadoRevision.RECHAZADO;

        reporte.setEstadoRevision(nuevoEstado);
        reporte.setFechaRevision(LocalDateTime.now());
        reporteEncuentroRepository.save(reporte);

        // Si se aprueba, marcar mascota como REUNIFICADA
        if (nuevoEstado == EstadoRevision.APROBADO) {
            mascotaRepository.findById(reporte.getIdMascota()).ifPresent(mascota -> {
                mascota.setEstado(EstadoMascota.REUNIFICADA);
                mascotaRepository.save(mascota);
                eventPublisher.publishEvent(new MascotaReunificadaEvent(mascota));
            });
        }
    }
}
