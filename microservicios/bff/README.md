# BFF — Backend for Frontend

**Proyecto Fullstack III · DuocUC 2026**  
**Sanos y Salvos — Plataforma de mascotas perdidas y encontradas**

Microservicio de tipo **BFF (Backend for Frontend)** que actúa como punto de entrada único para el frontend React. Implementa el patrón **Circuit Breaker** para tolerancia a fallos en la comunicación con los demás microservicios.

---

## Tecnologías utilizadas

| Tecnología       | Versión  | Propósito                                  |
|------------------|----------|--------------------------------------------|
| Java             | 17       | Lenguaje principal                         |
| Spring Boot      | 3.x      | Framework backend                          |
| Spring Web       | —        | REST Controller y proxy HTTP               |
| Maven            | 3.8+     | Gestión de dependencias y build            |

---

## Puerto

```
http://localhost:3005
```

---

## Patrón de diseño implementado

### Circuit Breaker
Ubicación: `src/main/java/.../pattern/CircuitBreaker.java`

Protege al frontend de fallos en cascada cuando un microservicio no responde. Tiene tres estados:
- **CLOSED**: operación normal, las peticiones pasan
- **OPEN**: microservicio caído, retorna error inmediato sin reintentar
- **HALF_OPEN**: prueba si el servicio se recuperó antes de reabrir

---

## Requisitos previos

- Java 17+ (JDK)
- Apache Maven 3.8+
- Los 4 microservicios backend corriendo en sus puertos respectivos

---

## Instalación y ejecución

```bash
# Desde la raíz del repositorio de microservicios
cd bff
mvn spring-boot:run
```

El servicio arranca en **http://localhost:3005**

---

## Compilar sin ejecutar

```bash
mvn clean package
```

El `.jar` queda en `target/bff-*.jar`.

---

## Rutas que proxifica

| Ruta del BFF              | Redirige a                                      |
|---------------------------|-------------------------------------------------|
| `/api/mascotas/**`        | `http://localhost:3001/api/mascotas/**`         |
| `/api/geolocalizacion/**` | `http://localhost:3002/api/geolocalizacion/**`  |
| `/api/motor/**`           | `http://localhost:3003/api/motor/**`            |
| `/api/auth/**`            | `http://localhost:3004/api/auth/**`             |
| `/api/status`             | Estado de todos los microservicios              |

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
server.port=3005

microservicios.usuarios.url=http://localhost:3004
microservicios.mascotas.url=http://localhost:3001
microservicios.geolocalizacion.url=http://localhost:3002
microservicios.motor.url=http://localhost:3003
```

---

## Estructura del proyecto

```
bff/
├── pom.xml
└── src/
    └── main/
        ├── java/com/sanosysalvos/bff/
        │   ├── BffApplication.java         ← Punto de entrada
        │   ├── config/
        │   │   └── WebMvcConfig.java        ← Configuración CORS
        │   ├── controller/
        │   │   └── ProxyController.java     ← Proxy a microservicios
        │   └── pattern/
        │       └── CircuitBreaker.java      ← Patrón Circuit Breaker
        └── resources/
            └── application.properties
```

---

## Health check

```
GET http://localhost:3005/api/status
```

Retorna el estado de cada microservicio conectado.
