package com.sanosysalvos.geolocalizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZonaCalorDto {
    private Double lat;
    private Double lng;
    private Double weight;
}
