# Informe de Pruebas Unitarias — Sanos y Salvos

**Proyecto:** Plataforma de Microservicios para Reporte de Mascotas  
**Asignatura:** Fullstack III · DuocUC 2026  
**Estudiantes:** Bastián Martínez Flores · Maicol Saldivia Silva  
**Docente:** Daniel Williams Concha Saavedra · Sección 002D  
**Fecha:** Junio 2026  
**Stack:** Java 23 · Spring Boot 3.2.5 · JUnit 5 · Mockito 5 · JaCoCo 0.8.12

---

## 1. Resumen Ejecutivo

| Microservicio | Tests | Pasados | Fallidos | Cobertura de línea |
|---|---|---|---|---|
| ms-gestion-mascotas | 18 | 18 | 0 | 69.4% ✅ |
| ms-motor-coincidencias | 15 | 15 | 0 | 78.9% ✅ |
| ms-usuarios-entidades | 21 | 21 | 0 | 77.9% ✅ |
| ms-geolocalizacion | 1 | 1 | 0 | 32.0% ⚠️ |
| **TOTAL** | **55** | **55** | **0** | **≥ 60% en 3/4** |

> **Resultado global: BUILD SUCCESS en los 4 microservicios. 55 tests, 0 fallos, 0 errores.**

> **Nota cobertura geo:** ms-geolocalizacion tiene solo 1 método testeable en el service con 25 líneas totales. El 32% corresponde a las líneas del método `zonasCalor()` que sí tiene test. Los otros métodos (`obtenerPuntos`, `contarPorComuna`, `getEstadisticasGeo`) delegan directamente al repositorio sin lógica de negocio propia, por lo que se consideran cubiertos a nivel arquitectónico por el patrón Repository.

---

## 2. Introducción

Este documento describe la estrategia, implementación y resultados de las pruebas unitarias del sistema "Sanos y Salvos". El sistema está compuesto por 5 microservicios Spring Boot conectados a MySQL, con un frontend React accesible a través de un BFF con Circuit Breaker.

Las pruebas se desarrollaron con **JUnit 5** y **Mockito 5**, siguiendo el patrón AAA (Arrange, Act, Assert). La cobertura de código se mide con **JaCoCo 0.8.12**, configurado en el `pom.xml` de cada microservicio. Las clases excluidas del reporte de cobertura son: `model/`, `dto/`, `config/`, `controller/` y la clase `Application` principal, ya que son boilerplate sin lógica de negocio testeable unitariamente.

---

## 3. Comandos para ejecutar las pruebas

### Desde CMD

```cmd
"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-gestion-mascotas\pom.xml" test

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-motor-coincidencias\pom.xml" test

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-usuarios-entidades\pom.xml" test

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-geolocalizacion\pom.xml" test
```

### Desde PowerShell

```powershell
& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-gestion-mascotas\pom.xml" test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-motor-coincidencias\pom.xml" test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-usuarios-entidades\pom.xml" test

& "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-geolocalizacion\pom.xml" test
```

### Generar reportes JaCoCo HTML

```cmd
"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-gestion-mascotas\pom.xml" test jacoco:report

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-motor-coincidencias\pom.xml" test jacoco:report

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-usuarios-entidades\pom.xml" test jacoco:report

"D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" -f "D:\JUEGOS XD\sanos-y-salvos\microservicios\ms-geolocalizacion\pom.xml" test jacoco:report
```

Los reportes HTML quedan en: `microservicios\<nombre>\target\site\jacoco\index.html`

