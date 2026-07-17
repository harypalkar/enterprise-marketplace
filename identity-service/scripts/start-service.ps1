# Local wrapper — allows running from inside identity-service:
#   .\scripts\start-service.ps1 identity-service
#   .\scripts\start-service.ps1 identity-service -Force
param(
    [Parameter(Mandatory = $false)]
    [string]$ServiceModule = "identity-service",
    [switch]$Force
)

$RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$StartScript = Join-Path $RepoRoot "scripts\start-service.ps1"

if (-not (Test-Path $StartScript)) {
    Write-Error "Could not find repo start script at: $StartScript"
    exit 1
}

if ($Force) {
    & $StartScript $ServiceModule -Force
} else {
    & $StartScript $ServiceModule
}
