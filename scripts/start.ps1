$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Marker = Join-Path $Root '.setup-complete'

if (-Not (Test-Path $Marker)) {
  Write-Host "[start] Setup marker not found. Running setup..."
  & "$Root/scripts/setup.ps1"
}

function Run-Docker {
  Write-Host "[start] Launching via docker compose (mock profile)"
  $env:SPRING_PROFILES_ACTIVE='mock'
  docker compose up -d
  Write-Host "[start] Waiting for backend health"
  for ($i=0; $i -lt 30; $i++) {
    try { (Invoke-WebRequest -Uri http://localhost:8080/actuator/health -UseBasicParsing -TimeoutSec 3) | Out-Null; Write-Host "[start] Backend is up."; return $true } catch { Start-Sleep -Seconds 2 }
  }
  Write-Host "[start] Backend did not become healthy in time."; return $false
}

if (Get-Command docker -ErrorAction SilentlyContinue) {
  if (Run-Docker) {
    Write-Host "[start] URLs:";
    Write-Host "  API:    http://localhost:8080/swagger-ui.html";
    Write-Host "  Angular static (if built): http://localhost:8080/app/angular/";
    Write-Host "  React static (if built):   http://localhost:8080/app/react/";
    exit 0
  } else { Write-Host "[start] Docker path failed, falling back to local run." }
}

Write-Host "[start] Running backend locally (H2 / local profile)"
Push-Location "$Root/backend"
./mvnw -q -Dspring-boot.run.profiles=local spring-boot:run
Pop-Location
