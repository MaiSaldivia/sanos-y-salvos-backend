package com.sanosysalvos.motor.dto;

import com.sanosysalvos.motor.model.Mascota;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoDto {
    private Mascota mascota;
    private Integer score;
    private Double distanciaKm;
}
