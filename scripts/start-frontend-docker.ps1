# Start the React frontend in Docker (no local Node.js required)
param(
    [int]$Port = 5173,
    [string]$GatewayUrl = "http://host.docker.internal:9080"
)

$Root = Split-Path -Parent $PSScriptRoot
$Frontend = Join-Path $Root "frontend"
$ImageName = "marketplace-frontend-dev"

if (-not (Test-Path $Frontend)) {
    Write-Error "Frontend folder not found at: $Frontend"
    exit 1
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "[FAIL] Docker not found. Install Docker Desktop from https://docker.com/get-started/" -ForegroundColor Red
    exit 1
}

Write-Host "Pulling node:24-slim (if needed)..." -ForegroundColor Cyan
docker pull node:24-slim | Out-Null

Write-Host "Building frontend dev image..." -ForegroundColor Cyan
docker build -f (Join-Path $Frontend "Dockerfile.dev") -t $ImageName $Frontend
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker build failed"
    exit 1
}

Write-Host ""
Write-Host "Starting frontend at http://localhost:$Port" -ForegroundColor Green
Write-Host "Gateway on host must be at http://localhost:9080" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Gray
Write-Host ""

docker run -it --rm `
    --name marketplace-frontend `
    -p "${Port}:5173" `
    -e "VITE_GATEWAY_URL=$GatewayUrl" `
    -v "${Frontend}:/app" `
    -v marketplace-frontend-node-modules:/app/node_modules `
    $ImageName
