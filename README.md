# Sanos y Salvos 

**Proyecto Fullstack III · DuocUC 2026**  
**Estudiantes:** Bastian Martinez Flores · Maicol Saldivia Silva  
**Docente:** Daniel Williams Concha Saavedra · Sección: 002D

---

## Arquitectura del sistema

```
Frontend (React/Vite) — Puerto 5173
         |
         v  (todas las peticiones via /api/*)
     BFF — Puerto 3005   ← Circuit Breaker por servicio
         |
   +----------+------------+--------------+
   |          |            |              |
   v          v            v              v
ms-mascotas  ms-geo    ms-motor     ms-usuarios
  (3001)     (3002)     (3003)        (3004)

Todos conectados a MySQL (XAMPP) — Puerto 3306
```

---

## Requisitos previos

- Java 17+ (JDK)
- Apache Maven 3.8+
- XAMPP con MySQL activo en puerto 3306
- Node.js 18+ y npm (solo para el frontend)

---

## Pasos para ejecutar

### 1. Base de datos

1. Abre XAMPP y activa Apache y MySQL
2. Entra a phpMyAdmin: http://localhost/phpmyadmin
3. Importa el archivo `database/setup.sql` completo

### 2. Opción A — Script automático (recomendado)

**Windows:**
```
start.bat
```

**Linux / macOS:**
```bash
chmod +x start.sh
./start.sh
```

El script verifica XAMPP, compila y arranca los 5 microservicios en paralelo.  
Luego ejecuta el frontend automáticamente en http://localhost:5173

### 3. Opción B — Inicio manual (6 terminales)

```bash
# Terminal 1 — ms-gestion-mascotas (Puerto 3001)
cd microservicios/ms-gestion-mascotas
mvn spring-boot:run

# Terminal 2 — ms-geolocalizacion (Puerto 3002)
cd microservicios/ms-geolocalizacion
mvn spring-boot:run

# Terminal 3 — ms-motor-coincidencias (Puerto 3003)
cd microservicios/ms-motor-coincidencias
mvn spring-boot:run

# Terminal 4 — ms-usuarios-entidades (Puerto 3004)
cd microservicios/ms-usuarios-entidades
mvn spring-boot:run

# Terminal 5 — BFF (Puerto 3005)
cd microservicios/bff
mvn spring-boot:run

# Terminal 6 — Frontend React (Puerto 5173)
cd frontend
npm install
npm run dev
```

### 4. Abrir en navegador

http://localhost:5173

---

## Usuarios demo

| Email                     | Password    | Rol           |
|---------------------------|-------------|---------------|
| admin@sanosysalvos.cl     | admin123    | ADMIN         |
| refugio@esperanza.cl      | refugio123  | REFUGIO       |
| muni@conce.cl             | muni123     | MUNICIPALIDAD |
| dueno@demo.cl             | dueno123    | DUENO         |

---

## Patrones de diseño implementados

| Patrón          | Ubicación                              | Propósito                                    |
|-----------------|----------------------------------------|----------------------------------------------|
| Repository      | Todos los microservicios               | Aísla el acceso a datos de la lógica negocio |
| Factory Method  | ms-gestion-mascotas / pattern/factory  | Crea alertas y mascotas dinámicamente        |
| Observer        | ms-gestion-mascotas / pattern/observer | Notifica cambios de estado con Spring Events |
| Circuit Breaker | bff / pattern/CircuitBreaker.java      | Evita fallos en cascada (CLOSED/OPEN/HALF_OPEN) |

---

## Ejecutar pruebas unitarias

> Los proyectos son Maven. El comando es `mvn test`.
> Si `mvn` no está en el PATH, usa la ruta completa: `"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd"`

### Desde CMD

```cmd
"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-gestion-mascotas\pom.xml" clean test

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-motor-coincidencias\pom.xml" clean test

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-usuarios-entidades\pom.xml" clean test

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-geolocalizacion\pom.xml" clean test
```

**Desde PowerShell** (copia y pega cada línea):



mvn clean test -f microservicios/ms-gestion-mascotas/pom.xml
mvn clean test -f microservicios/ms-geolocalizacion/pom.xml
mvn clean test -f microservicios/ms-motor-coincidencias/pom.xml
mvn clean test -f microservicios/ms-usuarios-entidades/pom.xml



(mi compu XD)
```powershell
& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-gestion-mascotas\pom.xml" clean test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-motor-coincidencias\pom.xml" clean test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-usuarios-entidades\pom.xml" clean test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-geolocalizacion\pom.xml" clean test
```

Resultado esperado en cada microservicio:
```
Tests run: X, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Microservicio          | Tests |
|------------------------|-------|
| ms-gestion-mascotas    | 3     |
| ms-motor-coincidencias | 15    |
| ms-usuarios-entidades  | 21    |
| ms-geolocalizacion     | 1     |
| **Total**              | **40**|

---

## Generar reporte de cobertura JaCoCo

```bash
# En cada microservicio con tests:
mvn test jacoco:report

# El reporte HTML se genera en:
# target/site/jacoco/index.html
```

O bien, con el script incluido:

```bash
# Windows
generar-cobertura.bat

# Linux / macOS
./generar-cobertura.sh
```

Los reportes quedan en `docs/cobertura/<microservicio>/index.html`

---

## Estructura del proyecto

```
sanos-y-salvos/
├── database/
│   └── setup.sql                      ← Script completo de BD
├── docs/
│   ├── patrones-y-arquetipos.md
│   ├── plan-branching.md
│   └── cobertura/                     ← Reportes JaCoCo generados
├── microservicios/
│   ├── ms-gestion-mascotas/           ← Puerto 3001 | Factory + Observer
│   ├── ms-geolocalizacion/            ← Puerto 3002 | Repository
│   ├── ms-motor-coincidencias/        ← Puerto 3003 | Strategy (Scoring)
│   ├── ms-usuarios-entidades/         ← Puerto 3004 | Repository + JWT
│   └── bff/                           ← Puerto 3005 | Circuit Breaker
├── frontend/                          ← Puerto 5173 | React + Vite
├── start.bat                          ← Script arranque Windows
├── start.sh                           ← Script arranque Linux/macOS
├── generar-cobertura.bat              ← Genera reportes JaCoCo (Windows)
└── generar-cobertura.sh               ← Genera reportes JaCoCo (Linux/macOS)
```

---

## Endpoints principales

| Servicio             | Base URL                         |
|----------------------|----------------------------------|
| BFF (proxy)          | http://localhost:3005/api/       |
| ms-mascotas          | http://localhost:3001/api/       |
| ms-geolocalizacion   | http://localhost:3002/api/geo/   |
| ms-motor             | http://localhost:3003/api/motor/ |
| ms-usuarios          | http://localhost:3004/api/auth/  |

Todos los microservicios están accesibles también a través del BFF en el puerto 3005.
