# Starts one microservice with standalone profile (H2, no Kafka/Redis/security).
param(
    [Parameter(Mandatory = $true)]
    [string]$ServiceModule
)

$Root = Split-Path -Parent $PSScriptRoot
$ServicePath = Join-Path $Root $ServiceModule

if (-not (Test-Path $ServicePath)) {
    Write-Error "Service folder not found: $ServicePath"
    exit 1
}

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
Write-Host "Starting $ServiceModule ..."
Set-Location $ServicePath
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
