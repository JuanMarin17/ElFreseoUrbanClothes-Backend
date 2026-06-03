# build-all.ps1
# Compila todos los microservicios de Vexio en el orden correcto.
# Ejecutar antes de hacer "docker-compose up".
#
# Uso: .\build-all.ps1
# Desde la raíz del proyecto (Vexio(backend)\)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Build-Service {
    param([string]$name, [string]$path)
    Write-Host ""
    Write-Host "━━━ Compilando: $name ━━━" -ForegroundColor Cyan
    Set-Location "$root\$path"
    mvn clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Falló la compilación de $name" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ $name compilado correctamente" -ForegroundColor Green
}

Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "  Vexio — Compilación de todos los microservicios" -ForegroundColor Yellow
Write-Host "=====================================================" -ForegroundColor Yellow

# 1. Primero instala la librería común en el repositorio Maven local
#    (los demás módulos la necesitan como dependencia)
Write-Host ""
Write-Host "━━━ Instalando librería común: common-request-context-starter ━━━" -ForegroundColor Cyan
Set-Location "$root\common-request-context-starter"
mvn clean install -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Falló la instalación de common-request-context-starter" -ForegroundColor Red
    exit 1
}
Write-Host "✓ common-request-context-starter instalado" -ForegroundColor Green

# 2. Servicios sin dependencias entre microservicios (orden libre)
Build-Service "Auth"            "Auth"
Build-Service "Users"           "Users"
Build-Service "Store"           "Store"
Build-Service "Cart"            "Cart"
Build-Service "Supplier"        "Supplier"
Build-Service "Support"         "Support"
Build-Service "Promotion"       "Promotion"
Build-Service "LoyalCustomer"   "LoyalCustomer"
Build-Service "Preferences"     "Preferences"
Build-Service "Reviews"         "Reviews"

# 3. Servicios que dependen de otros microservicios (compilar después)
Build-Service "Product"         "Product"
Build-Service "Inventory"       "Inventory"
Build-Service "OrderPayment"    "OrderPayment"
Build-Service "Returns"         "Returns"

# 4. Servicios IA y auxiliares
Build-Service "Cms"             "Cms"
Build-Service "Reports"         "Reports"

# 5. Gateway al final (depende de que todos los demás estén listos)
Build-Service "Gateway"         "Gateway"

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "  Todos los servicios compilados exitosamente!" -ForegroundColor Green
Write-Host "  Ahora ejecuta: docker-compose up -d" -ForegroundColor Yellow
Write-Host "=====================================================" -ForegroundColor Yellow

Set-Location $root
