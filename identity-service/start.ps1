# Convenience launcher when your terminal is already inside identity-service
& (Join-Path (Split-Path -Parent $PSScriptRoot) "scripts\start-service.ps1") "identity-service"
