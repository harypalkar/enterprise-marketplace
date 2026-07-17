# Start marketplace gateway on port 9080 (avoids conflict with other apps on 8080)
param(
    [int]$Port = 9080,
    [switch]$Force
)

$Root = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
$env:GATEWAY_PORT = "$Port"
$env:MARKETPLACE_SECURITY_ENABLED = "false"

function Test-GatewayHealth($port) {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:$port/actuator/health" -UseBasicParsing -TimeoutSec 3
        return $r.StatusCode -eq 200 -and $r.Content -match '"status"\s*:\s*"UP"'
    } catch {
        return $false
    }
}

function Get-PortOwnerPid($port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) { return $conn.OwningProcess }
    return $null
}

if (Test-GatewayHealth $Port) {
    Write-Host "[OK] Gateway already running on http://localhost:$Port" -ForegroundColor Green
    Write-Host "     Health: http://localhost:$Port/actuator/health"
    Write-Host "     No need to start again. Continue with: .\start-essential-services.ps1"
    exit 0
}

$ownerPid = Get-PortOwnerPid $Port
if ($ownerPid -and -not $Force) {
    Write-Host "[WARN] Port $Port is in use by process PID $ownerPid" -ForegroundColor Yellow
    Write-Host "       Options:"
    Write-Host "       1) Stop it:  .\stop-gateway.ps1"
    Write-Host "       2) Force:    .\start-gateway.ps1 -Force"
    Write-Host "       3) Alt port: .\start-gateway.ps1 -Port 9090"
    exit 1
}

if ($ownerPid -and $Force) {
    Write-Host "Stopping process on port $Port (PID $ownerPid)..." -ForegroundColor Yellow
    Stop-Process -Id $ownerPid -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

Write-Host "Starting gateway on http://localhost:$Port ..." -ForegroundColor Cyan
Set-Location (Join-Path $Root "gateway-service")
mvn spring-boot:run
