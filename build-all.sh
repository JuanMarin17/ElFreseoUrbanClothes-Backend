#!/bin/bash
# build-all.sh
# Compila todos los microservicios de Vexio en el orden correcto.
# Ejecutar antes de hacer "docker-compose up".
#
# Uso: ./build-all.sh
# Desde la raíz del proyecto

set -e  # Detiene el script si algún comando falla

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

build_service() {
  local name=$1
  local path=$2
  echo ""
  echo "━━━ Compilando: $name ━━━"
  cd "$ROOT_DIR/$path"
  mvn clean package -DskipTests -q
  echo "✓ $name compilado correctamente"
}

echo "====================================================="
echo "  Vexio — Compilación de todos los microservicios"
echo "====================================================="

# 1. Instala la librería común primero (los demás dependen de ella)
echo ""
echo "━━━ Instalando librería común: common-request-context-starter ━━━"
cd "$ROOT_DIR/common-request-context-starter"
mvn clean install -DskipTests -q
echo "✓ common-request-context-starter instalado"

# 2. Servicios sin dependencias entre microservicios
build_service "Auth"           "Auth"
build_service "Users"          "Users"
build_service "Store"          "Store"
build_service "Cart"           "Cart"
build_service "Supplier"       "Supplier"
build_service "Support"        "Support"
build_service "Promotion"      "Promotion"
build_service "LoyalCustomer"  "LoyalCustomer"
build_service "Preferences"    "Preferences"
build_service "Reviews"        "Reviews"

# 3. Servicios que dependen de otros microservicios
build_service "Product"        "Product"
build_service "Inventory"      "Inventory"
build_service "OrderPayment"   "OrderPayment"
build_service "Returns"        "Returns"

# 4. Gateway al final
build_service "Gateway"        "Gateway"

echo ""
echo "====================================================="
echo "  Todos los servicios compilados exitosamente!"
echo "  Ahora ejecuta: docker-compose up -d"
echo "====================================================="

cd "$ROOT_DIR"
