# Start the React frontend dev server
$Root = Split-Path -Parent $PSScriptRoot
$Frontend = Join-Path $Root "frontend"

if (-not (Test-Path $Frontend)) {
    Write-Error "Frontend folder not found at: $Frontend"
    exit 1
}

Set-Location $Frontend

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host "[WARN] npm not found — starting frontend via Docker instead..." -ForegroundColor Yellow
    & (Join-Path $PSScriptRoot "start-frontend-docker.ps1")
    exit $LASTEXITCODE
}

if (-not (Test-Path "node_modules")) {
    Write-Host "Running npm install (first time only)..." -ForegroundColor Cyan
    npm install
}

Write-Host "Starting frontend at http://localhost:5173 ..." -ForegroundColor Green
Write-Host "Gateway must be on http://localhost:9080" -ForegroundColor Yellow
npm run dev
