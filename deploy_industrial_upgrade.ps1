# Deploy Industrial Upgrade pack bits -> AquaTech project folders + CurseForge instance
$ErrorActionPreference = "Stop"
$root = "C:\Users\xieto\Desktop\AquaTech"
$cf = "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech"

$jars = @(
  "IndustrialUpgrade-1.20.1-3.4.0.11.jar",
  "powerutils-1.8.jar",
  "quantumgenerators-1.7.jar",
  "simplyquarries-1.7.jar"
)

$modTargets = @(
  "$root\mods",
  "$root\server\mods",
  "$root\client\mods",
  "$root\server\client\mods",
  "$root\dist\AquaTech-Client\mods",
  "$cf\mods",
  "$cf\client\mods"
)

$configTargets = @(
  "$root\config",
  "$root\server\config",
  "$root\defaultconfigs",
  "$cf\config",
  "$cf\defaultconfigs"
)

$scriptTargets = @(
  "$root\scripts",
  "$root\server\scripts",
  "$root\client\scripts",
  "$cf\scripts"
)

$questSrcDirs = @(
  "$root\config\ftbquests",
  "$root\server\config\ftbquests"
)
$questDstDirs = @(
  "$root\config\ftbquests",
  "$root\server\config\ftbquests",
  "$cf\config\ftbquests"
)

function Ensure-Dir([string]$p) {
  if (-not (Test-Path $p)) { New-Item -ItemType Directory -Force -Path $p | Out-Null }
}

function Copy-Verified([string]$src, [string]$dst) {
  Ensure-Dir (Split-Path $dst -Parent)
  $srcFull = [System.IO.Path]::GetFullPath($src)
  $dstFull = [System.IO.Path]::GetFullPath($dst)
  if ($srcFull -ieq $dstFull) {
    return (Get-FileHash $src -Algorithm MD5).Hash
  }
  Copy-Item $src $dst -Force
  $a = (Get-FileHash $src -Algorithm MD5).Hash
  $b = (Get-FileHash $dst -Algorithm MD5).Hash
  if ($a -ne $b) { throw "Hash mismatch:`n  $src`n  $dst" }
  return $a
}

# --- jars ---
$srcMods = "$root\server\mods"
foreach ($jar in $jars) {
  $src = Join-Path $srcMods $jar
  if (-not (Test-Path $src)) { $src = Join-Path "$root\mods" $jar }
  if (-not (Test-Path $src)) { throw "Missing jar: $jar" }
  $hash = $null
  foreach ($d in $modTargets) {
    Ensure-Dir $d
    # remove older IU-family names with same prefix
    $prefix = ($jar -replace '-1\..*$','')
    Get-ChildItem $d -Filter "$prefix*.jar" -EA SilentlyContinue |
      Where-Object { $_.Name -ne $jar } |
      Remove-Item -Force -EA SilentlyContinue
    $hash = Copy-Verified $src (Join-Path $d $jar)
  }
  Write-Host "JAR OK $hash  $jar -> $($modTargets.Count) folders"
}

# --- IU common config (no transformer explosions) ---
$cfgSrc = "$root\server\config\industrialupgrade-common.toml"
if (-not (Test-Path $cfgSrc)) { $cfgSrc = "$root\config\industrialupgrade-common.toml" }
if (-not (Test-Path $cfgSrc)) { throw "Missing industrialupgrade-common.toml" }
foreach ($d in $configTargets) {
  $h = Copy-Verified $cfgSrc (Join-Path $d "industrialupgrade-common.toml")
}
Write-Host "CFG OK industrialupgrade-common.toml -> $($configTargets.Count) folders"

# --- CraftTweaker: disable quarries ---
$zsSrc = "$root\scripts\disable_quarries.zs"
if (-not (Test-Path $zsSrc)) { throw "Missing disable_quarries.zs" }
foreach ($d in $scriptTargets) {
  Ensure-Dir $d
  $h = Copy-Verified $zsSrc (Join-Path $d "disable_quarries.zs")
}
Write-Host "ZS  OK disable_quarries.zs -> $($scriptTargets.Count) folders"

# --- FTB Quests: sync workshop chapter 2F + chapter_groups from project config ---
$questRoot = "$root\config\ftbquests"
if (-not (Test-Path "$questRoot\quests\chapters\2F_ws_industrial_upgrade.snbt")) {
  throw "Missing 2F_ws_industrial_upgrade.snbt in $questRoot"
}
$questFiles = @(
  "quests\chapter_groups.snbt",
  "quests\chapters\2F_ws_industrial_upgrade.snbt"
)
# Also sync all workshop chapters so CF matches project
$wsChapters = Get-ChildItem "$questRoot\quests\chapters" -Filter "*_ws_*.snbt" -EA SilentlyContinue
foreach ($ch in $wsChapters) {
  $rel = "quests\chapters\$($ch.Name)"
  if ($questFiles -notcontains $rel) { $questFiles += $rel }
}

foreach ($dstRoot in $questDstDirs) {
  foreach ($rel in $questFiles) {
    $src = Join-Path $questRoot $rel
    if (-not (Test-Path $src)) { continue }
    $dst = Join-Path $dstRoot $rel
    Copy-Verified $src $dst | Out-Null
  }
}
Write-Host "FTB OK $($questFiles.Count) quest files -> $($questDstDirs.Count) trees"

# --- mirror server ftbquests from config (keep in sync) ---
robocopy "$root\config\ftbquests" "$root\server\config\ftbquests" /E /XO /NFL /NDL /NJH /NJS /nc /ns /np | Out-Null
Write-Host "SYNC OK config/ftbquests -> server/config/ftbquests"

Write-Host ""
Write-Host "Deploy Industrial Upgrade complete."
Write-Host "CurseForge: $cf"
Write-Host "Restart client/server (or /reload + CraftTweaker reload) to pick up config/quests."
