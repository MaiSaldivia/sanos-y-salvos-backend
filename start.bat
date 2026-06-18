@echo off
chcp 65001 >nul
title Sanos y Salvos

REM ============================================================
REM  start.bat — Inicia todos los servicios de Sanos y Salvos
REM  Compatible con cualquier PC Windows
REM  Requisitos: JDK 17 o 21, MySQL 8+ en 3306, Node.js 18+
REM ============================================================

set "BASE=%~dp0microservicios"
set "FRONT=%~dp0frontend"
set "RUN_MS=%~dp0run-ms.bat"
set "SETUP_SQL=%~dp0database\setup.sql"

echo.
echo  ================================================
echo   SANOS Y SALVOS — Iniciando sistema completo
echo  ================================================
echo.

REM ── Verificar Node.js ──────────────────────────────────────
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js no encontrado.
    echo         Descargalo desde: https://nodejs.org
    pause
    exit /b 1
)
for /f "tokens=*" %%V in ('node -v 2^>nul') do echo [OK] Node.js %%V detectado.

REM ── Verificar Maven o Maven Wrapper ────────────────────────
set MVN_OK=0
where mvn >nul 2>&1
if %errorlevel% equ 0 set MVN_OK=1
if exist "%BASE%\ms-gestion-mascotas\mvnw.cmd" set MVN_OK=1
if %MVN_OK% equ 0 (
    echo [ERROR] Maven no encontrado y no hay Maven Wrapper.
    echo         Descargalo desde: https://maven.apache.org
    pause
    exit /b 1
)
echo [OK] Maven detectado.

REM ── Buscar MySQL en rutas comunes ──────────────────────────
echo.
echo  Buscando MySQL...
set MYSQL_EXE=

for %%P in (
    "c:\xampp\mysql\bin\mysql.exe"
    "d:\xampp\mysql\bin\mysql.exe"
    "e:\xampp\mysql\bin\mysql.exe"
    "f:\xampp\mysql\bin\mysql.exe"
    "c:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    "c:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    "c:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe"
    "c:\Program Files\MariaDB 10.11\bin\mysql.exe"
    "c:\Program Files\MariaDB 11.0\bin\mysql.exe"
    "c:\Program Files\MariaDB 11.1\bin\mysql.exe"
) do (
    if not defined MYSQL_EXE (
        if exist %%P set "MYSQL_EXE=%%~P"
    )
)

if not defined MYSQL_EXE (
    where mysql >nul 2>&1
    if %errorlevel% equ 0 set "MYSQL_EXE=mysql"
)

if defined MYSQL_EXE (
    echo [OK] MySQL encontrado: %MYSQL_EXE%

    REM -- Probar conexion sin contraseña (XAMPP por defecto)
    "%MYSQL_EXE%" -u root --connect-timeout=5 -e "SELECT 1;" >nul 2>&1
    if %errorlevel% equ 0 (
        echo [OK] Conexion a MySQL exitosa.

        "%MYSQL_EXE%" -u root -e "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='sanos_y_salvos_db';" 2>nul | findstr "sanos_y_salvos_db" >nul 2>&1
        if %errorlevel% neq 0 (
            echo  Base de datos no encontrada. Creando esquema y datos de prueba...
            "%MYSQL_EXE%" -u root < "%SETUP_SQL%"
            if %errorlevel% equ 0 (
                echo [OK] Base de datos creada correctamente.
            ) else (
                echo [AVISO] No se pudo crear la BD automaticamente.
                echo         Ejecuta en MySQL: source database/setup.sql
            )
        ) else (
            echo [OK] Base de datos sanos_y_salvos_db ya existe.
        )
    ) else (
        echo [AVISO] MySQL requiere contrasena de root.
        echo         Ejecuta manualmente: mysql -u root -p ^< database\setup.sql
        echo         Luego edita los application.properties con tu contrasena.
    )
) else (
    echo [AVISO] MySQL no encontrado automaticamente.
    echo         Asegurate de que MySQL este corriendo en localhost:3306
    echo         y de haber ejecutado database\setup.sql al menos una vez.
)

echo.
echo  Presiona una tecla para iniciar los microservicios...
echo  (Ctrl+C para cancelar)
pause >nul

REM ── Liberar puertos 3001-3005 ──────────────────────────────
echo.
echo  Liberando puertos 3001-3005...
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":3001 "') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":3002 "') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":3003 "') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":3004 "') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":3005 "') do taskkill /PID %%a /F >nul 2>&1
timeout /t 2 /nobreak >nul

REM ── Levantar microservicios ────────────────────────────────
echo.
echo  Iniciando microservicios (cada uno en su propia ventana)...
echo.

echo  [1/5] ms-gestion-mascotas    ^> puerto 3001
start "ms-mascotas :3001" cmd /c ""%RUN_MS%" "%BASE%\ms-gestion-mascotas\pom.xml""
timeout /t 30 /nobreak >nul

echo  [2/5] ms-geolocalizacion     ^> puerto 3002
start "ms-geo :3002" cmd /c ""%RUN_MS%" "%BASE%\ms-geolocalizacion\pom.xml""
timeout /t 30 /nobreak >nul

echo  [3/5] ms-motor-coincidencias ^> puerto 3003
start "ms-motor :3003" cmd /c ""%RUN_MS%" "%BASE%\ms-motor-coincidencias\pom.xml""
timeout /t 30 /nobreak >nul

echo  [4/5] ms-usuarios-entidades  ^> puerto 3004
start "ms-usuarios :3004" cmd /c ""%RUN_MS%" "%BASE%\ms-usuarios-entidades\pom.xml""
timeout /t 30 /nobreak >nul

echo  [5/5] BFF API Gateway        ^> puerto 3005
start "bff :3005" cmd /c ""%RUN_MS%" "%BASE%\bff\pom.xml""
timeout /t 25 /nobreak >nul

echo.
echo  Puertos activos:
netstat -ano 2>nul | findstr "LISTENING" | findstr " :300"

REM ── Frontend React ─────────────────────────────────────────
echo.
echo  [6/6] Frontend React         ^> puerto 5173
start "Frontend :5173" cmd /c "cd /d "%FRONT%" && npm install && npm run dev & pause"

echo.
echo  ================================================
echo   SISTEMA INICIADO
echo.
echo   Abre en el navegador: http://localhost:5173
echo.
echo   Usuarios de prueba:
echo     admin@sanosysalvos.cl  / admin123
echo     refugio@esperanza.cl   / refugio123
echo     muni@conce.cl          / muni123
echo     dueno@demo.cl          / dueno123
echo.
echo   Si algun servicio falla, revisa su ventana.
echo   El error mas comun: MySQL no esta corriendo
echo   (abre XAMPP y activa MySQL primero).
echo  ================================================
echo.
pause
