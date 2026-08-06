@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ========================================================
echo   AquaTech: reset to endless DEEP OCEAN world
echo ========================================================
echo.
echo This backs up the current world, then generates a fresh
echo one where the whole overworld is flooded (sea level 190).
echo Players spawn on a small oak-log raft at 0, 190, 0.
echo No Skyblock/islands - it's one shared open world.
echo.
echo Stop the server BEFORE running this script.
echo.
pause

if not exist "Mohist-1.20.1.jar" (
  echo [ERROR] Run this from the server folder.
  pause
  exit /b 1
)

set "STAMP=%DATE:~-4%%DATE:~3,2%%DATE:~0,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%"
set "STAMP=%STAMP: =0%"
set "BACKUP=world_backup_%STAMP%"

if exist "world" (
  echo [*] Backing up world to %BACKUP% ...
  move "world" "%BACKUP%" >nul
  echo [+] Backup done.
) else (
  echo [*] No world folder found - fresh start.
)

if exist "mohist-config\worlds.yml" del /f /q "mohist-config\worlds.yml" >nul

if exist "world_datapack_templates\aquatech_deep_ocean" (
  echo [*] Restoring ocean world-gen datapacks into the new world...
  mkdir "world\datapacks" >nul 2>nul
  xcopy /E /I /Y "world_datapack_templates\aquatech_deep_ocean" "world\datapacks\aquatech_deep_ocean\" >nul
  xcopy /E /I /Y "world_datapack_templates\aquatech_spawn_raft" "world\datapacks\aquatech_spawn_raft\" >nul
  xcopy /E /I /Y "world_datapack_templates\aquatech_rare_ores" "world\datapacks\aquatech_rare_ores\" >nul
  xcopy /E /I /Y "world_datapack_templates\aquatech_water_pressure" "world\datapacks\aquatech_water_pressure\" >nul
  xcopy /E /I /Y "world_datapack_templates\aquatech_ocean_life" "world\datapacks\aquatech_ocean_life\" >nul
  xcopy /E /I /Y "world_datapack_templates\aquatech_resource_rods" "world\datapacks\aquatech_resource_rods\" >nul
  echo [+] Datapacks restored ^(ocean, raft spawn, rare ores, water pressure, ocean life, resource rods^).
) else (
  echo [WARNING] world_datapack_templates not found - the ocean flood + raft spawn will be MISSING!
)

echo.
echo [+] Ready. Start the server with start_server.bat.
echo     The whole world will generate as ocean automatically.
echo.
pause
