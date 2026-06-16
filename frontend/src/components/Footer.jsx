import { Link } from 'react-router-dom';
import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="footer-brand">
          <span>🐾</span>
          <span className="footer-name">Sanos y Salvos</span>
          <p>Plataforma de microservicios para reunificar mascotas perdidas con sus familias.</p>
        </div>

        <div className="footer-links">
          <h4>Navegación</h4>
          <Link to="/">Inicio</Link>
          <Link to="/mascotas">Ver reportes</Link>
          <Link to="/reportar">Reportar mascota</Link>
          <Link to="/mapa">Mapa de casos</Link>
        </div>

        <div className="footer-links">
          <h4>Ayuda</h4>
          <a href="#">¿Cómo funciona?</a>
          <a href="#">Consejos de búsqueda</a>
          <a href="#">Contactar soporte</a>
        </div>
      </div>

      <div className="footer-bottom">
        <div className="container">
          <p>© 2026 Sanos y Salvos — Proyecto Fullstack III, DuocUC · Hecho con ❤️ para los animales</p>
        </div>
      </div>
    </footer>
  );
}
