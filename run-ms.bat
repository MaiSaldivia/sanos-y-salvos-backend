@echo off
REM ============================================================
REM  run-ms.bat — Arranca un microservicio
REM  Uso: run-ms.bat <ruta-al-pom.xml>
REM  Compatible con cualquier PC Windows con JDK 17-21
REM ============================================================

REM ── Buscar Java compatible (17, 21, 22, 23) ─────────────────
set JAVA_FOUND=

REM 1) Intentar con el Java del PATH
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        for /f "tokens=3" %%v in ('"%JAVA_HOME%\bin\java" -version 2^>^&1 ^| findstr /i "version"') do (
            set JVER=%%v
        )
        REM Aceptar versiones 17 a 23
        for %%V in (17 18 19 20 21 22 23) do (
            echo %JVER% | findstr /c:"%%V" >nul 2>&1
            if not errorlevel 1 (
                set JAVA_FOUND=%JAVA_HOME%
            )
        )
    )
)

REM 2) Buscar en rutas estándar de instalación
if not defined JAVA_FOUND (
    for %%P in (
        "C:\Program Files\Java\jdk-21"
        "C:\Program Files\Java\jdk-21.0.9"
        "C:\Program Files\Java\jdk-21.0.12"
        "C:\Program Files\Java\jdk-17"
        "C:\Program Files\Java\jdk-17.0.9"
        "C:\Program Files\Java\jdk-17.0.12"
        "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.9-hotspot"
        "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.7-hotspot"
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot"
        "C:\Program Files\Microsoft\jdk-21.0.9.9-hotspot"
        "C:\Program Files\Microsoft\jdk-17.0.9.8-hotspot"
        "C:\Program Files\Amazon Corretto\jdk21.0.9_9"
        "C:\Program Files\Amazon Corretto\jdk17.0.9_9"
        "C:\Program Files\BellSoft\LibericaJDK-21"
        "C:\Program Files\BellSoft\LibericaJDK-17"
        "C:\Program Files\Zulu\zulu-21"
        "C:\Program Files\Zulu\zulu-17"
    ) do (
        if not defined JAVA_FOUND (
            if exist %%P\bin\java.exe (
                set "JAVA_FOUND=%%~P"
            )
        )
    )
)

REM 3) Buscar en el registro de Windows (cualquier JDK 17-21)
if not defined JAVA_FOUND (
    for /f "tokens=2*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /s /v JavaHome 2^>nul') do (
        if not defined JAVA_FOUND (
            if exist "%%B\bin\java.exe" (
                "%%B\bin\java.exe" -version 2>&1 | findstr /i "version 21\|version 17\|version 18\|version 19\|version 20\|version 22\|version 23" >nul 2>&1
                if not errorlevel 1 (
                    set "JAVA_FOUND=%%B"
                )
            )
        )
    )
)

REM 4) Último recurso: usar java del PATH sin importar versión
if not defined JAVA_FOUND (
    where java >nul 2>&1
    if %errorlevel% equ 0 (
        echo [AVISO] No se encontro JDK 17 o 21 especifico.
        echo [AVISO] Usando Java del PATH. Si hay errores, instala JDK 21:
        echo [AVISO] https://adoptium.net
        goto :run
    )
    echo [ERROR] No se encontro Java instalado.
    echo Instala JDK 21 desde: https://adoptium.net
    pause
    exit /b 1
)

set "JAVA_HOME=%JAVA_FOUND%"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [INFO] Java encontrado: %JAVA_HOME%

:run
REM ── Derivar ruta del proyecto desde el pom.xml ──────────────
set "POM_PATH=%~1"
for %%F in ("%POM_PATH%") do set "MS_DIR=%%~dpF"

REM ── Usar mvnw si existe, si no usar mvn del PATH ─────────────
set "MVNW=%MS_DIR%mvnw.cmd"
if exist "%MVNW%" (
    set "MVN_CMD=%MVNW%"
    echo [INFO] Usando Maven Wrapper
) else (
    where mvn >nul 2>&1
    if %errorlevel% equ 0 (
        set "MVN_CMD=mvn"
        echo [INFO] Usando Maven del PATH
    ) else (
        echo [ERROR] No se encontro Maven ni Maven Wrapper.
        echo Instala Maven desde: https://maven.apache.org
        pause
        exit /b 1
    )
)

set MAVEN_OPTS=-Xms32m -Xmx180m -XX:TieredStopAtLevel=1 -XX:+UseSerialGC
"%MVN_CMD%" -f "%POM_PATH%" clean spring-boot:run -Dmaven.test.skip=true
pause
