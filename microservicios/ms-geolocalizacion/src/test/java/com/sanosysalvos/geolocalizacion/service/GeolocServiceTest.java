package com.sanosysalvos.geolocalizacion.service;

import com.sanosysalvos.geolocalizacion.dto.PuntoMapaDto;
import com.sanosysalvos.geolocalizacion.dto.ZonaCalorDto;
import com.sanosysalvos.geolocalizacion.repository.GeolocRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeolocServiceTest {

    @Mock
    private GeolocRepository geolocRepository;

    @InjectMocks
    private GeolocService geolocService;

    @Test
    void testZonasCalor() {
        Object[] row1 = new Object[]{-33.4, -70.6, "PERDIDA"};
        Object[] row2 = new Object[]{-33.5, -70.5, "ENCONTRADA"};
        when(geolocRepository.zonasCalorBruto()).thenReturn(Arrays.asList(row1, row2));

        List<ZonaCalorDto> result = geolocService.zonasCalor();

        assertEquals(2, result.size());
        assertEquals(1.0, result.get(0).getWeight()); // PERDIDA -> 1.0
        assertEquals(0.6, result.get(1).getWeight()); // ENCONTRADA -> 0.6
    }
}
