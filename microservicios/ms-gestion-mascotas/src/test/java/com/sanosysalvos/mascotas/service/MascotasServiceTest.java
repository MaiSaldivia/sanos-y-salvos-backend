package com.sanosysalvos.mascotas.service;

import com.sanosysalvos.mascotas.dto.MascotaRequest;
import com.sanosysalvos.mascotas.dto.MascotaResponse;
import com.sanosysalvos.mascotas.model.EstadoMascota;
import com.sanosysalvos.mascotas.model.EstadoRevision;
import com.sanosysalvos.mascotas.model.Mascota;
import com.sanosysalvos.mascotas.model.ReporteEncuentro;
import com.sanosysalvos.mascotas.pattern.factory.Alerta;
import com.sanosysalvos.mascotas.pattern.factory.AlertaFactory;
import com.sanosysalvos.mascotas.pattern.observer.MascotaReportadaEvent;
import com.sanosysalvos.mascotas.pattern.observer.MascotaReunificadaEvent;
import com.sanosysalvos.mascotas.repository.MascotaRepository;
import com.sanosysalvos.mascotas.repository.ReporteEncuentroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MascotasService — Tests Unitarios")
class MascotasServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private ReporteEncuentroRepository reporteEncuentroRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MascotasService mascotasService;

    private Mascota mockMascota;
    private Mascota mockMascotaEncontrada;

    @BeforeEach
    void setUp() {
        mockMascota = Mascota.builder()
                .idMascota("mascota-123")
                .idUsuario("user-123")
                .nombre("Firulais")
                .tipoAnimal("Perro")
                .raza("Labrador")
                .colorPrimario("Amarillo")
                .sector("Centro")
                .estado(EstadoMascota.PERDIDA)
                .contacto("dueno@test.cl")
                .fechaReporte(LocalDateTime.now())
                .latitud(-36.8201)
                .longitud(-73.0444)
                .build();

        mockMascotaEncontrada = Mascota.builder()
                .idMascota("mascota-456")
                .idUsuario("user-456")
                .nombre("Luna")
                .tipoAnimal("Gato")
                .estado(EstadoMascota.ENCONTRADA)
                .sector("San Pedro")
                .contacto("rescate@test.cl")
                .fechaReporte(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // MascotasService — métodos principales
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrarMascota → guarda correctamente y dispara evento Observer")
    void testRegistrarMascota_Success() {
        MascotaRequest req = new MascotaRequest();
        req.setNombre("Firulais");
        req.setTipoAnimal("Perro");
        req.setEstado(EstadoMascota.PERDIDA);
        req.setContacto("dueno@test.cl");
        req.setLatitud(-36.8201);
        req.setLongitud(-73.0444);

        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mockMascota);

        MascotaResponse res = mascotasService.registrarMascota(req, "user-123");

        assertNotNull(res);
        assertEquals("Firulais", res.getNombre());
        verify(mascotaRepository, times(1)).save(any(Mascota.class));
        // Verifica patrón Observer
        verify(eventPublisher, times(1)).publishEvent(any(MascotaReportadaEvent.class));
    }

    @Test
    @DisplayName("registrarMascota → usa coordenadas por defecto si vienen null")
    void testRegistrarMascota_CoordenadasNull() {
        MascotaRequest req = new MascotaRequest();
        req.setTipoAnimal("Gato");
        req.setEstado(EstadoMascota.ENCONTRADA);
        req.setLatitud(null);
        req.setLongitud(null);

        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mockMascotaEncontrada);

        MascotaResponse res = mascotasService.registrarMascota(req, "user-456");

        assertNotNull(res);
        verify(mascotaRepository, times(1)).save(any(Mascota.class));
    }

    @Test
    @DisplayName("obtenerPorId → retorna mascota existente")
    void testObtenerPorId_Success() {
        when(mascotaRepository.findById("mascota-123")).thenReturn(Optional.of(mockMascota));

        MascotaResponse res = mascotasService.obtenerPorId("mascota-123");

        assertNotNull(res);
        assertEquals("Firulais", res.getNombre());
        assertEquals(EstadoMascota.PERDIDA, res.getEstado());
    }

    @Test
    @DisplayName("obtenerPorId → lanza excepción si no existe")
    void testObtenerPorId_NotFound() {
        when(mascotaRepository.findById("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mascotasService.obtenerPorId("unknown"));
        assertEquals("Mascota no encontrada", ex.getMessage());
    }

    @Test
    @DisplayName("obtenerTodas → retorna lista del repositorio")
    void testObtenerTodas() {
        when(mascotaRepository.findAll()).thenReturn(Arrays.asList(mockMascota, mockMascotaEncontrada));

        List<MascotaResponse> lista = mascotasService.obtenerTodas();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("obtenerPorUsuario → retorna solo las mascotas del usuario")
    void testObtenerPorUsuario() {
        when(mascotaRepository.findByIdUsuario("user-123")).thenReturn(List.of(mockMascota));

        List<MascotaResponse> lista = mascotasService.obtenerPorUsuario("user-123");

        assertEquals(1, lista.size());
        assertEquals("Firulais", lista.get(0).getNombre());
    }

    @Test
    @DisplayName("getEstadisticas → retorna totales correctos")
    void testGetEstadisticas() {
        when(mascotaRepository.countTotal()).thenReturn(10L);
        when(mascotaRepository.countByEstado(EstadoMascota.PERDIDA)).thenReturn(5L);
        when(mascotaRepository.countByEstado(EstadoMascota.ENCONTRADA)).thenReturn(3L);
        when(mascotaRepository.countByEstado(EstadoMascota.REUNIFICADA)).thenReturn(2L);

        Map<String, Object> stats = mascotasService.getEstadisticas();

        assertEquals(10L, stats.get("total"));
        assertEquals(5L, stats.get("perdidas"));
        assertEquals(3L, stats.get("encontradas"));
        assertEquals(2L, stats.get("reunificadas"));
    }

    @Test
    @DisplayName("actualizarMascota → actualiza correctamente si el usuario es dueño")
    void testActualizarMascota_Success() {
        MascotaRequest req = new MascotaRequest();
        req.setTipoAnimal("Perro");
        req.setNombre("Firulais Actualizado");
        req.setEstado(EstadoMascota.ENCONTRADA);

        when(mascotaRepository.findById("mascota-123")).thenReturn(Optional.of(mockMascota));
        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mockMascota);

        MascotaResponse res = mascotasService.actualizarMascota("mascota-123", req, "user-123");

        assertNotNull(res);
        verify(mascotaRepository, times(1)).save(any(Mascota.class));
    }

    @Test
    @DisplayName("actualizarMascota → lanza excepción si el usuario no es el dueño")
    void testActualizarMascota_PermisoDenegado() {
        MascotaRequest req = new MascotaRequest();
        req.setTipoAnimal("Perro");
        req.setEstado(EstadoMascota.ENCONTRADA);

        when(mascotaRepository.findById("mascota-123")).thenReturn(Optional.of(mockMascota));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mascotasService.actualizarMascota("mascota-123", req, "otro-user"));

        assertTrue(ex.getMessage().contains("permisos"));
    }

    @Test
    @DisplayName("eliminarMascota → elimina correctamente si el usuario es dueño")
    void testEliminarMascota_Success() {
        when(mascotaRepository.findById("mascota-123")).thenReturn(Optional.of(mockMascota));
        doNothing().when(mascotaRepository).delete(mockMascota);

        assertDoesNotThrow(() -> mascotasService.eliminarMascota("mascota-123", "user-123"));
        verify(mascotaRepository, times(1)).delete(mockMascota);
    }

    @Test
    @DisplayName("eliminarMascota → lanza excepción si el usuario no es el dueño")
    void testEliminarMascota_PermisoDenegado() {
        when(mascotaRepository.findById("mascota-123")).thenReturn(Optional.of(mockMascota));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mascotasService.eliminarMascota("mascota-123", "otro-user"));

        assertTrue(ex.getMessage().contains("permisos"));
    }

    @Test
    @DisplayName("reportarEncuentro → guarda el reporte correctamente")
    void testReportarEncuentro_Success() {
        when(mascotaRepository.findById("mascota-123")).thenReturn(Optional.of(mockMascota));
        when(reporteEncuentroRepository.save(any(ReporteEncuentro.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        com.sanosysalvos.mascotas.dto.ReporteEncuentroRequest request =
                new com.sanosysalvos.mascotas.dto.ReporteEncuentroRequest(
                        "https://foto.com/img.jpg",
                        "Parque Ecuador",
                        "Juan Pérez",
                        "+56912345678"
                );

        assertDoesNotThrow(() -> mascotasService.reportarEncuentro("mascota-123", request));
        verify(reporteEncuentroRepository, times(1)).save(any(ReporteEncuentro.class));
    }

    @Test
    @DisplayName("revisarEncuentro APROBAR → marca mascota como REUNIFICADA y dispara evento")
    void testRevisarEncuentro_Aprobar() {
        ReporteEncuentro reporte = ReporteEncuentro.builder()
                .idReporteEncuentro("rep-001")
                .idMascota("mascota-123")
                .estadoRevision(EstadoRevision.EN_REVISION)
                .build();

        when(reporteEncuentroRepository.findById("rep-001")).thenReturn(Optional.of(reporte));
        when(reporteEncuentroRepository.save(any(ReporteEncuentro.class))).thenReturn(reporte);
        when(mascotaRepository.findById("mascota-123")).thenReturn(Optional.of(mockMascota));
        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mockMascota);

        assertDoesNotThrow(() -> mascotasService.revisarEncuentro("rep-001", "APROBAR"));

        verify(mascotaRepository, times(1)).save(any(Mascota.class));
        verify(eventPublisher, times(1)).publishEvent(any(MascotaReunificadaEvent.class));
    }

    @Test
    @DisplayName("revisarEncuentro RECHAZAR → cambia estado sin tocar la mascota")
    void testRevisarEncuentro_Rechazar() {
        ReporteEncuentro reporte = ReporteEncuentro.builder()
                .idReporteEncuentro("rep-002")
                .idMascota("mascota-123")
                .estadoRevision(EstadoRevision.EN_REVISION)
                .build();

        when(reporteEncuentroRepository.findById("rep-002")).thenReturn(Optional.of(reporte));
        when(reporteEncuentroRepository.save(any(ReporteEncuentro.class))).thenReturn(reporte);

        assertDoesNotThrow(() -> mascotasService.revisarEncuentro("rep-002", "RECHAZAR"));

        verify(mascotaRepository, never()).save(any(Mascota.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ─────────────────────────────────────────────────────────────────────
    // Patrón Factory — AlertaFactory directo
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AlertaFactory → crea AlertaExtravio para estado PERDIDA")
    void testAlertaFactory_Perdida() {
        Alerta alerta = AlertaFactory.crear(EstadoMascota.PERDIDA, mockMascota);

        assertNotNull(alerta);
        assertNotNull(alerta.getMensaje());
        assertTrue(alerta.getMensaje().length() > 0);
        assertEquals("AlertaExtravio", alerta.getTipo());
    }

    @Test
    @DisplayName("AlertaFactory → crea AlertaHallazgo para estado ENCONTRADA")
    void testAlertaFactory_Encontrada() {
        Alerta alerta = AlertaFactory.crear(EstadoMascota.ENCONTRADA, mockMascotaEncontrada);

        assertNotNull(alerta);
        assertEquals("AlertaHallazgo", alerta.getTipo());
    }

    @Test
    @DisplayName("AlertaFactory → crea AlertaReunificacion para estado REUNIFICADA")
    void testAlertaFactory_Reunificada() {
        Mascota reunificada = Mascota.builder()
                .idMascota("m-r")
                .nombre("Rex")
                .estado(EstadoMascota.REUNIFICADA)
                .contacto("dueno@test.cl")
                .sector("Centro")
                .build();

        Alerta alerta = AlertaFactory.crear(EstadoMascota.REUNIFICADA, reunificada);

        assertNotNull(alerta);
        assertEquals("AlertaReunificacion", alerta.getTipo());
    }

    @Test
    @DisplayName("AlertaFactory → lanza excepción con estado null")
    void testAlertaFactory_EstadoNull() {
        assertThrows(IllegalArgumentException.class,
                () -> AlertaFactory.crear(null, mockMascota));
    }
}
