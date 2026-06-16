import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import FormMascota from '../components/FormMascota';
import { authService } from '../services/api';
import './Reportar.css';

export default function Reportar() {
  const navigate = useNavigate();

  // Redirigir al login si no está autenticado
  useEffect(() => {
    if (!authService.isLoggedIn()) {
      navigate('/login', { state: { from: '/reportar', message: 'Debes iniciar sesión para reportar una mascota.' } });
    }
  }, []);

  if (!authService.isLoggedIn()) return null;

  return (
    <div className="reportar-page page-enter">
      <div className="container reportar-container">
        <div className="reportar-header">
          <span className="reportar-icon">🐾</span>
          <h1>Reportar mascota</h1>
          <p>Completa el formulario con el máximo de detalles posible. Nuestro motor de coincidencias usará esta información para encontrar posibles matches.</p>
        </div>
        <div className="reportar-grid">
          <div className="form-card card">
            <div className="form-card-body">
              <FormMascota onSuccess={() => setTimeout(() => navigate('/mascotas'), 1500)} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
