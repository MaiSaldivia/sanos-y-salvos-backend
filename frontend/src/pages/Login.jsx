import { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { authService } from '../services/api';
import { useToast } from '../components/ToastProvider';
import './Login.css';

const ROLES_REGISTRO = [
  { value: 'DUENO',        label: '🐾 Dueño de mascota' },
  { value: 'VETERINARIA',  label: '🏥 Clínica Veterinaria' },
  { value: 'REFUGIO',      label: '🏠 Refugio de animales' },
  { value: 'MUNICIPALIDAD',label: '🏛️ Municipalidad' },
];

export default function Login() {
  const navigate        = useNavigate();
  const location        = useLocation();
  const { addToast }    = useToast();
  const [tab, setTab]   = useState(location.state?.tab || 'login'); // 'login' | 'registro'
  const [loading, setLoading] = useState(false);

  const redirectMessage = location.state?.message;

  const [loginForm, setLoginForm] = useState({ email: '', password: '' });
  const [regForm,   setRegForm]   = useState({ nombre: '', email: '', password: '', confirmar: '', rol: 'DUENO' });

  const handleLoginChange = e => setLoginForm(p => ({ ...p, [e.target.name]: e.target.value }));
  const handleRegChange   = e => setRegForm(p  => ({ ...p, [e.target.name]: e.target.value }));

  // ── Login ──────────────────────────────────────────────────
  const handleLogin = async (e) => {
    e.preventDefault();
    if (!loginForm.email || !loginForm.password) {
      addToast('Completa email y contraseña', 'error'); return;
    }
    setLoading(true);
    try {
      const user = await authService.login(loginForm.email, loginForm.password);
      addToast(`¡Bienvenido/a, ${user.nombre}! (${user.rol})`, 'success');
      navigate('/mascotas');
    } catch (err) {
      addToast(err.response?.data?.message || err.response?.data?.data || 'No se pudo iniciar sesión', 'error');
    } finally {
      setLoading(false);
    }
  };

  // ── Registro ───────────────────────────────────────────────
  const handleRegistro = async (e) => {
    e.preventDefault();
    const { nombre, email, password, confirmar, rol } = regForm;

    if (!nombre || !email || !password || !confirmar) {
      addToast('Completa todos los campos', 'error'); return;
    }
    if (password !== confirmar) {
      addToast('Las contraseñas no coinciden', 'error'); return;
    }
    if (password.length < 6) {
      addToast('La contraseña debe tener al menos 6 caracteres', 'error'); return;
    }

    setLoading(true);
    try {
      const user = await authService.register(nombre, email, password, rol);
      addToast(`¡Cuenta creada! Bienvenido/a, ${user.nombre}`, 'success');
      navigate('/mascotas');
    } catch (err) {
      addToast(err.response?.data?.message || err.response?.data?.data || 'Error al crear la cuenta', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page page-enter">
      <div className="container login-container">
        <div className="card login-card">

          {/* Tabs */}
          <div className="login-tabs">
            <button
              className={`login-tab ${tab === 'login' ? 'active' : ''}`}
              onClick={() => setTab('login')}
            >
              Iniciar sesión
            </button>
            <button
              className={`login-tab ${tab === 'registro' ? 'active' : ''}`}
              onClick={() => setTab('registro')}
            >
              Registrarse
            </button>
          </div>

          {/* ── Panel Login ── */}
          {tab === 'login' && (
            <>
              {redirectMessage && (
                <div style={{background:'#fff3cd', border:'1px solid #e9a010', borderRadius:8, padding:'10px 14px', marginBottom:16, fontSize:'0.9rem', color:'#856404'}}>
                  ℹ️ {redirectMessage}
                </div>
              )}
              <p className="login-sub">Accede con tu cuenta para gestionar reportes.</p>
              <form className="login-form" onSubmit={handleLogin}>
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input name="email" type="email" value={loginForm.email}
                    onChange={handleLoginChange} className="form-input"
                    placeholder="correo@ejemplo.com" autoComplete="email" />
                </div>
                <div className="form-group">
                  <label className="form-label">Contraseña</label>
                  <input name="password" type="password" value={loginForm.password}
                    onChange={handleLoginChange} className="form-input"
                    placeholder="••••••••" autoComplete="current-password" />
                </div>
                <button className="btn btn-primary btn-full" type="submit" disabled={loading}>
                  {loading ? 'Ingresando...' : 'Ingresar'}
                </button>
              </form>

              <div className="login-help">
                <p className="login-help-title">Usuarios demo:</p>
                <p><strong>Admin:</strong> admin@sanosysalvos.cl / admin123</p>
                <p><strong>Refugio:</strong> refugio@esperanza.cl / refugio123</p>
                <p><strong>Municipalidad:</strong> muni@conce.cl / muni123</p>
              </div>
            </>
          )}

          {/* ── Panel Registro ── */}
          {tab === 'registro' && (
            <>
              <p className="login-sub">Crea tu cuenta y únete a la comunidad.</p>
              <form className="login-form" onSubmit={handleRegistro}>
                <div className="form-group">
                  <label className="form-label">Nombre completo</label>
                  <input name="nombre" type="text" value={regForm.nombre}
                    onChange={handleRegChange} className="form-input"
                    placeholder="Tu nombre" autoComplete="name" />
                </div>
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input name="email" type="email" value={regForm.email}
                    onChange={handleRegChange} className="form-input"
                    placeholder="correo@ejemplo.com" autoComplete="email" />
                </div>
                <div className="form-group">
                  <label className="form-label">Contraseña</label>
                  <input name="password" type="password" value={regForm.password}
                    onChange={handleRegChange} className="form-input"
                    placeholder="Mínimo 6 caracteres" autoComplete="new-password" />
                </div>
                <div className="form-group">
                  <label className="form-label">Confirmar contraseña</label>
                  <input name="confirmar" type="password" value={regForm.confirmar}
                    onChange={handleRegChange} className="form-input"
                    placeholder="Repite tu contraseña" autoComplete="new-password" />
                </div>
                <button className="btn btn-primary btn-full" type="submit" disabled={loading}>
                  {loading ? 'Creando cuenta...' : 'Crear cuenta'}
                </button>
              </form>

              <p className="login-switch">
                ¿Ya tienes cuenta?{' '}
                <button className="btn-link" onClick={() => setTab('login')}>
                  Inicia sesión aquí
                </button>
              </p>
            </>
          )}

        </div>
      </div>
    </div>
  );
}
