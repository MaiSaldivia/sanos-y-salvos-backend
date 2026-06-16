package com.sanosysalvos.motor.service;

import com.sanosysalvos.motor.dto.BusquedaResultDto;
import com.sanosysalvos.motor.dto.ResumenDto;
import com.sanosysalvos.motor.model.Coincidencia;
import com.sanosysalvos.motor.model.Mascota;
import com.sanosysalvos.motor.pattern.ScoringAlgorithm;
import com.sanosysalvos.motor.repository.MascotaRepository;
import com.sanosysalvos.motor.repository.MotorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios del MotorService — cubre buscarCoincidencias, getHistorial y getResumen.
 * Se usa Mockito para aislar repositorios y el algoritmo de scoring real (Spy).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MotorService — Tests Unitarios")
class MotorServiceTest {

    @Mock
    private MotorRepository motorRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    // Usamos Spy para que el algoritmo real de Haversine/scoring funcione,
    // pero podamos verificar interacciones sobre él si es necesario.
    @Spy
    private ScoringAlgorithm scoringAlgorithm;

    @InjectMocks
    private MotorService motorService;

    // ── Mascotas de prueba ─────────────────────────────────────────────────────
    private Mascota mascotaPerdida;
    private Mascota mascotaEncontrada1;
    private Mascota mascotaEncontrada2;
    private Mascota mascotaDistinta;

