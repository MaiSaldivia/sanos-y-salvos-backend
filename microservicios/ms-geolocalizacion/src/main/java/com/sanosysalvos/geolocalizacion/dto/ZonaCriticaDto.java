package com.sanosysalvos.geolocalizacion.dto;

public interface ZonaCriticaDto {
    String getComuna();
    Long getTotal();
    Long getPerdidas();
    Long getEncontradas();
    Long getReunificadas();
    Double getLatCentro();
    Double getLngCentro();
}
