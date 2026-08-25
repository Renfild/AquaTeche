$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Building aqualumen-ui via Gradle under JDK 21..."
Set-Location 'c:\Users\xieto\Desktop\AquaTech\mods\aqualumen-ui'
.\gradlew.bat build --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Building aquatech-ui via Gradle under JDK 21..."
Set-Location 'c:\Users\xieto\Desktop\AquaTech\mods\aquatech-ui'
.\gradlew.bat build --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Both mods successfully built and reobfuscated via ForgeGradle!"
