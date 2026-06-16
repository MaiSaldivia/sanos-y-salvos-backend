import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { mascotasService } from '../services/api';
import './Mapa.css';

// Simple interactive dot-map (no external map library needed)
export default function Mapa() {
  const [mascotas, setMascotas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [filterEstado, setFilterEstado] = useState('');
  const navigate = useNavigate();
  const canvasRef = useRef(null);

  // Concepción bounding box
  const BOUNDS = {
    minLat: -36.90, maxLat: -36.76,
    minLng: -73.12, maxLng: -72.98
  };

  useEffect(() => {
    mascotasService.getAll().then(res => {
      setMascotas(Array.isArray(res.data) ? res.data : []);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const filtered = filterEstado
    ? mascotas.filter(m => m.estado === filterEstado)
    : mascotas;

  const toXY = (lat, lng, w, h) => {
    const x = ((lng - BOUNDS.minLng) / (BOUNDS.maxLng - BOUNDS.minLng)) * w;
    const y = ((BOUNDS.maxLat - lat) / (BOUNDS.maxLat - BOUNDS.minLat)) * h;
    return { x, y };
  };

  const estadoColor = {
    PERDIDA: '#C0392B',
    ENCONTRADA: '#2D7A4A',
    REUNIFICADA: '#D4882A'
  };

  return (
    <div className="mapa-page page-enter">
      <div className="container">
        <div className="page-header">
          <div>
            <h1 className="page-title">Mapa de casos</h1>
            <p className="page-sub">Microservicio de Geolocalización — zona Concepción, Chile</p>
          </div>
          <div className="mapa-filters">
            {['', 'PERDIDA', 'ENCONTRADA', 'REUNIFICADA'].map(e => (
              <button
                key={e}
                className={`btn btn-sm ${filterEstado === e ? 'btn-secondary' : 'btn-outline'}`}
                onClick={() => setFilterEstado(e)}
              >
                {e === '' ? 'Todos' : e.charAt(0) + e.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>

        <div className="mapa-layout">
          {/* Map */}
          <div className="mapa-container card">
            {loading ? (
              <div className="loader"><div className="spinner"></div></div>
            ) : (
              <div className="mapa-svg-wrap">
                <svg
                  viewBox="0 0 700 460"
                  className="mapa-svg"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  {/* Background */}
                  <rect width="700" height="460" fill="#E8F0E8" rx="12"/>

                  {/* Grid lines */}
                  {[...Array(7)].map((_, i) => (
                    <line key={`v${i}`} x1={i*100} y1="0" x2={i*100} y2="460"
                      stroke="rgba(255,255,255,0.5)" strokeWidth="0.5"/>
                  ))}
                  {[...Array(5)].map((_, i) => (
                    <line key={`h${i}`} x1="0" y1={i*100} x2="700" y2={i*100}
                      stroke="rgba(255,255,255,0.5)" strokeWidth="0.5"/>
                  ))}

                  {/* River (Río Biobío) */}
                  <path
                    d="M 0 380 Q 120 360 200 375 Q 350 390 500 370 Q 620 355 700 360"
                    fill="none" stroke="#7BC8F5" strokeWidth="14" opacity="0.7"/>
                  <text x="160" y="398" fontSize="10" fill="#5AAFE0" fontFamily="sans-serif">Río Biobío</text>

                  {/* City zones */}
                  <ellipse cx="350" cy="200" rx="120" ry="80" fill="rgba(255,255,255,0.15)"/>
                  <text x="350" y="148" textAnchor="middle" fontSize="11"
                    fill="#2D4A3E" fontFamily="sans-serif" fontWeight="600">Centro Concepción</text>

                  <ellipse cx="180" cy="280" rx="70" ry="45" fill="rgba(255,255,255,0.12)"/>
                  <text x="180" y="342" textAnchor="middle" fontSize="10"
                    fill="#2D4A3E" fontFamily="sans-serif">Hualpén</text>

                  <ellipse cx="540" cy="180" rx="80" ry="55" fill="rgba(255,255,255,0.12)"/>
                  <text x="540" y="145" textAnchor="middle" fontSize="10"
                    fill="#2D4A3E" fontFamily="sans-serif">San Pedro</text>

                  {/* Pins */}
                  {filtered.map(m => {
                    const idM  = m.idMascota  ?? m.id_mascota;
                    const fotoM= m.fotoUrl    ?? m.foto_url;
                    const tipoM= m.tipoAnimal ?? m.tipo_animal;
                    const { x, y } = toXY(m.latitud, m.longitud, 700, 460);
                    const color = estadoColor[m.estado] || '#888';
                    const isSelected = selected?.idMascota === idM || selected?.id_mascota === idM;
                    return (
                      <g key={idM}
                        onClick={() => setSelected(isSelected ? null : m)}
                        style={{ cursor: 'pointer' }}
                        transform={`translate(${x}, ${y})`}
                      >
                        <circle
                          r={isSelected ? 16 : 11}
                          fill={color}
                          opacity="0.9"
                          stroke="white"
                          strokeWidth={isSelected ? 3 : 2}
                        />
                        <text y="4" textAnchor="middle" fontSize="10" fill="white">
                          {tipoM === 'Perro' ? '🐕' : tipoM === 'Gato' ? '🐈' : '🐾'}
                        </text>
                        {isSelected && (
                          <text y="-22" textAnchor="middle" fontSize="11"
                            fill={color} fontWeight="700" fontFamily="sans-serif"
                            style={{filter: 'drop-shadow(0 1px 2px rgba(0,0,0,0.3))'}}>
                            {m.nombre}
                          </text>
                        )}
                      </g>
                    );
                  })}
                </svg>

                {/* Legend */}
                <div className="mapa-legend">
                  <span className="legend-item"><span className="dot" style={{background:'#C0392B'}}></span> Perdida</span>
                  <span className="legend-item"><span className="dot" style={{background:'#2D7A4A'}}></span> Encontrada</span>
                  <span className="legend-item"><span className="dot" style={{background:'#D4882A'}}></span> Reunificada</span>
                </div>
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="mapa-sidebar">
            {selected ? (
              <div className="selected-card card">
                <img
                  src={selected.fotoUrl ?? selected.foto_url}
                  alt={selected.nombre}
                  className="selected-img"
                  onError={e => e.target.src='https://images.unsplash.com/photo-1450778869180-41d0601e046e?w=300&q=80'}
                />
                <div className="selected-body">
                  <div style={{display:'flex',alignItems:'center',gap:'8px',marginBottom:'8px'}}>
                    <h3>{selected.nombre}</h3>
                    <span className={`tag tag-${selected.estado.toLowerCase()}`}>{selected.estado}</span>
                  </div>
                  <p><strong>Tipo:</strong> {selected.tipoAnimal ?? selected.tipo_animal}</p>
                  <p><strong>Raza:</strong> {selected.raza}</p>
                  <p><strong>Sector:</strong> {selected.sector}</p>
                  <p><strong>Contacto:</strong> {selected.contacto}</p>
                  <button
                    className="btn btn-primary btn-sm"
                    style={{marginTop:'12px',width:'100%',justifyContent:'center'}}
                    onClick={() => navigate(`/mascotas/${selected.idMascota ?? selected.id_mascota}`)}
                  >
                    Ver detalle completo →
                  </button>
                </div>
              </div>
            ) : (
              <div className="mapa-lista">
                <h3 className="lista-title">Casos en el mapa ({filtered.length})</h3>
                {filtered.length === 0 ? (
                  <p style={{color:'var(--stone-light)',fontSize:'0.88rem'}}>Sin casos con este filtro</p>
                ) : (
                  filtered.map(m => {
                    const idM = m.idMascota ?? m.id_mascota;
                    return (
                    <button
                      key={idM}
                      className="lista-item"
                      onClick={() => setSelected(m)}
                    >
                      <span className={`dot dot-sm`} style={{background: estadoColor[m.estado]}}></span>
                      <span className="lista-nombre">{m.nombre}</span>
                      <span className="lista-tipo">{m.tipoAnimal ?? m.tipo_animal}</span>
                      <span className="lista-sector">{m.sector}</span>
                    </button>
                    );
                  })
                )}
              </div>
            )}

            {/* Zona stats */}
            <div className="zona-stats card">
              <h4>📊 Estadísticas de zona</h4>
              <div className="zona-row">
                <span>Total en mapa:</span>
                <strong>{filtered.length}</strong>
              </div>
              <div className="zona-row">
                <span>🔴 Perdidas:</span>
                <strong>{filtered.filter(m=>m.estado==='PERDIDA').length}</strong>
              </div>
              <div className="zona-row">
                <span>🟢 Encontradas:</span>
                <strong>{filtered.filter(m=>m.estado==='ENCONTRADA').length}</strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
