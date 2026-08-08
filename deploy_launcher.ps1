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

# Step 4: Copy onedir launcher into releases (+ zip for friends)
New-Item -ItemType Directory -Force -Path $releasesDir | Out-Null
$bundleSrc = "$root\dist\AquaTechLauncher"
$bundleDst = "$releasesDir\AquaTechLauncher"
$zipDst = "$releasesDir\AquaTechLauncher.zip"

if (Test-Path "$bundleSrc\AquaTechLauncher.exe") {
    if (Test-Path $bundleDst) {
        Remove-Item $bundleDst -Recurse -Force
    }
    Copy-Item $bundleSrc $bundleDst -Recurse -Force
    # Python zip avoids Compress-Archive file-lock issues on Windows
    python -c @"
import zipfile, pathlib
src = pathlib.Path(r'$bundleDst')
dst = pathlib.Path(r'$zipDst')
if dst.exists():
    dst.unlink()
with zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as z:
    for p in src.rglob('*'):
        if p.is_file():
            z.write(p, p.relative_to(src).as_posix())
print('zip', dst.stat().st_size)
"@
    $exe = Get-Item "$bundleDst\AquaTechLauncher.exe"
    Write-Host ""
    Write-Host "[4/4] Launcher ready (onedir)!" -ForegroundColor Green
    Write-Host "  Folder: $bundleDst" -ForegroundColor White
    Write-Host "  EXE:    $($exe.FullName)  ($($exe.LastWriteTime))" -ForegroundColor White
    Write-Host "  Zip:    $zipDst" -ForegroundColor White
} elseif (Test-Path "$root\dist\AquaTechLauncher.exe") {
    # Legacy onefile leftover
    Copy-Item "$root\dist\AquaTechLauncher.exe" "$releasesDir\AquaTechLauncher.exe" -Force
    Write-Host "[WARN] Found legacy onefile exe - prefer onedir rebuild via AquaTechLauncher.spec" -ForegroundColor Yellow
} else {
    Write-Host "[WARN] AquaTechLauncher not found. Run PyInstaller first!" -ForegroundColor Red
    Write-Host "  Command: python -m PyInstaller --noconfirm --clean AquaTechLauncher.spec" -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Deploy Complete!" -ForegroundColor Cyan
Write-Host ""
Write-Host "  YOU:  publish_client_pack.py + start_sync_server.bat" -ForegroundColor White
Write-Host "        + Playit tunnel to sync port 8765" -ForegroundColor White
Write-Host "  FRIENDS: unpack AquaTechLauncher.zip, run AquaTechLauncher.exe" -ForegroundColor White
Write-Host "           put Playit URL into update_url field," -ForegroundColor White
Write-Host "           click Update once, then Play" -ForegroundColor White
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
