# Start marketplace gateway on port 9080 (avoids conflict with other apps on 8080)
$Root = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
$env:GATEWAY_PORT = "9080"
$env:MARKETPLACE_SECURITY_ENABLED = "false"

Write-Host "Starting gateway on http://localhost:9080 ..."
Set-Location (Join-Path $Root "gateway-service")
mvn spring-boot:run
