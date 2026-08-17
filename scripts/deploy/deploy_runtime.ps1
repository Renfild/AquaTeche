# Deploy AquaTech runtime assets: kubejs scripts + overlay mods to all instances.
$ErrorActionPreference = "Stop"
$root = "C:\Users\xieto\Desktop\AquaTech"
$cf = "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech"

$modTargets = @(
  "$root\mods",
  "$root\server\mods",
  "$root\client\mods",
  "$root\server\client\mods",
  "$root\dist\AquaTech-Client\mods",
  "$cf\mods",
  "$cf\client\mods"
)

$kubeTargets = @(
  "$root\server\kubejs",
  "$root\client\kubejs",
  "$root\dist\AquaTech-Client\kubejs",
  "$cf\kubejs"
)
# Live Mohist host (Lodestone) — must stay in sync with pack kubejs
$lodestoneKube = Get-ChildItem "$env:USERPROFILE\.lodestone\instances" -Directory -EA SilentlyContinue |
  Where-Object { $_.Name -like 'AquaTech*' } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1
if ($lodestoneKube) {
  $kubeTargets += (Join-Path $lodestoneKube.FullName "kubejs")
}

$clientOnlyMods = @(
  "$root\client\mods",
  "$root\server\client\mods",
  "$root\dist\AquaTech-Client\mods",
  "$cf\mods",
  "$cf\client\mods"
)

function Ensure-Dir([string]$p) {
  if (-not (Test-Path $p)) { New-Item -ItemType Directory -Force -Path $p | Out-Null }
}

function Copy-Mod([string]$src, [string[]]$targets, [string]$globClean) {
  if (-not (Test-Path $src)) { throw "Missing mod: $src" }
  $name = Split-Path $src -Leaf
  $srcFull = (Resolve-Path $src).Path
  foreach ($d in $targets) {
    Ensure-Dir $d
    $dest = Join-Path $d $name
    if ((Test-Path $dest) -and ((Resolve-Path $dest).Path -eq $srcFull)) {
      Write-Host "SKIP mod $name (already source) $d"
      continue
    }
    if ($globClean) {
      Get-ChildItem $d -Filter $globClean -EA SilentlyContinue | Where-Object { $_.Name -ne $name } | Remove-Item -Force
    }
    Copy-Item $src $dest -Force
    Write-Host "OK mod  $name -> $d"
  }
}

# --- kubejs scripts (source of truth: $root\kubejs) ---
$kubeSrc = Join-Path $root "kubejs"
if (-not (Test-Path (Join-Path $kubeSrc "server_scripts"))) {
  throw "Missing kubejs source: $kubeSrc\server_scripts"
}
foreach ($dst in $kubeTargets) {
  if (Test-Path $dst) { Remove-Item $dst -Recurse -Force }
  Copy-Item $kubeSrc $dst -Recurse -Force
  Write-Host "OK kubejs -> $dst"
}

# Prefer already-installed jars; optional download cache if present
$cache = Join-Path $root "_mod_dl_cache"
$parkedCt = Join-Path $root "_parked_mods\removed_crafttweaker"
$parkedMods = Join-Path $root "_parked_mods\mods"

function Resolve-Jar([string[]]$candidates) {
  foreach ($c in $candidates) {
    if ($c -and (Test-Path $c)) { return $c }
  }
  return $null
}

$kubejs = Resolve-Jar @(
  "$cache\kubejs-forge-2001.6.5-build.26.jar",
  "$root\mods\kubejs-forge-2001.6.5-build.26.jar",
  "$root\server\mods\kubejs-forge-2001.6.5-build.26.jar"
)
$rhino = Resolve-Jar @(
  "$cache\rhino-forge-2001.2.3-build.10.jar",
  "$root\mods\rhino-forge-2001.2.3-build.10.jar",
  "$root\server\mods\rhino-forge-2001.2.3-build.10.jar"
)
$rg = Resolve-Jar @(
  "$parkedCt\recipe_generator-1.1.0_beta-forge-1.20.1.jar",
  "$cache\recipe_generator-1.1.0_beta-forge-1.20.1.jar",
  "$root\mods\recipe_generator-1.1.0_beta-forge-1.20.1.jar",
  "$root\server\mods\recipe_generator-1.1.0_beta-forge-1.20.1.jar"
)
$blueprint = Resolve-Jar @(
  "$parkedMods\blueprint-1.20.1-7.1.4.jar",
  "$root\mods\blueprint-1.20.1-7.1.4.jar",
  "$root\server\mods\blueprint-1.20.1-7.1.4.jar"
)
$probe = Resolve-Jar @(
  "$cache\ProbeJSLegacy-1.20.1-6.2.0.jar",
  "$root\client\mods\ProbeJSLegacy-1.20.1-6.2.0.jar"
)
$aquatech = Resolve-Jar @(
  "$root\server\mods\aquatech_ui-1.0.24.jar",
  "$root\mods\aquatech-ui\build\libs\aquatech_ui-1.0.24.jar",
  "$root\mods\aquatech_ui-1.0.24.jar"
)

$aqualumen = Resolve-Jar @(
  "$root\server\mods\aqualumen-forge-1.20.1-0.3.1-alpha.jar",
  "$root\mods\aqualumen-ui\build\libs\aqualumen-forge-1.20.1-0.3.1-alpha.jar",
  "$root\mods\aqualumen-forge-1.20.1-0.3.1-alpha.jar"
)

$easynpc = Resolve-Jar @(
  "$cache\easy_npc-forge-1.20.1-6.0.21.jar",
  "$root\mods\easy_npc-forge-1.20.1-6.0.21.jar"
)

Copy-Mod $kubejs $modTargets "kubejs-*.jar"
Copy-Mod $rhino $modTargets "rhino-*.jar"
Copy-Mod $rg $modTargets "recipe_generator*.jar"
Copy-Mod $blueprint $modTargets "blueprint-*.jar"
if ($aqualumen) { Copy-Mod $aqualumen $modTargets "aqualumen-*.jar" }
if ($easynpc) { Copy-Mod $easynpc $modTargets "easy_npc-*.jar" }
if ($probe) { Copy-Mod $probe $clientOnlyMods "ProbeJS*.jar" }
if ($aquatech) {
  # clean nested source junk in CF mods
  $cfJunk = "$cf\mods\aquatech-ui"
  if (Test-Path $cfJunk) {
    $park = "$cf\_parked_aquatech-ui_src"
    if (Test-Path $park) { Remove-Item $park -Recurse -Force }
    Move-Item $cfJunk $park -Force
    Write-Host "Parked CF aquatech-ui source -> $park"
  }
  Copy-Mod $aquatech $modTargets "aquatech_ui*.jar"
}

# Ensure CraftTweaker stays parked (not reintroduced)
foreach ($d in $modTargets) {
  if (-not (Test-Path $d)) { continue }
  Get-ChildItem $d -Filter "CraftTweaker*.jar" -EA SilentlyContinue | ForEach-Object {
    Write-Host "REMOVE leftover CT $($_.FullName)"
    $_.Delete()
  }
  Get-ChildItem $d -Filter "EasyTweaker*.jar" -EA SilentlyContinue | ForEach-Object {
    Write-Host "REMOVE leftover EasyTweaker $($_.FullName)"
    $_.Delete()
  }
}

Write-Host ""
Write-Host "Deploy complete."
Write-Host "kubejs scripts: $((Get-ChildItem "$kubeSrc\server_scripts\*.js").Name -join ', ')"
