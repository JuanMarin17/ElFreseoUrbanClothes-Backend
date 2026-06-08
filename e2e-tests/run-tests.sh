#!/bin/bash
# Ejecutar pruebas E2E de Vexio desde WSL/bash
#
# Uso:
#   ./run-tests.sh           → todas las pruebas
#   ./run-tests.sh Auth      → solo AuthTest
#   ./run-tests.sh Store     → solo StoreTest

set -e

MODULE=${1:-""}

echo "════════════════════════════════════════"
echo "  Vexio E2E Tests"
echo "════════════════════════════════════════"

# Verificar gateway
echo ""
echo "Verificando gateway en localhost:8080..."
if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "Gateway OK"
else
    echo "ADVERTENCIA: Gateway no responde. Asegúrate de que docker-compose esté activo."
fi

echo ""
if [ -n "$MODULE" ]; then
    echo "Ejecutando pruebas del módulo: ${MODULE}..."
    mvn test -Dtest="${MODULE}Test"
else
    echo "Ejecutando TODAS las pruebas..."
    mvn test
fi

echo ""
echo "Reportes en: target/surefire-reports/"
