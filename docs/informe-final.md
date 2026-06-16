# Informe Fullstack III
# Caso C: "Sanos y Salvos"

**Nombres:** Bastián Amir André Martínez Flores · Maicol Antonio Saldivia Silva  
**Docente:** Daniel Williams Concha Saavedra  
**Fecha:** Junio 2026  
**Asignatura:** Fullstack III  
**Sección:** 002D

---

## 1. Introducción

En la actualidad, el extravío de mascotas es un problema creciente que genera angustia en las familias. El desafío principal no es la falta de reportes, sino que la información está totalmente fragmentada en redes sociales, carteles públicos y registros aislados de veterinarias o municipalidades. Esta desorganización impide que los datos de mascotas perdidas y encontradas coincidan a tiempo, reduciendo drásticamente las probabilidades de éxito en los rescates.

Para solucionar esto, surge **Sanos y Salvos**, una plataforma tecnológica que centraliza y estructura estos reportes mediante una arquitectura de microservicios. El sistema automatiza la detección de coincidencias con un algoritmo de scoring geográfico y utiliza herramientas de geolocalización para identificar zonas críticas de extravío.

Este informe documenta la implementación completa del sistema, incluyendo arquitectura, persistencia de datos, patrones de diseño, pruebas unitarias y métricas de cobertura.

---

## 2. Arquitectura de Microservicios

### 2.1 Justificación: Microservicios vs. Arquitectura Monolítica

| Criterio | Justificación |
|---|---|
| **Escalabilidad** | El Motor de Coincidencias puede escalar de forma independiente durante picos de reportes |
| **Resiliencia** | Un fallo en Geolocalización no detiene el registro de nuevas mascotas |
| **Mantenibilidad** | Los algoritmos de búsqueda se actualizan sin afectar el sistema completo |
| **Independencia** | Cada equipo puede trabajar en un microservicio sin interferir con los demás |

### 2.2 Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                    USUARIO FINAL                                 │
│                 http://localhost:5173                            │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP /api/*
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│              BFF — Backend For Frontend                         │
│                   Puerto 3005                                    │
│         Circuit Breaker (CLOSED/OPEN/HALF_OPEN)                 │
│         ProxyController → enruta por path                       │
└──────┬───────────┬──────────────┬────────────────┬──────────────┘
       │           │              │                │
       ▼           ▼              ▼                ▼
┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────────┐
│ms-mascotas│ │  ms-geo  │ │  ms-motor    │ │ ms-usuarios  │
│ :3001    │ │  :3002   │ │  :3003       │ │  :3004       │
│ Factory  │ │Repository│ │ Strategy     │ │  Repository  │
│ Observer │ │          │ │ (Haversine)  │ │  JWT/RBAC    │
└────┬─────┘ └─────┬────┘ └──────┬───────┘ └──────┬───────┘
     │             │             │                  │
     └─────────────┴─────────────┴──────────────────┘
                           │
                           ▼
              ┌─────────────────────────┐
              │   MySQL — Puerto 3306   │
              │   sanos_y_salvos_db     │
              │   XAMPP / local         │
              └─────────────────────────┘
