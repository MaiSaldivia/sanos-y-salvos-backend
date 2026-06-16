import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { mascotasService, motorService, authService } from '../services/api';
import { useToast } from '../components/ToastProvider';
import './DetalleMascota.css';

const ESTADOS = ['PERDIDA', 'ENCONTRADA', 'REUNIFICADA'];

export default function DetalleMascota() {
  const { id }       = useParams();
  const navigate     = useNavigate();
  const { addToast } = useToast();
  const user         = authService.getStoredUser();
  const esModerador  = user && ['ADMIN','REFUGIO','MUNICIPALIDAD'].includes(user.rol);

  const [mascota,       setMascota]       = useState(null);
  const [coincidencias, setCoincidencias] = useState([]);
  const [loading,       setLoading]       = useState(true);
  const [imgError,      setImgError]      = useState(false);

  // Modales
  const [openEncuentro,      setOpenEncuentro]      = useState(false);
  const [reportingEncuentro, setReportingEncuentro] = useState(false);
  const [openEstado,         setOpenEstado]         = useState(false);
  const [nuevoEstado,        setNuevoEstado]        = useState('');
  const [cambiandoEstado,    setCambiandoEstado]    = useState(false);

  const [encuentroForm, setEncuentroForm] = useState({
    foto_evidencia_url: '', encontrada_en: '', contacto_nombre: '', contacto_telefono: ''
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const mRes = await mascotasService.getById(id);
        setMascota(mRes.data);
        setNuevoEstado(mRes.data.estado);
        // Coincidencias en paralelo — si falla no bloquea la página
        motorService.getCoincidencias(id)
          .then(r => {
            const data = r.data?.resultados ?? r.data ?? [];
            setCoincidencias(Array.isArray(data) ? data : []);
          })
          .catch(() => setCoincidencias([]));
      } catch {
        addToast('No se pudo cargar el reporte', 'error');
        navigate('/mascotas');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  if (loading) return <div className="loader"><div className="spinner"></div></div>;
  if (!mascota) return null;

  const estadoCfg = {
    PERDIDA:    { label: 'Perdida',    cls: 'tag-perdida',    icon: '🔴' },
    ENCONTRADA: { label: 'Encontrada', cls: 'tag-encontrada', icon: '🟢' },
    REUNIFICADA:{ label: 'Reunificada',cls: 'tag-reunificada',icon: '🏠' }
  };
  const cfg   = estadoCfg[mascota.estado] || estadoCfg.PERDIDA;
  // Soporte camelCase (backend real) y snake_case (legacy)
  const idMascota     = mascota.idMascota     ?? mascota.id_mascota;
  const fotoUrl       = mascota.fotoUrl       ?? mascota.foto_url;
  const tipoAnimal    = mascota.tipoAnimal    ?? mascota.tipo_animal;
  const colorPrimario = mascota.colorPrimario ?? mascota.color_primario;
  const fechaReporte  = mascota.fechaReporte  ?? mascota.fecha_reporte;

  const fecha = new Date(fechaReporte).toLocaleDateString('es-CL', {
    weekday:'long', day:'numeric', month:'long', year:'numeric'
  });

  // ── Reportar encuentro ─────────────────────────────────────
  const submitEncuentro = async () => {
    const { foto_evidencia_url, encontrada_en, contacto_nombre, contacto_telefono } = encuentroForm;
    if (!encontrada_en || !contacto_nombre || !contacto_telefono) {
      addToast('Completa los campos obligatorios (ubicación, nombre y teléfono)', 'error'); return;
    }
    if (encontrada_en.trim().length < 3) {
      addToast('La ubicación donde fue encontrada debe tener al menos 3 caracteres', 'error'); return;
    }
    if (contacto_nombre.trim().length < 2) {
      addToast('El nombre de contacto debe tener al menos 2 caracteres', 'error'); return;
    }
    // Validar teléfono: entre 7 y 20 chars con dígitos, +, -, espacios, paréntesis
    const telefonoRegex = /^[+0-9\s()\-]{7,20}$/;
    if (!telefonoRegex.test(contacto_telefono.trim())) {
      addToast('Teléfono inválido. Ejemplo: +56 9 1234 5678 (7–20 caracteres)', 'error'); return;
    }
    setReportingEncuentro(true);
    try {
      await mascotasService.reportarEncuentro(idMascota, encuentroForm);
      addToast('Solicitud enviada a revisión ✅', 'success');
      setOpenEncuentro(false);
      setEncuentroForm({ foto_evidencia_url:'', encontrada_en:'', contacto_nombre:'', contacto_telefono:'' });
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al reportar encuentro', 'error');
    } finally { setReportingEncuentro(false); }
  };

  // ── Cambiar estado (moderadores) ───────────────────────────
  const submitEstado = async () => {
    if (nuevoEstado === mascota.estado) { setOpenEstado(false); return; }
    setCambiandoEstado(true);
    try {
      const res = await mascotasService.actualizar(idMascota, { ...mascota, estado: nuevoEstado });
      setMascota(res.data);
      addToast(`Estado actualizado a ${nuevoEstado}`, 'success');
      setOpenEstado(false);
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al actualizar estado', 'error');
    } finally { setCambiandoEstado(false); }
  };

  return (
    <div className="detalle-page page-enter">
      <div className="container">
        <button className="btn btn-ghost back-btn" onClick={() => navigate(-1)}>← Volver</button>

        <div className="detalle-grid">
          {/* Foto */}
          <div className="detalle-foto-wrap">
            <img
              src={imgError ? 'https://images.unsplash.com/photo-1450778869180-41d0601e046e?w=600&q=80' : fotoUrl}
              alt={mascota.nombre}
              className="detalle-foto"
              onError={() => setImgError(true)}
            />
            <span className={`tag ${cfg.cls} detalle-estado`}>{cfg.icon} {cfg.label}</span>
          </div>

          {/* Info */}
          <div className="detalle-info">
            <div className="detalle-top">
              <h1 className="detalle-nombre">{mascota.nombre}</h1>
              <span className="detalle-tipo">{tipoAnimal}</span>
            </div>

            <div className="detalle-attrs">
              {[
                { label:'Raza',           value: mascota.raza },
                { label:'Color',          value: colorPrimario },
                { label:'Tamaño',         value: mascota.tamano },
                { label:'Sexo',           value: mascota.sexo },
                { label:'Edad',           value: mascota.edad },
                { label:'Sector',         value: mascota.sector },
                { label:'Comuna',         value: mascota.comuna },
                { label:'Dirección',      value: mascota.direccion },
                { label:'Fecha reporte',  value: fecha },
              ].filter(a => a.value && a.value !== 'No especificado' && a.value !== 'No especificada').map(a => (
                <div key={a.label} className="attr-row">
                  <span className="attr-label">{a.label}</span>
                  <span className="attr-value">{a.value}</span>
                </div>
              ))}
            </div>

            {mascota.descripcion && (
              <div className="detalle-desc-box">
                <h3>Descripción</h3>
                <p>{mascota.descripcion}</p>
              </div>
            )}

            <div className="detalle-contacto">
              <h3>📞 Contacto</h3>
              <div className="contacto-grid">
                {mascota.contacto && (
                  /\S+@\S+\.\S+/.test(mascota.contacto)
                    ? <a href={`mailto:${mascota.contacto}`} className="contacto-item">✉️ {mascota.contacto}</a>
                    : <a href={`tel:${mascota.contacto}`}   className="contacto-item">📱 {mascota.contacto}</a>
                )}
                {mascota.telefono && (
                  <a href={`tel:${mascota.telefono}`} className="contacto-item">📱 {mascota.telefono}</a>
                )}
              </div>
            </div>

            {/* Acciones */}
            <div className="detalle-acciones">
              {mascota.estado === 'PERDIDA' && (
                <button className="btn btn-primary" onClick={() => setOpenEncuentro(true)}>
                  🐾 Reportar encuentro
                </button>
              )}
              {esModerador && (
                <button className="btn btn-outline" onClick={() => setOpenEstado(true)}>
                  ✏️ Cambiar estado
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Coincidencias del motor */}
        {coincidencias.length > 0 && (
          <div className="coincidencias-section">
            <h2 className="coincidencias-title">
              🔍 Posibles coincidencias
              <span className="coincidencias-badge">{coincidencias.length}</span>
            </h2>
            <div className="coincidencias-grid">
              {coincidencias.map(c => {
                const cId  = c.idMascota   ?? c.mascota?.idMascota   ?? c.id_mascota;
                const cFoto= c.fotoUrl     ?? c.mascota?.fotoUrl     ?? c.foto_url;
                const cNombre = c.nombre   ?? c.mascota?.nombre;
                const cScore  = c.score;
                const cDist   = c.distanciaKm ?? c.distancia_km;
                return (
                <button key={cId} className="coincidencia-card"
                  onClick={() => navigate(`/mascotas/${cId}`)}>
                  <img src={cFoto} alt={cNombre}
                    onError={e => e.target.src='https://images.unsplash.com/photo-1450778869180-41d0601e046e?w=200&q=80'} />
                  <div className="coin-info">
                    <span className="coin-nombre">{cNombre || 'Sin nombre'}</span>
                    <span className="coin-score">Score: {cScore}%</span>
                    <span className="coin-dist">~{cDist} km</span>
                  </div>
                </button>
                );
              })}
            </div>
          </div>
        )}

        {/* Modal: Reportar encuentro */}
        {openEncuentro && (
          <div className="modal-overlay" onClick={() => setOpenEncuentro(false)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <div className="modal-header">
                <h2>Reportar encuentro</h2>
                <button className="btn btn-ghost btn-sm" onClick={() => setOpenEncuentro(false)}>✕</button>
              </div>
              <div className="modal-body">
                {[
                  { label:'Foto de evidencia *', name:'foto_evidencia_url', type:'file' },
                  { label:'¿Dónde fue encontrada? *', name:'encontrada_en', type:'text' },
                  { label:'Tu nombre *',          name:'contacto_nombre',   type:'text' },
                  { label:'Tu teléfono *',        name:'contacto_telefono', type:'tel'  },
                ].map(f => (
                  <div key={f.name} className="form-group" style={{marginBottom:12}}>
                    <label className="form-label">{f.label}</label>
                    {f.type === 'file'
                      ? <input type="file" accept="image/*" className="form-input"
                          onChange={e => {
                            const file = e.target.files?.[0];
                            if (!file) return;
                            const r = new FileReader();
                            r.onload = () => setEncuentroForm(p => ({...p, foto_evidencia_url: r.result}));
                            r.readAsDataURL(file);
                          }} />
                      : <input type={f.type} name={f.name} className="form-input"
                          value={encuentroForm[f.name]}
                          onChange={e => setEncuentroForm(p => ({...p, [e.target.name]: e.target.value}))} />
                    }
                  </div>
                ))}
              </div>
              <div className="modal-footer">
                <button className="btn btn-outline" onClick={() => setOpenEncuentro(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={submitEncuentro} disabled={reportingEncuentro}>
                  {reportingEncuentro ? 'Enviando...' : 'Enviar a revisión'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Modal: Cambiar estado (moderadores) */}
        {openEstado && (
          <div className="modal-overlay" onClick={() => setOpenEstado(false)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <div className="modal-header">
                <h2>Cambiar estado del reporte</h2>
                <button className="btn btn-ghost btn-sm" onClick={() => setOpenEstado(false)}>✕</button>
              </div>
              <div className="modal-body">
                <p style={{marginBottom:16, color:'var(--text-muted)'}}>
                  Estado actual: <strong>{mascota.estado}</strong>
                </p>
                <div className="form-group">
                  <label className="form-label">Nuevo estado</label>
                  <select className="form-input" value={nuevoEstado}
                    onChange={e => setNuevoEstado(e.target.value)}>
                    {ESTADOS.map(e => (
                      <option key={e} value={e}>{e}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-outline" onClick={() => setOpenEstado(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={submitEstado} disabled={cambiandoEstado}>
                  {cambiandoEstado ? 'Guardando...' : 'Guardar cambio'}
                </button>
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
