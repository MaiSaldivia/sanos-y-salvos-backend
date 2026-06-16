package com.sanosysalvos.usuarios.service;

import com.sanosysalvos.usuarios.config.JwtUtil;
import com.sanosysalvos.usuarios.dto.LoginResponse;
import com.sanosysalvos.usuarios.dto.RegisterRequest;
import com.sanosysalvos.usuarios.dto.PasswordRequest;
import com.sanosysalvos.usuarios.dto.ProfileRequest;
import com.sanosysalvos.usuarios.model.Rol;
import com.sanosysalvos.usuarios.model.Usuario;
import com.sanosysalvos.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de AuthService.
 * Sincronizan exactamente con la implementación real: texto plano de password,
 * findByEmailAndActivoTrue para login, y generarToken(id, nombre, email, rol).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Tests Unitarios")
class AuthServiceTest {

    @Mock
    private UsuarioRepository repo;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioActivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = Usuario.builder()
                .idUsuario("uuid-1234")
                .nombre("Carlos López")
                .email("carlos@demo.cl")
                .passwordHash("password123")   // texto plano, como usa la implementación real
                .rol(Rol.DUENO)
                .activo(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // login()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login → exitoso con credenciales correctas")
    void testLogin_Exitoso() {
        when(repo.findByEmailAndActivoTrue("carlos@demo.cl")).thenReturn(Optional.of(usuarioActivo));
        when(jwtUtil.generarToken("uuid-1234", "Carlos López", "carlos@demo.cl", Rol.DUENO))
                .thenReturn("jwt-token-valido");

        LoginResponse resp = authService.login("carlos@demo.cl", "password123");

        assertNotNull(resp);
        assertEquals("jwt-token-valido", resp.getToken());
        assertNotNull(resp.getUsuario());
        assertEquals("carlos@demo.cl", resp.getUsuario().getEmail());
        assertEquals(Rol.DUENO, resp.getUsuario().getRol());
    }

    @Test
    @DisplayName("login → lanza 401 si la contraseña es incorrecta")
    void testLogin_PasswordIncorrecta() {
        when(repo.findByEmailAndActivoTrue("carlos@demo.cl")).thenReturn(Optional.of(usuarioActivo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login("carlos@demo.cl", "wrongpass"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("login → lanza 401 si el email no existe o el usuario está inactivo")
    void testLogin_EmailNoExiste() {
        when(repo.findByEmailAndActivoTrue("noexiste@demo.cl")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login("noexiste@demo.cl", "cualquier"));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("login → lanza 400 si el email es nulo o vacío")
    void testLogin_EmailVacio() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login("", "password123"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("login → lanza 400 si la contraseña es nula o vacía")
    void testLogin_PasswordVacia() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login("carlos@demo.cl", null));

        assertEquals(400, ex.getStatusCode().value());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register → crea usuario nuevo con rol DUENO por defecto")
    void testRegister_Exitoso() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Ana García");
        req.setEmail("ana@demo.cl");
        req.setPassword("segura123");
        req.setRol(null); // null → debe asignar DUENO

        when(repo.existsByEmail("ana@demo.cl")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            return u;
        });
        when(repo.findById(anyString())).thenReturn(Optional.of(
                Usuario.builder().idUsuario("nuevo-uuid").nombre("Ana García")
                        .email("ana@demo.cl").passwordHash("segura123").rol(Rol.DUENO).activo(true).build()
        ));
        when(jwtUtil.generarToken(anyString(), anyString(), anyString(), any(Rol.class)))
                .thenReturn("jwt-nuevo");

        LoginResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("jwt-nuevo", resp.getToken());
        verify(repo, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("register → lanza 409 si el email ya está registrado")
    void testRegister_EmailDuplicado() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Alguien");
        req.setEmail("carlos@demo.cl");
        req.setPassword("pass123");

        when(repo.existsByEmail("carlos@demo.cl")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(req));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("register → lanza 400 si el nombre tiene menos de 2 caracteres")
    void testRegister_NombreCorto() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("A");
        req.setEmail("a@demo.cl");
        req.setPassword("pass123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(req));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("register → lanza 400 si la contraseña tiene menos de 6 caracteres")
    void testRegister_PasswordCorta() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Juan Pérez");
        req.setEmail("juan@demo.cl");
        req.setPassword("123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(req));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("register → no permite crear usuarios con rol ADMIN")
    void testRegister_NoPermiteRolAdmin() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Hacker");
        req.setEmail("hack@demo.cl");
        req.setPassword("hacker123");
        req.setRol(Rol.ADMIN);

        when(repo.existsByEmail("hack@demo.cl")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findById(anyString())).thenReturn(Optional.of(
                Usuario.builder().idUsuario("u-hack").nombre("Hacker")
                        .email("hack@demo.cl").passwordHash("hacker123").rol(Rol.DUENO).activo(true).build()
        ));
        when(jwtUtil.generarToken(anyString(), anyString(), anyString(), any(Rol.class)))
                .thenReturn("jwt-tok");

        LoginResponse resp = authService.register(req);

        // El rol final guardado debe ser DUENO, no ADMIN
        assertNotEquals(Rol.ADMIN, resp.getUsuario().getRol(),
                "No debe poderse crear un usuario ADMIN por registro público");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPerfil()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getPerfil → retorna datos del usuario existente")
    void testGetPerfil_Exitoso() {
        when(repo.findById("uuid-1234")).thenReturn(Optional.of(usuarioActivo));

        var perfil = authService.getPerfil("uuid-1234");

        assertNotNull(perfil);
        assertEquals("carlos@demo.cl", perfil.getEmail());
        assertEquals("Carlos López", perfil.getNombre());
    }

    @Test
    @DisplayName("getPerfil → lanza 404 si el usuario no existe")
    void testGetPerfil_NoEncontrado() {
        when(repo.findById("no-existe")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.getPerfil("no-existe"));

        assertEquals(404, ex.getStatusCode().value());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // cambiarPassword()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cambiarPassword → exitosa con contraseña actual correcta")
    void testCambiarPassword_Exitosa() {
        PasswordRequest req = new PasswordRequest();
        req.setPasswordActual("password123");
        req.setPasswordNuevo("nuevaPass456");

        when(repo.findById("uuid-1234")).thenReturn(Optional.of(usuarioActivo));
        when(repo.save(any(Usuario.class))).thenReturn(usuarioActivo);

        assertDoesNotThrow(() -> authService.cambiarPassword("uuid-1234", req));
        verify(repo, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarPassword → lanza 401 si la contraseña actual es incorrecta")
    void testCambiarPassword_ActualIncorrecta() {
        PasswordRequest req = new PasswordRequest();
        req.setPasswordActual("wrong");
        req.setPasswordNuevo("nuevaPass456");

        when(repo.findById("uuid-1234")).thenReturn(Optional.of(usuarioActivo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.cambiarPassword("uuid-1234", req));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("cambiarPassword → lanza 400 si la nueva contraseña es igual a la actual")
    void testCambiarPassword_MismaContrasena() {
        PasswordRequest req = new PasswordRequest();
        req.setPasswordActual("password123");
        req.setPasswordNuevo("password123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.cambiarPassword("uuid-1234", req));

        assertEquals(400, ex.getStatusCode().value());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // actualizarPerfil()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizarPerfil → actualiza nombre correctamente")
    void testActualizarPerfil_Exitoso() {
        ProfileRequest req = new ProfileRequest();
        req.setNombre("Carlos A. López");

        when(repo.findById("uuid-1234")).thenReturn(Optional.of(usuarioActivo));
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = authService.actualizarPerfil("uuid-1234", req);

        assertNotNull(resultado);
        assertEquals("Carlos A. López", resultado.getNombre());
    }

    @Test
    @DisplayName("actualizarPerfil → lanza 400 si el nombre es muy corto")
    void testActualizarPerfil_NombreInvalido() {
        ProfileRequest req = new ProfileRequest();
        req.setNombre("X");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.actualizarPerfil("uuid-1234", req));

        assertEquals(400, ex.getStatusCode().value());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin: listar, cambiarRol, toggleActivo
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarUsuarios → retorna todos los usuarios del repositorio")
    void testListarUsuarios() {
        when(repo.findAll()).thenReturn(Arrays.asList(usuarioActivo,
                Usuario.builder().idUsuario("u2").nombre("Admin").email("admin@demo.cl")
                        .passwordHash("admin").rol(Rol.ADMIN).activo(true).build()));

        List<?> lista = authService.listarUsuarios();

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("cambiarRol → lanza 400 si el admin intenta cambiarse su propio rol")
    void testCambiarRol_NoAutoModificacion() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.cambiarRol("uuid-1234", "uuid-1234", Rol.ADMIN));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("toggleActivo → lanza 400 si el admin intenta desactivar su propia cuenta")
    void testToggleActivo_NoAutoDesactivacion() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.toggleActivo("uuid-1234", "uuid-1234"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("toggleActivo → cambia activo de true a false correctamente")
    void testToggleActivo_DesactivaUsuario() {
        Usuario otraPersona = Usuario.builder()
                .idUsuario("uuid-9999").nombre("Otro").email("otro@demo.cl")
                .passwordHash("x").rol(Rol.DUENO).activo(true).build();

        when(repo.findById("uuid-9999")).thenReturn(Optional.of(otraPersona));
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = authService.toggleActivo("uuid-1234", "uuid-9999");

        assertFalse(resultado.getActivo(), "El usuario debe quedar inactivo después del toggle");
    }
}
