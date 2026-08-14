# Patch StarCatcher rod textures from AquaTech fishing PNG pack.
$ErrorActionPreference = "Stop"
$root = "C:\Users\xieto\Desktop\AquaTech"
$srcDir = "C:\Users\xieto\Desktop\fishing\items"
$jarName = "starcatcher-2.3.19-FORGE-1.20.1.jar"
$srcJar = Join-Path $root $jarName
if (-not (Test-Path $srcJar)) {
    $srcJar = Join-Path $root "starcatcher-2.3.19-FORGE-1.20.1 (1).jar"
}
if (-not (Test-Path $srcJar)) { throw "Missing StarCatcher jar in $root" }
if (-not (Test-Path $srcDir)) { throw "Missing texture source: $srcDir" }

# StarCatcher rod id -> source PNG prefix (without _fishing_rod suffix)
$map = @{
    "humble_rod"         = "copper"
    "good_old_rod"       = "iron"
    "bamboo_rod"         = "coral"
    "naturalist_rod"     = "iron"
    "boner_rod"          = "bone"
    "slimed_rod"         = "slime"
    "starcatcher_rod"    = "golden"
    "sky_rod"            = "end"
    "obsidian_rod"       = "diamond"
    "iceborn_rod"        = "ice"
    "sharktooth_rod"     = "gemstone"
    "azure_crystal_rod"  = "amethyst"
    "magmaforged_rod"    = "magma"
    "alpha_rod"          = "netherite"
    "lush_glowberry_rod" = "prismarine"
}

$work = Join-Path $root "_tmp_starcatcher_patch"
$assets = Join-Path $work "assets\starcatcher\textures\item"
if (Test-Path $work) { Remove-Item $work -Recurse -Force }
New-Item -ItemType Directory -Force -Path $assets | Out-Null

function Copy-RodTex([string]$scRod, [string]$prefix) {
    $uncastSrc = Join-Path $srcDir "${prefix}_fishing_rod.png"
    $castSrc = Join-Path $srcDir "${prefix}_fishing_rod_cast.png"
    if (-not (Test-Path $uncastSrc)) { throw "Missing $uncastSrc" }
    if (-not (Test-Path $castSrc)) { throw "Missing $castSrc" }
    Copy-Item $uncastSrc (Join-Path $assets "${scRod}_uncast.png") -Force
    Copy-Item $castSrc (Join-Path $assets "${scRod}_cast.png") -Force
    Write-Host "  $scRod <- ${prefix}_fishing_rod"
}

Write-Host "Mapping textures from $srcDir"
foreach ($entry in $map.GetEnumerator()) {
    Copy-RodTex $entry.Key $entry.Value
}

# Also mirror into project starcatcher extract folder (if present)
$mirror = Join-Path $root "starcatcher\textures\item"
if (Test-Path (Join-Path $root "starcatcher")) {
    New-Item -ItemType Directory -Force -Path $mirror | Out-Null
    Copy-Item "$assets\*" $mirror -Force
}

# Build patched jar
$outJar = Join-Path $work "patched.jar"
Copy-Item $srcJar $outJar -Force
Push-Location $work
jar uf patched.jar -C assets starcatcher/textures/item
Pop-Location

$hash = (Get-FileHash $outJar -Algorithm MD5).Hash
Write-Host "Patched jar MD5: $hash"

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

Write-Host "Done. $($map.Count) rod textures patched."
