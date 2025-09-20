Param(
  [switch]$Force
)
$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Marker = Join-Path $Root '.setup-complete'

function Get-Hash($Path) {
  if (Test-Path $Path) { (Get-FileHash -Algorithm SHA256 $Path).Hash } else { 'none' }
}

$Signature = "backend=$(Get-Hash "$Root/backend/pom.xml");angular=$(Get-Hash "$Root/angular-client/package.json");react=$(Get-Hash "$Root/react-client/package.json")"

if (-Not $Force -and (Test-Path $Marker) -and (Select-String -Path $Marker -Pattern [Regex]::Escape($Signature) -Quiet)) {
  Write-Host "[setup] Already complete (signature match). Use -Force to rebuild."; exit 0
}

if (Get-Command docker -ErrorAction SilentlyContinue) {
  Write-Host "[setup] Docker detected: pre-pulling postgres:latest"
  docker pull postgres:latest | Out-Null
} else { Write-Host "[setup] Docker not found (will rely on local run)." }

Write-Host "[setup] Backend Maven offline warmup"
Push-Location "$Root/backend"
chmod +x mvnw 2>$null | Out-Null
./mvnw -q -DskipTests dependency:go-offline package
Pop-Location

function Build-Frontend($Dir) {
  if (Test-Path (Join-Path $Dir 'package.json')) {
    Write-Host "[setup] Installing Node deps in $Dir"
    Push-Location $Dir
    npm install --no-audit --no-fund | Out-Null
    if (Test-Path (Join-Path $Dir 'angular.json')) { npm run build | Out-Null } else { npm run build | Out-Null }
    Pop-Location
  }
}

Build-Frontend "$Root/angular-client"
Build-Frontend "$Root/react-client"

$StaticDir = Join-Path $Root 'backend/src/main/resources/static/app'
New-Item -ItemType Directory -Force -Path $StaticDir | Out-Null
if (Test-Path "$Root/angular-client/dist") {
  $dist = Get-ChildItem "$Root/angular-client/dist" -Directory | Select-Object -First 1
  if ($dist) { Remove-Item -Recurse -Force (Join-Path $StaticDir 'angular') -ErrorAction SilentlyContinue; Copy-Item -Recurse $dist.FullName (Join-Path $StaticDir 'angular') }
}
if (Test-Path "$Root/react-client/build") {
  Remove-Item -Recurse -Force (Join-Path $StaticDir 'react') -ErrorAction SilentlyContinue; Copy-Item -Recurse "$Root/react-client/build" (Join-Path $StaticDir 'react')
}

Set-Content -Path $Marker -Value $Signature
Write-Host "[setup] Complete."
