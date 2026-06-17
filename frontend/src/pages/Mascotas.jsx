import { useState, useEffect, useCallback } from 'react';
import { authService, mascotasService } from '../services/api';
import CardMascota from '../components/CardMascota';
import { useToast } from '../components/ToastProvider';
import './Mascotas.css';

const ESTADO_OPTIONS = ['', 'PERDIDA', 'ENCONTRADA', 'REUNIFICADA'];
const TIPO_OPTIONS = ['', 'Perro', 'Gato', 'Ave', 'Conejo', 'Otro'];
const TAMANO_OPTIONS = ['', 'Pequeño', 'Mediano', 'Grande'];

export default function Mascotas() {
  const [mascotas, setMascotas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ estado: '', tipo_animal: '', tamano: '', raza: '' });
  const [modalMascota, setModalMascota] = useState(null);
  const [nuevoEstado, setNuevoEstado] = useState('');
  const [updating, setUpdating] = useState(false);
  const [encuentroMascota, setEncuentroMascota] = useState(null);
  const [encuentroForm, setEncuentroForm] = useState({
    foto_evidencia_url: '',
    encontrada_en: '',
    contacto_nombre: '',
    contacto_telefono: ''
  });
  const [reportingEncuentro, setReportingEncuentro] = useState(false);
  const { addToast } = useToast();
  const user = authService.getStoredUser();
  const canModerate = ['ADMIN', 'REFUGIO', 'MUNICIPALIDAD'].includes(user?.rol);

  const fetchMascotas = useCallback(async () => {
    setLoading(true);
    try {
      const activeFilters = Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== ''));
      const res = await mascotasService.getAll(activeFilters);
      const lista = Array.isArray(res.data) ? res.data : [];
      setMascotas(lista);
    } catch {
      addToast('Error al cargar los reportes', 'error');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => { fetchMascotas(); }, [fetchMascotas]);

  const handleFilterChange = e => {
    setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const clearFilters = () => {
    setFilters({ estado: '', tipo_animal: '', tamano: '', raza: '' });
  };

  const hasActiveFilters = Object.values(filters).some(v => v !== '');

  const handleEstadoOpen = (mascota) => {
    setModalMascota(mascota);
    setNuevoEstado(mascota.estado);
  };

  const handleEstadoSubmit = async () => {
    if (!nuevoEstado || nuevoEstado === modalMascota.estado) {
      setModalMascota(null);
      return;
    }
    setUpdating(true);
    try {
      const id = modalMascota.idMascota ?? modalMascota.id_mascota;
      await mascotasService.actualizar(id, { ...modalMascota, estado: nuevoEstado });
      addToast('Estado actualizado correctamente ✅', 'success');
      setModalMascota(null);
      fetchMascotas();
    } catch {
      addToast('Error al actualizar el estado', 'error');
    } finally {
      setUpdating(false);
    }
  };

  const handleEncuentroOpen = (mascota) => {
    setEncuentroMascota(mascota);
    setEncuentroForm({
      foto_evidencia_url: '',
      encontrada_en: '',
      contacto_nombre: '',
      contacto_telefono: ''
    });
  };

  const handleEncuentroInput = (e) => {
    const { name, value } = e.target;
    setEncuentroForm(prev => ({ ...prev, [name]: value }));
  };

  const handleEncuentroFile = (file) => {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      setEncuentroForm(prev => ({ ...prev, foto_evidencia_url: reader.result }));
    };
    reader.readAsDataURL(file);
  };

  const submitEncuentro = async () => {
    if (!encuentroForm.foto_evidencia_url || !encuentroForm.encontrada_en || !encuentroForm.contacto_nombre || !encuentroForm.contacto_telefono) {
      addToast('Completa todos los datos para reportar el encuentro', 'error');
      return;
    }

    setReportingEncuentro(true);
    try {
      const id = encuentroMascota.idMascota ?? encuentroMascota.id_mascota;
      await mascotasService.reportarEncuentro(id, encuentroForm);
      addToast('Solicitud enviada: quedó EN REVISION para validación ✅', 'success');
      setEncuentroMascota(null);
    } catch (err) {
      addToast(err.response?.data?.message || 'No se pudo enviar el reporte de encuentro', 'error');
    } finally {
      setReportingEncuentro(false);
    }
  };

  return (
    <div className="mascotas-page page-enter">
      <div className="container">
        <div className="page-header">
          <div>
            <h1 className="page-title">Reportes de mascotas</h1>
            <p className="page-sub">{loading ? 'Cargando...' : `${mascotas.length} resultado${mascotas.length !== 1 ? 's' : ''} encontrado${mascotas.length !== 1 ? 's' : ''}`}</p>
          </div>
        </div>

        {/* Filters */}
        <div className="filters-bar">
          <div className="filters-row">
            <select name="estado" value={filters.estado} onChange={handleFilterChange} className="form-input filter-select">
              <option value="">Todos los estados</option>
              {ESTADO_OPTIONS.filter(e => e).map(e => (
                <option key={e} value={e}>{e}</option>
              ))}
            </select>
            <select name="tipo_animal" value={filters.tipo_animal} onChange={handleFilterChange} className="form-input filter-select">
              <option value="">Todos los animales</option>
              {TIPO_OPTIONS.filter(t => t).map(t => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
            <select name="tamano" value={filters.tamano} onChange={handleFilterChange} className="form-input filter-select">
              <option value="">Cualquier tamaño</option>
              {TAMANO_OPTIONS.filter(t => t).map(t => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
            <input
              name="raza"
              value={filters.raza}
              onChange={handleFilterChange}
              className="form-input filter-input"
              placeholder="Buscar por raza..."
            />
            {hasActiveFilters && (
              <button className="btn btn-ghost btn-sm" onClick={clearFilters}>
                ✕ Limpiar filtros
              </button>
            )}
          </div>
        </div>

        {/* Content */}
        {loading ? (
          <div className="loader"><div className="spinner"></div><p>Cargando reportes...</p></div>
        ) : mascotas.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🐾</div>
            <h3>No se encontraron reportes</h3>
            <p>Intenta con otros filtros o reporta una nueva mascota.</p>
          </div>
        ) : (
          <div className="mascotas-grid">
            {mascotas.map(m => (
              <CardMascota
                key={m.idMascota ?? m.id_mascota}
                mascota={m}
                onEstadoChange={canModerate ? handleEstadoOpen : undefined}
                onReportEncuentro={handleEncuentroOpen}
              />
            ))}
          </div>
        )}
      </div>

      {/* Estado Modal */}
      {modalMascota && (
        <div className="modal-overlay" onClick={() => setModalMascota(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Actualizar estado</h2>
              <button className="btn btn-ghost btn-sm" onClick={() => setModalMascota(null)}>✕</button>
            </div>
            <div className="modal-body">
              <p style={{ color: 'var(--stone)', marginBottom: '20px' }}>
                Mascota: <strong>{modalMascota.nombre}</strong> ({modalMascota.tipoAnimal ?? modalMascota.tipo_animal})
              </p>
              <div className="form-group">
                <label className="form-label">Nuevo estado</label>
                <select value={nuevoEstado} onChange={e => setNuevoEstado(e.target.value)} className="form-input">
                  <option value="PERDIDA">🔴 Perdida</option>
                  <option value="ENCONTRADA">🟢 Encontrada</option>
                  <option value="REUNIFICADA">🏠 Reunificada</option>
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={() => setModalMascota(null)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleEstadoSubmit} disabled={updating}>
                {updating ? 'Guardando...' : 'Guardar cambio'}
              </button>
            </div>
          </div>
        </div>
      )}

      {encuentroMascota && (
        <div className="modal-overlay" onClick={() => setEncuentroMascota(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Reportar encuentro</h2>
              <button className="btn btn-ghost btn-sm" onClick={() => setEncuentroMascota(null)}>✕</button>
            </div>
            <div className="modal-body">
              <p style={{ color: 'var(--stone)', marginBottom: '20px' }}>
                Mascota: <strong>{encuentroMascota.nombre}</strong> ({encuentroMascota.tipoAnimal ?? encuentroMascota.tipo_animal})
              </p>

              <div className="form-group" style={{ marginBottom: '12px' }}>
                <label className="form-label">Foto de evidencia *</label>
                <input type="file" accept="image/*" className="form-input" onChange={(e) => handleEncuentroFile(e.target.files?.[0])} />
              </div>

              <div className="form-group" style={{ marginBottom: '12px' }}>
                <label className="form-label">Dónde fue encontrada? *</label>
                <input name="encontrada_en" value={encuentroForm.encontrada_en} onChange={handleEncuentroInput} className="form-input" placeholder="Comuna, dirección o referencia" />
              </div>

              <div className="form-group" style={{ marginBottom: '12px' }}>
                <label className="form-label">Nombre de contacto *</label>
                <input name="contacto_nombre" value={encuentroForm.contacto_nombre} onChange={handleEncuentroInput} className="form-input" placeholder="Tu nombre" />
              </div>

              <div className="form-group">
                <label className="form-label">Teléfono de contacto *</label>
                <input name="contacto_telefono" value={encuentroForm.contacto_telefono} onChange={handleEncuentroInput} className="form-input" placeholder="+56 9 ..." />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={() => setEncuentroMascota(null)}>Cancelar</button>
              <button className="btn btn-primary" onClick={submitEncuentro} disabled={reportingEncuentro}>
                {reportingEncuentro ? 'Enviando...' : 'Enviar a revisión'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
