# ms-motor-coincidencias

**Proyecto Fullstack III · DuocUC 2026**  
**Sanos y Salvos — Plataforma de mascotas perdidas y encontradas**

Microservicio que implementa el algoritmo de coincidencias entre mascotas perdidas y encontradas. Calcula un **score de similitud** basado en tipo de animal, raza, color, tamaño y distancia geográfica. Implementa el patrón **Strategy** para el algoritmo de scoring.

---

## Tecnologías utilizadas

| Tecnología        | Versión  | Propósito                              |
|-------------------|----------|----------------------------------------|
| Java              | 17       | Lenguaje principal                     |
| Spring Boot       | 3.x      | Framework backend                      |
| Spring Data JPA   | —        | Persistencia con Hibernate             |
| MySQL             | 8.x      | Base de datos relacional               |
| Bean Validation   | —        | Validación de parámetros de consulta   |
| Lombok            | —        | Reducción de boilerplate               |
| JaCoCo            | —        | Cobertura de pruebas unitarias         |
| JUnit 5 + Mockito | —        | Framework de testing                   |
| Maven             | 3.8+     | Gestión de dependencias y build        |

---

## Puerto

```
http://localhost:3003
```

---

## Patrón de diseño implementado

### Strategy (Algoritmo de Scoring)
Ubicación: `src/main/java/.../pattern/ScoringAlgorithm.java`

Encapsula el algoritmo de cálculo de similitud entre dos mascotas en una clase intercambiable. El score (0-100) se calcula sumando pesos por:
- Tipo de animal coincidente: +40 puntos
- Raza coincidente: +20 puntos
- Color coincidente: +15 puntos
- Tamaño coincidente: +10 puntos
- Distancia geográfica (Haversine): hasta +15 puntos según cercanía

---

## Requisitos previos

- Java 17+ (JDK)
- Apache Maven 3.8+
- XAMPP con MySQL activo en puerto 3306
- Base de datos `sanos_y_salvos_db` con datos (ver `database/setup.sql`)

---

## Instalación y ejecución

```bash
# Desde la raíz del repositorio de microservicios
cd ms-motor-coincidencias
mvn spring-boot:run
```

El servicio arranca en **http://localhost:3003**

---

## Compilar sin ejecutar

```bash
mvn clean package
```

---

## Endpoints principales

| Método | Ruta                          | Descripción                                          | Parámetros opcionales                 |
|--------|-------------------------------|------------------------------------------------------|---------------------------------------|
| GET    | `/api/motor/buscar/{idMascota}` | Busca coincidencias para una mascota              | `maxKm`, `minScore`, `limite`         |
| GET    | `/api/motor/historial`        | Últimas coincidencias calculadas                     | `limite` (1-100, default 20)          |
| GET    | `/api/motor/resumen`          | Estadísticas del motor                               | —                                     |

**Ejemplo de petición:**
```
GET /api/motor/buscar/msc-001?maxKm=10&minScore=50&limite=5
```

**Ejemplo de respuesta:**
```json
{
  "idMascotaBuscada": "msc-001",
  "resultados": [
    {
      "idMascota": "msc-002",
      "nombre": "Luna",
      "tipoAnimal": "Perro",
      "score": 75,
      "distanciaKm": 2.3
    }
  ]
}
```

### Validaciones de parámetros

| Parámetro  | Rango válido    | Mensaje de error                               |
|------------|-----------------|------------------------------------------------|
| `maxKm`    | 0.1 – 500.0 km  | "La distancia máxima debe ser al menos 0.1 km" |
| `minScore` | 0 – 100         | "El score mínimo debe ser 0 o mayor"           |
| `limite`   | 1 – 50          | "El límite debe ser al menos 1"                |

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
server.port=3003
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
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test                                  | Descripción                                              |
|---------------------------------------|----------------------------------------------------------|
| testBuscarCoincidencias_PerdidaVsEncontrada | Empareja PERDIDA con ENCONTRADA del mismo tipo    |
| testBuscarCoincidencias_SinResultados | Retorna lista vacía si no hay candidatos               |
| testBuscarCoincidencias_FiltroMaxKm   | Filtra por distancia máxima correctamente              |
| testBuscarCoincidencias_FiltroScore   | Filtra por score mínimo correctamente                  |
| testBuscarCoincidencias_Limite        | Respeta el límite de resultados                        |
| testBuscarCoincidencias_NoExiste      | Lanza 404 si la mascota no existe                      |
| testGetHistorial                      | Retorna lista del historial                            |
| testGetResumen                        | Retorna estadísticas del motor                         |
| testScoring_TipoIgual                 | +40 si el tipo de animal coincide                      |
| testScoring_RazaIgual                 | +20 adicionales si la raza coincide                    |
| testScoring_ColorIgual                | +15 adicionales si el color coincide                   |
| testScoring_DistanciaCercana          | Score máximo si la distancia es menor a 1 km           |
| testScoring_DistanciaLejana           | Score bajo si la distancia supera el umbral            |

---

## Generar reporte de cobertura JaCoCo

```bash
mvn test jacoco:report
```

El reporte HTML se genera en: `target/site/jacoco/index.html`

---

## Estructura del proyecto

```
ms-motor-coincidencias/
├── pom.xml
└── src/
    ├── main/java/com/sanosysalvos/motor/
    │   ├── MotorApplication.java
    │   ├── controller/       ← MotorController (REST + validaciones @Validated)
    │   ├── dto/              ← BusquedaResultDto, CandidatoDto, HistorialDto, ResumenDto
    │   ├── model/            ← Coincidencia, Mascota (@Immutable, solo lectura)
    │   ├── pattern/
    │   │   └── ScoringAlgorithm.java  ← Patrón Strategy
    │   ├── repository/       ← MascotaRepository, MotorRepository
    │   └── service/          ← MotorService (lógica del motor)
    ├── main/resources/
    │   └── application.properties
    └── test/java/com/sanosysalvos/motor/
        └── service/
            └── MotorServiceTest.java
```
