#!/usr/bin/env bash
# ============================================================
#  generar-cobertura.sh — Ejecuta tests y genera reportes JaCoCo
#  DuocUC 2026 · Fullstack III
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MS_DIR="$BASE_DIR/microservicios"
REPORT_DIR="$BASE_DIR/docs/cobertura"
FAILED=0

mkdir -p "$REPORT_DIR"

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║     SANOS Y SALVOS — Generando reportes de cobertura     ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

run_jacoco() {
    local name="$1"
    local dir="$2"

    echo -e "${YELLOW}[${name}]${NC} Ejecutando mvn test jacoco:report..."
    cd "$dir"

    if mvn test jacoco:report -q; then
        echo -e "${GREEN}[OK]${NC} Tests y reporte generados para ${name}"
        if [ -d "target/site/jacoco" ]; then
            mkdir -p "$REPORT_DIR/$name"
            cp -r target/site/jacoco/. "$REPORT_DIR/$name/"
            echo -e "${GREEN}[OK]${NC} Reporte copiado a docs/cobertura/${name}/"
        fi
    else
        echo -e "${RED}[ERROR]${NC} Falló en ${name}"
        FAILED=$((FAILED + 1))
    fi

    cd "$BASE_DIR"
    echo ""
}

# ── Ejecutar en cada microservicio con tests ──────────────────────────────────
run_jacoco "ms-gestion-mascotas"    "$MS_DIR/ms-gestion-mascotas"
run_jacoco "ms-motor-coincidencias" "$MS_DIR/ms-motor-coincidencias"
run_jacoco "ms-usuarios-entidades"  "$MS_DIR/ms-usuarios-entidades"
run_jacoco "ms-geolocalizacion"     "$MS_DIR/ms-geolocalizacion"

echo ""
if [ $FAILED -eq 0 ]; then
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║  ✓ Todos los reportes generados correctamente            ║${NC}"
    echo -e "${CYAN}║                                                          ║${NC}"
    echo -e "${CYAN}║  Abre: docs/cobertura/<microservicio>/index.html         ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
else
    echo -e "${RED}[ADVERTENCIA]${NC} ${FAILED} microservicio(s) fallaron. Revisa la salida."
fi
echo ""

exit $FAILED
