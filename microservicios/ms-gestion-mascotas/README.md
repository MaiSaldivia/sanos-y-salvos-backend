# ms-gestion-mascotas

**Proyecto Fullstack III · DuocUC 2026**  
**Sanos y Salvos — Plataforma de mascotas perdidas y encontradas**

Microservicio principal del sistema. Gestiona el ciclo de vida completo de los reportes de mascotas: registro, búsqueda, actualización, eliminación y el flujo de reporte/revisión de encuentros. Implementa los patrones **Factory Method** y **Observer**.

---

## Tecnologías utilizadas

| Tecnología        | Versión  | Propósito                              |
|-------------------|----------|----------------------------------------|
| Java              | 17       | Lenguaje principal                     |
| Spring Boot       | 3.x      | Framework backend                      |
| Spring Data JPA   | —        | Persistencia con Hibernate             |
| MySQL             | 8.x      | Base de datos relacional               |
| Bean Validation   | —        | Validación de entradas (@Valid)        |
| Lombok            | —        | Reducción de boilerplate               |
| JaCoCo            | —        | Cobertura de pruebas unitarias         |
| JUnit 5 + Mockito | —        | Framework de testing                   |
| Maven             | 3.8+     | Gestión de dependencias y build        |

---

## Puerto

```
http://localhost:3001
```

---

## Patrones de diseño implementados

### Factory Method
Ubicación: `src/main/java/.../pattern/factory/`

Crea distintos tipos de alertas según el estado de la mascota:
- `AlertaExtravio` → cuando el estado es `PERDIDA`
- `AlertaHallazgo` → cuando el estado es `ENCONTRADA`
- `AlertaReunificacion` → cuando el estado es `REUNIFICADA`

### Observer (Spring Events)
Ubicación: `src/main/java/.../pattern/observer/`

Publica eventos de dominio cuando cambia el estado de una mascota:
- `MascotaReportadaEvent` → al registrar un nuevo reporte
- `MascotaReunificadaEvent` → al aprobar un encuentro

---

## Requisitos previos

- Java 17+ (JDK)
- Apache Maven 3.8+
- XAMPP con MySQL activo en puerto 3306
- Base de datos `sanos_y_salvos_db` creada (ver `database/setup.sql`)

---

## Instalación y ejecución

```bash
# Desde la raíz del repositorio de microservicios
cd ms-gestion-mascotas
mvn spring-boot:run
```

El servicio arranca en **http://localhost:3001**

---

## Compilar sin ejecutar

```bash
mvn clean package
```

---

## Endpoints principales

| Método | Ruta                                  | Descripción                             | Auth |
|--------|---------------------------------------|-----------------------------------------|------|
| GET    | `/api/mascotas`                       | Listar todas las mascotas               | No   |
| GET    | `/api/mascotas/busqueda`              | Buscar con filtros (estado, tipo, etc.) | No   |
| GET    | `/api/mascotas/{id}`                  | Obtener mascota por ID                  | No   |
| GET    | `/api/mascotas/estadisticas`          | Totales por estado                      | No   |
| POST   | `/api/mascotas/reportar`              | Registrar nueva mascota                 | Sí   |
| PUT    | `/api/mascotas/{id}`                  | Actualizar mascota                      | Sí   |
| DELETE | `/api/mascotas/{id}`                  | Eliminar mascota                        | Sí   |
| GET    | `/api/mascotas/usuario/mis-reportes`  | Mascotas del usuario autenticado        | Sí   |
| POST   | `/api/mascotas/{id}/reportar-encuentro` | Reportar que encontró la mascota      | Sí   |
| GET    | `/api/mascotas/encuentros/revision`   | Listar encuentros pendientes            | No   |
| PUT    | `/api/mascotas/encuentros/revision/{id}` | Aprobar o rechazar encuentro         | Sí   |

**Ejemplo de petición — Registrar mascota:**
```json
POST /api/mascotas/reportar
Authorization: Bearer <token>

{
  "tipoAnimal": "Perro",
  "nombre": "Firulais",
  "raza": "Labrador",
  "colorPrimario": "Amarillo",
  "tamano": "Grande",
  "estado": "PERDIDA",
  "sector": "Centro",
  "comuna": "Concepción",
  "direccion": "Barros Arana 450",
  "contacto": "dueno@ejemplo.com",
  "latitud": -36.8201,
  "longitud": -73.0444
}
```

**Ejemplo de respuesta:**
```json
{
  "idMascota": "uuid-generado",
  "tipoAnimal": "Perro",
  "nombre": "Firulais",
  "estado": "PERDIDA",
  "fechaReporte": "2026-06-15T18:00:00"
}
```

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
server.port=3001
spring.datasource.url=jdbc:mysql://localhost:3306/sanos_y_salvos_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
jwt.secret=sanos-y-salvos-secret-2026
```

---

## Pruebas unitarias

```bash
mvn test
```

Resultado esperado:
```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test                                  | Descripción                                      |
|---------------------------------------|--------------------------------------------------|
| testRegistrarMascota_Success          | Registra correctamente y dispara evento Observer |
| testRegistrarMascota_CoordenadasNull  | Usa coordenadas por defecto si vienen null       |
| testObtenerPorId_Success              | Retorna mascota existente                        |
| testObtenerPorId_NotFound             | Lanza excepción si no existe                     |
| testObtenerTodas                      | Retorna lista completa                           |
| testObtenerPorUsuario                 | Filtra por usuario                               |
| testGetEstadisticas                   | Retorna totales correctos                        |
| testActualizarMascota_Success         | Actualiza si el usuario es dueño                 |
| testActualizarMascota_PermisoDenegado | Lanza 403 si no es el dueño                      |
| testEliminarMascota_Success           | Elimina si el usuario es dueño                   |
| testEliminarMascota_PermisoDenegado   | Lanza 403 si no es el dueño                      |
| testReportarEncuentro_Success         | Guarda el reporte correctamente                  |
| testRevisarEncuentro_Aprobar          | Marca mascota como REUNIFICADA y dispara evento  |
| testRevisarEncuentro_Rechazar         | Cambia estado sin tocar la mascota               |
| testAlertaFactory_Perdida             | Crea AlertaExtravio para estado PERDIDA          |

---

## Generar reporte de cobertura JaCoCo

```bash
mvn test jacoco:report
```

El reporte HTML se genera en: `target/site/jacoco/index.html`

---

## Estructura del proyecto

```
ms-gestion-mascotas/
├── pom.xml
└── src/
    ├── main/java/com/sanosysalvos/mascotas/
    │   ├── MascotasApplication.java
    │   ├── config/           ← JWT, interceptores, CORS
    │   ├── controller/       ← MascotasController (REST)
    │   ├── dto/              ← Request y Response DTOs con validaciones
    │   ├── model/            ← Entidades JPA (Mascota, ReporteEncuentro)
    │   ├── pattern/
    │   │   ├── factory/      ← AlertaFactory, AlertaExtravio, etc.
    │   │   └── observer/     ← Eventos Spring y MascotaEventListener
    │   ├── repository/       ← JPA Repositories
    │   └── service/          ← MascotasService (lógica de negocio)
    ├── main/resources/
    │   └── application.properties
    └── test/java/com/sanosysalvos/mascotas/
        └── service/
            └── MascotasServiceTest.java
```
