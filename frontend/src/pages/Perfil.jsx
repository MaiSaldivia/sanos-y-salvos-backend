import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService, mascotasService } from '../services/api';
import { useToast } from '../components/ToastProvider';

export default function Perfil() {
  const navigate     = useNavigate();
  const { addToast } = useToast();
  const user         = authService.getStoredUser();

  const [perfil,    setPerfil]    = useState(null);
  const [mascotas,  setMascotas]  = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [tab,       setTab]       = useState('datos');     // 'datos' | 'password' | 'reportes'

  const [nombre,    setNombre]    = useState('');
  const [guardando, setGuardando] = useState(false);
  const [pwForm,    setPwForm]    = useState({ password_actual:'', password_nuevo:'', confirmar:'' });

  useEffect(() => {
    if (!user) { navigate('/login'); return; }
    Promise.all([
      authService.me(),
      mascotasService.getMisMascotas().catch(() => ({ data: [] }))
    ]).then(([pRes, mRes]) => {
      // me() devuelve ResponseWrapper: { success, data: UserPublicDto }
      const perfilData = pRes.data?.data ?? pRes.data;
      setPerfil(perfilData);
      setNombre(perfilData.nombre);
      const lista = Array.isArray(mRes.data) ? mRes.data : [];
      setMascotas(lista);
    }).catch(() => {
      addToast('Error al cargar perfil', 'error');
      navigate('/');
    }).finally(() => setLoading(false));
  }, []);

  const guardarNombre = async () => {
    if (!nombre.trim()) { addToast('El nombre no puede estar vacío', 'error'); return; }
    if (nombre.trim().length < 2) { addToast('El nombre debe tener al menos 2 caracteres', 'error'); return; }
    if (nombre.trim().length > 120) { addToast('El nombre no puede superar los 120 caracteres', 'error'); return; }
    setGuardando(true);
    try {
      const res = await authService.actualizarPerfil({ nombre });
      const perfilActualizado = res.data?.data ?? res.data;
      setPerfil(perfilActualizado);
      localStorage.setItem('sanos_user', JSON.stringify({ ...user, nombre }));
      addToast('Nombre actualizado correctamente', 'success');
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al actualizar', 'error');
    } finally { setGuardando(false); }
  };

  const cambiarPassword = async () => {
    if (!pwForm.password_actual) { addToast('Ingresa tu contraseña actual', 'error'); return; }
    if (!pwForm.password_nuevo || pwForm.password_nuevo.length < 6) {
      addToast('La nueva contraseña debe tener al menos 6 caracteres', 'error'); return;
    }
    if (pwForm.password_nuevo !== pwForm.confirmar) { addToast('Las contraseñas no coinciden', 'error'); return; }
    if (pwForm.password_actual === pwForm.password_nuevo) {
      addToast('La nueva contraseña debe ser diferente a la actual', 'error'); return;
    }
    setGuardando(true);
    try {
      await authService.cambiarPassword({ password_actual: pwForm.password_actual, password_nuevo: pwForm.password_nuevo });
      addToast('Contraseña actualizada', 'success');
      setPwForm({ password_actual:'', password_nuevo:'', confirmar:'' });
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al cambiar contraseña', 'error');
    } finally { setGuardando(false); }
  };

  const rolLabel = { ADMIN:'Admin', MUNICIPALIDAD:'Municipalidad', REFUGIO:'Refugio', VETERINARIA:'Veterinaria', DUENO:'Dueño' };

  if (loading) return <div className="loader"><div className="spinner"></div></div>;

  return (
    <div className="page-enter" style={{padding:'40px 0'}}>
      <div className="container" style={{maxWidth:720}}>
        <h1 style={{marginBottom:8}}>Mi perfil</h1>
        <p style={{color:'var(--text-muted)', marginBottom:28}}>
          {perfil?.email} · <strong>{rolLabel[perfil?.rol] || perfil?.rol}</strong>
        </p>

        {/* Tabs */}
        <div style={{display:'flex', borderBottom:'2px solid var(--border)', marginBottom:28}}>
          {[['datos','Mis datos'],['password','Contraseña'],['reportes','Mis reportes']].map(([k,l]) => (
            <button key={k} onClick={() => setTab(k)} style={{
              flex:1, background:'none', border:'none', padding:'10px 0',
              fontWeight: tab===k ? 700 : 400,
              color: tab===k ? 'var(--primary)' : 'var(--text-muted)',
              borderBottom: tab===k ? '2px solid var(--primary)' : '2px solid transparent',
              marginBottom:-2, cursor:'pointer', fontSize:'0.95rem'
            }}>{l}</button>
          ))}
        </div>

        {/* Tab: Datos */}
        {tab === 'datos' && (
          <div className="card" style={{padding:28}}>
            <h2 style={{marginBottom:20, fontSize:'1.1rem'}}>Editar datos personales</h2>
            <div className="form-group" style={{marginBottom:16}}>
              <label className="form-label">Nombre completo</label>
              <input className="form-input" value={nombre} onChange={e => setNombre(e.target.value)} />
            </div>
            <div className="form-group" style={{marginBottom:16}}>
              <label className="form-label">Email</label>
              <input className="form-input" value={perfil?.email || ''} disabled style={{opacity:0.6}} />
              <small style={{color:'var(--text-muted)'}}>El email no se puede cambiar.</small>
            </div>
            <div className="form-group" style={{marginBottom:24}}>
              <label className="form-label">Rol</label>
              <input className="form-input" value={rolLabel[perfil?.rol] || perfil?.rol} disabled style={{opacity:0.6}} />
              <small style={{color:'var(--text-muted)'}}>El rol es asignado por un administrador.</small>
            </div>
            <button className="btn btn-primary" onClick={guardarNombre} disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </div>
        )}

        {/* Tab: Contraseña */}
        {tab === 'password' && (
          <div className="card" style={{padding:28}}>
            <h2 style={{marginBottom:20, fontSize:'1.1rem'}}>Cambiar contraseña</h2>
            {[
              { label:'Contraseña actual',   key:'password_actual' },
              { label:'Nueva contraseña',    key:'password_nuevo' },
              { label:'Confirmar contraseña',key:'confirmar' },
            ].map(f => (
              <div key={f.key} className="form-group" style={{marginBottom:16}}>
                <label className="form-label">{f.label}</label>
                <input type="password" className="form-input" value={pwForm[f.key]}
                  onChange={e => setPwForm(p => ({...p, [f.key]: e.target.value}))} />
              </div>
            ))}
            <button className="btn btn-primary" onClick={cambiarPassword} disabled={guardando}>
              {guardando ? 'Cambiando...' : 'Cambiar contraseña'}
            </button>
          </div>
        )}

        {/* Tab: Mis reportes */}
        {tab === 'reportes' && (
          <div>
            {mascotas.length === 0 ? (
              <div className="card" style={{padding:40, textAlign:'center', color:'var(--text-muted)'}}>
                <p style={{fontSize:'2rem', marginBottom:8}}>🐾</p>
                <p>No tienes reportes aún.</p>
                <button className="btn btn-primary" style={{marginTop:16}} onClick={() => navigate('/reportar')}>
                  Crear primer reporte
                </button>
              </div>
            ) : (
              <div style={{display:'flex', flexDirection:'column', gap:12}}>
                {mascotas.map(m => {
                  const idM   = m.idMascota  ?? m.id_mascota;
                  const fotoM = m.fotoUrl    ?? m.foto_url;
                  const tipoM = m.tipoAnimal ?? m.tipo_animal;
                  return (
                  <div key={idM} className="card"
                    style={{display:'flex', alignItems:'center', gap:16, padding:16, cursor:'pointer'}}
                    onClick={() => navigate(`/mascotas/${idM}`)}>
                    <img src={fotoM} alt={m.nombre}
                      style={{width:64, height:64, borderRadius:8, objectFit:'cover', flexShrink:0}}
                      onError={e => e.target.src='https://images.unsplash.com/photo-1450778869180-41d0601e046e?w=80&q=60'} />
                    <div style={{flex:1}}>
                      <strong>{m.nombre || 'Sin nombre'}</strong>
                      <span style={{marginLeft:8, fontSize:'0.82rem', color:'var(--text-muted)'}}>
                        {tipoM} · {m.sector}
                      </span>
                    </div>
                    <span className={`tag tag-${m.estado.toLowerCase()}`}>{m.estado}</span>
                  </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
