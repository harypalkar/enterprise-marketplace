# Starts the minimum services needed for frontend E2E testing (6 services + opens windows)
$ScriptDir = $PSScriptRoot
$StartService = Join-Path $ScriptDir "start-service.ps1"

$Essential = @(
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

Write-Host "Starting $($Essential.Count) essential microservices..."
Write-Host "Make sure gateway is running: .\start-gateway.ps1 (port 9080)"
Write-Host ""

foreach ($service in $Essential) {
    Write-Host "Launching $service ..."
    Start-Process powershell -ArgumentList "-NoExit", "-File", $StartService, "-ServiceModule", $service
    Start-Sleep -Seconds 3
}

Write-Host ""
Write-Host "Wait 60-90 seconds for Maven to compile and start each service."
Write-Host "Then run: .\check-setup.ps1"
Write-Host "Then open: http://localhost:5173/services"
