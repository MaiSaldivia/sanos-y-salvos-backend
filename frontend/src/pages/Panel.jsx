import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService, mascotasService } from '../services/api';
import { useToast } from '../components/ToastProvider';

export default function Panel() {
  const navigate     = useNavigate();
  const { addToast } = useToast();
  const user         = authService.getStoredUser();

  const [solicitudes, setSolicitudes] = useState([]);
  const [loading,     setLoading]     = useState(true);
  const [revisando,   setRevisando]   = useState(null);

  useEffect(() => {
    if (!user || !['ADMIN','REFUGIO','MUNICIPALIDAD'].includes(user.rol)) {
      navigate('/'); return;
    }
    cargarSolicitudes();
  }, []);

  const cargarSolicitudes = async () => {
    setLoading(true);
    try {
      const res = await mascotasService.getEncuentrosRevision();
      setSolicitudes(Array.isArray(res.data) ? res.data : []);
    } catch {
      addToast('Error al cargar solicitudes', 'error');
    } finally { setLoading(false); }
  };

  const revisar = async (id, accion) => {
    setRevisando(id);
    try {
      await mascotasService.revisarEncuentro(id, accion);
      addToast(accion === 'APROBAR' ? '✅ Encuentro aprobado — mascota reunificada' : 'Solicitud rechazada', 'success');
      await cargarSolicitudes();
    } catch (err) {
      addToast(err.response?.data?.message || 'Error al procesar solicitud', 'error');
    } finally { setRevisando(null); }
  };

  const estadoCfg = { EN_REVISION:'🟡 En revisión', APROBADO:'✅ Aprobado', RECHAZADO:'❌ Rechazado' };

  if (loading) return <div className="loader"><div className="spinner"></div></div>;

  return (
    <div className="page-enter" style={{padding:'40px 0'}}>
      <div className="container">
        <h1 style={{marginBottom:6}}>Panel de moderación</h1>
        <p style={{color:'var(--text-muted)', marginBottom:32}}>
          Gestiona las solicitudes de encuentro enviadas por la comunidad.
        </p>

        {/* Estadísticas rápidas */}
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(160px, 1fr))', gap:16, marginBottom:32}}>
          {[
            { label:'Total solicitudes',   val: solicitudes.length,                                         color:'#457b9d' },
            { label:'En revisión',         val: solicitudes.filter(s=>s.estado_revision==='EN_REVISION').length, color:'#e9a010' },
            { label:'Aprobadas',           val: solicitudes.filter(s=>s.estado_revision==='APROBADO').length,    color:'#2a9d8f' },
            { label:'Rechazadas',          val: solicitudes.filter(s=>s.estado_revision==='RECHAZADO').length,   color:'#e63946' },
          ].map(s => (
            <div key={s.label} className="card" style={{padding:20, textAlign:'center'}}>
              <div style={{fontSize:'2rem', fontWeight:700, color:s.color}}>{s.val}</div>
              <div style={{fontSize:'0.82rem', color:'var(--text-muted)', marginTop:4}}>{s.label}</div>
            </div>
          ))}
        </div>

        {solicitudes.length === 0 ? (
          <div className="card" style={{padding:48, textAlign:'center', color:'var(--text-muted)'}}>
            <p style={{fontSize:'2.5rem', marginBottom:8}}>🎉</p>
            <p>No hay solicitudes pendientes.</p>
          </div>
        ) : (
          <div style={{display:'flex', flexDirection:'column', gap:16}}>
            {solicitudes.map(s => {
              const idReporte = s.idReporteEncuentro ?? s.id_reporte_encuentro;
              const idMasc    = s.idMascota          ?? s.id_mascota;
              const fotoEv    = s.fotoEvidenciaUrl   ?? s.foto_evidencia_url;
              const nomMasc   = s.mascotaNombre      ?? s.mascota_nombre;
              const tipoAn    = s.tipoAnimal         ?? s.tipo_animal;
              const encontradaEn = s.encontradaEn   ?? s.encontrada_en;
              const contactoNom  = s.contactoNombre  ?? s.contacto_nombre;
              const contactoTel  = s.contactoTelefono?? s.contacto_telefono;
              const estadoRev    = s.estadoRevision  ?? s.estado_revision;
              const fechaRep     = s.fechaReporte    ?? s.fecha_reporte;
              return (
              <div key={idReporte} className="card" style={{padding:20}}>
                <div style={{display:'flex', flexWrap:'wrap', gap:16, alignItems:'flex-start'}}>
                  {fotoEv && (
                    <img src={fotoEv} alt="evidencia"
                      style={{width:100, height:100, objectFit:'cover', borderRadius:8, flexShrink:0}}
                      onError={e => e.target.style.display='none'} />
                  )}
                  <div style={{flex:1, minWidth:200}}>
                    <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:6}}>
                      <strong style={{fontSize:'1rem'}}>
                        {nomMasc || 'Mascota'} — {tipoAn}
                      </strong>
                      <span style={{fontSize:'0.8rem', background:'var(--surface-alt,#f1f3f5)', padding:'2px 8px', borderRadius:10}}>
                        {estadoCfg[estadoRev] || estadoRev}
                      </span>
                    </div>
                    <div style={{fontSize:'0.88rem', color:'var(--text-muted)', display:'flex', flexDirection:'column', gap:4}}>
                      <span>📍 Encontrada en: <strong>{encontradaEn}</strong></span>
                      <span>👤 Reportó: <strong>{contactoNom}</strong> · {contactoTel}</span>
                      <span>📅 {new Date(fechaRep).toLocaleDateString('es-CL')}</span>
                    </div>
                    <button className="btn btn-ghost btn-sm" style={{marginTop:10}}
                      onClick={() => navigate(`/mascotas/${idMasc}`)}>
                      Ver mascota →
                    </button>
                  </div>
                  {estadoRev === 'EN_REVISION' && (
                    <div style={{display:'flex', gap:8, flexShrink:0, alignSelf:'center'}}>
                      <button className="btn btn-primary btn-sm"
                        disabled={revisando === idReporte}
                        onClick={() => revisar(idReporte, 'APROBAR')}>
                        {revisando === idReporte ? '...' : '✅ Aprobar'}
                      </button>
                      <button className="btn btn-outline btn-sm"
                        style={{borderColor:'#e63946', color:'#e63946'}}
                        disabled={revisando === idReporte}
                        onClick={() => revisar(idReporte, 'RECHAZAR')}>
                        ❌ Rechazar
                      </button>
                    </div>
                  )}
                </div>
              </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
