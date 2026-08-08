# AquaTech Launcher: Full Deploy Script
# 1. Regenerates manifest.json from dist/AquaTech-Client
# 2. Copies manifest.json into sync folder
# 3. Copies onedir AquaTechLauncher bundle to releases (+ zip)

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

# Step 4: Copy standalone onefile EXE into releases
New-Item -ItemType Directory -Force -Path $releasesDir | Out-Null
$onefileExe = "$root\dist\AquaTechLauncher.exe"
$releaseExe = "$releasesDir\AquaTechLauncher.exe"

if (Test-Path $onefileExe) {
    Copy-Item $onefileExe $releaseExe -Force
    $desktopExe = "C:\Users\xieto\Desktop\AquaTechLauncher.exe"
    Copy-Item $onefileExe $desktopExe -Force
    $exe = Get-Item $desktopExe
    $sizeMb = [math]::Round($exe.Length / 1MB, 2)
    Write-Host ""
    Write-Host "[4/4] Single Standalone EXE Launcher Ready!" -ForegroundColor Green
    Write-Host "  Desktop: $desktopExe" -ForegroundColor Yellow
    Write-Host "  Releases: $releaseExe" -ForegroundColor White
    Write-Host "  EXE Size: $sizeMb MB" -ForegroundColor White
    Write-Host "  Updated:  $($exe.LastWriteTime)" -ForegroundColor White
} elseif (Test-Path "$root\dist\AquaTechLauncher\AquaTechLauncher.exe") {
    Copy-Item "$root\dist\AquaTechLauncher\AquaTechLauncher.exe" $releaseExe -Force
    Write-Host "[4/4] Copied EXE to $releaseExe" -ForegroundColor Green
} else {
    Write-Host "[WARN] AquaTechLauncher.exe not found. Run PyInstaller first!" -ForegroundColor Red
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Deploy Complete!" -ForegroundColor Cyan
Write-Host "  Single File EXE for Players:" -ForegroundColor White
Write-Host "  $releaseExe" -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