```

### 2.3 Microservicios Implementados

| Microservicio | Puerto | Responsabilidad | Patrones |
|---|---|---|---|
| ms-gestion-mascotas | 3001 | Registro y gestión de reportes de mascotas | Factory, Observer, Repository |
| ms-geolocalizacion | 3002 | Datos geográficos, zonas críticas, mapa de calor | Repository |
| ms-motor-coincidencias | 3003 | Algoritmo de matching por scoring geográfico | Strategy (Haversine), Repository |
| ms-usuarios-entidades | 3004 | Autenticación JWT, roles RBAC | Repository |
| bff | 3005 | API Gateway con Circuit Breaker | Circuit Breaker |

### 2.4 Tecnologías Utilizadas

| Componente | Tecnología | Versión |
|---|---|---|
| Backend microservicios | Java + Spring Boot | 23 / 3.2.5 |
| ORM / Persistencia | Spring Data JPA + Hibernate | 6.4.x |
| Base de datos | MySQL (XAMPP) | 8.x |
| Frontend | React + Vite | 18 / 5.x |
| Autenticación | JWT (jjwt) | 0.11.5 |
| Build | Apache Maven | 3.9.16 |
| Tests | JUnit 5 + Mockito | 5.x |
| Cobertura | JaCoCo | 0.8.12 |

---

## 3. Persistencia de Datos

### 3.1 Implementación con Spring Data JPA

Todos los microservicios utilizan **Spring Data JPA** con **Hibernate** como ORM, conectados a una base de datos MySQL compartida (`sanos_y_salvos_db`). La configuración de cada microservicio apunta al mismo servidor MySQL local.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sanos_y_salvos_db
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

La propiedad `ddl-auto=update` permite que Hibernate actualice automáticamente el esquema de tablas al iniciar, sin perder datos existentes.

### 3.2 Modelo de Datos — Tablas Principales

#### Tabla: `mascotas`

| Columna | Tipo | Descripción |
|---|---|---|
| id_mascota | VARCHAR(36) PK | UUID único del registro |
| tipo_animal | VARCHAR(50) | Especie (Perro, Gato, etc.) |
| raza | VARCHAR(80) | Raza para el motor de coincidencias |
| nombre | VARCHAR(80) | Nombre de la mascota |
| color_primario | VARCHAR(60) | Color principal del pelaje |
| tamano | ENUM | Pequeño, Mediano, Grande |
| sexo | VARCHAR(20) | Macho / Hembra |
| foto_url | LONGTEXT | URL o base64 de la imagen |
| latitud / longitud | DOUBLE | Coordenadas geográficas |
| sector / comuna | VARCHAR(120) | Ubicación del suceso |
| fecha_reporte | DATETIME | Timestamp automático |
| estado | ENUM | PERDIDA, ENCONTRADA, REUNIFICADA |
| id_usuario | VARCHAR(36) FK | Referencia al usuario que reportó |

#### Tabla: `usuarios`

| Columna | Tipo | Descripción |
|---|---|---|
| id_usuario | VARCHAR(36) PK | UUID único |
| nombre | VARCHAR(120) | Nombre completo |
| email | VARCHAR(120) UNIQUE | Email de acceso |
| password_hash | VARCHAR(255) | Contraseña (texto plano en dev) |
| rol | ENUM | DUENO, VETERINARIA, REFUGIO, MUNICIPALIDAD, ADMIN |
| activo | BOOLEAN | Estado de la cuenta |

#### Tabla: `reportes_encuentro`

| Columna | Tipo | Descripción |
|---|---|---|
| id_reporte_encuentro | VARCHAR(36) PK | UUID único |
| id_mascota | VARCHAR(36) FK | Mascota relacionada |
| foto_evidencia_url | LONGTEXT | Foto de evidencia |
| encontrada_en | VARCHAR(200) | Lugar del encuentro |
| estado_revision | ENUM | EN_REVISION, APROBADO, RECHAZADO |

#### Tabla: `coincidencias`

| Columna | Tipo | Descripción |
|---|---|---|
| id_coincidencia | VARCHAR(36) PK | UUID único |
| id_mascota_a / b | VARCHAR(36) FK | Par de mascotas comparadas |
| score | INT | Score 0-100 del algoritmo |
| distancia_km | DECIMAL(8,2) | Distancia calculada con Haversine |
| notificado | BOOLEAN | Si ya se notificó al dueño |

### 3.3 Patrón Repository

Cada microservicio implementa el patrón Repository mediante interfaces que extienden `JpaRepository`:

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
        @Param("tipoAnimal") String tipoAnimal, ...);
}
```

Este patrón aísla completamente la lógica de negocio del acceso a datos, permitiendo que las pruebas unitarias mockeen el repositorio sin necesitar MySQL activo.

---

## 4. Patrones de Diseño Implementados

### 4.1 Patrón Repository

**Implementado en:** todos los microservicios  
**Tecnología:** Spring Data JPA

Aísla la capa de acceso a datos de la lógica de negocio. Los servicios solo llaman métodos del repositorio, sin escribir SQL directamente.

### 4.2 Patrón Factory Method

**Implementado en:** ms-gestion-mascotas  
**Archivos:** `pattern/factory/AlertaFactory.java`, `AlertaExtravio.java`, `AlertaHallazgo.java`, `AlertaReunificacion.java`

Crea dinámicamente el tipo correcto de alerta según el estado de la mascota, sin que el código cliente conozca las subclases:

