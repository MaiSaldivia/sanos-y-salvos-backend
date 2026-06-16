# Análisis de Patrones de Diseño y Arquetipos Arquitectónicos
## Proyecto "Sanos y Salvos" — Fullstack III · DuocUC 2026

**Estudiantes:** Bastián Martínez Flores · Maicol Saldivia Silva  
**Docente:** Daniel Williams Concha Saavedra · Sección 002D  
**Stack:** Java 23 · Spring Boot 3.2.5 · MySQL · React + Vite

---

## 1. Resumen

Este documento describe los patrones de diseño y arquetipos arquitectónicos implementados en "Sanos y Salvos", justificando cada decisión técnica en función del problema que resuelve. Todos los patrones están implementados en código Java real, no como pseudocódigo ni comentarios.

---

## 2. Patrones de Diseño Implementados

### 2.1 Patrón Repository

**Implementación:** Spring Data JPA — todos los microservicios

**Archivos:**
- `ms-gestion-mascotas` → `MascotaRepository.java`, `ReporteEncuentroRepository.java`
- `ms-geolocalizacion` → `GeolocRepository.java`
- `ms-motor-coincidencias` → `MascotaRepository.java`, `MotorRepository.java`
- `ms-usuarios-entidades` → `UsuarioRepository.java`

**Problema que resuelve:**
Sin este patrón, el código de negocio quedaría mezclado con consultas SQL directas. Un cambio en el esquema de base de datos obligaría a modificar múltiples capas del sistema. Las pruebas unitarias no podrían ejecutarse sin una base de datos real.

**Cómo se aplica:**
Cada microservicio tiene su propia interfaz Repository que extiende `JpaRepository`. El servicio llama exclusivamente a los métodos del Repository, sin escribir ninguna consulta SQL directamente.

```
Service → Repository (interfaz JPA) → Spring Data → MySQL
```

**Ejemplo concreto:**

```java
// MascotaRepository.java
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

// MascotasService.java — el servicio no sabe nada de SQL
public List<MascotaResponse> buscarConFiltros(String estado, ...) {
    return mascotaRepository.findWithFilters(estadoEnum, tipo, tamano, raza)
            .stream().map(MascotaResponse::new).collect(Collectors.toList());
}
```

**Beneficios obtenidos:**
- Los 40 tests unitarios mockean los repositorios sin necesitar MySQL activo
- Cambiar MySQL por otro motor solo requiere modificar la configuración JPA, no el código de negocio
- Código de servicio limpio y legible

---

### 2.2 Patrón Factory Method

**Implementación:** `ms-gestion-mascotas`

**Archivos:**
```
ms-gestion-mascotas/src/main/java/com/sanosysalvos/mascotas/pattern/factory/
├── Alerta.java              ← interfaz base
├── AlertaFactory.java       ← factory central
├── AlertaExtravio.java      ← estado PERDIDA
├── AlertaHallazgo.java      ← estado ENCONTRADA
└── AlertaReunificacion.java ← estado REUNIFICADA
```

**Problema que resuelve:**
Al reportar o actualizar el estado de una mascota, el sistema genera un tipo de alerta diferente según el estado. Sin Factory Method, habría condicionales `if/else` dispersos en múltiples clases, haciendo difícil agregar nuevos tipos de alerta sin romper el código existente.

**Jerarquía de clases:**
```
Alerta (interfaz)
├── AlertaExtravio    → estado "PERDIDA"
├── AlertaHallazgo    → estado "ENCONTRADA"
└── AlertaReunificacion → estado "REUNIFICADA"
```

**Cómo se aplica:**
`AlertaFactory.crear(estado, mascota)` recibe el estado y retorna la instancia correcta. El código cliente nunca instancia una subclase directamente.

```java
// AlertaFactory.java
public class AlertaFactory {
    public static Alerta crear(EstadoMascota estado, Mascota mascota) {
        return switch (estado) {
            case PERDIDA     -> new AlertaExtravio(mascota);
            case ENCONTRADA  -> new AlertaHallazgo(mascota);
            case REUNIFICADA -> new AlertaReunificacion(mascota);
        };
    }
}

// Uso en MascotaEventListener.java
Alerta alerta = AlertaFactory.crear(mascota.getEstado(), mascota);
System.out.println(alerta.getMensaje());
```

