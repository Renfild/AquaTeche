# Fix StarCatcher "Здесь нет рыбы..." on AquaTech deep ocean (sea_level=190).
# Surface fish are gated to Y 50-100; extend max_y to 320 so ocean at Y~190 works.
$ErrorActionPreference = "Stop"
$root = "C:\Users\xieto\Desktop\AquaTech"
$jarName = "starcatcher-2.3.19-FORGE-1.20.1.jar"

# Prefer already-texture-patched jar in mods/, else original
$candidates = @(
    (Join-Path $root "mods\$jarName"),
    (Join-Path $root $jarName),
    (Join-Path $root "starcatcher-2.3.19-FORGE-1.20.1 (1).jar")
)
$srcJar = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $srcJar) { throw "Missing StarCatcher jar" }
Write-Host "Source: $srcJar"

$work = Join-Path $root "_tmp_starcatcher_elev"
if (Test-Path $work) { Remove-Item $work -Recurse -Force }
New-Item -ItemType Directory -Force -Path $work | Out-Null
$outJar = Join-Path $work "patched.jar"
Copy-Item $srcJar $outJar -Force

Push-Location $work
$fishEntries = @(jar tf patched.jar | Select-String "starcatcher/fish/.*\.json$")
Write-Host "Fish entries: $($fishEntries.Count)"
foreach ($e in $fishEntries) {
    jar xf patched.jar $e.Line | Out-Null
}

$patched = 0
Get-ChildItem -Recurse -Filter "*.json" | Where-Object { $_.FullName -match "starcatcher[\\/]fish[\\/]" } | ForEach-Object {
    $raw = [IO.File]::ReadAllText($_.FullName)
    $orig = $raw

    # Case A: explicit surface label with max_y 100
    $raw = [regex]::Replace($raw,
        '("type"\s*:\s*"starcatcher:elevation_restriction"[^}]*?"max_y"\s*:\s*)100([^}]*?"translation_override"\s*:\s*"gui\.guide\.elevation\.surface")',
        '${1}320${2}',
        'Singleline')
    $raw = [regex]::Replace($raw,
        '("type"\s*:\s*"starcatcher:elevation_restriction"[^}]*?"translation_override"\s*:\s*"gui\.guide\.elevation\.surface"[^}]*?"max_y"\s*:\s*)100',
        '${1}320',
        'Singleline')

    # Case B: classic surface band min_y=50 max_y=100 (order either way)
    $raw = [regex]::Replace($raw,
        '("type"\s*:\s*"starcatcher:elevation_restriction"[^}]*?"max_y"\s*:\s*)100([^}]*?"min_y"\s*:\s*50\b)',
        '${1}320${2}',
        'Singleline')
    $raw = [regex]::Replace($raw,
        '("type"\s*:\s*"starcatcher:elevation_restriction"[^}]*?"min_y"\s*:\s*50\b[^}]*?"max_y"\s*:\s*)100\b',
        '${1}320',
        'Singleline')

    if ($raw -ne $orig) {
        [IO.File]::WriteAllText($_.FullName, $raw)
        $patched++
    }
}
Write-Host "Patched elevation on $patched fish files"

if (Test-Path "data") {
    Get-ChildItem "data" -Directory | ForEach-Object {
        $ns = $_.Name
        $rel = "data/$ns/starcatcher/fish"
        $disk = Join-Path "data" (Join-Path $ns "starcatcher\fish")
        if (Test-Path $disk) {
            jar uf patched.jar -C . $rel | Out-Null
            Write-Host "Updated jar namespace: $ns"
        }
    }
}

$hash = (Get-FileHash patched.jar -Algorithm MD5).Hash
Write-Host "Patched jar MD5: $hash"
Pop-Location

$targets = @(
    "$root\mods",
    "$root\server\mods",
    "$root\client\mods",
    "$root\server\client\mods",
    "$root\dist\AquaTech-Client\mods",
    "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods",
    "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"
)
foreach ($d in $targets) {
    if (-not (Test-Path $d)) { continue }
    Copy-Item $outJar (Join-Path $d $jarName) -Force
    Write-Host "Deployed -> $d"
}
Write-Host "Done. Restart client+server to apply."
