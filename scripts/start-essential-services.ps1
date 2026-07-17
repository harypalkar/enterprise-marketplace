# Starts essential microservices in separate windows (fixed for paths with spaces)
$Root = Split-Path -Parent $PSScriptRoot
$JavaHome = "C:\Program Files\Java\jdk-21.0.11"

$Essential = @(
    "identity-service",
    "seller-service",
    "buyer-service",
    "category-service",
    "product-service",
    "search-service",
    "notification-service",
    "workflow-service",
    "audit-service",
    "ai-service"
)

Write-Host "Starting $($Essential.Count) microservices..." -ForegroundColor Cyan
Write-Host "Gateway must be running: .\start-gateway.ps1 (port 9080)" -ForegroundColor Yellow
Write-Host ""

foreach ($service in $Essential) {
    $servicePath = Join-Path $Root $service
    if (-not (Test-Path $servicePath)) {
        Write-Host "[SKIP] $service - folder not found" -ForegroundColor Red
        continue
    }

    Write-Host "Launching $service ..."
    $command = @"
`$env:JAVA_HOME = '$JavaHome'
Set-Location '$servicePath'
Write-Host '=== $service ===' -ForegroundColor Cyan
mvn spring-boot:run '-Dspring-boot.run.profiles=local,standalone'
"@

    Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $command
    Start-Sleep -Seconds 4
}

Write-Host ""
Write-Host "Wait 3-5 MINUTES. Each window must show: Started ...Application" -ForegroundColor Yellow
Write-Host "Then run: .\check-setup.ps1"
Write-Host "Then run: .\start-frontend.ps1"
