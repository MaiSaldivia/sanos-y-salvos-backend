import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import FormMascota from '../components/FormMascota';
import { authService } from '../services/api';
import './Reportar.css';

export default function Reportar() {
  const navigate = useNavigate();

  useEffect(() => {
    if (!authService.isLoggedIn()) {
      navigate('/login', {
        state: { from: '/reportar', message: 'Debes iniciar sesión para reportar una mascota.' }
      });
    }
  }, []);

  if (!authService.isLoggedIn()) return null;

  return (
    <div className="reportar-page page-enter">
      <div className="container reportar-container">

        {/* Hero header */}
        <div className="reportar-hero">
          <div className="reportar-hero-badge">Nuevo reporte</div>
          <h1 className="reportar-hero-title">Reportar mascota</h1>
          <p className="reportar-hero-sub">
            Completa el formulario con el máximo de detalles posible.
            Nuestro motor de coincidencias usará esta información para encontrar posibles matches.
          </p>
        </div>

        {/* Steps indicator */}
        <div className="reportar-steps">
          <div className="step active">
            <div className="step-num">1</div>
            <span>Información básica</span>
          </div>
          <div className="step-line"></div>
          <div className="step">
            <div className="step-num">2</div>
            <span>Ubicación</span>
          </div>
          <div className="step-line"></div>
          <div className="step">
            <div className="step-num">3</div>
            <span>Contacto</span>
          </div>
        </div>

        <div className="reportar-layout">
          {/* Formulario */}
          <div className="form-card card">
            <FormMascota onSuccess={() => setTimeout(() => navigate('/mascotas'), 1500)} />
          </div>

          {/* Aside */}
          <aside className="reportar-aside">
            <div className="aside-tip card">
              <div className="aside-tip-icon" style={{fontSize:'1.3rem', lineHeight:1}}>i</div>
              <h3>Más detalles = más coincidencias</h3>
              <p>El motor compara tipo, raza, color, tamaño y ubicación. Mientras más datos ingreses, mayor es la probabilidad de encontrar una coincidencia.</p>
              <div className="aside-tags">
                {['Tipo animal', 'Raza', 'Color', 'Tamaño', 'Ubicación'].map(t => (
                  <span key={t} className="aside-tag">{t}</span>
                ))}
              </div>
            </div>

            <div className="aside-tip card aside-tip-warm">
              <div className="aside-tip-icon" style={{fontSize:'1.3rem', lineHeight:1}}>+</div>
              <h3>Adjunta una foto</h3>
              <p>Los reportes con foto tienen mucho más alcance. Puedes subir una imagen desde tu dispositivo o pegar una URL.</p>
            </div>

            <div className="aside-tip card aside-tip-danger">
              <div className="aside-tip-icon" style={{fontSize:'1.3rem', lineHeight:1}}>!</div>
              <h3>Animal en peligro</h3>
              <p>Si el animal necesita atención veterinaria urgente, contacta al refugio más cercano.</p>
              <button className="btn btn-outline btn-sm aside-cta" onClick={() => navigate('/mascotas')}>
                Ver mascotas encontradas
              </button>
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}
