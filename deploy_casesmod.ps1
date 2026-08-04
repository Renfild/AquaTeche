# Deploy casesmod jar to AquaTech project + CurseForge instance
$ErrorActionPreference = "Stop"
$root = "C:\Users\xieto\Desktop\AquaTech"
$libsDir = Join-Path $root "mods\casesmod\build\libs"
$jar = Join-Path $libsDir "casesmod-1.0.0.jar"
if (-not (Test-Path $jar)) {
  $libs = Get-ChildItem $libsDir -Filter "casesmod*.jar" -EA SilentlyContinue |
    Where-Object { $_.Name -notmatch "sources|javadoc" } |
    Sort-Object LastWriteTime -Descending
  if (-not $libs) { throw "Missing build jar - run gradlew build in mods/casesmod first" }
  $jar = $libs[0].FullName
}

$targets = @(
  "$root\mods",
  "$root\server\mods",
  "$root\client\mods",
  "$root\server\client\mods",
  "$root\dist\AquaTech-Client\mods",
  "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods",
  "C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\client\mods"
)

$hash = (Get-FileHash $jar -Algorithm MD5).Hash
foreach ($d in $targets) {
  if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force -Path $d | Out-Null }
  Get-ChildItem $d -Filter "casesmod*.jar" -EA SilentlyContinue | Remove-Item -Force
  $dest = Join-Path $d "casesmod-1.0.0.jar"
  Copy-Item $jar $dest -Force
  $h = (Get-FileHash $dest -Algorithm MD5).Hash
  if ($h -ne $hash) { throw "Hash mismatch in $d" }
  Write-Host "OK $h  $d"
}
Write-Host "Deployed casesmod-1.0.0.jar to $($targets.Count) folders (build MD5 $hash)"
