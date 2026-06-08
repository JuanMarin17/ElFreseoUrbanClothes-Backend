#!/bin/bash
# build-all.sh
# Compila todos los microservicios Java y prepara los servicios Python/Node.js.
# Ejecutar antes de hacer "docker-compose up".
#
# Uso: ./build-all.sh
# Desde la raíz del proyecto

set -e  # Detiene el script si algún comando falla

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

build_java() {
  local name=$1
  local path=$2
  echo ""
  echo "━━━ Compilando (Java): $name ━━━"
  cd "$ROOT_DIR/$path"
  mvn clean package -DskipTests -q
  echo "✓ $name compilado"
}

build_python() {
  local name=$1
  local path=$2
  echo ""
  echo "━━━ Verificando (Python): $name ━━━"
  if [ ! -f "$ROOT_DIR/$path/requirements.txt" ]; then
    echo "✗ $name: requirements.txt no encontrado"
    exit 1
  fi
  echo "✓ $name listo (Docker instala las dependencias al construir la imagen)"
}

build_node() {
  local name=$1
  local path=$2
  echo ""
  echo "━━━ Instalando dependencias (Node.js): $name ━━━"
  cd "$ROOT_DIR/$path"
  npm install --silent
  echo "✓ $name listo"
}

echo "====================================================="
echo "  Vexio — Build de todos los microservicios"
echo "====================================================="

# ── 1. Librería común (los demás dependen de ella) ───────────────────────────
echo ""
echo "━━━ Instalando librería común: common-request-context-starter ━━━"
cd "$ROOT_DIR/common-request-context-starter"
mvn clean install -DskipTests -q
echo "✓ common-request-context-starter instalado"

# ── 2. Java — sin dependencias entre microservicios ──────────────────────────
build_java "Auth"           "Auth"
build_java "Users"          "Users"
build_java "Store"          "Store"
build_java "Cart"           "Cart"
build_java "Supplier"       "Supplier"
build_java "Support"        "Support"
build_java "Promotion"      "Promotion"
build_java "LoyalCustomer"  "LoyalCustomer"
build_java "Preferences"    "Preferences"
build_java "Reviews"        "Reviews"
build_java "Payment"        "Payment"

# ── 3. Java — con dependencias entre microservicios ──────────────────────────
build_java "Product"        "Product"
build_java "Inventory"      "Inventory"
build_java "OrderPayment"   "OrderPayment"
build_java "Returns"        "Returns"
build_java "Cms"            "Cms"
build_java "Reports"        "Reports"

# ── 4. Gateway (último Java) ──────────────────────────────────────────────────
build_java "Gateway"        "Gateway"

# ── 5. Python — servicios IA ──────────────────────────────────────────────────
build_python "IaUser"   "IaUser"
build_python "IaAdmin"  "IaAdmin"

# ── 6. Node.js ────────────────────────────────────────────────────────────────
build_node "media-service" "media-service"

echo ""
echo "====================================================="
echo "  Todos los servicios listos!"
echo "  Ahora ejecuta: docker-compose up -d"
echo "====================================================="

cd "$ROOT_DIR"
