# Sanos y Salvos

Plataforma de microservicios para reportar y encontrar mascotas perdidas en Concepción, Chile.  
Proyecto Fullstack III — DuocUC 2026.

---

## Requisitos previos

| Herramienta | Versión mínima | Descarga |
|-------------|----------------|----------|
| JDK | 17 o 21 | https://adoptium.net |
| Node.js | 18+ | https://nodejs.org |
| MySQL | 8.0+ | XAMPP: https://apachefriends.org |
| Maven | 3.8+ (opcional) | Incluido como `mvnw.cmd` en cada microservicio |

---

## Inicio rápido

1. Abre XAMPP y pulsa **Start** en MySQL
2. Doble clic en `start.bat`
3. Espera ~3 minutos a que arranquen los servicios
4. Abre el navegador en `http://localhost:5173`

El script detecta Java, Node y MySQL automáticamente, crea la base de datos, levanta los 5 microservicios e inicia el frontend.

---

## Puertos

| Servicio | Puerto |
|----------|--------|
| Frontend React | 5173 |
| BFF / API Gateway | 3005 |
| ms-gestion-mascotas | 3001 |
| ms-geolocalizacion | 3002 |
| ms-motor-coincidencias | 3003 |
| ms-usuarios-entidades | 3004 |
| MySQL | 3306 |

---

## Usuarios de prueba

| Email | Contraseña | Rol |
|-------|------------|-----|
| `admin@sanosysalvos.cl` | `admin123` | Admin |
| `refugio@esperanza.cl` | `refugio123` | Refugio |
| `muni@conce.cl` | `muni123` | Municipalidad |
| `dueno@demo.cl` | `dueno123` | Dueño |

---

## Estructura del proyecto

```
sanos-y-salvos/
├── start.bat                      ← Inicia todo el sistema
├── run-ms.bat                     ← Script interno usado por start.bat
├── database/
│   └── setup.sql                  ← Esquema y datos de prueba
├── frontend/                      ← App React + Vite
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── services/api.js
│   │   └── styles/main.css
│   └── vite.config.js
└── microservicios/
    ├── bff/                       ← API Gateway (puerto 3005)
    ├── ms-gestion-mascotas/       ← Puerto 3001
    ├── ms-geolocalizacion/        ← Puerto 3002
    ├── ms-motor-coincidencias/    ← Puerto 3003
    └── ms-usuarios-entidades/     ← Puerto 3004
```

---

## Inicio manual (si start.bat no funciona)

**1. Crear la base de datos**

```bash
# XAMPP sin contraseña (por defecto)
c:\xampp\mysql\bin\mysql.exe -u root < database\setup.sql

# Con contraseña de root
mysql -u root -p < database\setup.sql
```

**2. Levantar cada microservicio** (una terminal por cada uno)

```bash
cd microservicios\ms-gestion-mascotas
mvnw.cmd spring-boot:run -Dmaven.test.skip=true

cd microservicios\ms-geolocalizacion
mvnw.cmd spring-boot:run -Dmaven.test.skip=true

cd microservicios\ms-motor-coincidencias
mvnw.cmd spring-boot:run -Dmaven.test.skip=true

cd microservicios\ms-usuarios-entidades
mvnw.cmd spring-boot:run -Dmaven.test.skip=true

cd microservicios\bff
mvnw.cmd spring-boot:run -Dmaven.test.skip=true
```

**3. Levantar el frontend**

```bash
cd frontend
npm install
npm run dev
```

---

## Pruebas unitarias

Cada microservicio tiene sus propias pruebas. Se ejecutan con Maven desde la carpeta del microservicio.

**Ejecutar todas las pruebas de un microservicio:**

```bash
cd microservicios\ms-gestion-mascotas
mvnw.cmd test

cd microservicios\ms-geolocalizacion
mvnw.cmd test

cd microservicios\ms-motor-coincidencias
mvnw.cmd test

cd microservicios\ms-usuarios-entidades
mvnw.cmd test
```

**Ejecutar solo una clase de prueba:**

```bash
mvnw.cmd test -Dtest=NombreDeLaClaseTest
```

**Ejecutar solo un método de prueba:**

```bash
mvnw.cmd test -Dtest=NombreDeLaClaseTest#nombreDelMetodo
```

**Ver reporte de cobertura (JaCoCo):**

```bash
mvnw.cmd test jacoco:report
```

El reporte se genera en `target/site/jacoco/index.html`. Ábrelo en el navegador para ver la cobertura por clase y método.

**Ejecutar pruebas de todos los microservicios desde la raíz** (si hay un `pom.xml` padre):

```bash
mvn test --fail-at-end
```

---

## Tecnologías

**Backend:** Java 21 · Spring Boot 3.2.5 · Spring Data JPA · MySQL 8 · JWT · Lombok  
**Frontend:** React 18 · Vite 5 · React Router v6 · Axios · Leaflet.js  
**Arquitectura:** BFF (Backend For Frontend) · Circuit Breaker · 4 microservicios + 1 gateway