**Beneficios:**
- Agregar un nuevo estado (ej: `EN_ADOPCION`) solo requiere una nueva subclase
- Código sin condicionales dispersos
- Cada tipo de alerta tiene comportamiento propio via polimorfismo

---

### 2.3 Patrón Observer (Spring Events)

**Implementación:** `ms-gestion-mascotas`

**Archivos:**
```
ms-gestion-mascotas/src/main/java/com/sanosysalvos/mascotas/pattern/observer/
├── MascotaReportadaEvent.java       ← evento al crear reporte
├── MascotaEstadoCambiadoEvent.java  ← evento al cambiar estado
├── MascotaReunificadaEvent.java     ← evento al reunificar
└── MascotaEventListener.java        ← suscriptor que procesa eventos
```

**Problema que resuelve:**
Cuando una mascota cambia de estado o se reporta, el sistema debe ejecutar acciones secundarias (logging, notificaciones, etc.). Sin Observer, el servicio principal tendría que conocer y llamar directamente a cada acción, generando acoplamiento fuerte.

**Cómo se aplica:**
Se usa el mecanismo nativo de Spring (`ApplicationEventPublisher` + `@EventListener`). El servicio publica un evento y los listeners reaccionan de forma independiente y desacoplada.

```java
// MascotasService.java — publica sin saber quién escucha
@Transactional
public MascotaResponse registrarMascota(MascotaRequest request, String userId) {
    Mascota saved = mascotaRepository.save(mascota);
    eventPublisher.publishEvent(new MascotaReportadaEvent(saved)); // Observer
    return new MascotaResponse(saved);
}

// MascotaEventListener.java — suscriptor desacoplado
@Component
public class MascotaEventListener {
    @EventListener
    public void onMascotaReportada(MascotaReportadaEvent event) {
        // lógica secundaria sin acoplar al servicio principal
    }
}
```

**Flujo:**
```
MascotasService.registrarMascota()
       │
       └── eventPublisher.publishEvent(MascotaReportadaEvent)
               │
               └── MascotaEventListener.onMascotaReportada()
                       └── Factory Method: AlertaFactory.crear(estado, mascota)
```

**Beneficios:**
- `MascotasService` no conoce las acciones secundarias
- Agregar una nueva reacción no requiere modificar el servicio principal
- Los patrones Observer y Factory se combinan naturalmente

---

### 2.4 Patrón Circuit Breaker

**Implementación:** `bff` (Backend For Frontend)

**Archivo:** `bff/src/main/java/com/sanosysalvos/bff/pattern/CircuitBreaker.java`

**Problema que resuelve:**
En arquitectura de microservicios, si `ms-motor-coincidencias` se cae, el BFF seguiría enviando peticiones acumulando timeouts y agotando recursos, pudiendo derribar el BFF entero (fallo en cascada).

**Estados implementados:**
```
CLOSED (operación normal)
   │
   ├── 3 fallos consecutivos
   ↓
OPEN (bloqueado — retorna fallback inmediato)
   │
   ├── 10 segundos de espera
   ↓
HALF_OPEN (prueba de recuperación)
   │
   ├── éxito → CLOSED
   └── fallo → OPEN
```

**Código real:**

```java
// CircuitBreaker.java
@Component
public class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    private final int failureThreshold = 3;
    private final long resetTimeout = 10000; // 10 segundos

    public <T> T execute(String serviceName,
                         Supplier<T> action,
                         Supplier<T> fallback) {
        CircuitState circuit = circuits.computeIfAbsent(serviceName,
                                   k -> new CircuitState());
        synchronized (circuit) {
            if (circuit.state == State.OPEN) {
                if (System.currentTimeMillis() - circuit.lastFailureTime > resetTimeout) {
                    circuit.state = State.HALF_OPEN; // prueba recuperación
                } else {
                    return fallback.get(); // retorna respuesta de emergencia
                }
            }
        }
        try {
            T result = action.get();
            // éxito → cierra el circuito
            if (circuit.state == State.HALF_OPEN) circuit.state = State.CLOSED;
            return result;
        } catch (Exception e) {
            circuit.failureCount++;
            if (circuit.failureCount >= failureThreshold) circuit.state = State.OPEN;
            return fallback.get();
        }
    }
}
```

