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

  const handleChange = e => {
    const { name, value } = e.target;

    // Formateo automático del teléfono chileno
    if (name === 'telefono' || name === 'contacto_tel') {
      const digits = value.replace(/\D/g, '');
      let formatted = value;

      // Si escribe solo dígitos, aplicar formato +56 9 XXXX XXXX
      if (/^\d+$/.test(value) || value === '') {
        if (digits.length === 0) {
          formatted = '';
        } else if (digits.startsWith('569')) {
          // 56 9 XXXX XXXX
          const local = digits.slice(3); // dígitos después del 569
          if (local.length <= 4) {
            formatted = `+56 9 ${local}`;
          } else {
            formatted = `+56 9 ${local.slice(0, 4)} ${local.slice(4, 8)}`;
          }
        } else if (digits.startsWith('9')) {
          const local = digits.slice(1);
          if (local.length <= 4) {
            formatted = `+56 9 ${local}`;
          } else {
            formatted = `+56 9 ${local.slice(0, 4)} ${local.slice(4, 8)}`;
          }
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

  // Valida teléfono chileno: +56 9 XXXX XXXX o variantes
  // Acepta: +56912345678, 56912345678, 912345678, +56 9 1234 5678, etc.
  const validarTelefonoChileno = (tel) => {
    if (!tel) return true; // campo opcional
    const digits = tel.replace(/\D/g, '');
    // Debe tener 9 dígitos locales empezando en 9, o con prefijo 56
    if (digits.startsWith('569') && digits.length === 11) return true;
    if (digits.startsWith('9')   && digits.length === 9)  return true;
    return false;
  };

  const validate = () => {
    const errs = {};
    if (!form.tipo_animal) errs.tipo_animal = 'Selecciona el tipo de animal';
    if (form.tipo_animal === 'Otro' && !form.tipo_animal_otro.trim()) {
      errs.tipo_animal_otro = 'Especifica el tipo de animal';
    }
    if (!form.estado) errs.estado = 'Selecciona el estado';
    if (!form.sector)    errs.sector    = 'Indica el sector';
    if (!form.comuna)    errs.comuna    = 'Indica la comuna';
    if (!form.direccion) errs.direccion = 'Indica la dirección';

    // Contacto: obligatorio, debe ser email o teléfono chileno válido
    if (!form.contacto) {
      errs.contacto = 'El contacto es obligatorio';
    } else {
      const esEmail = /\S+@\S+\.\S+/.test(form.contacto);
      const esTelChileno = validarTelefonoChileno(form.contacto);
      if (!esEmail && !esTelChileno) {
        errs.contacto = 'Ingresa un email válido o teléfono chileno (+56 9 XXXX XXXX)';
      }
    }

    // Teléfono adicional: opcional pero si se ingresa debe ser válido
    if (form.telefono && !validarTelefonoChileno(form.telefono)) {
      errs.telefono = 'Teléfono inválido. Formato: +56 9 XXXX XXXX (9 dígitos)';
    }

    return errs;
  };

  const handleFileUpload = (file, fieldName = 'foto_url') => {
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      setForm(prev => ({ ...prev, [fieldName]: reader.result }));
    };
    reader.onerror = () => addToast('No se pudo procesar la imagen', 'error');
    reader.readAsDataURL(file);
  };

  const useCurrentLocation = () => {
    if (!navigator.geolocation) {
      addToast('Tu navegador no soporta geolocalización', 'error');
      return;
    }

    setGeoLoading(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setForm(prev => ({
          ...prev,
          latitud: String(position.coords.latitude),
          longitud: String(position.coords.longitude)
        }));
        addToast('Ubicación actual cargada ✅', 'success');
        setGeoLoading(false);
      },
      () => {
        addToast('No fue posible obtener tu ubicación', 'error');
        setGeoLoading(false);
      }
    );
  };

  const handleSubmit = async e => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    try {
      // Convertir snake_case del formulario a camelCase que espera el backend
      const tipoFinal = form.tipo_animal === 'Otro' ? form.tipo_animal_otro : form.tipo_animal;
      const payload = {
        tipoAnimal:    tipoFinal,
        nombre:        form.nombre       || null,
        raza:          form.raza         || null,
        colorPrimario: form.color_primario || null,
        tamano:        form.tamano       || null,  // null si no seleccionó
        sexo:          form.sexo         || null,
        edad:          form.edad         || null,
        estado:        form.estado,
        descripcion:   form.descripcion  || null,
        contacto:      form.contacto,
        telefono:      form.telefono     || null,
        sector:        form.sector,
        comuna:        form.comuna,
        direccion:     form.direccion,
        fotoUrl:       form.foto_url     || null,
        latitud:       parseFloat(form.latitud)  || -36.8201,
        longitud:      parseFloat(form.longitud) || -73.0444,
      };
      await mascotasService.reportar(payload);
      addToast('¡Mascota reportada exitosamente! 🐾', 'success');
      setForm(INITIAL);
      onSuccess && onSuccess();
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al reportar. Intenta de nuevo.', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="form-mascota" onSubmit={handleSubmit}>
      <div className="form-section">
        <h3 className="form-section-title">📋 Información básica</h3>
        <div className="form-grid-2">
          <div className="form-group">
            <label className="form-label">Estado *</label>
            <select name="estado" value={form.estado} onChange={handleChange} className="form-input">
              <option value="PERDIDA">🔴 Perdida</option>
              <option value="ENCONTRADA">🟢 Encontrada</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Tipo de animal *</label>
            <select name="tipo_animal" value={form.tipo_animal} onChange={handleChange} className={`form-input ${errors.tipo_animal ? 'input-error' : ''}`}>
              <option value="">Seleccionar...</option>
              <option value="Perro">🐕 Perro</option>
              <option value="Gato">🐈 Gato</option>
              <option value="Ave">🦜 Ave</option>
              <option value="Conejo">🐇 Conejo</option>
              <option value="Otro">🐾 Otro</option>
            </select>
            {errors.tipo_animal && <span className="field-error">{errors.tipo_animal}</span>}
          </div>
        </div>

        {form.tipo_animal === 'Otro' && (
          <div className="form-group">
            <label className="form-label">Especifica el tipo de animal *</label>
            <input
              name="tipo_animal_otro"
              value={form.tipo_animal_otro}
              onChange={handleChange}
              className={`form-input ${errors.tipo_animal_otro ? 'input-error' : ''}`}
              placeholder="Ej: Hurón, Tortuga..."
            />
            {errors.tipo_animal_otro && <span className="field-error">{errors.tipo_animal_otro}</span>}
          </div>
        )}

        <div className="form-grid-2">
          <div className="form-group">
            <label className="form-label">Nombre de la mascota</label>
            <input name="nombre" value={form.nombre} onChange={handleChange} className="form-input" placeholder="Ej: Firulais" />
          </div>
          <div className="form-group">
            <label className="form-label">Raza</label>
            <input name="raza" value={form.raza} onChange={handleChange} className="form-input" placeholder="Ej: Labrador, Mestizo..." />
          </div>
        </div>

        <div className="form-grid-2">
          <div className="form-group">
            <label className="form-label">Color principal</label>
            <input name="color_primario" value={form.color_primario} onChange={handleChange} className="form-input" placeholder="Ej: Café, Negro, Blanco..." />
          </div>
          <div className="form-group">
            <label className="form-label">Tamaño</label>
            <select name="tamano" value={form.tamano} onChange={handleChange} className="form-input">
              <option value="">Seleccionar...</option>
              <option value="Pequeño">Pequeño (menos de 10 kg)</option>
              <option value="Mediano">Mediano (10–25 kg)</option>
              <option value="Grande">Grande (más de 25 kg)</option>
            </select>
          </div>
        </div>

        <div className="form-grid-2">
          <div className="form-group">
            <label className="form-label">Sexo</label>
            <select name="sexo" value={form.sexo} onChange={handleChange} className="form-input">
              <option value="">Seleccionar...</option>
              <option value="Macho">Macho</option>
              <option value="Hembra">Hembra</option>
              <option value="No definido">No definido</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Edad</label>
            <input name="edad" value={form.edad} onChange={handleChange} className="form-input" placeholder="Ej: 2 años, 8 meses" />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Descripción adicional</label>
          <textarea name="descripcion" value={form.descripcion} onChange={handleChange} className="form-input" placeholder="Señas particulares, collar, chip, comportamiento..." />
        </div>
      </div>

      <div className="form-section">
        <h3 className="form-section-title">📍 ¿Dónde fue visto por última vez?</h3>
        <div className="form-grid-2">
          <div className="form-group">
            <label className="form-label">Sector / Barrio *</label>
            <input name="sector" value={form.sector} onChange={handleChange} className={`form-input ${errors.sector ? 'input-error' : ''}`} placeholder="Ej: Barrio Norte, Hualpén..." />
            {errors.sector && <span className="field-error">{errors.sector}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Comuna *</label>
            <input name="comuna" value={form.comuna} onChange={handleChange} className={`form-input ${errors.comuna ? 'input-error' : ''}`} placeholder="Ej: Concepción" />
            {errors.comuna && <span className="field-error">{errors.comuna}</span>}
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Dirección *</label>
          <input name="direccion" value={form.direccion} onChange={handleChange} className={`form-input ${errors.direccion ? 'input-error' : ''}`} placeholder="Ej: Avenida, calle y número o referencia" />
          {errors.direccion && <span className="field-error">{errors.direccion}</span>}
        </div>

        <div className="location-actions">
          <button type="button" className="btn btn-outline btn-sm" onClick={useCurrentLocation} disabled={geoLoading}>
            {geoLoading ? 'Obteniendo ubicación...' : 'Usar ubicación actual'}
          </button>
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => navigate('/mapa')}>
            Seleccionar ubicación en el mapa
          </button>
        </div>

        <div className="form-grid-2">
          <div className="form-group">
            <label className="form-label">Latitud</label>
            <input name="latitud" value={form.latitud} onChange={handleChange} className="form-input" placeholder="-36.8201" type="number" step="any" />
          </div>
          <div className="form-group">
            <label className="form-label">Longitud</label>
            <input name="longitud" value={form.longitud} onChange={handleChange} className="form-input" placeholder="-73.0444" type="number" step="any" />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Foto desde tu dispositivo</label>
          <input
            type="file"
            accept="image/*"
            className="form-input"
            onChange={(e) => handleFileUpload(e.target.files?.[0])}
          />
          <small className="form-helper">También puedes pegar una URL de imagen si prefieres</small>
        </div>

        <div className="form-group">
          <label className="form-label">URL de foto (opcional)</label>
          <input name="foto_url" value={form.foto_url} onChange={handleChange} className="form-input" placeholder="https://..." />
        </div>
      </div>

      <div className="form-section">
        <h3 className="form-section-title">📞 Contacto</h3>
        <div className="form-grid-2">
          <div className="form-group">
            <label className="form-label">Email o teléfono *</label>
            <input name="contacto" value={form.contacto} onChange={handleChange} className={`form-input ${errors.contacto ? 'input-error' : ''}`} placeholder="correo@ejemplo.com o +56 9 1234 5678" />
            {errors.contacto && <span className="field-error">{errors.contacto}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Teléfono adicional</label>
            <input name="telefono" value={form.telefono} onChange={handleChange} className={`form-input ${errors.telefono ? 'input-error' : ''}`} placeholder="+56 9 1234 5678" maxLength={16} />
            {errors.telefono && <span className="field-error">{errors.telefono}</span>}
            <small className="form-helper">Formato chileno: +56 9 XXXX XXXX</small>
          </div>
        </div>
      </div>

      {form.foto_url && (
        <div className="foto-preview">
          <p className="form-label">Vista previa de foto:</p>
          <img src={form.foto_url} alt="Preview" onError={e => e.target.style.display='none'} />
        </div>
      )}

      <button type="submit" className="btn btn-primary btn-lg submit-btn" disabled={loading}>
        {loading ? '⏳ Enviando reporte...' : '🐾 Publicar reporte'}
      </button>
    </form>
  );
}
