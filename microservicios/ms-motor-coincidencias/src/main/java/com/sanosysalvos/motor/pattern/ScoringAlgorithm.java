package com.sanosysalvos.motor.pattern;

import com.sanosysalvos.motor.model.Mascota;
import org.springframework.stereotype.Component;

@Component
public class ScoringAlgorithm {

    public double distanciaKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public int calcularScore(Mascota origen, Mascota candidato) {
        if (origen.getTipoAnimal() == null || candidato.getTipoAnimal() == null ||
            !origen.getTipoAnimal().equalsIgnoreCase(candidato.getTipoAnimal())) {
            return 0;
        }

        int score = 30; // tipo_animal coincide

        if (origen.getRaza() != null && origen.getRaza().equalsIgnoreCase(candidato.getRaza())) {
            score += 30;
        }
        if (origen.getColorPrimario() != null && origen.getColorPrimario().equalsIgnoreCase(candidato.getColorPrimario())) {
            score += 20;
        }
        if (origen.getTamano() != null && origen.getTamano().equalsIgnoreCase(candidato.getTamano())) {
            score += 10;
        }

        double km = distanciaKm(origen.getLatitud(), origen.getLongitud(), candidato.getLatitud(), candidato.getLongitud());
        if (km < 2) {
            score += 10;
        } else if (km < 5) {
            score += 5;
        }

        return score;
    }
}
