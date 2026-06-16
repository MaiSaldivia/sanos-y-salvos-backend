# ms-geolocalizacion

**Proyecto Fullstack III · DuocUC 2026**  
**Sanos y Salvos — Plataforma de mascotas perdidas y encontradas**

Microservicio encargado de calcular y exponer datos geoespaciales del sistema: puntos de mapa, zonas críticas de mayor actividad y datos estadísticos geográficos. Es consumido por el módulo de mapa de calor del frontend.

---

## Tecnologías utilizadas

| Tecnología        | Versión  | Propósito                              |
|-------------------|----------|----------------------------------------|
| Java              | 17       | Lenguaje principal                     |
| Spring Boot       | 3.x      | Framework backend                      |
| Spring Data JPA   | —        | Consultas geoespaciales con Hibernate  |
| MySQL             | 8.x      | Base de datos relacional               |
| Lombok            | —        | Reducción de boilerplate               |
| JaCoCo            | —        | Cobertura de pruebas unitarias         |
| JUnit 5 + Mockito | —        | Framework de testing                   |
| Maven             | 3.8+     | Gestión de dependencias y build        |

---

## Puerto

```
http://localhost:3002
```

---

## Patrón de diseño implementado

### Repository
Ubicación: `src/main/java/.../repository/GeolocRepository.java`

Aísla toda la lógica de acceso a datos geoespaciales. Expone métodos de consulta personalizados (puntos de mapa, agrupación por comuna, cálculo de zonas de calor) sin que el servicio conozca detalles de SQL o JPA.

---

## Requisitos previos

- Java 17+ (JDK)
- Apache Maven 3.8+
- XAMPP con MySQL activo en puerto 3306
- Base de datos `sanos_y_salvos_db` con datos cargados (ver `database/setup.sql`)

---

## Instalación y ejecución

```bash
# Desde la raíz del repositorio de microservicios
cd ms-geolocalizacion
mvn spring-boot:run
```

El servicio arranca en **http://localhost:3002**

---

## Compilar sin ejecutar

```bash
mvn clean package
```

---

## Endpoints principales

| Método | Ruta                              | Descripción                                        |
|--------|-----------------------------------|----------------------------------------------------|
| GET    | `/api/geolocalizacion/puntos`     | Coordenadas de todas las mascotas para el mapa     |
| GET    | `/api/geolocalizacion/zonas-criticas` | Comunas con mayor cantidad de reportes         |
| GET    | `/api/geolocalizacion/mapa-calor` | Puntos agrupados para el mapa de calor             |
| GET    | `/api/geolocalizacion/estadisticas` | Estadísticas geográficas generales               |

**Ejemplo de respuesta — Puntos del mapa:**
```json
[
  {
    "idMascota": "msc-001",
    "latitud": -36.8201,
    "longitud": -73.0444,
    "estado": "PERDIDA",
    "tipoAnimal": "Perro"
  },
  {
    "idMascota": "msc-002",
    "latitud": -36.8301,
    "longitud": -73.0544,
    "estado": "ENCONTRADA",
    "tipoAnimal": "Gato"
  }
]
```

**Ejemplo de respuesta — Zonas críticas:**
```json
[
  { "comuna": "Concepción", "totalReportes": 12 },
  { "comuna": "Hualpén",    "totalReportes": 5  }
]
```

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
server.port=3002
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
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test                  | Descripción                                          |
|-----------------------|------------------------------------------------------|
| testZonasCalor        | Agrupa mascotas por coordenadas y retorna zonas calor|

---

## Generar reporte de cobertura JaCoCo

```bash
mvn test jacoco:report
```

El reporte HTML se genera en: `target/site/jacoco/index.html`

---

## Estructura del proyecto

```
ms-geolocalizacion/
├── pom.xml
└── src/
    ├── main/java/com/sanosysalvos/geolocalizacion/
    │   ├── GeolocalizacionApplication.java
    │   ├── controller/       ← GeolocController (REST)
    │   ├── dto/              ← PuntoMapaDto, ZonaCriticaDto, ZonaCalorDto, EstadisticasGeoDto
    │   ├── model/            ← MascotaGeo (entidad JPA @Immutable)
    │   ├── repository/       ← GeolocRepository (Spring Projections)
    │   └── service/          ← GeolocService (lógica geoespacial)
    ├── main/resources/
    │   └── application.properties
    └── test/java/com/sanosysalvos/geolocalizacion/
        └── service/
            └── GeolocServiceTest.java
```
