import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { mascotasService } from '../services/api';
import CardMascota from '../components/CardMascota';
import './Home.css';

const CAROUSEL_SLIDES = [
  {
    img: 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=900&q=85',
    pos: 'center 40%',
    title: 'Cada mascota merece volver a casa',
    desc: 'Ayudamos a reunificar familias en Concepción'
  },
  {
    img: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=900&q=85',
    pos: 'center 30%',
    title: 'Reporta en segundos',
    desc: 'Foto, descripción y ubicación — así de simple'
  },
  {
    img: 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=900&q=85',
    pos: 'center 35%',
    title: 'Motor de coincidencias inteligente',
    desc: 'El algoritmo cruza raza, color y zona automáticamente'
  },
  {
    img: 'https://images.unsplash.com/photo-1561037404-61cd46aa615b?w=900&q=85',
    pos: 'center 25%',
    title: 'Mapa en tiempo real',
    desc: 'Visualiza zonas críticas y reportes cercanos'
  },
];

export default function Home() {
  const [stats, setStats] = useState({ total: 0, perdidas: 0, encontradas: 0, reunificadas: 0 });
  const [recientes, setRecientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [slide, setSlide] = useState(0);
  const timerRef = useRef(null);

  const nextSlide = () => setSlide(s => (s + 1) % CAROUSEL_SLIDES.length);
  const prevSlide = () => setSlide(s => (s - 1 + CAROUSEL_SLIDES.length) % CAROUSEL_SLIDES.length);

  useEffect(() => {
    timerRef.current = setInterval(nextSlide, 4500);
    return () => clearInterval(timerRef.current);
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [statsRes, mascotasRes] = await Promise.all([
          mascotasService.getEstadisticas(),
          mascotasService.getAll()
        ]);
        // Mascotas devuelve array directo, estadisticas también directo (no ResponseWrapper)
        const statsData = statsRes.data?.data ?? statsRes.data ?? {};
        setStats({ total: 0, perdidas: 0, encontradas: 0, reunificadas: 0, ...statsData });
        const lista = Array.isArray(mascotasRes.data) ? mascotasRes.data : [];
        setRecientes(lista.slice(0, 3));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  return (
    <div className="home page-enter">
      {/* Hero */}
      <section className="hero">
        <div className="hero-bg">
          <div className="hero-blob hero-blob-1"></div>
          <div className="hero-blob hero-blob-2"></div>
        </div>
        <div className="container hero-inner">
          <div className="hero-content">
            <div className="hero-badge">🐾 Plataforma de Microservicios · Concepción, Chile</div>
            <h1 className="hero-title">
              Cada mascota<br />
              <em>merece volver a casa</em>
            </h1>
            <p className="hero-subtitle">
              Centralizamos reportes de mascotas perdidas y encontradas.
              Algoritmos inteligentes detectan coincidencias en tiempo real.
            </p>
            <div className="hero-ctas">
              <Link to="/reportar" className="btn btn-primary btn-lg">
                Reportar mascota
              </Link>
              <Link to="/mascotas" className="btn btn-outline btn-lg">
                Ver todos los reportes
              </Link>
            </div>
          </div>

          {/* Carrusel */}
          <div className="hero-carousel">
            <div
              className="carousel-track"
              style={{ transform: `translateX(-${slide * 100}%)` }}
            >
              {CAROUSEL_SLIDES.map((s, i) => (
                <div key={i} className="carousel-slide">
                  <img src={s.img} alt={s.title} style={{ objectPosition: s.pos }} />
                  <div className="carousel-overlay" />
                  <div className="carousel-caption">
                    <h3>{s.title}</h3>
                    <p>{s.desc}</p>
                  </div>
                </div>
              ))}
            </div>
            <button className="carousel-btn carousel-btn-prev" onClick={() => { clearInterval(timerRef.current); prevSlide(); }} aria-label="Anterior">‹</button>
            <button className="carousel-btn carousel-btn-next" onClick={() => { clearInterval(timerRef.current); nextSlide(); }} aria-label="Siguiente">›</button>
            <div className="carousel-dots">
              {CAROUSEL_SLIDES.map((_, i) => (
                <button
                  key={i}
                  className={`carousel-dot${slide === i ? ' active' : ''}`}
                  onClick={() => { clearInterval(timerRef.current); setSlide(i); }}
                  aria-label={`Slide ${i + 1}`}
                />
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="stats-section">
        <div className="container">
          <div className="stats-grid">
            {[
              { value: stats.total,        label: 'Reportes totales',     icon: null, color: 'neutral' },
              { value: stats.perdidas,     label: 'Mascotas perdidas',    icon: null, color: 'danger'  },
              { value: stats.encontradas,  label: 'Mascotas encontradas', icon: null, color: 'success' },
              { value: stats.reunificadas, label: 'Reunificaciones',      icon: null, color: 'amber'   },
            ].map((s, i) => (
              <div key={i} className={`stat-card stat-${s.color}`}>
                <span className="stat-value">{loading ? '—' : s.value}</span>
                <span className="stat-label">{s.label}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Recientes */}
      <section className="section">
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">Reportes recientes</h2>
              <p className="section-sub">Los últimos casos publicados en la plataforma</p>
            </div>
            <Link to="/mascotas" className="btn btn-ghost">Ver todos →</Link>
          </div>

          {loading ? (
            <div className="loader"><div className="spinner"></div></div>
          ) : (
            <div className="mascotas-grid">
              {recientes.map(m => <CardMascota key={m.id_mascota ?? m.idMascota} mascota={m} />)}
            </div>
          )}
        </div>
      </section>

      {/* How it works */}
      <section className="section how-section">
        <div className="container">
          <h2 className="section-title text-center">¿Cómo funciona?</h2>
          <p className="section-sub text-center">Sistema automatizado de microservicios</p>

          <div className="steps-grid">
            {[
              { n: '01', title: 'Reportas',                    desc: 'Completas el formulario con fotos y características físicas de la mascota.' },
              { n: '02', title: 'Motor de coincidencias',       desc: 'Nuestro algoritmo analiza raza, color, tamaño y ubicación para detectar coincidencias.' },
              { n: '03', title: 'Geolocalización',              desc: 'El mapa identifica zonas críticas y acerca casos similares por proximidad.' },
              { n: '04', title: 'Reunificación',                desc: 'Conectamos a los dueños con quienes encontraron a la mascota.' },
            ].map(s => (
              <div key={s.n} className="step-card">
                <span className="step-number">{s.n}</span>
                <h3 className="step-title">{s.title}</h3>
                <p className="step-desc">{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA banner */}
      <section className="cta-banner">
        <div className="container cta-inner">
          <div>
            <h2>¿Encontraste una mascota?</h2>
            <p>Publícalo ahora y ayuda a que vuelva con su familia.</p>
          </div>
          <Link to="/reportar" className="btn btn-primary btn-lg">
            Publicar hallazgo →
          </Link>
        </div>
      </section>
    </div>
  );
}
