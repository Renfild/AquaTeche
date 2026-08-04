# Deploy aquatech_ui jar to every mods folder used by this project.
$ErrorActionPreference = "Stop"
$root = "C:\Users\xieto\Desktop\AquaTech"
$jar = Join-Path $root "mods\aquatech-ui\build\libs\aquatech_ui-1.0.0.jar"
if (-not (Test-Path $jar)) { throw "Missing build jar: $jar - run gradlew build first" }

$targets = @(
  "$root\mods",
  "$root\server\mods",
  "$root\client\mods",
  "$root\server\client\mods",
  "$root\dist\AquaTech-Client\mods",
  # CurseForge play instance (--gameDir from launcher log)
  "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods",
  "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"
)

# Never leave a gradle source tree inside mods/ (Forge can pick nested jars)
$cfJunk = "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods\aquatech-ui"
if (Test-Path $cfJunk) {
  $park = "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\_parked_aquatech-ui_src"
  if (Test-Path $park) { Remove-Item $park -Recurse -Force }
  Move-Item $cfJunk $park -Force
  Write-Host "Parked mods/aquatech-ui source tree -> $park"
}

$hash = (Get-FileHash $jar -Algorithm MD5).Hash
foreach ($d in $targets) {
  if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force -Path $d | Out-Null }
  Get-ChildItem $d -Filter "aquatech_ui*.jar" -EA SilentlyContinue | Remove-Item -Force
  Copy-Item $jar (Join-Path $d "aquatech_ui-1.0.0.jar") -Force
  $h = (Get-FileHash (Join-Path $d "aquatech_ui-1.0.0.jar") -Algorithm MD5).Hash
  if ($h -ne $hash) { throw "Hash mismatch in $d" }
  Write-Host "OK $h  $d"
}
Write-Host "Deployed aquatech_ui-1.0.0.jar to $($targets.Count) folders (build MD5 $hash)"