```java
public class AlertaFactory {
    public static Alerta crear(EstadoMascota estado, Mascota m) {
        return switch (estado) {
            case PERDIDA     -> new AlertaExtravio(m.getIdMascota(), ...);
            case ENCONTRADA  -> new AlertaHallazgo(m.getIdMascota(), ...);
            case REUNIFICADA -> new AlertaReunificacion(m.getIdMascota(), ...);
        };
    }
}
```

**Jerarquía:**
```
Alerta (abstract)
├── AlertaExtravio    → PERDIDA
├── AlertaHallazgo    → ENCONTRADA
└── AlertaReunificacion → REUNIFICADA
```

### 4.3 Patrón Observer (Spring Events)

**Implementado en:** ms-gestion-mascotas  
**Archivos:** `pattern/observer/MascotaReportadaEvent.java`, `MascotaReunificadaEvent.java`, `MascotaEventListener.java`

Cuando una mascota es reportada o reunificada, el servicio publica un evento sin conocer quién lo escucha:

```java
// MascotasService.java
eventPublisher.publishEvent(new MascotaReportadaEvent(saved));

// MascotaEventListener.java — desacoplado
@EventListener
public void alReportarMascota(MascotaReportadaEvent event) {
    System.out.println("[OBSERVER] Nueva mascota reportada: " + event.getMascota().getNombre());
}
```

**Flujo:**
```
MascotasService → publishEvent(MascotaReportadaEvent)
                         ↓
              MascotaEventListener.onMascotaReportada()
                         ↓
              AlertaFactory.crear(estado, mascota)
```

### 4.4 Patrón Circuit Breaker

**Implementado en:** bff  
**Archivo:** `pattern/CircuitBreaker.java`

Previene fallos en cascada. Si un microservicio falla 3 veces, el circuito se ABRE y retorna una respuesta de fallback inmediata:

```java
// Estados implementados
CLOSED  → operación normal
OPEN    → bloqueado, retorna fallback (HTTP 503)
HALF_OPEN → prueba de recuperación tras 10 segundos
```

```java
return circuitBreaker.execute(
    serviceName,
    () -> restTemplate.exchange(fullUrl, method, httpEntity, byte[].class),
    () -> ResponseEntity.status(503).body("Servicio no disponible".getBytes())
);
```

---

## 5. API REST — Endpoints

Todas las peticiones pasan por el BFF en `http://localhost:3005`.  
Los endpoints marcados con 🔒 requieren header `Authorization: Bearer <token>`.

### Auth (ms-usuarios-entidades :3004)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /api/auth/login | No | Login → retorna JWT |
| POST | /api/auth/register | No | Registro nuevo usuario |
| GET | /api/auth/me | 🔒 | Perfil del usuario autenticado |
| PUT | /api/auth/me | 🔒 | Actualizar nombre |
| PUT | /api/auth/me/password | 🔒 | Cambiar contraseña |
| GET | /api/auth/usuarios | 🔒 ADMIN | Listar todos los usuarios |
| PUT | /api/auth/usuarios/{id}/rol | 🔒 ADMIN | Cambiar rol |
| PUT | /api/auth/usuarios/{id}/toggle-activo | 🔒 ADMIN | Activar/desactivar |

**Ejemplo respuesta login:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "usuario": {
      "id_usuario": "usr-admin-001",
      "nombre": "Admin",
      "email": "admin@sanosysalvos.cl",
      "rol": "ADMIN",
      "activo": true
    }
  }
}
```

### Mascotas (ms-gestion-mascotas :3001)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | /api/mascotas/busqueda | No | Buscar con filtros opcionales |
| GET | /api/mascotas/{id} | No | Detalle de una mascota |
| GET | /api/mascotas/estadisticas | No | Totales por estado |
| POST | /api/mascotas/reportar | 🔒 | Crear nuevo reporte |
| PUT | /api/mascotas/{id} | 🔒 | Actualizar mascota |
| DELETE | /api/mascotas/{id} | 🔒 | Eliminar mascota |
| GET | /api/mascotas/usuario/mis-reportes | 🔒 | Mis mascotas |
| POST | /api/mascotas/{id}/reportar-encuentro | 🔒 | Reportar encuentro |
| GET | /api/mascotas/encuentros/revision | No | Solicitudes pendientes |
| PUT | /api/mascotas/encuentros/revision/{id} | 🔒 | Aprobar/Rechazar |

### Geolocalización (ms-geolocalizacion :3002)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/geolocalizacion/puntos | Puntos para el mapa |
| GET | /api/geolocalizacion/zonas-criticas | Comunas con más casos |
| GET | /api/geolocalizacion/mapa-calor | Datos heatmap (PERDIDA=1.0, ENCONTRADA=0.6) |
| GET | /api/geolocalizacion/estadisticas | Métricas geográficas |

### Motor de Coincidencias (ms-motor-coincidencias :3003)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | /api/motor/buscar/{id} | Ejecutar algoritmo de matching |
| GET | /api/motor/historial | Historial de coincidencias |
| GET | /api/motor/resumen | Estadísticas del motor |

**Algoritmo de scoring:**
```
Score = tipo_animal (30pts)
      + raza (30pts)
      + color_primario (20pts)
      + tamano (10pts)
      + distancia < 2km (10pts) | < 5km (5pts)
      ─────────────────────────
      Máximo: 100 puntos
