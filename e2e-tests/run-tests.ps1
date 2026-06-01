# Script para ejecutar las pruebas E2E de Vexio
# Asegúrate de que todos los contenedores Docker estén activos antes de ejecutar:
#   docker-compose up -d
#
# Uso desde la carpeta e2e-tests:
#   .\run-tests.ps1                     → todas las pruebas
#   .\run-tests.ps1 -Module Auth        → solo AuthTest
#   .\run-tests.ps1 -Module Store       → solo StoreTest

param(
    [string]$Module = ""
)

$ErrorActionPreference = "Stop"

Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Vexio E2E Tests" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════" -ForegroundColor Cyan

# Verificar que el gateway esté disponible
Write-Host "`nVerificando gateway en localhost:8080..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
    Write-Host "Gateway OK ($($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "ADVERTENCIA: Gateway no responde. Asegúrate de que docker-compose esté activo." -ForegroundColor Red
}

# Ejecutar pruebas
if ($Module -ne "") {
    Write-Host "`nEjecutando pruebas del módulo: $Module" -ForegroundColor Yellow
    mvn test -Dtest="${Module}Test" -q
} else {
    Write-Host "`nEjecutando TODAS las pruebas..." -ForegroundColor Yellow
    mvn test -q
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✔ Todas las pruebas pasaron" -ForegroundColor Green
} else {
    Write-Host "`n✘ Algunas pruebas fallaron. Revisa los reportes en target/surefire-reports/" -ForegroundColor Red
    exit 1
}
