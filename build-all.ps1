# build-all.ps1
# Compila todos los microservicios en el orden correcto.
# Uso: .\build-all.ps1  (desde la raiz del proyecto)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Build-Service {
    param([string]$name, [string]$path)
    Write-Host ""
    Write-Host "--- Compilando: $name ---" -ForegroundColor Cyan
    Set-Location "$root\$path"
    mvn clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Fallo la compilacion de $name" -ForegroundColor Red
        exit 1
    }
    Write-Host "OK $name compilado correctamente" -ForegroundColor Green
}

Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "  ElFreseo - Compilacion de todos los microservicios" -ForegroundColor Yellow
Write-Host "=====================================================" -ForegroundColor Yellow

# 1. Libreria comun (los demas modulos la necesitan)
Write-Host ""
Write-Host "--- Instalando: common-request-context-starter ---" -ForegroundColor Cyan
Set-Location "$root\common-request-context-starter"
mvn clean install -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo la instalacion de common-request-context-starter" -ForegroundColor Red
    exit 1
}
Write-Host "OK common-request-context-starter instalado" -ForegroundColor Green

# 2. Servicios sin dependencias entre microservicios
Build-Service "Auth"           "Auth"
Build-Service "Users"          "Users"
Build-Service "Store"          "Store"
Build-Service "Cart"           "Cart"
Build-Service "Supplier"       "Supplier"
Build-Service "Support"        "Support"
Build-Service "Promotion"      "Promotion"
Build-Service "LoyalCustomer"  "LoyalCustomer"
Build-Service "Preferences"    "Preferences"
Build-Service "Reviews"        "Reviews"

# 3. Servicios que dependen de otros
Build-Service "Product"        "Product"
Build-Service "Inventory"      "Inventory"
Build-Service "OrderPayment"   "OrderPayment"
Build-Service "Returns"        "Returns"

# 4. Servicios adicionales
Build-Service "Cms"            "Cms"
Build-Service "Reports"        "Reports"

# 5. Gateway al final
Build-Service "Gateway"        "Gateway"

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "  Todos los servicios compilados exitosamente!" -ForegroundColor Green
Write-Host "  Ahora ejecuta: docker-compose up -d" -ForegroundColor Yellow
Write-Host "=====================================================" -ForegroundColor Yellow

Set-Location $root
