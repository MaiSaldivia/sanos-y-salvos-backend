import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { authService } from '../services/api';
import { useToast } from './ToastProvider';
import './Navbar.css';

export default function Navbar() {
  const location     = useLocation();
  const navigate     = useNavigate();
  const { addToast } = useToast();
  const [menuOpen, setMenuOpen] = useState(false);
  const user = authService.getStoredUser();

  // Solo 4 links — sin "Reportar" duplicado
  const links = [
    { to: '/',         label: 'Inicio'   },
    { to: '/mascotas', label: 'Reportes' },
    { to: '/mapa',     label: 'Mapa'     },
  ];

  const handleLogout = () => {
    authService.logout();
    setMenuOpen(false);
    addToast('Sesión cerrada', 'success');
    navigate('/');
    window.location.reload();
  };

  const rolBadge = {
    ADMIN:         { bg:'#e63946', label:'Admin' },
    MUNICIPALIDAD: { bg:'#457b9d', label:'Municipalidad' },
    REFUGIO:       { bg:'#2a9d8f', label:'Refugio' },
    VETERINARIA:   { bg:'#e9a010', label:'Veterinaria' },
    DUENO:         { bg:'#6c757d', label:'Dueño' }
  };
  const badge = user ? (rolBadge[user.rol] || rolBadge.DUENO) : null;

  return (
    <nav className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="navbar-brand" onClick={() => setMenuOpen(false)}>
          <span className="brand-icon">🐾</span>
          <span className="brand-text">
            <span className="brand-main">Sanos</span>
            <span className="brand-sep"> y </span>
            <span className="brand-main">Salvos</span>
          </span>
        </Link>

        <div className={`navbar-links ${menuOpen ? 'open' : ''}`}>
          {links.map(link => (
            <Link key={link.to} to={link.to}
              className={`nav-link ${location.pathname === link.to ? 'active' : ''}`}
              onClick={() => setMenuOpen(false)}>
              {link.label}
            </Link>
          ))}

          {user ? (
            <>
              {/* Link a perfil/panel según rol */}
              <Link
                to={user.rol === 'ADMIN' ? '/admin' : ['REFUGIO','MUNICIPALIDAD'].includes(user.rol) ? '/panel' : '/perfil'}
                className="nav-link"
                onClick={() => setMenuOpen(false)}
              >
                {user.rol === 'ADMIN' ? 'Panel Admin' : ['REFUGIO','MUNICIPALIDAD'].includes(user.rol) ? 'Panel' : 'Mi perfil'}
              </Link>
              <span className="nav-role-badge" style={{ background: badge.bg }}>
                {badge.label}
              </span>
              <span className="nav-username">{user.nombre?.split(' ')[0]}</span>
              <button className="btn btn-outline btn-sm" onClick={handleLogout}>
                Salir
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline btn-sm" onClick={() => setMenuOpen(false)}>
                Iniciar sesión
              </Link>
              <Link to="/login" state={{ tab: 'registro' }}
                className="btn btn-ghost-nav btn-sm" onClick={() => setMenuOpen(false)}>
                Registrarse
              </Link>
            </>
          )}

          <Link to="/reportar" className="btn btn-primary btn-sm" onClick={() => setMenuOpen(false)}>
            + Reportar
          </Link>
        </div>

        <button className="hamburger" onClick={() => setMenuOpen(!menuOpen)} aria-label="Menú">
          <span className={menuOpen ? 'open' : ''}></span>
        </button>
      </div>
    </nav>
  );
}
