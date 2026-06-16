package com.sanosysalvos.motor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDto {
    private Long totalProcesadas;
    private Long coincidenciasAltas;
    private Long coincidenciasMedias;
    private Long ultimaSemana;
}
