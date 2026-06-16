@echo off
chcp 65001 >nul
title Sanos y Salvos

set MVN=
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set MVN=mvn
) else if exist "D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd" (
    set MVN=D:\JUEGOS XD\Maven\apache-maven-3.9.16\bin\mvn.cmd
) else (
    echo [ERROR] Maven no encontrado.
    echo Instala Maven y agregalo al PATH, o edita este bat con la ruta correcta.
    pause
    exit /b 1
)
set BASE=D:\JUEGOS XD\sanos-y-salvos\microservicios
set FRONT=D:\JUEGOS XD\sanos-y-salvos\frontend

REM Memoria minima para que quepan los 5 microservicios + frontend
set OPTS=-Xms32m -Xmx180m -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom

echo.
echo  ================================================
echo   SANOS Y SALVOS - Iniciando sistema
echo  ================================================
echo.
echo  IMPORTANTE: Cierra Chrome, Discord, y cualquier
echo  programa pesado antes de continuar para liberar RAM.
echo.
pause

echo  Liberando puertos...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3001 " 2^>nul') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3002 " 2^>nul') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3003 " 2^>nul') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3004 " 2^>nul') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3005 " 2^>nul') do taskkill /PID %%a /F >nul 2>&1
timeout /t 3 /nobreak >nul

echo  [1/5] ms-gestion-mascotas (3001)...
start "ms-mascotas 3001" cmd /c "set MAVEN_OPTS=%OPTS% && %MVN% -f "%BASE%\ms-gestion-mascotas\pom.xml" spring-boot:run & pause"
timeout /t 25 /nobreak >nul

echo  [2/5] ms-geolocalizacion (3002)...
start "ms-geo 3002" cmd /c "set MAVEN_OPTS=%OPTS% && %MVN% -f "%BASE%\ms-geolocalizacion\pom.xml" spring-boot:run & pause"
timeout /t 25 /nobreak >nul

echo  [3/5] ms-motor-coincidencias (3003)...
start "ms-motor 3003" cmd /c "set MAVEN_OPTS=%OPTS% && %MVN% -f "%BASE%\ms-motor-coincidencias\pom.xml" spring-boot:run & pause"
timeout /t 25 /nobreak >nul

echo  [4/5] ms-usuarios-entidades (3004)...
start "ms-usuarios 3004" cmd /c "set MAVEN_OPTS=%OPTS% && %MVN% -f "%BASE%\ms-usuarios-entidades\pom.xml" spring-boot:run & pause"
timeout /t 25 /nobreak >nul

echo  [5/5] BFF (3005)...
start "bff 3005" cmd /c "set MAVEN_OPTS=%OPTS% && %MVN% -f "%BASE%\bff\pom.xml" spring-boot:run & pause"
timeout /t 25 /nobreak >nul

echo.
echo  Puertos activos:
netstat -ano | findstr "LISTENING" | findstr " :300"
echo.

echo  [6/6] Frontend React (5173)...
start "Frontend 5173" cmd /c "cd /d "%FRONT%" && npm run dev & pause"

echo.
echo  ================================================
echo   Listo. Abre: http://localhost:5173
echo  ================================================
pause