**Uso en ProxyController:**
```java
return circuitBreaker.execute(
    serviceName,
    () -> restTemplate.exchange(URI.create(fullUrl), method, httpEntity, byte[].class),
    () -> ResponseEntity.status(503).body("Servicio no disponible".getBytes())
);
```

**Beneficios:**
- Fallo en un microservicio no derriba los demás
- Respuesta inmediata con HTTP 503 en lugar de timeout infinito
- Auto-recuperación tras el tiempo de reset

---

## 3. Arquetipos Arquitectónicos

### 3.1 Arquitectura de Microservicios

```
Frontend React (Puerto 5173)
         │  HTTP /api/*
         ▼
     BFF — Puerto 3005
     (Circuit Breaker por servicio)
         │
    ┌────┼────────┬──────────┐
    ▼    ▼        ▼          ▼
 :3001 :3002   :3003      :3004
 ms-   ms-geo  ms-motor   ms-
 mascotas       coincid.   usuarios
    │    │        │          │
    └────┴────────┴──────────┘
                 │
          MySQL :3306
         sanos_y_salvos_db
```

### 3.2 Backend For Frontend (BFF)

**Puerto:** 3005

El BFF es el único punto de entrada del frontend. Su función es:
1. **Enrutar** peticiones al microservicio correcto según el path `/api/*`
2. **Proteger** cada llamada downstream con Circuit Breaker
3. **Filtrar headers** problemáticos (`Transfer-Encoding`, `Connection`) para evitar errores de proxy

Mapeo de rutas:

| Path del frontend | Microservicio destino | Puerto |
|---|---|---|
| `/api/auth/**` | ms-usuarios-entidades | 3004 |
| `/api/mascotas/**` | ms-gestion-mascotas | 3001 |
| `/api/geolocalizacion/**` | ms-geolocalizacion | 3002 |
| `/api/motor/**` | ms-motor-coincidencias | 3003 |

### 3.3 Separación Frontend / Backend

Frontend React (Vite, puerto 5173) y microservicios Spring Boot son procesos completamente independientes. Se comunican únicamente via HTTP a través del BFF. El proxy de Vite redirige `/api/*` → `http://localhost:3005`.

---

## 4. Persistencia de Datos — Spring Data JPA

**Base de datos:** MySQL 8.x (XAMPP, puerto 3306)  
**Base de datos:** `sanos_y_salvos_db`  
**ORM:** Hibernate (incluido en Spring Data JPA)

### Tablas del esquema

| Tabla | Microservicio | Descripción |
|---|---|---|
| `usuarios` | ms-usuarios-entidades | Cuentas, roles, JWT |
| `mascotas` | ms-gestion-mascotas, ms-motor | Reportes de mascotas |
| `reportes_encuentro` | ms-gestion-mascotas | Solicitudes de reunificación |
| `coincidencias` | ms-motor-coincidencias | Resultado del algoritmo de matching |
| `zonas_criticas` | ms-geolocalizacion | Zonas geográficas de alta actividad |

### Configuración JPA por microservicio

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.open-in-view=false
```

`ddl-auto=update` permite que Hibernate actualice el esquema automáticamente sin perder datos al reiniciar.

---

## 5. Resumen de tecnologías

| Componente | Tecnología | Versión |
|---|---|---|
| Backend microservicios | Java + Spring Boot | 23 / 3.2.5 |
| ORM | Spring Data JPA / Hibernate | 6.4.x |
| Base de datos | MySQL (XAMPP) | 8.x |
| Frontend | React + Vite | 18 / 5.x |
| HTTP cliente | Axios | 1.x |
| Autenticación | JWT (jjwt) | 0.11.5 |
| Tests | JUnit 5 + Mockito | 5.x / 5.x |
| Cobertura | JaCoCo | 0.8.12 |
| Build | Apache Maven | 3.9.16 |

---

*Documento preparado para Evaluación Parcial N°2 — DSY1106 Desarrollo Fullstack III*
