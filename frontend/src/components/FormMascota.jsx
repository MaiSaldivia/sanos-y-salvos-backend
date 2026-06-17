import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { mascotasService } from '../services/api';
import { useToast } from './ToastProvider';
import './FormMascota.css';

const INITIAL = {
  nombre: '',
  tipo_animal: '',
  tipo_animal_otro: '',
  raza: '',
  color_primario: '',
  tamano: '',
  sexo: '',
  edad: '',
  estado: 'PERDIDA',
  descripcion: '',
  contacto: '',
  telefono: '',
  sector: '',
  comuna: '',
  direccion: '',
  foto_url: '',
  latitud: '-36.8201',
  longitud: '-73.0444'
};

export default function FormMascota({ onSuccess }) {
  const navigate = useNavigate();
  const [form, setForm] = useState(INITIAL);
  const [loading, setLoading] = useState(false);
  const [geoLoading, setGeoLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const { addToast } = useToast();

  // ── Handlers ────────────────────────────────────────────────────────
  const handleChange = e => {
    const { name, value } = e.target;

    if (name === 'telefono' || name === 'contacto_tel') {
      const digits = value.replace(/\D/g, '');
      let formatted = value;
      if (/^\d+$/.test(value) || value === '') {
        if (digits.length === 0) {
          formatted = '';
        } else if (digits.startsWith('569')) {
          const local = digits.slice(3);
          formatted = local.length <= 4 ? `+56 9 ${local}` : `+56 9 ${local.slice(0, 4)} ${local.slice(4, 8)}`;
        } else if (digits.startsWith('9')) {
          const local = digits.slice(1);
          formatted = local.length <= 4 ? `+56 9 ${local}` : `+56 9 ${local.slice(0, 4)} ${local.slice(4, 8)}`;
        } else {
          formatted = value;
        }
      }
      setForm(prev => ({ ...prev, [name]: formatted }));
      if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
      return;
    }

    setForm(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validarTelefonoChileno = (tel) => {
    if (!tel) return true;
    const digits = tel.replace(/\D/g, '');
    if (digits.startsWith('569') && digits.length === 11) return true;
    if (digits.startsWith('9')   && digits.length === 9)  return true;
    return false;
  };

  const validate = () => {
    const errs = {};
    if (!form.tipo_animal) errs.tipo_animal = 'Selecciona el tipo de animal';
    if (form.tipo_animal === 'Otro' && !form.tipo_animal_otro.trim())
      errs.tipo_animal_otro = 'Especifica el tipo de animal';
    if (!form.estado)    errs.estado    = 'Selecciona el estado';
    if (!form.sector)    errs.sector    = 'Indica el sector';
    if (!form.comuna)    errs.comuna    = 'Indica la comuna';
    if (!form.direccion) errs.direccion = 'Indica la dirección';
    if (!form.contacto) {
      errs.contacto = 'El contacto es obligatorio';
    } else {
      const esEmail     = /\S+@\S+\.\S+/.test(form.contacto);
      const esTelChileno = validarTelefonoChileno(form.contacto);
      if (!esEmail && !esTelChileno)
        errs.contacto = 'Ingresa un email válido o teléfono chileno (+56 9 XXXX XXXX)';
    }
    if (form.telefono && !validarTelefonoChileno(form.telefono))
      errs.telefono = 'Teléfono inválido. Formato: +56 9 XXXX XXXX';
    return errs;
  };

  const handleFileUpload = (file, fieldName = 'foto_url') => {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => setForm(prev => ({ ...prev, [fieldName]: reader.result }));
    reader.onerror = () => addToast('No se pudo procesar la imagen', 'error');
    reader.readAsDataURL(file);
  };

  const useCurrentLocation = () => {
    if (!navigator.geolocation) { addToast('Tu navegador no soporta geolocalización', 'error'); return; }
    setGeoLoading(true);
    navigator.geolocation.getCurrentPosition(
      pos => {
        setForm(prev => ({ ...prev, latitud: String(pos.coords.latitude), longitud: String(pos.coords.longitude) }));
        addToast('Ubicación cargada correctamente', 'success');
        setGeoLoading(false);
      },
      () => { addToast('No fue posible obtener tu ubicación', 'error'); setGeoLoading(false); }
    );
  };

  const handleSubmit = async e => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setLoading(true);
    try {
      const tipoFinal = form.tipo_animal === 'Otro' ? form.tipo_animal_otro : form.tipo_animal;
      const payload = {
        tipoAnimal:    tipoFinal,
        nombre:        form.nombre        || null,
        raza:          form.raza          || null,
        colorPrimario: form.color_primario || null,
        tamano:        form.tamano        || null,
        sexo:          form.sexo          || null,
        edad:          form.edad          || null,
        estado:        form.estado,
        descripcion:   form.descripcion   || null,
        contacto:      form.contacto,
        telefono:      form.telefono      || null,
        sector:        form.sector,
        comuna:        form.comuna,
        direccion:     form.direccion,
        fotoUrl:       form.foto_url      || null,
        latitud:       parseFloat(form.latitud)  || -36.8201,
        longitud:      parseFloat(form.longitud) || -73.0444,
      };
      await mascotasService.reportar(payload);
      addToast('Mascota reportada exitosamente', 'success');
      setForm(INITIAL);
      onSuccess && onSuccess();
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al reportar. Intenta de nuevo.', 'error');
    } finally {
      setLoading(false);
    }
  };

  // ── Render ──────────────────────────────────────────────────────────
  return (
    <form className="form-mascota" onSubmit={handleSubmit} noValidate>

      {/* ── Sección 1: Información básica ── */}
      <div className="fsection">
        <div className="fsection-header">
          <div className="fsection-num">1</div>
          <div>
            <h3 className="fsection-title">Información básica</h3>
            <p className="fsection-sub">Estado del animal y sus características físicas</p>
          </div>
        </div>

        {/* Estado + Tipo */}
        <div className="fgrid-2">
          <div className="form-group">
            <label className="form-label">Estado <span className="req">*</span></label>
            <div className="estado-toggle">
              {[
                { val: 'PERDIDA',    label: 'Perdida',    color: '#e53e3e' },
                { val: 'ENCONTRADA', label: 'Encontrada', color: '#38a169' },
              ].map(({ val, label, color }) => (
                <button
                  key={val}
                  type="button"
                  className={`estado-btn ${form.estado === val ? 'active' : ''}`}
                  style={form.estado === val ? { '--btn-c': color } : {}}
                  onClick={() => { setForm(p => ({ ...p, estado: val })); if (errors.estado) setErrors(p => ({ ...p, estado: '' })); }}
                >
                  <span className="estado-dot" style={{ background: color }}></span>
                  {label}
                </button>
              ))}
            </div>
            {errors.estado && <span className="field-error">{errors.estado}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Tipo de animal <span className="req">*</span></label>
            <div className="tipo-grid">
              {[
                { val: 'Perro'  },
                { val: 'Gato'   },
                { val: 'Ave'    },
                { val: 'Conejo' },
                { val: 'Otro'   },
              ].map(({ val }) => (
                <button
                  key={val}
                  type="button"
                  className={`tipo-btn ${form.tipo_animal === val ? 'active' : ''} ${errors.tipo_animal && !form.tipo_animal ? 'btn-err' : ''}`}
                  onClick={() => { setForm(p => ({ ...p, tipo_animal: val })); if (errors.tipo_animal) setErrors(p => ({ ...p, tipo_animal: '' })); }}
                >
                  {val}
                </button>
              ))}
            </div>
            {errors.tipo_animal && <span className="field-error">{errors.tipo_animal}</span>}
          </div>
        </div>

        {form.tipo_animal === 'Otro' && (
          <div className="form-group fslide-in">
            <label className="form-label">Especifica el tipo <span className="req">*</span></label>
            <input name="tipo_animal_otro" value={form.tipo_animal_otro} onChange={handleChange}
              className={`form-input ${errors.tipo_animal_otro ? 'input-error' : ''}`}
              placeholder="Ej: Hurón, Tortuga, Hamster..." />
            {errors.tipo_animal_otro && <span className="field-error">{errors.tipo_animal_otro}</span>}
          </div>
        )}

        {/* Nombre + Raza */}
        <div className="fgrid-2">
          <div className="form-group">
            <label className="form-label">Nombre</label>
            <input name="nombre" value={form.nombre} onChange={handleChange}
              className="form-input" placeholder="Ej: Firulais" />
          </div>
          <div className="form-group">
            <label className="form-label">Raza</label>
            <input name="raza" value={form.raza} onChange={handleChange}
              className="form-input" placeholder="Ej: Labrador, Mestizo..." />
          </div>
        </div>

        {/* Color + Tamaño */}
        <div className="fgrid-2">
          <div className="form-group">
            <label className="form-label">Color principal</label>
            <input name="color_primario" value={form.color_primario} onChange={handleChange}
              className="form-input" placeholder="Ej: Café, Negro, Blanco..." />
          </div>
          <div className="form-group">
            <label className="form-label">Tamaño</label>
            <select name="tamano" value={form.tamano} onChange={handleChange} className="form-input">
              <option value="">Sin especificar</option>
              <option value="Pequeño">Pequeño — menos de 10 kg</option>
              <option value="Mediano">Mediano — 10 a 25 kg</option>
              <option value="Grande">Grande — más de 25 kg</option>
            </select>
          </div>
        </div>

        {/* Sexo + Edad */}
        <div className="fgrid-2">
          <div className="form-group">
            <label className="form-label">Sexo</label>
            <select name="sexo" value={form.sexo} onChange={handleChange} className="form-input">
              <option value="">Sin especificar</option>
              <option value="Macho">Macho</option>
              <option value="Hembra">Hembra</option>
              <option value="No definido">No definido</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Edad aproximada</label>
            <input name="edad" value={form.edad} onChange={handleChange}
              className="form-input" placeholder="Ej: 2 años, 8 meses..." />
          </div>
        </div>

        {/* Descripción */}
        <div className="form-group">
          <label className="form-label">Descripción adicional</label>
          <textarea name="descripcion" value={form.descripcion} onChange={handleChange}
            className="form-input" rows={3}
            placeholder="Señas particulares, collar, chip, comportamiento, marcas especiales..." />
        </div>
      </div>

      {/* ── Sección 2: Ubicación ── */}
      <div className="fsection">
        <div className="fsection-header">
          <div className="fsection-num">2</div>
          <div>
            <h3 className="fsection-title">Ubicación</h3>
            <p className="fsection-sub">¿Dónde fue visto por última vez?</p>
          </div>
        </div>

        <div className="fgrid-2">
          <div className="form-group">
            <label className="form-label">Sector / Barrio <span className="req">*</span></label>
            <input name="sector" value={form.sector} onChange={handleChange}
              className={`form-input ${errors.sector ? 'input-error' : ''}`}
              placeholder="Ej: Barrio Norte, Hualpén..." />
            {errors.sector && <span className="field-error">{errors.sector}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Comuna <span className="req">*</span></label>
            <input name="comuna" value={form.comuna} onChange={handleChange}
              className={`form-input ${errors.comuna ? 'input-error' : ''}`}
              placeholder="Ej: Concepción" />
            {errors.comuna && <span className="field-error">{errors.comuna}</span>}
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Dirección <span className="req">*</span></label>
          <input name="direccion" value={form.direccion} onChange={handleChange}
            className={`form-input ${errors.direccion ? 'input-error' : ''}`}
            placeholder="Ej: Barros Arana 450 o referencia del lugar" />
          {errors.direccion && <span className="field-error">{errors.direccion}</span>}
        </div>

        <div className="location-btns">
          <button type="button" className="btn btn-outline btn-sm" onClick={useCurrentLocation} disabled={geoLoading}>
            {geoLoading
              ? <><span className="btn-spinner"></span> Obteniendo...</>
              : <>Usar mi ubicación</>}
          </button>
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => navigate('/mapa')}>
            Ver en el mapa
          </button>
        </div>

        <div className="fgrid-2">
          <div className="form-group">
            <label className="form-label">Latitud</label>
            <input name="latitud" value={form.latitud} onChange={handleChange}
              className="form-input" placeholder="-36.8201" type="number" step="any" />
          </div>
          <div className="form-group">
            <label className="form-label">Longitud</label>
            <input name="longitud" value={form.longitud} onChange={handleChange}
              className="form-input" placeholder="-73.0444" type="number" step="any" />
          </div>
        </div>

        {/* Foto */}
        <div className="foto-upload-area">
          <div className="form-group">
            <label className="form-label">Foto del animal</label>
            <label className="file-drop">
              <input type="file" accept="image/*" hidden
                onChange={e => handleFileUpload(e.target.files?.[0])} />
              <span className="file-drop-icon">+</span>
              <span className="file-drop-text">
                {form.foto_url && form.foto_url.startsWith('data:')
                  ? 'Imagen cargada — haz clic para cambiar'
                  : 'Haz clic o arrastra una imagen aquí'}
              </span>
              <span className="file-drop-hint">JPG, PNG o GIF hasta 5 MB</span>
            </label>
          </div>

          <div className="form-group">
            <label className="form-label">O pega una URL de imagen</label>
            <input name="foto_url" value={form.foto_url} onChange={handleChange}
              className="form-input" placeholder="https://..." />
          </div>
        </div>

        {form.foto_url && (
          <div className="foto-preview">
            <img src={form.foto_url} alt="Vista previa"
              onError={e => e.target.style.display = 'none'} />
            <button type="button" className="foto-remove"
              onClick={() => setForm(p => ({ ...p, foto_url: '' }))}>
              Quitar foto
            </button>
          </div>
        )}
      </div>

      {/* ── Sección 3: Contacto ── */}
      <div className="fsection">
        <div className="fsection-header">
          <div className="fsection-num">3</div>
          <div>
            <h3 className="fsection-title">Contacto</h3>
            <p className="fsection-sub">¿Cómo pueden comunicarse contigo?</p>
          </div>
        </div>

        <div className="fgrid-2">
          <div className="form-group">
            <label className="form-label">Email o teléfono <span className="req">*</span></label>
            <input name="contacto" value={form.contacto} onChange={handleChange}
              className={`form-input ${errors.contacto ? 'input-error' : ''}`}
              placeholder="correo@ejemplo.com o +56 9 1234 5678" />
            {errors.contacto && <span className="field-error">{errors.contacto}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Teléfono adicional</label>
            <input name="telefono" value={form.telefono} onChange={handleChange}
              className={`form-input ${errors.telefono ? 'input-error' : ''}`}
              placeholder="+56 9 1234 5678" maxLength={16} />
            {errors.telefono && <span className="field-error">{errors.telefono}</span>}
            <small className="form-helper">Formato: +56 9 XXXX XXXX</small>
          </div>
        </div>
      </div>

      {/* ── Submit ── */}
      <div className="form-submit-area">
        <button type="submit" className="btn btn-primary btn-lg submit-btn" disabled={loading}>
          {loading
            ? <><span className="btn-spinner"></span> Publicando reporte...</>
            : 'Publicar reporte'}
        </button>
        <p className="submit-hint">
          Al publicar aceptas que la información sea visible para todos los usuarios de la plataforma.
        </p>
      </div>

    </form>
  );
}
