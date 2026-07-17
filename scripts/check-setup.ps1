# Quick diagnostic - run this when frontend steps fail
Write-Host "=== Enterprise Marketplace Setup Check ===" -ForegroundColor Cyan

function Test-Port($port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    return [bool]$conn
}

function Get-ResponseBody($response) {
    if ($response.Content -is [byte[]]) {
        return [System.Text.Encoding]::UTF8.GetString($response.Content)
    }
    return [string]$response.Content
}

function Test-JsonHealth($url, $label) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10
        $body = Get-ResponseBody $r
        if ($body -match '"status"\s*:\s*"UP"' -or ($body.TrimStart().StartsWith("{") -and $body -match '"status"')) {
            Write-Host "[OK] $label -> $url" -ForegroundColor Green
            return $true
        }
        if ($body -match '<html|<!DOCTYPE') {
            Write-Host "[FAIL] $label -> returned HTML (wrong app on port?)" -ForegroundColor Red
        } else {
            Write-Host "[WARN] $label -> unexpected response: $($body.Substring(0, [Math]::Min(120, $body.Length)))" -ForegroundColor Yellow
        }
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
    8081 = "Identity Service (Mobile OTP/PIN/QR)"
    8082 = "Product Service"
    8083 = "Seller Service"
    8084 = "Buyer Service"
    8085 = "Category Service"
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
$gwOk = Test-JsonHealth "http://localhost:9080/actuator/health" "Gateway (9080)"
if ($gwOk) {
    if (Test-Port 8083) {
        Test-JsonHealth "http://localhost:9080/api/sellers/api/v1/bootstrap/health" "Seller via Gateway" | Out-Null
        Test-JsonHealth "http://localhost:8083/api/v1/bootstrap/health" "Seller direct" | Out-Null
    } else {
        Write-Host "[SKIP] Seller checks - seller-service (8083) not running yet" -ForegroundColor Yellow
        Write-Host "       Seller 500 via gateway is NORMAL until seller-service starts." -ForegroundColor Yellow
    }
}

Write-Host "`nIf microservices show NOT RUNNING:" -ForegroundColor Cyan
Write-Host "  Maven needs 3-5 minutes PER service. Check the 9 PowerShell windows for 'Started ...Application'."
Write-Host "  Re-run: .\check-setup.ps1 after waiting."

Write-Host "`nStart frontend (from repo root):" -ForegroundColor Cyan
Write-Host "  cd ..\frontend    # if you are in scripts folder"
Write-Host "  OR: cd frontend   # if you are in enterprise-marketplace folder"
Write-Host "  npm install"
Write-Host "  npm run dev"
Write-Host "  Open: http://localhost:5173/services"
