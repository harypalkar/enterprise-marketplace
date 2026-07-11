# Quick diagnostic - run this when frontend steps fail
Write-Host "=== Enterprise Marketplace Setup Check ===" -ForegroundColor Cyan

function Test-Port($port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    return [bool]$conn
}

function Test-JsonHealth($url, $label) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        $body = $r.Content
        if ($body -match '^\s*\{' -or $body -match '"status"') {
            Write-Host "[OK] $label -> $url" -ForegroundColor Green
            return $true
        }
        Write-Host "[FAIL] $label -> returned HTML (wrong app on port?)" -ForegroundColor Red
        Write-Host "       URL: $url" -ForegroundColor Yellow
        return $false
    } catch {
        Write-Host "[FAIL] $label -> $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

$ports = @{
    8080 = "Other app (may conflict - NOT marketplace gateway)"
    9080 = "Marketplace Gateway (use this)"
    5173 = "Frontend (Vite)"
    8082 = "Product Service"
    8083 = "Seller Service"
    8084 = "Buyer Service"
    8090 = "Search Service"
    8091 = "AI Service"
}

Write-Host "`nPort status:" -ForegroundColor Cyan
foreach ($p in $ports.Keys | Sort-Object) {
    $up = Test-Port $p
    $color = if ($up) { "Green" } else { "Red" }
    $status = if ($up) { "LISTENING" } else { "NOT RUNNING" }
    Write-Host "  Port $p : $status - $($ports[$p])" -ForegroundColor $color
}

Write-Host "`nHealth checks:" -ForegroundColor Cyan
Test-JsonHealth "http://localhost:9080/actuator/health" "Gateway (9080)" | Out-Null
Test-JsonHealth "http://localhost:8080/actuator/health" "Port 8080 (may be wrong app)" | Out-Null
Test-JsonHealth "http://localhost:9080/api/sellers/api/v1/bootstrap/health" "Seller via Gateway" | Out-Null
Test-JsonHealth "http://localhost:8083/api/v1/bootstrap/health" "Seller direct" | Out-Null

Write-Host "`nFix if services are DOWN:" -ForegroundColor Cyan
Write-Host "  1. Terminal 1: .\scripts\start-gateway.ps1"
Write-Host "  2. Terminal 2: .\scripts\start-essential-services.ps1"
Write-Host "  3. Terminal 3: cd frontend && npm install && npm run dev"
Write-Host "  4. Open: http://localhost:5173/services"
