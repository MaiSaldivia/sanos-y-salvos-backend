package com.sanosysalvos.geolocalizacion.service;

import com.sanosysalvos.geolocalizacion.dto.EstadisticasGeoDto;
import com.sanosysalvos.geolocalizacion.dto.PuntoMapaDto;
import com.sanosysalvos.geolocalizacion.dto.ZonaCalorDto;
import com.sanosysalvos.geolocalizacion.dto.ZonaCriticaDto;
import com.sanosysalvos.geolocalizacion.repository.GeolocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeolocService {

    private final GeolocRepository geolocRepository;

    public List<PuntoMapaDto> obtenerPuntos() {
        return geolocRepository.obtenerPuntos();
    }

    public List<ZonaCriticaDto> contarPorComuna() {
        return geolocRepository.contarPorComuna();
    }

    public List<ZonaCalorDto> zonasCalor() {
        List<Object[]> rawZonas = geolocRepository.zonasCalorBruto();
        return rawZonas.stream().map(obj -> {
            Double lat = (Double) obj[0];
            Double lng = (Double) obj[1];
            String estado = (String) obj[2];
            Double weight = "PERDIDA".equals(estado) ? 1.0 : 0.6;
            return new ZonaCalorDto(lat, lng, weight);
        }).collect(Collectors.toList());
    }

    public EstadisticasGeoDto getEstadisticasGeo() {
        Long total = geolocRepository.countTotalConGeolocalizacion();
        Long comunas = geolocRepository.countComunasConReportes();
        Long activas = geolocRepository.countCasosActivos();
        String zona = geolocRepository.getZonaMasActivaSemana();

        if (zona == null || zona.isBlank()) {
            zona = "Sin datos";
        }

        return new EstadisticasGeoDto(total, comunas, activas, zona);
    }
}
