# Minimal batch: starts core services needed for frontend E2E testing.
# Each service opens in a new PowerShell window.

$Root = Split-Path -Parent $PSScriptRoot
$Script = Join-Path $PSScriptRoot "start-service.ps1"

$Services = @(
    "product-service",
    "seller-service",
    "buyer-service",
    "category-service",
    "search-service",
    "ai-service",
    "notification-service",
    "workflow-service",
    "audit-service",
    "inventory-service",
    "pricing-service",
    "subscription-service",
    "report-service",
    "admin-service"
)

Write-Host "Launching $($Services.Count) microservices in separate windows..."
Write-Host "Ensure gateway is running on port 8080 with MARKETPLACE_SECURITY_ENABLED=false"
Write-Host ""

foreach ($service in $Services) {
    Start-Process powershell -ArgumentList "-NoExit", "-File", $Script, "-ServiceModule", $service
    Start-Sleep -Seconds 2
}

Write-Host "Done. Wait ~60 seconds for services to start, then open http://localhost:5173/services"
