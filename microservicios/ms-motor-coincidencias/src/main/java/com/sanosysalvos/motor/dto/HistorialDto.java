package com.sanosysalvos.motor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialDto {
    private String idCoincidencia;
    private Integer score;
    private Double distanciaKm;
    private Boolean notificado;
    private LocalDateTime createdAt;
    
    private String idA;
    private String nombreA;
    private String estadoA;
    private String tipoA;

    private String idB;
    private String nombreB;
    private String estadoB;
    private String tipoB;
}