```

---

## 6. Seguridad — JWT y RBAC

El sistema implementa autenticación stateless con **JWT (JSON Web Token)**. El token se genera en ms-usuarios-entidades y es validado por cada microservicio mediante un `JwtInterceptor`.

### Roles implementados

| Rol | Permisos |
|---|---|
| DUENO | Reportar, ver mascotas, reportar encuentro |
| VETERINARIA | Todo DUENO + ver historial |
| REFUGIO | Todo VETERINARIA + revisar encuentros, ver estadísticas |
| MUNICIPALIDAD | Todo REFUGIO + ver zonas geográficas |
| ADMIN | Acceso completo + gestión de usuarios y roles |

### Flujo de autenticación

```
1. POST /api/auth/login  →  JWT generado (válido 8 horas)
2. Frontend guarda token en localStorage
3. Cada petición: Authorization: Bearer <token>
4. JwtInterceptor valida token y extrae userId + rol
5. @RequireRole verifica permisos antes de ejecutar
```

---

## 7. Frontend — React + Vite

### 7.1 Páginas implementadas

| Ruta | Descripción |
|---|---|
| `/` | Home con estadísticas y reportes recientes |
| `/mascotas` | Lista con filtros (estado, tipo, tamaño, raza) |
| `/mascotas/:id` | Detalle + coincidencias del motor |
| `/reportar` | Formulario de reporte (requiere login) |
| `/mapa` | Mapa SVG interactivo con pins por estado |
| `/login` | Login + Registro de cuenta |
| `/perfil` | Editar datos, cambiar contraseña, mis reportes |
| `/panel` | Moderación de encuentros (REFUGIO/MUNICIPALIDAD/ADMIN) |
| `/admin` | Gestión de usuarios y roles (solo ADMIN) |

### 7.2 Comunicación con el backend

El frontend usa Axios con proxy de Vite hacia el BFF:

```javascript
// vite.config.js
proxy: {
  '/api': { target: 'http://localhost:3005', changeOrigin: true }
}

