package com.sanosysalvos.motor.dto;

import com.sanosysalvos.motor.model.Mascota;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaResultDto {
    private Mascota origen;
    private int total;
    private List<CandidatoDto> resultados;
}
