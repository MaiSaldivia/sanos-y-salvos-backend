import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { mascotasService, geoService } from '../services/api';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './Mapa.css';

// Fix Leaflet default icon paths (broken with Vite)
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl:       'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl:     'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const ESTADO_COLOR = {
  PERDIDA:    '#e53e3e',
  ENCONTRADA: '#38a169',
  REUNIFICADA:'#d69e2e',
};

const ESTADO_LABEL = {
  PERDIDA:    'Perdida',
  ENCONTRADA: 'Encontrada',
  REUNIFICADA:'Reunificada',
};

// Concepción centro por defecto
const DEFAULT_CENTER = [-36.8201, -73.0444];
const DEFAULT_ZOOM   = 13;

export default function Mapa() {
  const navigate             = useNavigate();
  const mapRef               = useRef(null);   // div del mapa
  const leafletRef           = useRef(null);   // instancia L.map
  const markersRef           = useRef([]);     // marcadores actuales
  const heatLayerRef         = useRef(null);

  const [mascotas,     setMascotas]     = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [filterEstado, setFilterEstado] = useState('');
  const [selected,     setSelected]     = useState(null);
  const [vistaCalor,   setVistaCalor]   = useState(false);
  const [zonas,        setZonas]        = useState([]);

  // ── Cargar datos ─────────────────────────────────────────────────────
  useEffect(() => {
    Promise.all([
      mascotasService.getAll().catch(() => ({ data: [] })),
      geoService.getZonas().catch(() => ({ data: [] })),
    ]).then(([mRes, zRes]) => {
      const lista = Array.isArray(mRes.data) ? mRes.data : [];
      setMascotas(lista);
      const z = Array.isArray(zRes.data) ? zRes.data : [];
      setZonas(z);
    }).finally(() => setLoading(false));
  }, []);

  // ── Inicializar mapa ─────────────────────────────────────────────────
  useEffect(() => {
    if (loading || !mapRef.current || leafletRef.current) return;

    const map = L.map(mapRef.current, {
      center: DEFAULT_CENTER,
      zoom:   DEFAULT_ZOOM,
      zoomControl: true,
    });

    // Tile layer — OpenStreetMap (sin API key)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);

    leafletRef.current = map;

    return () => {
      map.remove();
      leafletRef.current = null;
    };
  }, [loading]);

  // ── Actualizar marcadores cuando cambian filtros o datos ─────────────
  useEffect(() => {
    const map = leafletRef.current;
    if (!map) return;

    // Limpiar marcadores anteriores
    markersRef.current.forEach(m => m.remove());
    markersRef.current = [];
    if (heatLayerRef.current) { heatLayerRef.current.remove(); heatLayerRef.current = null; }

    const filtered = filterEstado
      ? mascotas.filter(m => m.estado === filterEstado)
      : mascotas;

    const validas = filtered.filter(m =>
      m.latitud != null && m.longitud != null &&
      !isNaN(m.latitud) && !isNaN(m.longitud)
    );

    if (vistaCalor) {
      // Mapa de calor con leaflet.heat
      import('leaflet.heat').then(() => {
        const points = validas.map(m => [m.latitud, m.longitud, 1]);
        if (points.length === 0) return;
        const heat = L.heatLayer(points, {
          radius:    25,
          blur:      15,
          maxZoom:   17,
          gradient:  { 0.2: '#38a169', 0.5: '#d69e2e', 0.8: '#e53e3e' },
        }).addTo(map);
        heatLayerRef.current = heat;
      });
      return;
    }

    // Marcadores normales
    validas.forEach(m => {
      const idM   = m.idMascota   ?? m.id_mascota;
      const fotoM = m.fotoUrl     ?? m.foto_url;
      const tipoM = m.tipoAnimal  ?? m.tipo_animal;
      const color = ESTADO_COLOR[m.estado] || '#888';
      const emoji = tipoM === 'Perro' ? '🐕' : tipoM === 'Gato' ? '🐈' : '🐾';

      const icon = L.divIcon({
        className: '',
        html: `
          <div class="map-pin" style="--pin-color:${color}">
            <span class="pin-emoji">${emoji}</span>
          </div>`,
        iconSize:   [36, 36],
        iconAnchor: [18, 36],
        popupAnchor:[0, -36],
      });

      const marker = L.marker([m.latitud, m.longitud], { icon });

      marker.bindPopup(`
        <div class="map-popup">
          ${fotoM ? `<img src="${fotoM}" alt="${m.nombre || 'Sin nombre'}" class="popup-img" onerror="this.style.display='none'"/>` : ''}
          <div class="popup-body">
            <div class="popup-header">
              <strong>${m.nombre || 'Sin nombre'}</strong>
              <span class="popup-badge" style="background:${color}20;color:${color};border:1px solid ${color}40">
                ${ESTADO_LABEL[m.estado] || m.estado}
              </span>
            </div>
            <p class="popup-tipo">${tipoM}${m.raza ? ` · ${m.raza}` : ''}</p>
            ${m.sector ? `<p class="popup-sector">📍 ${m.sector}${m.comuna ? `, ${m.comuna}` : ''}</p>` : ''}
            ${m.contacto ? `<p class="popup-contacto">📞 ${m.contacto}</p>` : ''}
            <button class="popup-btn" onclick="window.__mapaNavigate('${idM}')">
              Ver detalle →
            </button>
          </div>
        </div>
      `, { maxWidth: 260, className: 'leaflet-popup-custom' });

      marker.on('click', () => setSelected(m));
      marker.addTo(map);
      markersRef.current.push(marker);
    });

    // Centrar mapa si hay marcadores
    if (validas.length > 0 && markersRef.current.length > 0) {
      const group = L.featureGroup(markersRef.current);
      map.fitBounds(group.getBounds().pad(0.15), { maxZoom: 15 });
    }
  }, [mascotas, filterEstado, vistaCalor, leafletRef.current]);

  // Puente para el botón dentro del popup (no puede usar navigate directamente)
  useEffect(() => {
    window.__mapaNavigate = (id) => navigate(`/mascotas/${id}`);
    return () => { delete window.__mapaNavigate; };
  }, [navigate]);

  const filtered = filterEstado
    ? mascotas.filter(m => m.estado === filterEstado)
    : mascotas;

  const perdidas    = mascotas.filter(m => m.estado === 'PERDIDA').length;
  const encontradas = mascotas.filter(m => m.estado === 'ENCONTRADA').length;
  const reunificadas= mascotas.filter(m => m.estado === 'REUNIFICADA').length;

  return (
    <div className="mapa-page page-enter">
      <div className="container">

        {/* Header */}
        <div className="mapa-header">
          <div>
            <h1 className="page-title">Mapa de casos</h1>
            <p className="page-sub">Zona Concepción · {filtered.length} casos visibles</p>
          </div>
          <div className="mapa-controls">
            {/* Filtro estado */}
            <div className="filter-group">
              {[
                { val: '',            label: 'Todos' },
                { val: 'PERDIDA',     label: 'Perdidas' },
                { val: 'ENCONTRADA',  label: 'Encontradas' },
                { val: 'REUNIFICADA', label: 'Reunificadas' },
              ].map(({ val, label }) => (
                <button
                  key={val}
                  className={`filter-btn ${filterEstado === val ? 'active' : ''}`}
                  style={filterEstado === val && val ? { '--btn-color': ESTADO_COLOR[val] } : {}}
                  onClick={() => setFilterEstado(val)}
                >
                  {label}
                </button>
              ))}
            </div>
            {/* Toggle mapa calor */}
            <button
              className={`toggle-heat ${vistaCalor ? 'active' : ''}`}
              onClick={() => setVistaCalor(v => !v)}
              title="Alternar mapa de calor"
            >
              {vistaCalor ? 'Ver marcadores' : 'Ver mapa de calor'}
            </button>
          </div>
        </div>

        {/* Stats rápidas */}
        <div className="mapa-stats">
          <div className="stat-pill" style={{'--c':'#e53e3e'}}>
            <span className="stat-dot"></span>
            <span>{perdidas} perdidas</span>
          </div>
          <div className="stat-pill" style={{'--c':'#38a169'}}>
            <span className="stat-dot"></span>
            <span>{encontradas} encontradas</span>
          </div>
          <div className="stat-pill" style={{'--c':'#d69e2e'}}>
            <span className="stat-dot"></span>
            <span>{reunificadas} reunificadas</span>
          </div>
          {zonas.length > 0 && (
            <div className="stat-pill" style={{'--c':'#3b82f6'}}>
              <span className="stat-dot"></span>
              <span>{zonas.length} zonas críticas</span>
            </div>
          )}
        </div>

        <div className="mapa-layout">
          {/* Mapa */}
          <div className="mapa-wrap card">
            {loading && (
              <div className="mapa-loader">
                <div className="spinner"></div>
              </div>
            )}
            <div ref={mapRef} className="mapa-leaflet" />
          </div>

          {/* Sidebar */}
          <div className="mapa-sidebar">

            {/* Card seleccionada */}
            {selected && (
              <div className="selected-card card">
                <div className="selected-estado" style={{ background: ESTADO_COLOR[selected.estado] }}>
                  {ESTADO_LABEL[selected.estado]}
                </div>
                {(selected.fotoUrl ?? selected.foto_url) && (
                  <img
                    src={selected.fotoUrl ?? selected.foto_url}
                    alt={selected.nombre}
                    className="selected-img"
                    onError={e => e.target.style.display = 'none'}
                  />
                )}
                <div className="selected-body">
                  <h3>{selected.nombre || 'Sin nombre'}</h3>
                  <p>{selected.tipoAnimal ?? selected.tipo_animal}{selected.raza ? ` · ${selected.raza}` : ''}</p>
                  {selected.sector && <p>📍 {selected.sector}{selected.comuna ? `, ${selected.comuna}` : ''}</p>}
                  {selected.contacto && <p>📞 {selected.contacto}</p>}
                  <div className="selected-actions">
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => navigate(`/mascotas/${selected.idMascota ?? selected.id_mascota}`)}
                    >
                      Ver detalle completo →
                    </button>
                    <button className="btn btn-ghost btn-sm" onClick={() => setSelected(null)}>
                      Cerrar
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Lista de casos */}
            <div className="mapa-lista card">
              <h3 className="lista-title">Casos ({filtered.length})</h3>
              <div className="lista-scroll">
                {filtered.length === 0 ? (
                  <p className="lista-empty">Sin casos con este filtro</p>
                ) : (
                  filtered.map(m => {
                    const idM   = m.idMascota  ?? m.id_mascota;
                    const tipoM = m.tipoAnimal ?? m.tipo_animal;
                    const isActive = (selected?.idMascota ?? selected?.id_mascota) === idM;
                    return (
                      <button
                        key={idM}
                        className={`lista-item ${isActive ? 'active' : ''}`}
                        onClick={() => {
                          setSelected(isActive ? null : m);
                          // Centrar mapa en el marcador
                          if (!isActive && m.latitud && leafletRef.current) {
                            leafletRef.current.setView([m.latitud, m.longitud], 16);
                          }
                        }}
                      >
                        <span className="lista-dot" style={{ background: ESTADO_COLOR[m.estado] }}></span>
                        <span className="lista-nombre">{m.nombre || 'Sin nombre'}</span>
                        <span className="lista-tipo">{tipoM}</span>
                      </button>
                    );
                  })
                )}
              </div>
            </div>

            {/* Zonas críticas */}
            {zonas.length > 0 && (
              <div className="zonas-card card">
                <h3 className="lista-title">Zonas críticas</h3>
                {zonas.slice(0, 5).map((z, i) => (
                  <div key={i} className="zona-row">
                    <span>{z.comuna ?? z.nombreZona ?? `Zona ${i + 1}`}</span>
                    <strong>{z.totalReportes ?? z.total ?? '—'}</strong>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
