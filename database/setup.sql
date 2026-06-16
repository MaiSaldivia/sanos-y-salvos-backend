-- ============================================
-- Sanos y Salvos - Setup Base de Datos MySQL
-- Proyecto Fullstack III - DuocUC 2026
-- Ejecutar en XAMPP antes de iniciar los servicios
-- ============================================

CREATE DATABASE IF NOT EXISTS sanos_y_salvos_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sanos_y_salvos_db;

-- Microservicio: ms-usuarios-entidades
CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario    VARCHAR(36)  PRIMARY KEY,
  nombre        VARCHAR(120) NOT NULL,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  rol           ENUM('DUENO','VETERINARIA','REFUGIO','MUNICIPALIDAD','ADMIN') DEFAULT 'DUENO',
  activo        BOOLEAN      DEFAULT TRUE,
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- Microservicio: ms-gestion-mascotas
CREATE TABLE IF NOT EXISTS mascotas (
  id_mascota     VARCHAR(36)  PRIMARY KEY,
  tipo_animal    VARCHAR(50)  NOT NULL,
  raza           VARCHAR(80),
  nombre         VARCHAR(80),
  color_primario VARCHAR(60),
  tamano         ENUM('Pequeño','Mediano','Grande'),
  sexo           VARCHAR(20),
  edad           VARCHAR(30),
  foto_url       TEXT,
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
);

CREATE TABLE IF NOT EXISTS reportes_encuentro (
  id_reporte_encuentro VARCHAR(36) PRIMARY KEY,
  id_mascota           VARCHAR(36) NOT NULL,
  foto_evidencia_url   TEXT,
  encontrada_en        VARCHAR(200),
  contacto_nombre      VARCHAR(120),
  contacto_telefono    VARCHAR(30),
  estado_revision      ENUM('EN_REVISION','APROBADO','RECHAZADO') DEFAULT 'EN_REVISION',
  fecha_reporte        DATETIME DEFAULT CURRENT_TIMESTAMP,
  fecha_revision       DATETIME,
  FOREIGN KEY (id_mascota) REFERENCES mascotas(id_mascota) ON DELETE CASCADE
);

-- Microservicio: ms-motor-coincidencias
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
);

-- Microservicio: ms-geolocalizacion
CREATE TABLE IF NOT EXISTS zonas_criticas (
  id_zona        VARCHAR(36)  PRIMARY KEY,
  nombre_zona    VARCHAR(120) NOT NULL,
  lat_centro     DOUBLE       NOT NULL,
  lng_centro     DOUBLE       NOT NULL,
  radio_km       DECIMAL(5,2) DEFAULT 1.0,
  total_reportes INT          DEFAULT 0,
  updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Datos de ejemplo
INSERT IGNORE INTO usuarios (id_usuario, nombre, email, password_hash, rol) VALUES
  ('usr-admin-001',  'Admin Sanos y Salvos',    'admin@sanosysalvos.cl',  'admin123',   'ADMIN'),
  ('usr-muni-001',   'Municipalidad Concepcion','muni@conce.cl',          'muni123',    'MUNICIPALIDAD'),
  ('usr-refugio-001','Refugio Esperanza',        'refugio@esperanza.cl',   'refugio123', 'REFUGIO'),
  ('usr-dueno-001',  'Dueno Demo',               'dueno@demo.cl',          'dueno123',   'DUENO');

INSERT IGNORE INTO mascotas
  (id_mascota, tipo_animal, raza, nombre, color_primario, tamano, sexo, edad,
   foto_url, latitud, longitud, sector, comuna, direccion, estado, descripcion, contacto, telefono)
VALUES
  ('msc-001','Perro','Labrador','Max','Amarillo','Grande','Macho','4 anos',
   'https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&q=80',
   -36.8201,-73.0444,'Concepcion Centro','Concepcion','Barros Arana 450',
   'PERDIDA','Muy amigable, tiene collar azul con plaquita.','contacto@ejemplo.com','+56 9 1234 5678'),

  ('msc-002','Gato','Siames','Luna','Blanco/Cafe','Pequeño','Hembra','2 anos',
   'https://images.unsplash.com/photo-1513245543132-31f507417b26?w=400&q=80',
   -36.8301,-73.0544,'Barrio Universitario','Concepcion','Parque Ecuador',
   'ENCONTRADA','Encontrada en el parque, muy asustada pero sana.','rescate@ejemplo.com','+56 9 8765 4321'),

  ('msc-003','Perro','Mestizo','Coco','Cafe','Mediano','Macho','6 anos',
   'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&q=80',
   -36.8150,-73.0380,'Hualpen','Hualpen','Pasaje Los Canelos 120',
   'PERDIDA','Se escapo del patio, tiene chip pero sin collar.','dueno@ejemplo.com','+56 9 5555 6666');

-- ── Parche: ampliar foto_url para soportar base64 ──
ALTER TABLE mascotas MODIFY foto_url LONGTEXT;
ALTER TABLE reportes_encuentro MODIFY foto_evidencia_url LONGTEXT;
