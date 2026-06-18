#!/usr/bin/env bash
# ============================================================
#  start.sh — Arranca todos los microservicios de Sanos y Salvos
#  DuocUC 2026 · Fullstack III
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MS_DIR="$BASE_DIR/microservicios"

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║          SANOS Y SALVOS — Iniciando sistema              ║${NC}"
echo -e "${CYAN}║          DuocUC 2026 · Fullstack III                     ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

# ── Verificar dependencias ───────────────────────────────────────────────────
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[ERROR]${NC} Maven no encontrado. Instala Maven 3.8+ y agrégalo al PATH."
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo -e "${RED}[ERROR]${NC} Java no encontrado. Instala JDK 17+ y agrégalo al PATH."
    exit 1
fi

echo -e "${GREEN}[INFO]${NC} Maven y Java detectados."
echo ""
echo -e "${YELLOW}[AVISO]${NC} Asegúrate de que MySQL esté activo en puerto 3306"
echo "         y de haber importado database/setup.sql."
echo ""
read -p "Presiona ENTER para continuar..."

# Función para lanzar un microservicio en background con log
start_service() {
    local name="$1"
    local port="$2"
    local dir="$3"
    local log_file="$BASE_DIR/logs/${name}.log"

    mkdir -p "$BASE_DIR/logs"
    echo -e "${GREEN}[INFO]${NC} Iniciando ${name} (Puerto ${port})..."
    cd "$dir"
    mvn spring-boot:run -Dmaven.test.skip=true > "$log_file" 2>&1 &
    echo $! > "$BASE_DIR/logs/${name}.pid"
    cd "$BASE_DIR"
    sleep 2
}

# ── Microservicios ────────────────────────────────────────────────────────────
start_service "ms-gestion-mascotas"    3001 "$MS_DIR/ms-gestion-mascotas"
start_service "ms-geolocalizacion"     3002 "$MS_DIR/ms-geolocalizacion"
start_service "ms-motor-coincidencias" 3003 "$MS_DIR/ms-motor-coincidencias"
start_service "ms-usuarios-entidades"  3004 "$MS_DIR/ms-usuarios-entidades"
start_service "bff"                    3005 "$MS_DIR/bff"

echo ""
echo -e "${GREEN}[INFO]${NC} Los 5 microservicios iniciados en background."
echo "       Logs en: $BASE_DIR/logs/"
echo ""
echo "Esperando 20 segundos antes de iniciar el frontend..."
sleep 20

# ── Frontend ──────────────────────────────────────────────────────────────────
echo -e "${GREEN}[FRONTEND]${NC} Instalando dependencias y arrancando React (Puerto 5173)..."
cd "$BASE_DIR/frontend"
npm install
npm run dev &
echo $! > "$BASE_DIR/logs/frontend.pid"
cd "$BASE_DIR"

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║  Sistema iniciado. Abre: http://localhost:5173           ║${NC}"
echo -e "${CYAN}║                                                          ║${NC}"
echo -e "${CYAN}║  Para ver logs: cat logs/<servicio>.log                  ║${NC}"
echo -e "${CYAN}║  Para detener:  ./stop.sh                                ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
