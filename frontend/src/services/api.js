import axios from 'axios';

const TOKEN_KEY = 'sanos_token';
const USER_KEY  = 'sanos_user';

const API = axios.create({
  baseURL: '/api',
  timeout: 8000,
  headers: { 'Content-Type': 'application/json' }
});

// Adjunta JWT si existe
API.interceptors.request.use(config => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

API.interceptors.response.use(
  res => res,
  err => {
    const cb = err.response?.data?.circuitBreaker;
    if (cb) console.warn(`[CircuitBreaker] ${cb.nombre} está ${cb.estado}`);
    return Promise.reject(err);
  }
);

// ── Mascotas ──────────────────────────────────────────────────
// Backend: /api/mascotas/** en ms-gestion-mascotas (3001)
export const mascotasService = {
  getAll:                (filters = {}) => API.get('/mascotas/busqueda', { params: filters }),
  getById:               (id)           => API.get(`/mascotas/${id}`),
  reportar:              (data)         => API.post('/mascotas/reportar', data),
  actualizar:            (id, data)     => API.put(`/mascotas/${id}`, data),
  eliminar:              (id)           => API.delete(`/mascotas/${id}`),
  getEstadisticas:       ()             => API.get('/mascotas/estadisticas'),
  getMisMascotas:        ()             => API.get('/mascotas/usuario/mis-reportes'),
  reportarEncuentro:     (id, data)     => API.post(`/mascotas/${id}/reportar-encuentro`, data),
  getEncuentrosRevision: ()             => API.get('/mascotas/encuentros/revision'),
  revisarEncuentro:      (id, accion)   => API.put(`/mascotas/encuentros/revision/${id}`, { accion }),
};

// ── Motor de coincidencias ─────────────────────────────────────
// Backend: /api/motor/** en ms-motor-coincidencias (3003)
// Rutas reales: GET /motor/buscar/{id}, /motor/historial, /motor/resumen
export const motorService = {
  getCoincidencias: (id, opts = {}) => API.get(`/motor/buscar/${id}`, { params: opts }),
  getHistorial:     (limite = 20)   => API.get('/motor/historial', { params: { limite } }),
  getResumen:       ()              => API.get('/motor/resumen'),
};

// ── Auth / Usuarios ───────────────────────────────────────────
// Backend: /api/auth/** en ms-usuarios-entidades (3004)
// Respuesta: { success: true, data: { token, usuario } }
export const authService = {
  login: async (email, password) => {
    const res = await API.post('/auth/login', { email, password });
    // El backend devuelve ResponseWrapper: { success, data: { token, usuario } }
    const payload = res.data?.data ?? res.data;
    const { token, usuario } = payload;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(usuario));
    return usuario;
  },
  register: async (nombre, email, password, rol) => {
    const res = await API.post('/auth/register', { nombre, email, password, rol });
    const payload = res.data?.data ?? res.data;
    const { token, usuario } = payload;
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(usuario));
    }
    return usuario;
  },
  me:              ()  => API.get('/auth/me'),
  actualizarPerfil:(d) => API.put('/auth/me', d),
  cambiarPassword: (d) => API.put('/auth/me/password', d),
  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
  getStoredUser: () => {
    try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); }
    catch { return null; }
  },
  isLoggedIn: () => Boolean(localStorage.getItem(TOKEN_KEY)),
};

// ── Admin de usuarios ─────────────────────────────────────────
export const adminService = {
  listarUsuarios:  ()        => API.get('/auth/usuarios'),
  getEstadisticas: ()        => API.get('/auth/usuarios/estadisticas'),
  cambiarRol:      (id, rol) => API.put(`/auth/usuarios/${id}/rol`, { rol }),
  toggleActivo:    (id)      => API.put(`/auth/usuarios/${id}/toggle-activo`),
};

// ── Geolocalización ───────────────────────────────────────────
// Backend: /api/geolocalizacion/** en ms-geolocalizacion (3002)
// Rutas reales: /geolocalizacion/puntos, /zonas-criticas, /mapa-calor, /estadisticas
export const geoService = {
  getPuntos:       () => API.get('/geolocalizacion/puntos'),
  getZonas:        () => API.get('/geolocalizacion/zonas-criticas'),
  getHeatmap:      () => API.get('/geolocalizacion/mapa-calor'),
  getEstadisticas: () => API.get('/geolocalizacion/estadisticas'),
};

// ── Sistema ───────────────────────────────────────────────────
export const sistemaService = {
  getStatus: () => API.get('/status'),
};

export default API;