// api.js — todas las peticiones van a /api/*
const API = axios.create({ baseURL: '/api', timeout: 8000 });
```

---

## 8. Pruebas Unitarias

### 8.1 Resumen de resultados

| Microservicio | Tests | Pasados | Cobertura |
|---|---|---|---|
| ms-gestion-mascotas | 18 | 18 ✅ | 69.4% |
| ms-motor-coincidencias | 15 | 15 ✅ | 78.9% |
| ms-usuarios-entidades | 21 | 21 ✅ | 77.9% |
| ms-geolocalizacion | 1 | 1 ✅ | 32.0% |
| **TOTAL** | **55** | **55** | **≥60% en 3/4** |

**Resultado: BUILD SUCCESS en los 4 microservicios. 0 fallos, 0 errores.**

### 8.2 Herramientas

- **JUnit 5** — framework de tests
- **Mockito 5** — mocking de dependencias (repositorios, eventos, JWT)
- **JaCoCo 0.8.12** — cobertura de código (compatible con Java 23)
- **maven-surefire-plugin** con flags `-Dnet.bytebuddy.experimental=true` para Java 23

### 8.3 Comandos para ejecutar pruebas

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

### 8.4 Ejemplos de tests destacados

**Test Observer — verifica que se publica el evento al registrar:**
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

**Test Factory Method — verifica creación correcta de alertas:**
```java
@Test
void testAlertaFactory_Perdida() {
    Alerta alerta = AlertaFactory.crear(EstadoMascota.PERDIDA, mockMascota);
    assertNotNull(alerta);
    assertEquals("AlertaExtravio", alerta.getTipo());
}
```

**Test Haversine — distancia geográfica real:**
```java
@Test
void testDistanciaKm_CoordenadaNula() {
    double dist = scoringAlgorithm.distanciaKm(null, -70.6693, -33.4489, -70.6693);
    assertEquals(Double.MAX_VALUE, dist); // coordenada null → MAX_VALUE
}
```

**Test AuthService — seguridad de roles:**
```java
@Test
void testRegister_NoPermiteRolAdmin() {
    // Registrarse como ADMIN debe resultar en rol DUENO
    LoginResponse resp = authService.register(req);
    assertNotEquals(Rol.ADMIN, resp.getUsuario().getRol());
}
```

### 8.5 Relación tests ↔ patrones de diseño

| Patrón | Test que lo verifica |
|---|---|
| Observer | `testRegistrarMascota_Success` — `verify(eventPublisher).publishEvent(...)` |
| Factory | `testAlertaFactory_Perdida/Encontrada/Reunificada` |
| Repository | Todos los tests — mocks de JPA Repository |
| Circuit Breaker | Validado manualmente en BFF |

---

## 9. Estrategia de Branching — Git Flow

El proyecto adoptó **Git Flow** como estrategia de control de versiones.

### Ramas principales

| Rama | Propósito |
|---|---|
| `main` | Código estable de producción. Solo recibe merges desde `release/*` |
| `develop` | Integración continua. Todas las features se integran aquí primero |

### Ramas de corto plazo (se eliminan tras el merge)

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
feat(ms-mascotas): implementa patron Factory para alertas
feat(bff): Circuit Breaker con estados CLOSED/OPEN/HALF_OPEN
fix(vite): actualiza proxy a puerto 3005 del BFF
test(ms-motor): agrega pruebas algoritmo Haversine
docs: actualiza README con comandos Maven
```

---

## 10. Cómo Ejecutar el Proyecto

### Requisitos previos

- Java 23 (JDK)
- Apache Maven 3.8+ (en PATH)
- Node.js 18+
- XAMPP con MySQL activo

### Pasos

**1. Base de datos**
1. Abrir XAMPP → activar MySQL
2. Ir a phpMyAdmin: `http://localhost/phpmyadmin`
3. Importar `database/setup.sql`

**2. Iniciar todo (doble clic en `start.bat`)**

El script libera los puertos, arranca los 5 microservicios y el frontend automáticamente.

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

## 11. Repositorios GitHub

Ver archivo `repositorios.txt` para los enlaces actualizados.

| Repositorio | Contenido |
|---|---|
| Proyecto Principal | Documentación, database, scripts, README |
| Frontend | Código React/Vite, package.json, src/ |
| Microservicios | Los 5 servicios Spring Boot con pom.xml y tests |

---

## 12. Conclusión

El proyecto "Sanos y Salvos" implementa una arquitectura de microservicios completamente funcional que responde al problema de fragmentación de información sobre mascotas extraviadas.

**Lo implementado:**
- 5 microservicios Spring Boot en Java 23 con Spring Data JPA y MySQL real
- Frontend React conectado al BFF mediante proxy Vite
- Autenticación JWT con RBAC (5 roles)
- 4 patrones de diseño en código real: Repository, Factory Method, Observer, Circuit Breaker
- 55 tests unitarios con BUILD SUCCESS y cobertura ≥60% en 3/4 microservicios
- Scripts de arranque automatizados (`start.bat`)
- Postman Collection con todos los endpoints documentados

**Evolución respecto al parcial anterior:**
- Se migró de Node.js a Java 23 / Spring Boot 3.2.5
- Se reemplazó Gradle por Maven
- Se implementó el Circuit Breaker propio en el BFF (sin Resilience4j)
- Se completó la autenticación JWT y los roles RBAC
- Se implementó el algoritmo Haversine real en el motor de coincidencias
- Se agregaron 55 tests unitarios con JaCoCo configurado al 60%

---

*Informe preparado para Evaluación Parcial N°3 — DSY1106 Desarrollo Fullstack III · DuocUC 2026*
