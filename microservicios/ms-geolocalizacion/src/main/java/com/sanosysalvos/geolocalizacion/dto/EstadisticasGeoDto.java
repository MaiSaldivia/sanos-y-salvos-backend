package com.sanosysalvos.geolocalizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasGeoDto {
    private Long totalConGeolocalizacion;
    private Long comunasConReportes;
    private Long casosActivos;
    private String zonaMasActivaSemana;
}
