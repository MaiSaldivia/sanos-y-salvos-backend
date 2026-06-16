package com.sanosysalvos.geolocalizacion.dto;

import java.time.LocalDateTime;

public interface PuntoMapaDto {
    String getId();
    String getNombre();
    String getTipoAnimal();
    String getEstado();
    Double getLat();
    Double getLng();
    String getSector();
    String getComuna();
    String getFotoUrl();
    LocalDateTime getFechaReporte();
}
