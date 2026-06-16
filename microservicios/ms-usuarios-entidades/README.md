# ms-usuarios-entidades

**Proyecto Fullstack III · DuocUC 2026**  
**Sanos y Salvos — Plataforma de mascotas perdidas y encontradas**

Microservicio de autenticación y gestión de usuarios. Maneja el registro, login, perfiles y administración de cuentas. Emite **tokens JWT** que los demás microservicios validan para proteger sus endpoints. Implementa el patrón **Repository**.

---

## Tecnologías utilizadas

| Tecnología        | Versión  | Propósito                              |
|-------------------|----------|----------------------------------------|
| Java              | 17       | Lenguaje principal                     |
| Spring Boot       | 3.x      | Framework backend                      |
| Spring Data JPA   | —        | Persistencia con Hibernate             |
| MySQL             | 8.x      | Base de datos relacional               |
| JWT               | —        | Autenticación stateless                |
| Bean Validation   | —        | Validación de entradas (@Valid)        |
| Lombok            | —        | Reducción de boilerplate               |
| JaCoCo            | —        | Cobertura de pruebas unitarias         |
| JUnit 5 + Mockito | —        | Framework de testing                   |
| Maven             | 3.8+     | Gestión de dependencias y build        |

---

## Puerto

```
http://localhost:3004
```

---

## Patrón de diseño implementado

### Repository
Ubicación: `src/main/java/.../repository/UsuarioRepository.java`

Aísla completamente el acceso a datos de la lógica de negocio. Extiende `JpaRepository` y agrega consultas personalizadas como `findByEmailAndActivoTrue` para el login seguro y `countByRol` para estadísticas.

---

## Roles del sistema

| Rol            | Descripción                                      |
|----------------|--------------------------------------------------|
| `DUENO`        | Dueño de mascota — puede reportar y gestionar    |
| `REFUGIO`      | Refugio de animales — puede moderar encuentros   |
| `VETERINARIA`  | Clínica veterinaria                              |
| `MUNICIPALIDAD`| Municipalidad — puede moderar y ver estadísticas |
| `ADMIN`        | Administrador — gestión completa de usuarios     |

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
cd ms-usuarios-entidades
mvn spring-boot:run
```

El servicio arranca en **http://localhost:3004**

---

## Compilar sin ejecutar

```bash
mvn clean package
```

---

## Endpoints principales

| Método | Ruta                              | Descripción                          | Auth        |
|--------|-----------------------------------|--------------------------------------|-------------|
| POST   | `/api/auth/login`                 | Iniciar sesión, retorna JWT          | No          |
| POST   | `/api/auth/register`              | Registrar nueva cuenta               | No          |
| GET    | `/api/auth/me`                    | Obtener perfil del usuario actual    | Sí          |
| PUT    | `/api/auth/me`                    | Actualizar nombre del perfil         | Sí          |
| PUT    | `/api/auth/me/password`           | Cambiar contraseña                   | Sí          |
| GET    | `/api/auth/usuarios`              | Listar todos los usuarios            | ADMIN       |
| GET    | `/api/auth/usuarios/estadisticas` | Estadísticas por rol                 | ADMIN       |
| PUT    | `/api/auth/usuarios/{id}/rol`     | Cambiar rol de un usuario            | ADMIN       |
| PUT    | `/api/auth/usuarios/{id}/toggle-activo` | Activar/desactivar usuario     | ADMIN       |

**Ejemplo de petición — Login:**
```json
POST /api/auth/login

{
  "email": "dueno@demo.cl",
  "password": "dueno123"
}
```

**Ejemplo de respuesta:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "usuario": {
      "idUsuario": "usr-dueno-001",
      "nombre": "Dueno Demo",
      "email": "dueno@demo.cl",
      "rol": "DUENO",
      "activo": true
    }
  }
}
```

**Ejemplo de petición — Registro:**
```json
POST /api/auth/register

{
  "nombre": "Ana García",
  "email": "ana@demo.cl",
  "password": "segura123"
}
```

### Validaciones aplicadas

| Campo      | Regla                                          |
|------------|------------------------------------------------|
| `nombre`   | Obligatorio, entre 2 y 120 caracteres          |
| `email`    | Obligatorio, formato email válido, máx. 120    |
| `password` | Obligatorio, entre 6 y 100 caracteres          |
| `rol`      | No puede ser ADMIN en registro público         |

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
server.port=3004
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
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test                               | Descripción                                         |
|------------------------------------|-----------------------------------------------------|
| testLogin_Exitoso                  | Login con credenciales correctas retorna JWT        |
| testLogin_PasswordIncorrecta       | Lanza 401 con contraseña incorrecta                 |
| testLogin_EmailNoExiste            | Lanza 401 si el email no existe                     |
| testLogin_EmailVacio               | Lanza 400 si el email está vacío                    |
| testLogin_PasswordVacia            | Lanza 400 si la contraseña está vacía               |
| testRegister_Exitoso               | Crea usuario con rol DUENO por defecto              |
| testRegister_EmailDuplicado        | Lanza 409 si el email ya existe                     |
| testRegister_NombreCorto           | Lanza 400 si el nombre tiene menos de 2 caracteres  |
| testRegister_PasswordCorta         | Lanza 400 si la contraseña tiene menos de 6 chars   |
| testRegister_NoPermiteRolAdmin     | Fuerza rol DUENO aunque se envíe ADMIN              |
| testGetPerfil_Exitoso              | Retorna datos del usuario existente                 |
| testGetPerfil_NoEncontrado         | Lanza 404 si el usuario no existe                   |
| testCambiarPassword_Exitosa        | Cambia contraseña con actual correcta               |
| testCambiarPassword_ActualIncorrecta | Lanza 401 con contraseña actual incorrecta        |
| testCambiarPassword_MismaContrasena | Lanza 400 si nueva es igual a la actual            |
| testActualizarPerfil_Exitoso       | Actualiza el nombre correctamente                   |
| testActualizarPerfil_NombreInvalido | Lanza 400 si el nombre es muy corto               |

---

## Generar reporte de cobertura JaCoCo

```bash
mvn test jacoco:report
```

El reporte HTML se genera en: `target/site/jacoco/index.html`

---

## Estructura del proyecto

```
ms-usuarios-entidades/
├── pom.xml
└── src/
    ├── main/java/com/sanosysalvos/usuarios/
    │   ├── UsuariosApplication.java
    │   ├── config/           ← JwtUtil, JwtInterceptor, RequireAuth, RequireRole
    │   ├── controller/       ← AuthController, HealthController
    │   ├── dto/              ← LoginRequest, RegisterRequest, PasswordRequest,
    │   │                        ProfileRequest, RolRequest, LoginResponse,
    │   │                        UserPublicDto, ResponseWrapper
    │   ├── model/            ← Usuario (entidad JPA), Rol (enum)
    │   ├── repository/       ← UsuarioRepository
    │   └── service/          ← AuthService (lógica de autenticación)
    ├── main/resources/
    │   └── application.properties
    └── test/java/com/sanosysalvos/usuarios/
        └── service/
            └── AuthServiceTest.java
```
