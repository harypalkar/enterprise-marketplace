# Stop whatever is listening on the marketplace gateway port (default 9080)
param(
    [int]$Port = 9080
)

$conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $conn) {
    Write-Host "[OK] Port $Port is free. Nothing to stop." -ForegroundColor Green
    exit 0
}

$pid = $conn.OwningProcess
$proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
$name = if ($proc) { $proc.ProcessName } else { "unknown" }

Write-Host "Stopping process on port $Port (PID $pid, $name)..." -ForegroundColor Yellow
Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

$still = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if ($still) {
    Write-Host "[FAIL] Port $Port still in use." -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Port $Port is now free. Run .\start-gateway.ps1" -ForegroundColor Green
