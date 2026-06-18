@echo off
chcp 65001 >nul
title Sanos y Salvos — Generando Reportes JaCoCo

REM ── Detectar Maven ─────────────────────────────────────────────────────────
set MVN=
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set MVN=mvn
) else (
    echo [ERROR] No se encontro Maven.
    echo Instala Maven y agregalo al PATH.
    pause
    exit /b 1
)

echo.
echo ==========================================================
echo   SANOS Y SALVOS - Generando reportes de cobertura
echo ==========================================================
echo.

set BASE_DIR=%~dp0
set MS_DIR=%BASE_DIR%microservicios
set REPORT_DIR=%BASE_DIR%docs\cobertura
set FAILED=0

if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"

call :run_jacoco "ms-gestion-mascotas"    "%MS_DIR%\ms-gestion-mascotas"
call :run_jacoco "ms-motor-coincidencias" "%MS_DIR%\ms-motor-coincidencias"
call :run_jacoco "ms-usuarios-entidades"  "%MS_DIR%\ms-usuarios-entidades"
call :run_jacoco "ms-geolocalizacion"     "%MS_DIR%\ms-geolocalizacion"

echo.
if %FAILED% equ 0 (
    echo ==========================================================
    echo   Todos los reportes generados correctamente
    echo   Abre: docs\cobertura\^<microservicio^>\index.html
    echo ==========================================================
) else (
    echo [ADVERTENCIA] %FAILED% microservicio(s) fallaron.
)
echo.
pause
exit /b %FAILED%

:run_jacoco
set NAME=%~1
set DIR=%~2
echo [%NAME%] Ejecutando tests y generando reporte JaCoCo...
"%MVN%" -f "%DIR%\pom.xml" test jacoco:report -q
if %errorlevel% equ 0 (
    echo [OK] %NAME% - reporte generado
    if exist "%DIR%\target\site\jacoco" (
        if not exist "%REPORT_DIR%\%NAME%" mkdir "%REPORT_DIR%\%NAME%"
        xcopy /E /Y /Q "%DIR%\target\site\jacoco" "%REPORT_DIR%\%NAME%\" >nul
    )
) else (
    echo [ERROR] Fallo en %NAME%
    set /a FAILED+=1
)
echo.
exit /b 0
