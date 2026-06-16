import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './CardMascota.css';

export default function CardMascota({ mascota, onEstadoChange, onReportEncuentro }) {
  const navigate = useNavigate();
  const [imgError, setImgError] = useState(false);

  const estadoConfig = {
    PERDIDA: { label: 'Perdida', cls: 'tag-perdida', icon: '🔴' },
    ENCONTRADA: { label: 'Encontrada', cls: 'tag-encontrada', icon: '🟢' },
    REUNIFICADA: { label: 'Reunificada', cls: 'tag-reunificada', icon: '🟡' }
  };

  const cfg = estadoConfig[mascota.estado] || estadoConfig.PERDIDA;

  // Soporte para camelCase (backend real) y snake_case (legacy)
  const idMascota    = mascota.idMascota    ?? mascota.id_mascota;
  const fotoUrl      = mascota.fotoUrl      ?? mascota.foto_url;
  const tipoAnimal   = mascota.tipoAnimal   ?? mascota.tipo_animal;
  const colorPrimario= mascota.colorPrimario?? mascota.color_primario;
  const fechaReporte = mascota.fechaReporte ?? mascota.fecha_reporte;

  const fechaFormato = new Date(fechaReporte).toLocaleDateString('es-CL', {
    day: 'numeric', month: 'short', year: 'numeric'
  });

  return (
    <div className="card mascota-card">
      <div className="mascota-img-wrap">
        <img
          src={imgError ? 'https://images.unsplash.com/photo-1450778869180-41d0601e046e?w=400&q=80' : fotoUrl}
          alt={`${mascota.nombre} - ${tipoAnimal}`}
          className="mascota-img"
          onError={() => setImgError(true)}
          loading="lazy"
        />
        <span className={`tag ${cfg.cls} mascota-estado-tag`}>
          {cfg.icon} {cfg.label}
        </span>
        <span className="mascota-tipo-tag">{tipoAnimal}</span>
      </div>

      <div className="mascota-body">
        <div className="mascota-header-row">
          <h3 className="mascota-nombre">{mascota.nombre}</h3>
          <span className="mascota-tamano">{mascota.tamano}</span>
        </div>

        <p className="mascota-raza">{mascota.raza} · {colorPrimario}</p>

        <div className="mascota-info">
          <span className="info-item">📍 {mascota.sector}</span>
          <span className="info-item">📅 {fechaFormato}</span>
        </div>

        {mascota.descripcion && (
          <p className="mascota-desc">{mascota.descripcion}</p>
        )}

        <div className="mascota-actions">
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => navigate(`/mascotas/${idMascota}`)}
          >
            Ver detalle
          </button>
          {onReportEncuentro && mascota.estado === 'PERDIDA' && (
            <button
              className="btn btn-primary btn-sm"
              onClick={() => onReportEncuentro(mascota)}
            >
              Reportar encuentro
            </button>
          )}
          {onEstadoChange && mascota.estado !== 'REUNIFICADA' && (
            <button
              className="btn btn-outline btn-sm"
              onClick={() => onEstadoChange(mascota)}
            >
              Actualizar estado
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
