# Informe Fullstack III
# Caso C: "Sanos y Salvos"

**Nombres:** Bastián Amir André Martínez Flores · Maicol Antonio Saldivia Silva  
**Docente:** Daniel Williams Concha Saavedra  
**Fecha:** 17-06-2026  
**Asignatura:** Fullstack III  
**Sección:** 002D

---

## 1. Introducción

El extravío de mascotas constituye una problemática creciente que afecta a miles de familias, generando dificultades para la reunificación entre los animales y sus dueños. Actualmente, la información relacionada con mascotas perdidas o encontradas suele encontrarse dispersa en redes sociales, carteles físicos, municipalidades, veterinarias y refugios, dificultando la coordinación de esfuerzos y reduciendo las probabilidades de éxito en la búsqueda.

Con el objetivo de contribuir a la solución de esta problemática surge **"Sanos y Salvos"**, una plataforma tecnológica orientada a centralizar los reportes de mascotas extraviadas o encontradas mediante el uso de una arquitectura basada en microservicios. La propuesta busca facilitar la comunicación entre distintos actores involucrados en el proceso de búsqueda, optimizar la gestión de información y ofrecer una base tecnológica escalable y mantenible.

El presente informe documenta la implementación realizada durante la Evaluación Parcial N°3, describiendo la arquitectura utilizada, los microservicios desarrollados, los mecanismos de persistencia, la integración entre frontend y backend, las pruebas unitarias efectuadas y las herramientas empleadas para la validación del sistema.

---

## 2. Objetivos

### 2.1 Objetivo General

Diseñar e implementar una solución basada en arquitectura de microservicios que permita gestionar reportes de mascotas perdidas o encontradas mediante la integración de componentes frontend y backend, aplicando buenas prácticas de desarrollo y mecanismos de persistencia independientes.

### 2.2 Objetivos Específicos

- Implementar una arquitectura basada en microservicios utilizando Spring Boot y Maven.
- Integrar un frontend desarrollado en React con los servicios backend mediante APIs REST.
- Incorporar mecanismos de persistencia utilizando Spring Data JPA con MySQL.
- Aplicar patrones de diseño que favorezcan la mantenibilidad y escalabilidad del sistema.
- Desarrollar pruebas unitarias que permitan validar el comportamiento de los componentes implementados con una cobertura mínima del 60%.
- Documentar técnicamente la solución desarrollada.

---

## 3. Descripción General del Proyecto

"Sanos y Salvos" corresponde a una plataforma orientada a facilitar la búsqueda y recuperación de mascotas perdidas mediante la centralización de reportes y la coordinación entre distintos actores de la comunidad.

La solución fue construida bajo una arquitectura distribuida basada en microservicios, permitiendo desacoplar las distintas responsabilidades del sistema y facilitando su evolución independiente.

Entre las principales funcionalidades del sistema se encuentran:

- Registro y búsqueda de mascotas perdidas y encontradas con filtros por estado, tipo, tamaño y raza.
- Administración de usuarios con roles diferenciados (Dueño, Veterinaria, Refugio, Municipalidad, Admin).
- Gestión de información geográfica con mapa interactivo y zonas críticas.
- Generación automática de coincidencias potenciales mediante algoritmo de scoring geográfico.
- Reportes de encuentro con flujo de revisión y aprobación.
- Autenticación segura mediante JWT con control de acceso basado en roles (RBAC).
- Consumo de servicios mediante APIs REST a través de un BFF centralizado.

---

## 4. Arquitectura de la Solución

