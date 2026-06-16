# Sanos y Salvos — Frontend

**Proyecto Fullstack III · DuocUC 2026**  
**Estudiantes:** Bastian Martinez Flores · Maicol Saldivia Silva

Interfaz web desarrollada con **React 18 + Vite**, que consume los microservicios del sistema a través del BFF.

---

## Tecnologías utilizadas

| Tecnología       | Versión  | Propósito                              |
|------------------|----------|----------------------------------------|
| React            | 18.2.0   | Framework UI basado en componentes     |
| Vite             | 5.1.0    | Bundler y servidor de desarrollo       |
| React Router DOM | 6.22.0   | Navegación SPA entre páginas           |
| Axios            | 1.6.0    | Cliente HTTP para consumir la API REST |

---

## Requisitos previos

- Node.js 18 o superior
- npm 9 o superior
- BFF corriendo en `http://localhost:3005` (ver repositorio de microservicios)

---

## Instalación

```bash
# Clonar el repositorio
git clone https://github.com/TU_USUARIO/sanos-y-salvos-frontend.git
cd sanos-y-salvos-frontend

# Instalar dependencias
npm install
```

---

## Ejecutar en desarrollo

```bash
npm run dev
```

La aplicación estará disponible en: **http://localhost:5173**

> El proxy de Vite redirige automáticamente todas las peticiones `/api/*` al BFF en el puerto 3005.

---

## Construir para producción

```bash
npm run build
```

Los archivos compilados quedan en la carpeta `dist/`.

Para previsualizar el build:

```bash
npm run preview
```

---

## Estructura del proyecto

```
src/
├── components/
│   ├── CardMascota.jsx       ← Tarjeta de mascota para listados
│   ├── FormMascota.jsx       ← Formulario de reporte con validaciones
│   ├── Navbar.jsx            ← Barra de navegación con autenticación
│   ├── Footer.jsx            ← Pie de página
│   └── ToastProvider.jsx     ← Sistema global de notificaciones
├── pages/
│   ├── Home.jsx              ← Página de inicio con estadísticas
│   ├── Login.jsx             ← Login y registro de usuarios
│   ├── Mascotas.jsx          ← Listado con filtros
│   ├── DetalleMascota.jsx    ← Detalle y reporte de encuentro
│   ├── Reportar.jsx          ← Formulario de reporte rápido
│   ├── Mapa.jsx              ← Mapa de calor de zonas críticas
│   ├── Perfil.jsx            ← Perfil y gestión de cuenta
│   ├── Panel.jsx             ← Panel de revisión de encuentros
│   └── Admin.jsx             ← Administración de usuarios
├── services/
│   └── api.js                ← Servicios Axios para cada microservicio
├── styles/
│   └── main.css              ← Estilos globales
├── App.jsx                   ← Rutas principales con React Router
└── main.jsx                  ← Punto de entrada de la aplicación
```

---

## Páginas y funcionalidades

| Ruta                  | Página            | Descripción                                         |
|-----------------------|-------------------|-----------------------------------------------------|
| `/`                   | Home              | Estadísticas generales del sistema                  |
| `/login`              | Login             | Iniciar sesión o registrarse                        |
| `/mascotas`           | Mascotas          | Listado filtrable de mascotas perdidas/encontradas  |
| `/mascotas/:id`       | DetalleMascota    | Detalle completo + coincidencias + reporte encuentro|
| `/reportar`           | Reportar          | Formulario para reportar mascota perdida/encontrada |
| `/mapa`               | Mapa              | Mapa de calor con zonas de mayor actividad          |
| `/perfil`             | Perfil            | Datos personales, contraseña y mis reportes         |
| `/panel`              | Panel             | Revisión de reportes de encuentro (moderadores)     |
| `/admin`              | Admin             | Gestión de usuarios (solo ADMIN)                    |

---

## Usuarios demo

| Email                     | Contraseña   | Rol           |
|---------------------------|--------------|---------------|
| admin@sanosysalvos.cl     | admin123     | ADMIN         |
| refugio@esperanza.cl      | refugio123   | REFUGIO       |
| muni@conce.cl             | muni123      | MUNICIPALIDAD |
| dueno@demo.cl             | dueno123     | DUENO         |

---

## Comunicación con el backend

Todas las peticiones HTTP se realizan a través del servicio centralizado en `src/services/api.js`:

```js
// Ejemplo de uso
import { mascotasService, authService } from './services/api';

// Login
const usuario = await authService.login(email, password);

// Obtener mascotas con filtros
const mascotas = await mascotasService.getAll({ estado: 'PERDIDA' });
```

El proxy de Vite (`vite.config.js`) redirige `/api` → `http://localhost:3005` (BFF).

---

## Scripts disponibles

| Script            | Descripción                                  |
|-------------------|----------------------------------------------|
| `npm run dev`     | Inicia servidor de desarrollo con hot-reload |
| `npm run build`   | Genera build de producción en `dist/`        |
| `npm run preview` | Previsualiza el build de producción          |
