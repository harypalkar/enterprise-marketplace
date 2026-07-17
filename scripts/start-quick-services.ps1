# Start only 3 core services for quick frontend testing (seller, product, buyer)
$Root = Split-Path -Parent $PSScriptRoot
$JavaHome = "C:\Program Files\Java\jdk-21.0.11"

$Quick = @("seller-service", "buyer-service", "product-service")

Write-Host "Quick start: $($Quick -join ', ')" -ForegroundColor Cyan
Write-Host "Gateway must be on port 9080" -ForegroundColor Yellow

foreach ($service in $Quick) {
    $servicePath = Join-Path $Root $service
    $command = @"
`$env:JAVA_HOME = '$JavaHome'
Set-Location '$servicePath'
Write-Host '=== $service ===' -ForegroundColor Cyan
mvn spring-boot:run '-Dspring-boot.run.profiles=local,standalone'
"@
    Start-Process powershell.exe -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $command
    Start-Sleep -Seconds 4
}

Write-Host "Wait ~2 minutes, then: .\check-setup.ps1"