### 4.1 Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                         │
│  Usuario → Frontend React + Vite (Puerto 5173)                  │
│  Páginas: Home, Mascotas, Mapa, Reportar, Login, Perfil, Admin  │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP /api/* (Proxy Vite → :3005)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│            CAPA DE ORQUESTACIÓN — BFF / API GATEWAY             │
│           BFF Spring Boot 3.2.5 — Puerto 3005                   │
│      ProxyController → enruta por path (/api/mascotas, etc.)    │
│      Circuit Breaker propio (CLOSED / OPEN / HALF_OPEN)         │
│      JWT Validation · Roles: DUENO, VET, REFUGIO, MUNI, ADMIN  │
└──────┬───────────┬──────────────┬────────────────┬──────────────┘
       │           │              │                │
 /api/mascotas  /api/geo    /api/motor        /api/auth
       ▼           ▼              ▼                ▼
┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────────┐
│ms-mascotas│ │  ms-geo  │ │  ms-motor    │ │ ms-usuarios  │
│  :3001   │ │  :3002   │ │  :3003       │ │  :3004       │
│ Factory  │ │Repository│ │ Strategy     │ │  Repository  │
│ Observer │ │          │ │ (Haversine)  │ │  JWT/RBAC    │
└────┬─────┘ └─────┬────┘ └──────┬───────┘ └──────┬───────┘
     │             │             │                  │
     └─────────────┴─────────────┴──────────────────┘
                           │
                           ▼
              ┌────────────────────────────┐
              │   MySQL 8.x — Puerto 3306  │
              │   XAMPP / Local            │
              │   sanos_y_salvos_db        │
              │   Spring Data JPA/Hibernate│
              └────────────────────────────┘
```

### 4.2 Flujo de comunicación

```
Usuario → Frontend React → BFF (Puerto 3005) → Microservicio correspondiente → MySQL
```

El BFF actúa como único punto de entrada. El frontend no conoce las URLs internas de los microservicios — solo conoce el BFF. Esta aproximación permite mejorar la escalabilidad, favorecer el desacoplamiento entre módulos y facilitar la evolución independiente de cada servicio.

### 4.3 Justificación: Microservicios vs. Arquitectura Monolítica

| Criterio | Justificación |
|---|---|
| **Escalabilidad** | El Motor de Coincidencias puede escalar de forma independiente durante picos de reportes |
| **Resiliencia** | Un fallo en Geolocalización no detiene el registro de nuevas mascotas |
| **Mantenibilidad** | Los algoritmos de búsqueda se actualizan sin afectar el sistema completo |
| **Independencia** | Cada microservicio tiene su propio ciclo de vida y configuración |

---

## 5. Componentes Implementados

### 5.1 Frontend

El frontend fue desarrollado utilizando **React 18 + Vite 5**, constituyendo la interfaz principal de interacción con el usuario.

**Stack tecnológico frontend:**
- React 18 con Hooks (useState, useEffect, useCallback)
- Vite 5 como bundler y servidor de desarrollo
- Axios para peticiones HTTP con interceptores JWT
- React Router v6 para navegación
- CSS modular por componente

**Páginas implementadas:**

| Ruta | Descripción |
|---|---|
| `/` | Home con estadísticas en tiempo real y últimos reportes |
| `/mascotas` | Lista con filtros por estado, tipo, tamaño y raza |
| `/mascotas/:id` | Detalle completo + coincidencias del motor |
| `/reportar` | Formulario de reporte con geolocalización (requiere login) |
| `/mapa` | Mapa SVG interactivo con pins por estado |
| `/login` | Login y registro de cuenta |
| `/perfil` | Editar datos, cambiar contraseña, mis reportes |
| `/panel` | Moderación de encuentros (REFUGIO/MUNICIPALIDAD/ADMIN) |
| `/admin` | Gestión de usuarios y roles (solo ADMIN) |

**Responsabilidades principales:**
- Visualización y filtrado de reportes de mascotas.
- Registro y gestión de formularios con validación.
- Consumo de servicios expuestos por el BFF.
- Presentación de mapa interactivo con casos georreferenciados.
- Gestión de sesión con JWT almacenado en localStorage.

### 5.2 Backend For Frontend (BFF)

Se implementó un **BFF en Spring Boot 3.2.5** en el puerto 3005, cuya función es actuar como intermediario entre el frontend y los microservicios internos.

**Responsabilidades:**
- Enrutamiento de peticiones al microservicio correcto según el path `/api/*`.
- Implementación del patrón Circuit Breaker para tolerancia a fallos.
- Filtrado de headers problemáticos (Transfer-Encoding, Connection) para evitar errores de proxy.
- Centralización del acceso a los servicios backend.

**Mapeo de rutas:**

| Path del frontend | Microservicio destino | Puerto |
|---|---|---|
| `/api/auth/**` | ms-usuarios-entidades | 3004 |
| `/api/mascotas/**` | ms-gestion-mascotas | 3001 |
| `/api/geolocalizacion/**` | ms-geolocalizacion | 3002 |
| `/api/motor/**` | ms-motor-coincidencias | 3003 |

### 5.3 Microservicios

Los microservicios fueron implementados en **Java 23 con Spring Boot 3.2.5** y construidos con **Apache Maven 3.9.16**.

---

## 6. Microservicios Implementados

### 6.1 Microservicio de Gestión de Mascotas (Puerto 3001)

Responsable del registro y administración de mascotas perdidas o encontradas.

**Incluye:**
- Entidades JPA (Mascota, ReporteEncuentro).
- Controladores REST con validación de entrada.
- Servicios de negocio con lógica de dominio.
- Repositorios JPA con queries personalizadas.
- Patrones Factory Method y Observer implementados.

**Endpoints principales:**

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | /api/mascotas/busqueda | No | Buscar con filtros (estado, tipo, tamaño, raza) |
| GET | /api/mascotas/{id} | No | Detalle de una mascota |
| GET | /api/mascotas/estadisticas | No | Totales por estado |
| POST | /api/mascotas/reportar | Si | Crear nuevo reporte |
| PUT | /api/mascotas/{id} | Si | Actualizar mascota |
| DELETE | /api/mascotas/{id} | Si | Eliminar mascota |
| POST | /api/mascotas/{id}/reportar-encuentro | Si | Reportar encuentro |
| GET | /api/mascotas/encuentros/revision | No | Solicitudes pendientes |
| PUT | /api/mascotas/encuentros/revision/{id} | Si | Aprobar o rechazar encuentro |

### 6.2 Microservicio de Usuarios y Entidades (Puerto 3004)

Encargado de gestionar la información relacionada con usuarios y organizaciones colaboradoras.

**Incluye:**
- Gestión completa de usuarios (DUENO, VETERINARIA, REFUGIO, MUNICIPALIDAD, ADMIN).
- Autenticación mediante JWT con expiración de 8 horas.
- Control de acceso basado en roles (RBAC).
- Endpoints de administración protegidos por rol ADMIN.

**Endpoints principales:**

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /api/auth/login | No | Login → retorna JWT |
| POST | /api/auth/register | No | Registro nuevo usuario |
| GET | /api/auth/me | Si | Perfil del usuario autenticado |
| PUT | /api/auth/me | Si | Actualizar nombre |
| PUT | /api/auth/me/password | Si | Cambiar contraseña |
| GET | /api/auth/usuarios | Si (solo ADMIN) | Listar todos los usuarios |
| PUT | /api/auth/usuarios/{id}/rol | Si (solo ADMIN) | Cambiar rol |
| PUT | /api/auth/usuarios/{id}/toggle-activo | Si (solo ADMIN) | Activar/desactivar cuenta |

**Ejemplo de respuesta login:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "usuario": {
      "id_usuario": "usr-admin-001",
      "nombre": "Admin Sanos y Salvos",
      "email": "admin@sanosysalvos.cl",
      "rol": "ADMIN",
      "activo": true
    }
  }
}
```

### 6.3 Microservicio de Geolocalización (Puerto 3002)

Responsable del manejo de información geográfica asociada a reportes.

**Sus funciones incluyen:**
- Obtención de todos los puntos georreferenciados para el mapa.
- Análisis de zonas críticas por comuna ordenadas por total de casos.
- Generación de datos para mapa de calor (heatmap): PERDIDA=1.0, ENCONTRADA=0.6.
- Estadísticas geográficas del sistema.

**Endpoints:**

| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/geolocalizacion/puntos | Puntos para el mapa |
| GET | /api/geolocalizacion/zonas-criticas | Comunas con más casos |
| GET | /api/geolocalizacion/mapa-calor | Datos heatmap |
| GET | /api/geolocalizacion/estadisticas | Métricas geográficas |

### 6.4 Motor de Coincidencias (Puerto 3003)

Encargado de analizar información proveniente de distintos reportes para identificar posibles coincidencias mediante un **algoritmo de scoring geográfico**.

**Algoritmo de scoring:**

```
Score = tipo_animal  (30 puntos)
      + raza         (30 puntos)
      + color        (20 puntos)
      + tamaño       (10 puntos)
      + distancia < 2km (10pts) | < 5km (5pts)
      ──────────────────────────────────────────
      Máximo: 100 puntos
```

La distancia se calcula con la **fórmula de Haversine**, que considera la curvatura de la Tierra para mayor precisión geográfica. Solo se persisten en la base de datos las coincidencias con score ≥ 60.

**Endpoints:**

| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/motor/buscar/{id} | Ejecutar algoritmo de matching |
| GET | /api/motor/historial | Historial de coincidencias guardadas |
| GET | /api/motor/resumen | Estadísticas totales del motor |

---

## 7. Persistencia de Datos

### 7.1 Implementación con Spring Data JPA

La persistencia fue implementada utilizando **Spring Data JPA** con **Hibernate** como ORM, conectados a **MySQL 8.x** (XAMPP, puerto 3306).

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sanos_y_salvos_db
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.open-in-view=false
```

La propiedad `ddl-auto=update` permite que Hibernate actualice automáticamente el esquema sin perder datos existentes.

### 7.2 Modelo de Datos

#### Tabla: mascotas

| Columna | Tipo | Descripción |
|---|---|---|
| id_mascota | VARCHAR(36) PK | UUID único generado |
| tipo_animal | VARCHAR(50) | Especie (Perro, Gato, etc.) |
| raza | VARCHAR(80) | Raza para el motor de coincidencias |
| nombre | VARCHAR(80) | Nombre de la mascota |
| color_primario | VARCHAR(60) | Color principal del pelaje |
| tamano | ENUM | Pequeño, Mediano, Grande |
| foto_url | LONGTEXT | URL o imagen base64 |
| latitud / longitud | DOUBLE | Coordenadas geográficas del suceso |
| sector / comuna | VARCHAR(120) | Ubicación del suceso |
| fecha_reporte | DATETIME | Timestamp automático (DEFAULT NOW) |
| estado | ENUM | PERDIDA, ENCONTRADA, REUNIFICADA |
| id_usuario | VARCHAR(36) FK | Referencia al usuario que reportó |

#### Tabla: usuarios

| Columna | Tipo | Descripción |
|---|---|---|
| id_usuario | VARCHAR(36) PK | UUID único |
| nombre / email | VARCHAR | Datos de acceso |
| password_hash | VARCHAR(255) | Contraseña |
| rol | ENUM | DUENO, VETERINARIA, REFUGIO, MUNICIPALIDAD, ADMIN |
| activo | BOOLEAN | Estado de la cuenta |
| created_at | DATETIME | Fecha de creación automática |

#### Tabla: reportes_encuentro

| Columna | Tipo | Descripción |
|---|---|---|
| id_reporte_encuentro | VARCHAR(36) PK | UUID único |
| id_mascota | VARCHAR(36) FK | Mascota relacionada |
| foto_evidencia_url | LONGTEXT | Fotografía de evidencia |
| encontrada_en | VARCHAR(200) | Lugar del encuentro |
| contacto_nombre / telefono | VARCHAR | Datos del informante |
| estado_revision | ENUM | EN_REVISION, APROBADO, RECHAZADO |
| fecha_reporte / revision | DATETIME | Timestamps del proceso |

#### Tabla: coincidencias

| Columna | Tipo | Descripción |
|---|---|---|
| id_coincidencia | VARCHAR(36) PK | UUID único |
| id_mascota_a / b | VARCHAR(36) FK | Par de mascotas comparadas |
| score | INT | Puntuación 0-100 del algoritmo |
| distancia_km | DECIMAL(8,2) | Distancia calculada con Haversine |
| notificado | BOOLEAN | Si se notificó al dueño |
| created_at | DATETIME | Fecha del cálculo |

### 7.3 Patrón Database per Service

Cada microservicio accede a sus propias tablas dentro de la base de datos compartida. Esta estrategia permite:
- Reducir el acoplamiento entre dominios.
- Mejorar la escalabilidad del sistema.
- Favorecer la autonomía de cada componente.
- Incrementar la resiliencia frente a fallos.

---

## 8. Patrones de Diseño Aplicados

### 8.1 Patrón Repository

**Implementado en:** todos los microservicios mediante Spring Data JPA.

Aísla la lógica de negocio del acceso a datos. Los servicios solo invocan métodos del repositorio sin escribir SQL directamente.

```java
// Ejemplo: MascotaRepository.java
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, String> {

    List<Mascota> findByIdUsuario(String idUsuario);

    @Query("SELECT m FROM Mascota m WHERE " +
           "(:estado IS NULL OR m.estado = :estado) AND " +
           "(:tipoAnimal IS NULL OR LOWER(m.tipoAnimal) = LOWER(:tipoAnimal))")
    List<Mascota> findWithFilters(
        @Param("estado") EstadoMascota estado,
        @Param("tipoAnimal") String tipoAnimal,
        @Param("tamano") String tamano,
        @Param("raza") String raza);
}
```

**Beneficios:**
- Separación de responsabilidades entre capas.
- Simplificación del acceso a persistencia.
- Facilita pruebas unitarias al poder mockear el repositorio.

### 8.2 Patrón Factory Method

**Implementado en:** ms-gestion-mascotas  
**Archivos:** `pattern/factory/AlertaFactory.java`, `AlertaExtravio.java`, `AlertaHallazgo.java`, `AlertaReunificacion.java`

Crea dinámicamente el tipo correcto de alerta según el estado de la mascota, sin que el código cliente conozca las subclases.

```java
public class AlertaFactory {
    public static Alerta crear(EstadoMascota estado, Mascota m) {
        return switch (estado) {
            case PERDIDA     -> new AlertaExtravio(m.getIdMascota(), m.getContacto(), ...);
            case ENCONTRADA  -> new AlertaHallazgo(m.getIdMascota(), m.getContacto(), ...);
            case REUNIFICADA -> new AlertaReunificacion(m.getIdMascota(), m.getContacto(), ...);
        };
    }
}
```

**Jerarquía de clases:**
```
Alerta (abstract)
├── AlertaExtravio    → estado PERDIDA
├── AlertaHallazgo    → estado ENCONTRADA
└── AlertaReunificacion → estado REUNIFICADA
```

**Beneficios:**
- Mayor flexibilidad para agregar nuevos tipos de alerta.
- Facilita futuras extensiones sin modificar código existente.
- Reduce dependencias directas entre clases.

### 8.3 Patrón Observer (Spring Events)

**Implementado en:** ms-gestion-mascotas  
**Archivos:** `pattern/observer/MascotaReportadaEvent.java`, `MascotaReunificadaEvent.java`, `MascotaEventListener.java`

Cuando se registra una mascota o se aprueba una reunificación, el servicio publica un evento sin conocer quién lo escucha.

```java
// MascotasService.java — publica el evento
Mascota saved = mascotaRepository.save(mascota);
eventPublisher.publishEvent(new MascotaReportadaEvent(saved)); // Observer

// MascotaEventListener.java — suscriptor desacoplado
@EventListener
public void alReportarMascota(MascotaReportadaEvent event) {
    Mascota m = event.getMascota();
    // Lógica secundaria sin acoplar al servicio principal
    AlertaFactory.crear(m.getEstado(), m);
}
```

**Flujo del patrón:**
```
MascotasService.registrarMascota()
       ↓
eventPublisher.publishEvent(MascotaReportadaEvent)
       ↓
MascotaEventListener.alReportarMascota()
       ↓
AlertaFactory.crear(estado, mascota)  ← combina Observer + Factory
```

**Beneficios:**
- El servicio principal no conoce las acciones secundarias.
- Agregar nuevas reacciones no requiere modificar el servicio.
- Desacoplamiento total entre lógica de negocio y efectos secundarios.

### 8.4 Patrón Circuit Breaker

**Implementado en:** bff  
**Archivo:** `pattern/CircuitBreaker.java`  
**Implementación:** Código Java propio, sin librerías externas.

Previene fallos en cascada. Si un microservicio falla 3 veces consecutivas, el circuito se abre y retorna una respuesta de fallback inmediata (HTTP 503).

```java
@Component
public class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold = 3;
    private final long resetTimeout = 10000; // 10 segundos

    public <T> T execute(String serviceName,
                         Supplier<T> action,
                         Supplier<T> fallback) {
        // Si está OPEN y pasaron 10 seg → HALF_OPEN (prueba)
        // Si la prueba es exitosa → CLOSED
        // Si acumula 3 fallos → OPEN
    }
}
```

**Estados del Circuit Breaker:**

```
CLOSED ──(3 fallos)──→ OPEN ──(10 seg)──→ HALF_OPEN
  ↑                                            │
  └──────────────── (éxito) ──────────────────┘
```

**Beneficios:**
- Prevención de fallos en cascada entre microservicios.
- Respuesta inmediata HTTP 503 en lugar de timeout infinito.
- Auto-recuperación tras el tiempo de reset configurado.

---

## 9. Seguridad — JWT y RBAC

El sistema implementa autenticación stateless con **JWT (JSON Web Token)**. El token se genera en ms-usuarios-entidades y es validado por cada microservicio mediante un `JwtInterceptor`.

### Roles implementados

| Rol | Permisos principales |
|---|---|
| DUENO | Reportar mascotas, ver reportes, reportar encuentros |
| VETERINARIA | Todo DUENO + ver historial |
| REFUGIO | Todo VETERINARIA + revisar encuentros, estadísticas |
| MUNICIPALIDAD | Todo REFUGIO + ver zonas geográficas |
| ADMIN | Acceso completo + gestión de usuarios y roles |

### Flujo de autenticación

```
1. POST /api/auth/login → JWT generado (válido 8 horas)
2. Frontend guarda token en localStorage
3. Cada petición: Authorization: Bearer <token>
4. JwtInterceptor valida token y extrae userId + rol
5. @RequireRole verifica permisos antes de ejecutar
```

---

## 10. Integración Frontend — Backend

La integración entre el frontend y los servicios backend se realizó mediante **APIs REST** usando **Axios** con interceptores automáticos de JWT.

```javascript
// api.js — Axios con interceptor JWT automático
const API = axios.create({ baseURL: '/api', timeout: 8000 });

API.interceptors.request.use(config => {
  const token = localStorage.getItem('sanos_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

El frontend consume información exclusivamente a través del BFF, el cual se comunica con los microservicios internos mediante HTTP. Esta aproximación permite:
- Reducir el acoplamiento entre capas.
- Facilitar la evolución independiente del frontend.
- Simplificar la exposición de funcionalidades hacia el cliente.

### Colección Postman

Se desarrolló una **Postman Collection** con todos los endpoints de los 4 microservicios, incluyendo:
- Guardado automático del token JWT tras el login.
- Variables de colección para `base_url` y `token`.
- Ejemplos de request y response para cada endpoint.
- Descripción de parámetros de query (filtros, paginación).

El archivo `docs/sanos-y-salvos.postman_collection.json` puede importarse directamente en Postman para probar todos los servicios.

---

## 11. Pruebas Unitarias

### 11.1 Resumen de Resultados

| Microservicio | Tests | Pasados | Fallidos | Cobertura |
|---|---|---|---|---|
| ms-gestion-mascotas | 18 | 18 ✅ | 0 | 69.4% |
| ms-motor-coincidencias | 15 | 15 ✅ | 0 | 78.9% |
| ms-usuarios-entidades | 21 | 21 ✅ | 0 | 77.9% |
| ms-geolocalizacion | 1 | 1 ✅ | 0 | 32.0% |
| **TOTAL** | **55** | **55** | **0** | **≥60% en 3/4** |

**Resultado global: BUILD SUCCESS en los 4 microservicios. 0 fallos, 0 errores.**

### 11.2 Herramientas Utilizadas

- **JUnit 5 (Jupiter)** — framework de tests, incluido con Spring Boot 3.2.5
- **Mockito 5** — mocking de dependencias (repositorios JPA, eventos Spring, JWT)
- **JaCoCo 0.8.12** — cobertura de código (versión compatible con Java 23)
- **maven-surefire-plugin** — ejecución de tests con flags para Java 23:

```xml
<argLine>
    @{argLine}
    -Dnet.bytebuddy.experimental=true
    -Djdk.attach.allowAttachSelf=true
    --add-opens java.base/java.lang=ALL-UNNAMED
</argLine>
```

### 11.3 Comandos para Ejecutar las Pruebas

**Desde PowerShell:**
```powershell
& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-gestion-mascotas\pom.xml" clean test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-motor-coincidencias\pom.xml" clean test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-usuarios-entidades\pom.xml" clean test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-geolocalizacion\pom.xml" clean test
```

**Generar reporte JaCoCo HTML:**
```powershell
& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-motor-coincidencias\pom.xml" test jacoco:report
```
Reporte en: `microservicios\<nombre>\target\site\jacoco\index.html`

### 11.4 Casos de Prueba Destacados

#### Test Observer — verifica publicación del evento al registrar
```java
@Test
void testRegistrarMascota_Success() {
    when(mascotaRepository.save(any(Mascota.class))).thenReturn(mockMascota);

    MascotaResponse res = mascotasService.registrarMascota(req, "user-123");

    assertNotNull(res);
    // Verifica el patrón Observer
    verify(eventPublisher, times(1)).publishEvent(any(MascotaReportadaEvent.class));
}
```

#### Test Factory Method — verifica creación correcta de alertas
```java
@Test
void testAlertaFactory_Perdida() {
    Alerta alerta = AlertaFactory.crear(EstadoMascota.PERDIDA, mockMascota);
    assertNotNull(alerta);
    assertEquals("AlertaExtravio", alerta.getTipo()); // Factory crea el tipo correcto
}
```

#### Test Haversine — distancia geográfica real
```java
@Test
void testDistanciaKm_CoordenadaNula() {
    double dist = scoringAlgorithm.distanciaKm(null, -70.66, -33.44, -70.66);
    assertEquals(Double.MAX_VALUE, dist); // null → valor máximo, sin crash
}
```

#### Test AuthService — seguridad de roles
```java
@Test
void testRegister_NoPermiteRolAdmin() {
    req.setRol(Rol.ADMIN); // intento de registro como ADMIN
    LoginResponse resp = authService.register(req);
    assertNotEquals(Rol.ADMIN, resp.getUsuario().getRol()); // debe ser DUENO
}
```

### 11.5 Relación Tests ↔ Patrones de Diseño

| Patrón | Test que lo verifica |
|---|---|
| Observer | `testRegistrarMascota_Success` — `verify(eventPublisher).publishEvent(...)` |
| Factory Method | `testAlertaFactory_Perdida/Encontrada/Reunificada/EstadoNull` |
| Repository | Todos los tests — mocks de JPA Repository |
| Circuit Breaker | Validado en BFF — CLOSED/OPEN/HALF_OPEN |

---

## 12. Estrategia de Branching — Git Flow

El proyecto adoptó **Git Flow** como estrategia de control de versiones.

### Ramas principales

| Rama | Propósito |
|---|---|
| `main` | Código estable. Solo recibe merges desde `release/*` o `hotfix/*` |
| `develop` | Integración continua. Todas las features se integran aquí primero |

### Ramas de corto plazo

```
feature/ms-gestion-mascotas
feature/ms-geolocalizacion
feature/ms-motor-coincidencias
feature/ms-usuarios-entidades
feature/bff-circuit-breaker
feature/frontend-react
feature/patrones-diseno
feature/pruebas-unitarias
release/parcial-3
```

### Convención de commits (Conventional Commits)

```
feat(ms-mascotas): implementa Factory Method para AlertaExtravio y AlertaHallazgo
feat(bff): implementa Circuit Breaker con estados CLOSED/OPEN/HALF_OPEN
feat(ms-motor): agrega algoritmo Haversine para calculo de distancia geografica
fix(vite): actualiza proxy a puerto 3005 del BFF
test(ms-motor): agrega 15 pruebas unitarias para MotorService y ScoringAlgorithm
docs: actualiza README con comandos Maven para pruebas
```

---

## 13. Cómo Ejecutar el Proyecto

### Requisitos previos

- Java 23 (JDK)
- Apache Maven 3.8+ en PATH
- Node.js 18+
- XAMPP con MySQL activo en puerto 3306

### Pasos

**1. Base de datos**
1. Abrir XAMPP → activar MySQL
2. Ir a phpMyAdmin: `http://localhost/phpmyadmin`
3. Importar `database/setup.sql` — crea la base de datos, tablas y usuarios demo

**2. Iniciar microservicios y frontend**

Hacer doble clic en `start.bat` (Windows). El script:
- Libera los puertos 3001-3005 si están ocupados
- Arranca los 5 microservicios con memoria optimizada (-Xmx180m)
- Inicia el frontend React automáticamente

**3. Abrir en el navegador**
```
http://localhost:5173
```

### Usuarios demo

| Email | Password | Rol |
|---|---|---|
| admin@sanosysalvos.cl | admin123 | ADMIN |
| refugio@esperanza.cl | refugio123 | REFUGIO |
| muni@conce.cl | muni123 | MUNICIPALIDAD |
| dueno@demo.cl | dueno123 | DUENO |

---

## 14. Limitaciones y Trabajo Futuro

A pesar del avance alcanzado, existen áreas que podrían mejorarse en futuras iteraciones:

- **Contenerización:** La arquitectura está preparada para dockerizarse — cada microservicio es independiente con su propio `pom.xml` y configuración, lo que facilita crear Dockerfiles.
- **Despliegue cloud:** El sistema podría desplegarse en AWS utilizando RDS para MySQL y ECS para los microservicios.
- **Monitoreo:** Incorporar Spring Actuator y herramientas de observabilidad como Prometheus/Grafana.
- **Cobertura de ms-geolocalizacion:** Actualmente en 32% — los métodos que delegan directamente al repositorio podrían tener tests de integración adicionales.
- **Notificaciones:** Implementar notificaciones por email o push cuando el motor detecta una coincidencia con score alto.

---

## 15. Conclusiones

El desarrollo del proyecto "Sanos y Salvos" permitió implementar una solución completamente funcional basada en microservicios, abordando una problemática real mediante el uso de tecnologías modernas del ecosistema Java.

La arquitectura implementada demuestra correctamente principios de **desacoplamiento, modularidad, escalabilidad y resiliencia**, distribuyendo responsabilidades entre 5 microservicios especializados conectados mediante un BFF con Circuit Breaker.

Los patrones de diseño **Repository, Factory Method, Observer y Circuit Breaker** fueron implementados en código real y verificados mediante pruebas unitarias, evidenciando una orientación hacia buenas prácticas de desarrollo.

Las **55 pruebas unitarias** con BUILD SUCCESS y cobertura ≥60% en 3 de 4 microservicios validan el comportamiento esperado de la lógica de negocio, incluyendo el algoritmo Haversine, las reglas de seguridad RBAC y el flujo completo de reportes de encuentro.

Finalmente, la plataforma cumple el objetivo principal de centralizar reportes de mascotas, con un sistema de coincidencias inteligente, autenticación JWT, gestión de roles y una interfaz web funcional accesible desde `http://localhost:5173`.

---

## 16. Anexos

Se adjuntan los siguientes elementos como evidencia del trabajo realizado:

- **Diagrama de arquitectura** — `docs/diagrama-arquitectura.drawio` (exportar como PNG/PDF desde draw.io)
- **Colección Postman** — `docs/sanos-y-salvos.postman_collection.json`
- **Reportes de cobertura JaCoCo** — `docs/cobertura/<microservicio>/index.html`
- **Informe de pruebas unitarias** — `docs/informe-pruebas.md`
- **Patrones de diseño** — `docs/patrones-y-arquetipos.md`
- **Plan de branching** — `docs/plan-branching.md`
- **Script de arranque** — `start.bat` / `start.sh`
- **Script de cobertura** — `generar-cobertura.bat`
- **Base de datos** — `database/setup.sql`
- **Capturas de pantalla del frontend** — *(insertar capturas al convertir a PDF)*
- **Capturas de Postman con respuestas** — *(insertar capturas al convertir a PDF)*
- **Capturas de JaCoCo HTML** — *(insertar capturas al convertir a PDF)*
- **Enlace a repositorios GitHub** — `repositorios.txt`

---

*Informe preparado para Evaluación Parcial N°3 — DSY1106 Desarrollo Fullstack III · DuocUC 2026*
