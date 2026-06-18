import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService, adminService, mascotasService } from '../services/api';
import { useToast } from '../components/ToastProvider';

const ROLES = ['DUENO','VETERINARIA','REFUGIO','MUNICIPALIDAD','ADMIN'];
const rolColor = { ADMIN:'#e63946', MUNICIPALIDAD:'#457b9d', REFUGIO:'#2a9d8f', VETERINARIA:'#e9a010', DUENO:'#6c757d' };

export default function Admin() {
  const navigate     = useNavigate();
  const { addToast } = useToast();
  const user         = authService.getStoredUser();

  const [tab,        setTab]        = useState('usuarios');
  const [usuarios,   setUsuarios]   = useState([]);
  const [estadUsers, setEstadUsers] = useState(null);
  const [solicitudes,setSolicitudes]= useState([]);
  const [loading,    setLoading]    = useState(true);

  const [editando,   setEditando]   = useState(null);  // id del usuario editando rol
  const [nuevoRol,   setNuevoRol]   = useState('');
  const [guardando,  setGuardando]  = useState(false);
  const [revisando,  setRevisando]  = useState(null);

  useEffect(() => {
    if (!user || user.rol !== 'ADMIN') { navigate('/'); return; }
    cargarTodo();
  }, []);

  const cargarTodo = async () => {
    setLoading(true);
    // Cada petición independiente — si una falla las otras siguen
    const [uRes, eRes, sRes] = await Promise.allSettled([
      adminService.listarUsuarios(),
      adminService.getEstadisticas(),
      mascotasService.getEncuentrosRevision(),
    ]);

    if (uRes.status === 'fulfilled') {
      const uData = Array.isArray(uRes.value.data?.data)
        ? uRes.value.data.data
        : Array.isArray(uRes.value.data) ? uRes.value.data : [];
      setUsuarios(uData);
    } else {
      addToast('Error al cargar usuarios', 'error');
    }

    if (eRes.status === 'fulfilled') {
      setEstadUsers(eRes.value.data?.data ?? eRes.value.data);
    }

    if (sRes.status === 'fulfilled') {
      setSolicitudes(Array.isArray(sRes.value.data) ? sRes.value.data : []);
    }

    setLoading(false);
  };

  const cambiarRol = async (id) => {
    setGuardando(true);
    try {
      await adminService.cambiarRol(id, nuevoRol);
      addToast('Rol actualizado', 'success');
      setEditando(null);
      await cargarTodo();
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al cambiar rol', 'error');
    } finally { setGuardando(false); }
  };

  const toggleActivo = async (id, nombre) => {
    try {
      const res = await adminService.toggleActivo(id);
      const u = res.data?.data ?? res.data;
      addToast(`${nombre}: ${u.activo ? 'activado' : 'desactivado'}`, 'success');
      await cargarTodo();
    } catch (err) {
      addToast(err.response?.data?.message || 'Error', 'error');
    }
  };

  const revisarEncuentro = async (id, accion) => {
    setRevisando(id);
    try {
      await mascotasService.revisarEncuentro(id, accion);
      addToast(accion === 'APROBAR' ? '✅ Aprobado — mascota reunificada' : 'Rechazado', 'success');
      await cargarTodo();
    } catch (err) {
      addToast(err.response?.data?.message || 'Error', 'error');
    } finally { setRevisando(null); }
  };

  if (loading) return <div className="loader"><div className="spinner"></div></div>;

  const pendientes = solicitudes.filter(s => (s.estadoRevision ?? s.estado_revision) === 'EN_REVISION').length;

  return (
    <div className="page-enter" style={{padding:'40px 0'}}>
      <div className="container">
        <h1 style={{marginBottom:6}}>Panel de administración</h1>
        <p style={{color:'var(--text-muted)', marginBottom:28}}>
          Gestión completa del sistema Sanos y Salvos.
        </p>

        {/* Tabs */}
        <div style={{display:'flex', borderBottom:'2px solid var(--border)', marginBottom:28}}>
          {[
            ['usuarios', `Usuarios (${usuarios.length})`],
            ['solicitudes', `Solicitudes ${pendientes > 0 ? `(${pendientes} pendientes)` : ''}`],
          ].map(([k, l]) => (
            <button key={k} onClick={() => setTab(k)} style={{
              flex: 1, background: 'none', border: 'none', padding: '10px 0',
              fontWeight: tab===k ? 700 : 400,
              color: tab===k ? 'var(--primary)' : 'var(--text-muted)',
              borderBottom: tab===k ? '2px solid var(--primary)' : '2px solid transparent',
              marginBottom: -2, cursor: 'pointer', fontSize: '0.95rem'
            }}>{l}</button>
          ))}
        </div>

        {/* ── Tab: Usuarios ── */}
        {tab === 'usuarios' && (
          <>
            {/* Estadísticas por rol */}
            {estadUsers && (
              <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(130px, 1fr))', gap:12, marginBottom:28}}>
                <div className="card" style={{padding:16, textAlign:'center'}}>
                  <div style={{fontSize:'1.8rem', fontWeight:700}}>{estadUsers.total}</div>
                  <div style={{fontSize:'0.8rem', color:'var(--text-muted)'}}>Total usuarios</div>
                </div>
                {(estadUsers.por_rol || []).map(r => (
                  <div key={r.rol} className="card" style={{padding:16, textAlign:'center'}}>
                    <div style={{fontSize:'1.8rem', fontWeight:700, color: rolColor[r.rol]}}>{r.total}</div>
                    <div style={{fontSize:'0.8rem', color:'var(--text-muted)'}}>{r.rol}</div>
                  </div>
                ))}
              </div>
            )}

            {/* Tabla de usuarios */}
            <div className="card" style={{overflow:'auto'}}>
              <table style={{width:'100%', borderCollapse:'collapse', fontSize:'0.9rem'}}>
                <thead>
                  <tr style={{borderBottom:'2px solid var(--border)'}}>
                    {['Nombre','Email','Rol','Estado','Miembro desde','Acciones'].map(h => (
                      <th key={h} style={{padding:'12px 16px', textAlign:'left', color:'var(--text-muted)', fontWeight:600}}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map(u => (
                    <tr key={u.id_usuario} style={{borderBottom:'1px solid var(--border)'}}>
                      <td style={{padding:'12px 16px', fontWeight:500}}>{u.nombre}</td>
                      <td style={{padding:'12px 16px', color:'var(--text-muted)', fontSize:'0.85rem'}}>{u.email}</td>
                      <td style={{padding:'12px 16px'}}>
                        {editando === u.id_usuario ? (
                          <select value={nuevoRol} onChange={e => setNuevoRol(e.target.value)}
                            style={{padding:'4px 8px', borderRadius:6, border:'1px solid var(--border)', fontSize:'0.85rem'}}>
                            {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                          </select>
                        ) : (
                          <span style={{
                            background: rolColor[u.rol], color:'#fff',
                            padding:'2px 10px', borderRadius:12, fontSize:'0.78rem', fontWeight:600
                          }}>{u.rol}</span>
                        )}
                      </td>
                      <td style={{padding:'12px 16px'}}>
                        <span style={{color: u.activo ? '#2a9d8f' : '#e63946', fontSize:'0.85rem', fontWeight:600}}>
                          {u.activo ? '● Activo' : '● Inactivo'}
                        </span>
                      </td>
                      <td style={{padding:'12px 16px', color:'var(--text-muted)', fontSize:'0.82rem'}}>
                        {new Date(u.created_at).toLocaleDateString('es-CL')}
                      </td>
                      <td style={{padding:'12px 16px'}}>
                        {u.id_usuario === user?.id_usuario ? (
                          <span style={{fontSize:'0.8rem', color:'var(--text-muted)'}}>Tu cuenta</span>
                        ) : editando === u.id_usuario ? (
                          <div style={{display:'flex', gap:6}}>
                            <button className="btn btn-primary btn-sm"
                              disabled={guardando} onClick={() => cambiarRol(u.id_usuario)}>
                              {guardando ? '...' : 'Guardar'}
                            </button>
                            <button className="btn btn-outline btn-sm"
                              onClick={() => setEditando(null)}>Cancelar</button>
                          </div>
                        ) : (
                          <div style={{display:'flex', gap:6}}>
                            <button className="btn btn-outline btn-sm"
                              onClick={() => { setEditando(u.id_usuario); setNuevoRol(u.rol); }}>
                              Editar rol
                            </button>
                            <button className="btn btn-outline btn-sm"
                              style={{borderColor: u.activo ? '#e63946' : '#2a9d8f', color: u.activo ? '#e63946' : '#2a9d8f'}}
                              onClick={() => toggleActivo(u.id_usuario, u.nombre)}>
                              {u.activo ? 'Desactivar' : 'Activar'}
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}

        {/* ── Tab: Solicitudes ── */}
        {tab === 'solicitudes' && (
          <>
            {solicitudes.length === 0 ? (
              <div className="card" style={{padding:48, textAlign:'center', color:'var(--text-muted)'}}>
                <p style={{fontSize:'2.5rem', marginBottom:8}}>🎉</p>
                <p>No hay solicitudes.</p>
              </div>
            ) : (
              <div style={{display:'flex', flexDirection:'column', gap:12}}>
                {solicitudes.map(s => {
                  const idReporte   = s.idReporteEncuentro ?? s.id_reporte_encuentro;
                  const idMasc      = s.idMascota          ?? s.id_mascota;
                  const fotoEv      = s.fotoEvidenciaUrl   ?? s.foto_evidencia_url;
                  const nomMasc     = s.mascotaNombre      ?? s.mascota_nombre;
                  const tipoAn      = s.tipoAnimal         ?? s.tipo_animal;
                  const encontEn    = s.encontradaEn       ?? s.encontrada_en;
                  const contNom     = s.contactoNombre     ?? s.contacto_nombre;
                  const contTel     = s.contactoTelefono   ?? s.contacto_telefono;
                  const estadoRev   = s.estadoRevision     ?? s.estado_revision;
                  const fechaRep    = s.fechaReporte       ?? s.fecha_reporte;
                  return (
                  <div key={idReporte} className="card" style={{padding:18}}>
                    <div style={{display:'flex', flexWrap:'wrap', gap:14, alignItems:'center'}}>
                      {fotoEv && (
                        <img src={fotoEv} alt="ev"
                          style={{width:80, height:80, objectFit:'cover', borderRadius:8, flexShrink:0}}
                          onError={e => e.target.style.display='none'} />
                      )}
                      <div style={{flex:1, minWidth:180}}>
                        <strong>{nomMasc || 'Mascota'} — {tipoAn}</strong>
                        <div style={{fontSize:'0.85rem', color:'var(--text-muted)', marginTop:4}}>
                          📍 {encontEn} · 👤 {contNom} · 📱 {contTel}
                        </div>
                        <div style={{fontSize:'0.8rem', color:'var(--text-muted)', marginTop:2}}>
                          📅 {fechaRep ? new Date(fechaRep).toLocaleDateString('es-CL') : ''}
                        </div>
                        <button className="btn btn-ghost btn-sm" style={{marginTop:8}}
                          onClick={() => navigate(`/mascotas/${idMasc}`)}>
                          Ver mascota →
                        </button>
                      </div>
                      <div style={{display:'flex', flexDirection:'column', alignItems:'flex-end', gap:6}}>
                        <span style={{
                          fontSize:'0.8rem', padding:'3px 10px', borderRadius:10,
                          background: estadoRev==='EN_REVISION' ? '#fff3cd' : estadoRev==='APROBADO' ? '#d4edda' : '#f8d7da',
                          color: estadoRev==='EN_REVISION' ? '#856404' : estadoRev==='APROBADO' ? '#155724' : '#721c24'
                        }}>
                          {estadoRev === 'EN_REVISION' ? '🟡 En revisión' : estadoRev === 'APROBADO' ? '✅ Aprobado' : '❌ Rechazado'}
                        </span>
                        {estadoRev === 'EN_REVISION' && (
                          <div style={{display:'flex', gap:6}}>
                            <button className="btn btn-primary btn-sm"
                              disabled={revisando===idReporte}
                              onClick={() => revisarEncuentro(idReporte,'APROBAR')}>
                              {revisando===idReporte ? '...' : '✅ Aprobar'}
                            </button>
                            <button className="btn btn-outline btn-sm"
                              style={{borderColor:'#e63946', color:'#e63946'}}
                              disabled={revisando===idReporte}
                              onClick={() => revisarEncuentro(idReporte,'RECHAZAR')}>
                              ❌ Rechazar
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                  );
                })}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