    @BeforeEach
    void setUp() {
        // Mascota origen: perro perdido en Santiago centro
        mascotaPerdida = buildMascota("m-001", "Perro", "Labrador", "Amarillo", "GRANDE", -33.4489, -70.6693, "PERDIDA");
        // Candidato 1: perro encontrado MUY cerca (mismo barrio) — score alto esperado
        mascotaEncontrada1 = buildMascota("m-002", "Perro", "Labrador", "Amarillo", "GRANDE", -33.4495, -70.6700, "ENCONTRADA");
        // Candidato 2: perro encontrado lejos pero misma raza — score medio
        mascotaEncontrada2 = buildMascota("m-003", "Perro", "Labrador", "Negro", "GRANDE", -33.5200, -70.7000, "ENCONTRADA");
        // Candidato descartado: gato — tipo distinto, score 0
        mascotaDistinta = buildMascota("m-004", "Gato", "Siamés", "Blanco", "PEQUEÑO", -33.4490, -70.6695, "ENCONTRADA");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. buscarCoincidencias — flujo exitoso con resultados
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarCoincidencias → retorna candidatos ordenados por score DESC")
    void testBuscarCoincidencias_ConResultados() {
        when(mascotaRepository.findById("m-001")).thenReturn(Optional.of(mascotaPerdida));
        when(mascotaRepository.findByEstadoAndIdMascotaNot("ENCONTRADA", "m-001"))
                .thenReturn(Arrays.asList(mascotaEncontrada1, mascotaEncontrada2));
        when(motorRepository.findByIdMascotaAAndIdMascotaB(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(motorRepository.save(any(Coincidencia.class))).thenAnswer(i -> i.getArgument(0));

        BusquedaResultDto resultado = motorService.buscarCoincidencias("m-001", 50.0, 10, 10);

        assertNotNull(resultado, "El resultado no debe ser null");
        assertEquals(mascotaPerdida, resultado.getOrigen(), "La mascota origen debe coincidir");
        assertTrue(resultado.getTotal() > 0, "Debe haber al menos un candidato");

        // El primer resultado debe tener score >= al segundo (orden DESC)
        if (resultado.getResultados().size() >= 2) {
            int score0 = resultado.getResultados().get(0).getScore();
            int score1 = resultado.getResultados().get(1).getScore();
            assertTrue(score0 >= score1, "Los resultados deben estar ordenados por score descendente");
        }
    }

    @Test
    @DisplayName("buscarCoincidencias → mascota exactamente igual tiene score máximo (100)")
    void testBuscarCoincidencias_ScoreMaximoConCandidatoIdentico() {
        when(mascotaRepository.findById("m-001")).thenReturn(Optional.of(mascotaPerdida));
        when(mascotaRepository.findByEstadoAndIdMascotaNot("ENCONTRADA", "m-001"))
                .thenReturn(Collections.singletonList(mascotaEncontrada1));
        when(motorRepository.findByIdMascotaAAndIdMascotaB(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(motorRepository.save(any(Coincidencia.class))).thenAnswer(i -> i.getArgument(0));

        BusquedaResultDto resultado = motorService.buscarCoincidencias("m-001", 50.0, 0, 10);

        assertFalse(resultado.getResultados().isEmpty(), "Debe haber resultados");
        // tipo+raza+color+tamano+distancia <2km = 30+30+20+10+10 = 100
        assertEquals(100, resultado.getResultados().get(0).getScore(),
                "Un perro labrador amarillo grande a < 2 km debe obtener score 100");
    }

    @Test
    @DisplayName("buscarCoincidencias → candidatos de tipo distinto son descartados (score 0)")
    void testBuscarCoincidencias_DescartaCandidatosDeDistintoTipo() {
        when(mascotaRepository.findById("m-001")).thenReturn(Optional.of(mascotaPerdida));
        when(mascotaRepository.findByEstadoAndIdMascotaNot("ENCONTRADA", "m-001"))
                .thenReturn(Collections.singletonList(mascotaDistinta)); // gato
        when(motorRepository.findByIdMascotaAAndIdMascotaB(anyString(), anyString()))
                .thenReturn(Optional.empty());

        BusquedaResultDto resultado = motorService.buscarCoincidencias("m-001", 50.0, 10, 10);

        // El gato tiene score 0, menor que minScore=10, por lo que no aparece
        assertEquals(0, resultado.getTotal(), "No debe haber candidatos si el único tiene score 0");
        verify(motorRepository, never()).save(any(Coincidencia.class));
    }

    @Test
    @DisplayName("buscarCoincidencias → sin candidatos disponibles retorna lista vacía")
    void testBuscarCoincidencias_SinCandidatos() {
        when(mascotaRepository.findById("m-001")).thenReturn(Optional.of(mascotaPerdida));
        when(mascotaRepository.findByEstadoAndIdMascotaNot("ENCONTRADA", "m-001"))
                .thenReturn(Collections.emptyList());

        BusquedaResultDto resultado = motorService.buscarCoincidencias("m-001", 20.0, 30, 10);

        assertNotNull(resultado);
        assertEquals(0, resultado.getTotal());
        assertTrue(resultado.getResultados().isEmpty());
        verify(motorRepository, never()).save(any(Coincidencia.class));
    }

    @Test
    @DisplayName("buscarCoincidencias → lanza excepción si la mascota origen no existe")
    void testBuscarCoincidencias_MascotaNoEncontrada() {
        when(mascotaRepository.findById("no-existe")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> motorService.buscarCoincidencias("no-existe", 20.0, 30, 10));

        assertEquals("Mascota no encontrada", ex.getMessage());
    }

    @Test
    @DisplayName("buscarCoincidencias → candidato fuera del radio máximo es filtrado")
    void testBuscarCoincidencias_FiltraPorRadioKm() {
        // Mascota en Buenos Aires — más de 1000 km de distancia
        Mascota muyLejos = buildMascota("m-999", "Perro", "Labrador", "Amarillo", "GRANDE",
                -34.6037, -58.3816, "ENCONTRADA");

        when(mascotaRepository.findById("m-001")).thenReturn(Optional.of(mascotaPerdida));
        when(mascotaRepository.findByEstadoAndIdMascotaNot("ENCONTRADA", "m-001"))
                .thenReturn(Collections.singletonList(muyLejos));
        when(motorRepository.findByIdMascotaAAndIdMascotaB(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Radio muy pequeño: 5 km
        BusquedaResultDto resultado = motorService.buscarCoincidencias("m-001", 5.0, 30, 10);

        assertEquals(0, resultado.getTotal(), "La mascota a más de 5 km debe ser filtrada");
    }

    @Test
    @DisplayName("buscarCoincidencias → respeta parámetro límite de resultados")
    void testBuscarCoincidencias_RespetaLimite() {
        // Creamos 5 candidatos muy parecidos
        List<Mascota> candidatos = Arrays.asList(
                buildMascota("c-1", "Perro", "Labrador", "Amarillo", "GRANDE", -33.449, -70.669, "ENCONTRADA"),
                buildMascota("c-2", "Perro", "Labrador", "Amarillo", "GRANDE", -33.450, -70.670, "ENCONTRADA"),
                buildMascota("c-3", "Perro", "Labrador", "Amarillo", "GRANDE", -33.451, -70.671, "ENCONTRADA"),
                buildMascota("c-4", "Perro", "Labrador", "Amarillo", "GRANDE", -33.452, -70.672, "ENCONTRADA"),
                buildMascota("c-5", "Perro", "Labrador", "Amarillo", "GRANDE", -33.453, -70.673, "ENCONTRADA")
        );

        when(mascotaRepository.findById("m-001")).thenReturn(Optional.of(mascotaPerdida));
        when(mascotaRepository.findByEstadoAndIdMascotaNot("ENCONTRADA", "m-001")).thenReturn(candidatos);
        when(motorRepository.findByIdMascotaAAndIdMascotaB(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(motorRepository.save(any(Coincidencia.class))).thenAnswer(i -> i.getArgument(0));

        // Pedimos máximo 2 resultados
        BusquedaResultDto resultado = motorService.buscarCoincidencias("m-001", 50.0, 0, 2);

        assertTrue(resultado.getResultados().size() <= 2,
                "No debe devolver más resultados que el límite solicitado");
    }

    @Test
    @DisplayName("buscarCoincidencias → coincidencia ya existente no se vuelve a guardar")
    void testBuscarCoincidencias_NoGuardaDuplicados() {
        Coincidencia existente = Coincidencia.builder()
                .idCoincidencia("dup-001")
                .idMascotaA("m-001")
                .idMascotaB("m-002")
                .score(90)
                .distanciaKm(0.1)
                .notificado(false)
                .build();

        when(mascotaRepository.findById("m-001")).thenReturn(Optional.of(mascotaPerdida));
        when(mascotaRepository.findByEstadoAndIdMascotaNot("ENCONTRADA", "m-001"))
                .thenReturn(Collections.singletonList(mascotaEncontrada1));
        when(motorRepository.findByIdMascotaAAndIdMascotaB(anyString(), anyString()))
                .thenReturn(Optional.of(existente)); // ya existe

        motorService.buscarCoincidencias("m-001", 50.0, 0, 10);

        // Como ya existe, NO debe llamarse save()
        verify(motorRepository, never()).save(any(Coincidencia.class));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. getResumen — estadísticas generales
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getResumen → retorna totales correctos desde el repositorio")
    void testGetResumen_RetornaDatos() {
        when(motorRepository.countTotalProcesadas()).thenReturn(42L);
        when(motorRepository.countAltas()).thenReturn(15L);
        when(motorRepository.countMedias()).thenReturn(10L);
        when(motorRepository.countUltimaSemana()).thenReturn(5L);

        ResumenDto resumen = motorService.getResumen();

        assertNotNull(resumen);
        assertEquals(42L, resumen.getTotalProcesadas());
        assertEquals(15L, resumen.getCoincidenciasAltas());
        assertEquals(10L, resumen.getCoincidenciasMedias());
        assertEquals(5L, resumen.getUltimaSemana());
    }

    @Test
    @DisplayName("getResumen → funciona correctamente cuando no hay coincidencias (todo en 0)")
    void testGetResumen_SinDatos() {
        when(motorRepository.countTotalProcesadas()).thenReturn(0L);
        when(motorRepository.countAltas()).thenReturn(0L);
        when(motorRepository.countMedias()).thenReturn(0L);
        when(motorRepository.countUltimaSemana()).thenReturn(0L);

        ResumenDto resumen = motorService.getResumen();

        assertNotNull(resumen);
        assertEquals(0L, resumen.getTotalProcesadas());
        assertEquals(0L, resumen.getCoincidenciasAltas());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. ScoringAlgorithm — tests directos del algoritmo (no del service)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ScoringAlgorithm → score 0 cuando los tipos no coinciden")
    void testScoring_TipoDistintoScoreCero() {
        int score = scoringAlgorithm.calcularScore(mascotaPerdida, mascotaDistinta);
        assertEquals(0, score, "Tipos distintos deben producir score 0");
    }

    @Test
    @DisplayName("ScoringAlgorithm → score base 30 cuando solo coincide el tipo")
    void testScoring_SoloTipoCoincide() {
        Mascota soloTipo = buildMascota("x", "Perro", "Poodle", "Café", "PEQUEÑO", -33.9, -71.0, "ENCONTRADA");
        int score = scoringAlgorithm.calcularScore(mascotaPerdida, soloTipo);
        // tipo=30, raza no, color no, tamano no, distancia > 5km → sin bonus
        assertEquals(30, score);
    }

    @Test
    @DisplayName("ScoringAlgorithm → distancia Haversine entre mismas coordenadas es 0")
    void testDistanciaKm_MismasCoordenadas() {
        double dist = scoringAlgorithm.distanciaKm(-33.4489, -70.6693, -33.4489, -70.6693);
        assertEquals(0.0, dist, 0.001, "Distancia entre el mismo punto debe ser 0");
    }

    @Test
    @DisplayName("ScoringAlgorithm → retorna MAX_VALUE si alguna coordenada es null")
    void testDistanciaKm_CoordenadaNula() {
        double dist = scoringAlgorithm.distanciaKm(null, -70.6693, -33.4489, -70.6693);
        assertEquals(Double.MAX_VALUE, dist, "Coordenada null debe retornar Double.MAX_VALUE");
    }

    @Test
    @DisplayName("ScoringAlgorithm → distancia Santiago-Valparaíso es aprox 100 km")
    void testDistanciaKm_SantiagoValparaiso() {
        double dist = scoringAlgorithm.distanciaKm(-33.4489, -70.6693, -33.0472, -71.6127);
        assertTrue(dist > 90 && dist < 130, "Distancia Santiago-Valparaíso debe ser ~100 km, fue: " + dist);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Crea un mock de Mascota (la entidad del motor es @Immutable sin setters ni builder,
     * por lo que el mock de Mockito es la forma estándar de instanciarla en unit tests).
     */
    private Mascota buildMascota(String id, String tipo, String raza, String color,
                                 String tamano, double lat, double lon, String estado) {
        Mascota mock = mock(Mascota.class);
        when(mock.getIdMascota()).thenReturn(id);
        when(mock.getTipoAnimal()).thenReturn(tipo);
        when(mock.getRaza()).thenReturn(raza);
        when(mock.getColorPrimario()).thenReturn(color);
        when(mock.getTamano()).thenReturn(tamano);
        when(mock.getLatitud()).thenReturn(lat);
        when(mock.getLongitud()).thenReturn(lon);
        when(mock.getEstado()).thenReturn(estado);
        return mock;
    }
}
