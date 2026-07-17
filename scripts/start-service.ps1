# Starts one microservice with standalone profile (H2, no Kafka/Redis/security).
# Run from repo root:  .\scripts\start-service.ps1 identity-service
param(
    [Parameter(Mandatory = $true)]
    [string]$ServiceModule,
    [switch]$Force
)

$Root = Split-Path -Parent $PSScriptRoot
$ServicePath = Join-Path $Root $ServiceModule
$JavaHome = "C:\Program Files\Java\jdk-21.0.11"

$ServicePorts = @{
    "identity-service"     = 8081
    "product-service"      = 8082
    "seller-service"       = 8083
    "buyer-service"        = 8084
    "category-service"     = 8085
    "inventory-service"    = 8086
    "pricing-service"      = 8087
    "workflow-service"     = 8088
    "notification-service" = 8089
    "search-service"       = 8090
    "ai-service"           = 8091
    "audit-service"        = 8092
    "subscription-service" = 8093
    "report-service"       = 8094
    "admin-service"        = 8095
}

function Get-PortOwnerPid([int]$port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) { return $conn.OwningProcess }
    return $null
}

function Test-ServiceHealth([int]$port) {
    try {
        $url = "http://localhost:$port/api/v1/bootstrap/health"
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3
        return $r.StatusCode -eq 200 -and ($r.Content -match '"status"\s*:\s*"UP"' -or $r.Content -match '"success"\s*:\s*true')
    } catch {
        return $false
    }
}

if (-not (Test-Path $ServicePath)) {
    Write-Error "Service folder not found: $ServicePath"
    exit 1
}

if (-not (Test-Path "$JavaHome\bin\java.exe")) {
    Write-Host "[FAIL] JDK 21 not found at $JavaHome" -ForegroundColor Red
    exit 1
}

$Port = $ServicePorts[$ServiceModule]
if ($Port) {
    $healthy = Test-ServiceHealth $Port
    $ownerPid = Get-PortOwnerPid $Port

    if ($healthy -and -not $Force) {
        Write-Host "[OK] $ServiceModule already running on http://localhost:$Port" -ForegroundColor Green
        Write-Host "     Health: http://localhost:$Port/api/v1/bootstrap/health"
        Write-Host "     No need to start again. Use -Force to restart."
        exit 0
    }

    if ($ownerPid -and -not $Force) {
        Write-Host "[WARN] Port $Port is already in use by PID $ownerPid" -ForegroundColor Yellow
        Write-Host "       Restart with: .\scripts\start-service.ps1 $ServiceModule -Force"
        exit 1
    }

    if ($ownerPid -and $Force) {
        Write-Host "Stopping process on port $Port (PID $ownerPid)..." -ForegroundColor Yellow
        Stop-Process -Id $ownerPid -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 3
        $still = Get-PortOwnerPid $Port
        if ($still) {
            Write-Host "Port still held by PID $still - stopping..." -ForegroundColor Yellow
            Stop-Process -Id $still -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
        }
    }
}

$env:JAVA_HOME = $JavaHome
$env:FLYWAY_ENABLED = "false"
Write-Host "Starting $ServiceModule from:" -ForegroundColor Cyan
Write-Host "  $ServicePath"
if ($Port) {
    Write-Host "  Port: $Port"
}
Write-Host "Profiles: local,standalone" -ForegroundColor Gray
Set-Location $ServicePath
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
