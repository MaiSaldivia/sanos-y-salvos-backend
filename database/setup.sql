-- ============================================================
-- Sanos y Salvos — Setup Base de Datos MySQL
-- Proyecto Fullstack III - DuocUC 2026
-- ============================================================
-- Cómo ejecutar:
--
--   Windows XAMPP (sin contraseña de root):
--     c:\xampp\mysql\bin\mysql.exe -u root < database\setup.sql
--
--   Windows MySQL Installer (sin contraseña):
--     "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root < database\setup.sql
--
--   Con contraseña de root:
--     mysql -u root -p < database\setup.sql
--
--   Linux / Mac:
--     mysql -u root < database/setup.sql
--
-- ✅ Se puede ejecutar más de una vez — usa IF NOT EXISTS e INSERT IGNORE
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET SQL_MODE = '';

CREATE DATABASE IF NOT EXISTS sanos_y_salvos_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sanos_y_salvos_db;

ALTER DATABASE sanos_y_salvos_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- ── Microservicio: ms-usuarios-entidades ─────────────────────
-- NOTA: password_hash guarda el password en TEXTO PLANO.
--       El AuthService usa .equals() para comparar (proyecto académico).
CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario    VARCHAR(36)  PRIMARY KEY,
  nombre        VARCHAR(120) NOT NULL,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  rol           ENUM('DUENO','VETERINARIA','REFUGIO','MUNICIPALIDAD','ADMIN') DEFAULT 'DUENO',
  activo        BOOLEAN      DEFAULT TRUE,
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Microservicio: ms-gestion-mascotas ───────────────────────
CREATE TABLE IF NOT EXISTS mascotas (
  id_mascota     VARCHAR(36)  PRIMARY KEY,
  tipo_animal    VARCHAR(50)  NOT NULL,
  raza           VARCHAR(80),
  nombre         VARCHAR(80),
  color_primario VARCHAR(60),
  tamano         VARCHAR(20),
  sexo           VARCHAR(20),
  edad           VARCHAR(30),
  foto_url       LONGTEXT,
  latitud        DOUBLE,
  longitud       DOUBLE,
  sector         VARCHAR(120),
  comuna         VARCHAR(120),
  direccion      VARCHAR(200),
  fecha_reporte  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  estado         ENUM('PERDIDA','ENCONTRADA','REUNIFICADA') NOT NULL DEFAULT 'PERDIDA',
  descripcion    TEXT,
  contacto       VARCHAR(120),
  telefono       VARCHAR(30),
  id_usuario     VARCHAR(36),
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reportes_encuentro (
  id_reporte_encuentro VARCHAR(36) PRIMARY KEY,
  id_mascota           VARCHAR(36) NOT NULL,
  foto_evidencia_url   LONGTEXT,
  encontrada_en        VARCHAR(200),
  contacto_nombre      VARCHAR(120),
  contacto_telefono    VARCHAR(30),
  estado_revision      ENUM('EN_REVISION','APROBADO','RECHAZADO') DEFAULT 'EN_REVISION',
  fecha_reporte        DATETIME DEFAULT CURRENT_TIMESTAMP,
  fecha_revision       DATETIME,
  FOREIGN KEY (id_mascota) REFERENCES mascotas(id_mascota) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Microservicio: ms-motor-coincidencias ────────────────────
CREATE TABLE IF NOT EXISTS coincidencias (
  id_coincidencia VARCHAR(36) PRIMARY KEY,
  id_mascota_a    VARCHAR(36) NOT NULL,
  id_mascota_b    VARCHAR(36) NOT NULL,
  score           INT         NOT NULL,
  distancia_km    DECIMAL(8,2),
  notificado      BOOLEAN     DEFAULT FALSE,
  created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_mascota_a) REFERENCES mascotas(id_mascota) ON DELETE CASCADE,
  FOREIGN KEY (id_mascota_b) REFERENCES mascotas(id_mascota) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Microservicio: ms-geolocalizacion ────────────────────────
CREATE TABLE IF NOT EXISTS zonas_criticas (
  id_zona        VARCHAR(36)  PRIMARY KEY,
  nombre_zona    VARCHAR(120) NOT NULL,
  lat_centro     DOUBLE       NOT NULL,
  lng_centro     DOUBLE       NOT NULL,
  radio_km       DECIMAL(5,2) DEFAULT 1.0,
  total_reportes INT          DEFAULT 0,
  updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ══════════════════════════════════════════════════════════════
-- DATOS DE PRUEBA
-- ══════════════════════════════════════════════════════════════

-- ── Usuarios demo ─────────────────────────────────────────────
-- Credenciales:
--   admin@sanosysalvos.cl  / admin123     → Admin (acceso total)
--   refugio@esperanza.cl   / refugio123   → Refugio (modera encuentros)
--   muni@conce.cl          / muni123      → Municipalidad (modera encuentros)
--   dueno@demo.cl          / dueno123     → Dueño (reporta mascotas)
INSERT IGNORE INTO usuarios (id_usuario, nombre, email, password_hash, rol) VALUES
  ('usr-admin-001',   'Admin Sanos y Salvos',    'admin@sanosysalvos.cl', 'admin123',   'ADMIN'),
  ('usr-muni-001',    'Municipalidad Concepcion','muni@conce.cl',         'muni123',    'MUNICIPALIDAD'),
  ('usr-refugio-001', 'Refugio Esperanza',       'refugio@esperanza.cl',  'refugio123', 'REFUGIO'),
  ('usr-dueno-001',   'Dueno Demo',              'dueno@demo.cl',         'dueno123',   'DUENO');

-- ── Mascotas demo ─────────────────────────────────────────────
-- id_usuario referencia a usuarios ya insertados arriba
INSERT IGNORE INTO mascotas
  (id_mascota, tipo_animal, raza, nombre, color_primario, tamano, sexo, edad,
   foto_url, latitud, longitud, sector, comuna, direccion,
   estado, descripcion, contacto, telefono, id_usuario)
VALUES
  ('msc-001', 'Perro', 'Labrador', 'Max', 'Amarillo', 'Grande', 'Macho', '4 anos',
   'https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&q=80',
   -36.8201, -73.0444, 'Concepcion Centro', 'Concepcion', 'Barros Arana 450',
   'PERDIDA', 'Muy amigable, tiene collar azul con plaquita.',
   'dueno@demo.cl', '+56 9 1234 5678', 'usr-dueno-001'),

  ('msc-002', 'Gato', 'Siames', 'Luna', 'Blanco y Cafe', 'Pequeno', 'Hembra', '2 anos',
   'https://images.unsplash.com/photo-1513245543132-31f507417b26?w=400&q=80',
   -36.8301, -73.0544, 'Barrio Universitario', 'Concepcion', 'Parque Ecuador',
   'ENCONTRADA', 'Encontrada en el parque, muy asustada pero sana.',
   'refugio@esperanza.cl', '+56 9 8765 4321', 'usr-refugio-001'),

  ('msc-003', 'Perro', 'Mestizo', 'Coco', 'Cafe', 'Mediano', 'Macho', '6 anos',
   'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&q=80',
   -36.8150, -73.0380, 'Hualpen', 'Hualpen', 'Pasaje Los Canelos 120',
   'PERDIDA', 'Se escapo del patio, tiene chip pero sin collar.',
   'dueno@demo.cl', '+56 9 5555 6666', 'usr-dueno-001'),

  ('msc-004', 'Perro', 'Golden Retriever', 'Rocky', 'Dorado', 'Grande', 'Macho', '3 anos',
   'https://images.unsplash.com/photo-1586671267731-da2cf3ceeb80?w=400&q=80',
   -36.8250, -73.0500, 'San Pedro de la Paz', 'San Pedro de la Paz', 'Av. Carlos Cardoen 200',
   'PERDIDA', 'Muy jugueton, responde al nombre Rocky. Collar rojo.',
   'muni@conce.cl', '+56 9 9999 1111', 'usr-muni-001'),

  ('msc-005', 'Gato', 'Mestizo', 'Michi', 'Negro', 'Pequeno', 'Hembra', '1 ano',
   'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&q=80',
   -36.8100, -73.0600, 'Talcahuano Centro', 'Talcahuano', 'Calle Colon 88',
   'ENCONTRADA', 'Gatita negra encontrada en la calle, aparentemente sana.',
   'refugio@esperanza.cl', '+56 9 2222 3333', 'usr-refugio-001'),

  ('msc-006', 'Perro', 'Beagle', 'Manchas', 'Blanco Negro y Cafe', 'Pequeno', 'Macho', '5 anos',
   'https://images.unsplash.com/photo-1561037404-61cd46aa615b?w=400&q=80',
   -36.8350, -73.0350, 'Chiguayante', 'Chiguayante', 'Los Robles 450',
   'REUNIFICADA', 'Fue encontrado y devuelto a su familia.',
   'dueno@demo.cl', '+56 9 7777 8888', 'usr-dueno-001');

-- ── Zonas críticas demo ───────────────────────────────────────
INSERT IGNORE INTO zonas_criticas
  (id_zona, nombre_zona, lat_centro, lng_centro, radio_km, total_reportes)
VALUES
  ('zona-001', 'Concepcion Centro',    -36.8201, -73.0444, 1.5, 3),
  ('zona-002', 'Barrio Universitario', -36.8301, -73.0544, 1.0, 2),
  ('zona-003', 'Hualpen',              -36.8150, -73.0380, 2.0, 2),
  ('zona-004', 'San Pedro de la Paz',  -36.8250, -73.0500, 1.5, 1),
  ('zona-005', 'Talcahuano',           -36.8100, -73.0600, 2.0, 1);
