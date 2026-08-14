# AquaTech Launcher: Full Deploy Script
# 1. Regenerates manifest.json from dist/AquaTech-Client
# 2. Copies manifest.json into sync folder
# 3. Builds the Go bootstrap (AquaTech.exe) and copies to releases

$ErrorActionPreference = "Stop"
$root = "C:\Users\xieto\Desktop\AquaTech"
$distClient = "$root\dist\AquaTech-Client"
$distLauncher = "$root\dist\launcher"
$releasesDir = "$root\dist\releases"

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  AquaTech Launcher Deploy Tool" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Run deploy_runtime.ps1 to sync kubejs, mods, etc into dist
Write-Host "[1/4] Sync kubejs and mods into dist..." -ForegroundColor Yellow
& "$root\deploy_runtime.ps1"

# Step 2: Regenerate manifest.json
Write-Host ""
Write-Host "[2/4] Generating manifest.json..." -ForegroundColor Yellow
python "$root\tools\generate_manifest.py"

# Step 3: Copy manifest.json into the client sync folder (so sync server serves it)
$manifestSrc = "$distLauncher\manifest.json"
$manifestDst = "$distClient\manifest.json"
if (Test-Path $manifestSrc) {
    Copy-Item $manifestSrc $manifestDst -Force
    Write-Host "  OK  manifest.json -> dist\AquaTech-Client\manifest.json" -ForegroundColor Green
}

# Step 4: Build Bootstrap and copy to releases
Write-Host ""
Write-Host "[4/4] Building Bootstrap (AquaTech.exe)..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path $releasesDir | Out-Null

$bootstrapSrc = "$root\bootstrap"
$bootstrapExe = "$root\AquaTech.exe"
$releaseExe = "$releasesDir\AquaTech.exe"

Push-Location $bootstrapSrc
go build -ldflags="-H windowsgui -s -w" -o $bootstrapExe .
Pop-Location

if (Test-Path $bootstrapExe) {
    Copy-Item $bootstrapExe $releaseExe -Force
    $desktopExe = "C:\Users\xieto\Desktop\AquaTech.exe"
    Copy-Item $bootstrapExe $desktopExe -Force
    $exe = Get-Item $desktopExe
    $sizeMb = [math]::Round($exe.Length / 1MB, 2)
    Write-Host ""
    Write-Host "[4/4] Bootstrap (AquaTech.exe) Ready!" -ForegroundColor Green
    Write-Host "  Desktop: $desktopExe" -ForegroundColor Yellow
    Write-Host "  Releases: $releaseExe" -ForegroundColor White
    Write-Host "  EXE Size: $sizeMb MB" -ForegroundColor White
    Write-Host "  Updated:  $($exe.LastWriteTime)" -ForegroundColor White
} else {
    Write-Host "[WARN] AquaTech.exe not found. Build failed!" -ForegroundColor Red
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Deploy Complete!" -ForegroundColor Cyan
Write-Host "  Single File EXE for Players (Bootstrap):" -ForegroundColor White
Write-Host "  $releaseExe" -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