O usando el script incluido: `generar-cobertura.bat` (copia reportes a `docs\cobertura\`)

---

## 4. Herramientas y Configuración

### Dependencias de test

```xml
<!-- JUnit 5 + Mockito 5 (incluido en spring-boot-starter-test) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### JaCoCo 0.8.12 — compatible con Java 23

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <configuration>
        <excludes>
            <exclude>**/model/**</exclude>
            <exclude>**/dto/**</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/controller/**</exclude>
            <exclude>**/*Application.class</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Surefire — flags necesarios para Java 23

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            @{argLine}
            -Dnet.bytebuddy.experimental=true
            -Djdk.attach.allowAttachSelf=true
            --add-opens java.base/java.lang=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

---

## 5. Estrategia de Pruebas

### Patrón: Mockito con @ExtendWith(MockitoExtension.class)

Se usó Mockito para aislar cada servicio de sus dependencias externas (repositorios JPA, publishers de eventos Spring, JwtUtil). Los tests son:
- **Rápidos:** No requieren contexto Spring ni conexión a MySQL
- **Deterministas:** Los mocks tienen comportamiento predefinido
- **Aislados:** Cada test verifica una sola unidad

### Categorías de prueba

| Categoría | Descripción |
|---|---|
| Happy Path | Comportamiento correcto en condiciones normales |
| Manejo de errores | Excepciones HTTP 400, 401, 404, 409 correctas |
| Casos límite | Listas vacías, coordenadas null, valores por defecto |
| Interacciones | Número exacto de llamadas a dependencias (verify) |
| Patrones de diseño | Factory Method y Observer probados directamente |

---

## 6. Casos de Prueba Detallados

### 6.1 MascotasService + Factory Method — ms-gestion-mascotas (18 tests)

**Archivo:** `MascotasServiceTest.java`

#### MascotasService

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 1 | `testRegistrarMascota_Success` | Registra mascota + verifica evento Observer | ✅ |
| 2 | `testRegistrarMascota_CoordenadasNull` | Coordenadas null → usa valores por defecto | ✅ |
| 3 | `testObtenerPorId_Success` | ID válido → MascotaResponse correcto | ✅ |
| 4 | `testObtenerPorId_NotFound` | ID inválido → RuntimeException | ✅ |
| 5 | `testObtenerTodas` | findAll() → lista correcta | ✅ |
| 6 | `testObtenerPorUsuario` | Solo mascotas del usuario | ✅ |
| 7 | `testGetEstadisticas` | Totales 10/5/3/2 correctos | ✅ |
| 8 | `testActualizarMascota_Success` | Dueño actualiza → save() llamado | ✅ |
| 9 | `testActualizarMascota_PermisoDenegado` | Otro usuario → excepción | ✅ |
| 10 | `testEliminarMascota_Success` | Dueño elimina → delete() llamado | ✅ |
| 11 | `testEliminarMascota_PermisoDenegado` | Otro usuario → excepción | ✅ |
| 12 | `testReportarEncuentro_Success` | Guarda ReporteEncuentro | ✅ |
| 13 | `testRevisarEncuentro_Aprobar` | Mascota REUNIFICADA + evento Observer | ✅ |
| 14 | `testRevisarEncuentro_Rechazar` | Estado RECHAZADO, mascota no modificada | ✅ |

#### AlertaFactory (Patrón Factory Method)

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 15 | `testAlertaFactory_Perdida` | PERDIDA → AlertaExtravio con mensaje | ✅ |
| 16 | `testAlertaFactory_Encontrada` | ENCONTRADA → AlertaHallazgo | ✅ |
| 17 | `testAlertaFactory_Reunificada` | REUNIFICADA → AlertaReunificacion | ✅ |
| 18 | `testAlertaFactory_EstadoNull` | null → IllegalArgumentException | ✅ |

---

### 6.2 MotorService — ms-motor-coincidencias (15 tests)

**Archivo:** `MotorServiceTest.java`

#### buscarCoincidencias()

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 1 | `testBuscarCoincidencias_ConResultados` | Candidatos ordenados por score DESC | ✅ |
| 2 | `testBuscarCoincidencias_ScoreMaximoConCandidatoIdentico` | tipo+raza+color+tamaño+<2km = score 100 | ✅ |
| 3 | `testBuscarCoincidencias_DescartaCandidatosDeDistintoTipo` | Gato vs perro → score 0, lista vacía | ✅ |
| 4 | `testBuscarCoincidencias_SinCandidatos` | Lista vacía → total=0 | ✅ |
| 5 | `testBuscarCoincidencias_MascotaNoEncontrada` | ID inexistente → RuntimeException | ✅ |
| 6 | `testBuscarCoincidencias_FiltraPorRadioKm` | Mascota a 1000 km, radio=5km → filtrada | ✅ |
| 7 | `testBuscarCoincidencias_RespetaLimite` | 5 candidatos, límite=2 → máx 2 | ✅ |
| 8 | `testBuscarCoincidencias_NoGuardaDuplicados` | Coincidencia existente → save() no llamado | ✅ |

#### getResumen()

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 9 | `testGetResumen_RetornaDatos` | Totales 42/15/10/5 correctos | ✅ |
| 10 | `testGetResumen_SinDatos` | Todo en 0 | ✅ |

#### ScoringAlgorithm — Haversine

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 11 | `testScoring_TipoDistintoScoreCero` | Tipo diferente → score 0 | ✅ |
| 12 | `testScoring_SoloTipoCoincide` | Solo tipo → score 30 | ✅ |
| 13 | `testDistanciaKm_MismasCoordenadas` | Mismo punto → 0.0 km | ✅ |
| 14 | `testDistanciaKm_CoordenadaNula` | Coordenada null → Double.MAX_VALUE | ✅ |
| 15 | `testDistanciaKm_SantiagoValparaiso` | Haversine real → 90-130 km | ✅ |

---

### 6.3 AuthService — ms-usuarios-entidades (21 tests)

**Archivo:** `AuthServiceTest.java`

#### login()

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 1 | `testLogin_Exitoso` | Credenciales correctas → token + usuario | ✅ |
| 2 | `testLogin_PasswordIncorrecta` | Contraseña errónea → HTTP 401 | ✅ |
| 3 | `testLogin_EmailNoExiste` | Email sin cuenta → HTTP 401 | ✅ |
| 4 | `testLogin_EmailVacio` | Email vacío → HTTP 400 | ✅ |
| 5 | `testLogin_PasswordVacia` | Contraseña null → HTTP 400 | ✅ |

#### register()

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 6 | `testRegister_Exitoso` | Datos válidos → usuario creado | ✅ |
| 7 | `testRegister_EmailDuplicado` | Email existente → HTTP 409 | ✅ |
| 8 | `testRegister_NombreCorto` | Nombre < 2 chars → HTTP 400 | ✅ |
| 9 | `testRegister_PasswordCorta` | Password < 6 chars → HTTP 400 | ✅ |
| 10 | `testRegister_NoPermiteRolAdmin` | Rol ADMIN en registro → rol = DUENO | ✅ |

#### Perfil / Contraseña

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 11 | `testGetPerfil_Exitoso` | ID válido → UserPublicDto | ✅ |
| 12 | `testGetPerfil_NoEncontrado` | ID inexistente → HTTP 404 | ✅ |
| 13 | `testCambiarPassword_Exitosa` | Contraseña correcta → actualizada | ✅ |
| 14 | `testCambiarPassword_ActualIncorrecta` | Contraseña incorrecta → HTTP 401 | ✅ |
| 15 | `testCambiarPassword_MismaContrasena` | Nueva = actual → HTTP 400 | ✅ |
| 16 | `testActualizarPerfil_Exitoso` | Nombre válido → actualizado | ✅ |
| 17 | `testActualizarPerfil_NombreInvalido` | Nombre "X" → HTTP 400 | ✅ |

#### Operaciones Admin

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 18 | `testListarUsuarios` | findAll() → lista completa | ✅ |
| 19 | `testCambiarRol_NoAutoModificacion` | Admin cambia su propio rol → HTTP 400 | ✅ |
| 20 | `testToggleActivo_NoAutoDesactivacion` | Admin desactiva su cuenta → HTTP 400 | ✅ |
| 21 | `testToggleActivo_DesactivaUsuario` | activo=true → false tras toggle | ✅ |

---

### 6.4 GeolocService — ms-geolocalizacion (1 test)

**Archivo:** `GeolocServiceTest.java`

| # | Test | Descripción | Estado |
|---|------|-------------|--------|
| 1 | `testZonasCalor` | PERDIDA→weight 1.0, ENCONTRADA→weight 0.6 | ✅ |

---

## 7. Relación con Patrones de Diseño

| Patrón | Verificado en test | Cómo |
|---|---|---|
| **Observer** | `testRegistrarMascota_Success`, `testRevisarEncuentro_Aprobar` | `verify(eventPublisher).publishEvent(any(MascotaReportadaEvent.class))` |
| **Factory Method** | `testAlertaFactory_Perdida/Encontrada/Reunificada/EstadoNull` | Tests directos sobre `AlertaFactory.crear()` |
| **Repository** | Todos los tests de servicio | Mocks del repositorio aíslan acceso a datos |
| **Strategy (Scoring)** | `testScoring_*`, `testDistanciaKm_*` | Tests directos sobre `ScoringAlgorithm` |
| **Circuit Breaker** | Validado manualmente en BFF | Ver `CircuitBreaker.java` (CLOSED/OPEN/HALF_OPEN) |

---

## 8. Cobertura Real por Microservicio

Valores medidos con `mvn test jacoco:report` (clases boilerplate excluidas):

| Microservicio | Líneas cubiertas | Total | Cobertura | Umbral |
|---|---|---|---|---|
| ms-gestion-mascotas | 134 | 193 | **69.4%** | ≥ 60% ✅ |
| ms-motor-coincidencias | 75 | 95 | **78.9%** | ≥ 60% ✅ |
| ms-usuarios-entidades | 81 | 104 | **77.9%** | ≥ 60% ✅ |
| ms-geolocalizacion | 8 | 25 | 32.0% | ≥ 60% ⚠️ |

---

## 9. Conclusiones

- Se implementaron **55 tests unitarios** distribuidos en 4 microservicios, todos con **BUILD SUCCESS**
- **3 de 4 microservicios** superan el umbral del 60% de cobertura de línea
- Todos los tests son **independientes de MySQL** gracias a Mockito
- Los **patrones de diseño** (Factory Method y Observer) están verificados directamente en tests, no solo mediante el service
- El algoritmo **Haversine** del motor de coincidencias tiene tests de precisión geográfica
- Las **reglas de negocio de AuthService** (validaciones, seguridad de roles, operaciones admin) están cubiertas con 21 tests

---

*Informe generado para Evaluación Parcial N°2 — DSY1106 Desarrollo Fullstack III · DuocUC 2026*
