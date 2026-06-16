package com.sanosysalvos.motor.service;

import com.sanosysalvos.motor.dto.BusquedaResultDto;
import com.sanosysalvos.motor.dto.CandidatoDto;
import com.sanosysalvos.motor.dto.HistorialDto;
import com.sanosysalvos.motor.dto.ResumenDto;
import com.sanosysalvos.motor.model.Coincidencia;
import com.sanosysalvos.motor.model.Mascota;
import com.sanosysalvos.motor.pattern.ScoringAlgorithm;
import com.sanosysalvos.motor.repository.MascotaRepository;
import com.sanosysalvos.motor.repository.MotorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MotorService {

    private final MotorRepository motorRepository;
    private final MascotaRepository mascotaRepository;
    private final ScoringAlgorithm scoringAlgorithm;

    @Transactional
    public BusquedaResultDto buscarCoincidencias(String idMascota, Double maxKmParam, Integer minScoreParam, Integer limiteParam) {
        double maxKm = maxKmParam != null ? maxKmParam : 20.0;
        int minScore = minScoreParam != null ? minScoreParam : 30;
        int limite = limiteParam != null ? limiteParam : 10;

        Mascota origen = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        String estadoBuscar = "PERDIDA".equals(origen.getEstado()) ? "ENCONTRADA" : "PERDIDA";

        List<Mascota> candidatos = mascotaRepository.findByEstadoAndIdMascotaNot(estadoBuscar, idMascota);

        List<CandidatoDto> resultados = new ArrayList<>();
        for (Mascota c : candidatos) {
            int score = scoringAlgorithm.calcularScore(origen, c);
            double km = scoringAlgorithm.distanciaKm(origen.getLatitud(), origen.getLongitud(), c.getLatitud(), c.getLongitud());
            
            if (score >= minScore && km <= maxKm) {
                BigDecimal bd = new BigDecimal(Double.toString(km));
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                double roundedKm = bd.doubleValue();
                
                resultados.add(new CandidatoDto(c, score, roundedKm));
            }
        }

        resultados.sort((a, b) -> {
            int scoreCompare = b.getScore().compareTo(a.getScore());
            if (scoreCompare != 0) return scoreCompare;
            return a.getDistanciaKm().compareTo(b.getDistanciaKm());
        });

        if (resultados.size() > limite) {
            resultados = resultados.subList(0, limite);
        }

        for (CandidatoDto r : resultados) {
            if (r.getScore() >= 60) {
                guardarCoincidencia(idMascota, r.getMascota().getIdMascota(), r.getScore(), r.getDistanciaKm());
            }
        }

        return new BusquedaResultDto(origen, resultados.size(), resultados);
    }

    private void guardarCoincidencia(String idA, String idB, int score, double km) {
        String keyA = idA.compareTo(idB) < 0 ? idA : idB;
        String keyB = idA.compareTo(idB) < 0 ? idB : idA;

        motorRepository.findByIdMascotaAAndIdMascotaB(keyA, keyB).ifPresentOrElse(
                c -> {
                    // Ya existe, se podría actualizar si cambia el score
                },
                () -> {
                    Coincidencia c = Coincidencia.builder()
                            .idCoincidencia(UUID.randomUUID().toString())
                            .idMascotaA(keyA)
                            .idMascotaB(keyB)
                            .score(score)
                            .distanciaKm(km)
                            .notificado(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    motorRepository.save(c);
                }
        );
    }

    public List<HistorialDto> getHistorial(Integer limite) {
        int lim = (limite != null) ? Math.min(limite, 100) : 20;
        List<Object[]> rows = motorRepository.getHistorial(lim);
        return rows.stream().map(row -> {
            String idCoincidencia = (String) row[0];
            Integer score = (Integer) row[1];
            Double distanciaKm = (Double) row[2];
            Boolean notificado = (row[3] instanceof Boolean) ? (Boolean) row[3] : ((Number) row[3]).intValue() != 0;
            LocalDateTime createdAt = ((Timestamp) row[4]).toLocalDateTime();
            String idA = (String) row[5];
            String nombreA = (String) row[6];
            String estadoA = (String) row[7];
            String tipoA = (String) row[8];
            String idB = (String) row[9];
            String nombreB = (String) row[10];
            String estadoB = (String) row[11];
            String tipoB = (String) row[12];
            
            return new HistorialDto(idCoincidencia, score, distanciaKm, notificado, createdAt,
                    idA, nombreA, estadoA, tipoA, idB, nombreB, estadoB, tipoB);
        }).collect(Collectors.toList());
    }

    public ResumenDto getResumen() {
        Long total = motorRepository.countTotalProcesadas();
        Long altas = motorRepository.countAltas();
        Long medias = motorRepository.countMedias();
        Long recientes = motorRepository.countUltimaSemana();
        return new ResumenDto(total, altas, medias, recientes);
    }
}
