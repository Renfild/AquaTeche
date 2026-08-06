@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ========================================================
echo   AquaTech: reset Skyblock raft world (SB + depth)
echo ========================================================
echo.
echo Backs up world, seeds SB-safe datapacks (NO deep_ocean).
echo Personal 4x4 rafts via SkyblockBuilder; sea Y190 = depth.
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
set "BACKUP=world_backup_skyblock_%STAMP%"

if exist "world" (
  echo [*] Backing up world to %BACKUP% ...
  move "world" "%BACKUP%" >nul
  echo [+] Backup done.
) else (
  echo [*] No world folder - fresh start.
)

if exist "mohist-config\worlds.yml" del /f /q "mohist-config\worlds.yml" >nul

mkdir "world\datapacks" >nul 2>nul
xcopy /E /I /Y "world_datapack_templates\aquatech_rare_ores" "world\datapacks\aquatech_rare_ores\" >nul
xcopy /E /I /Y "world_datapack_templates\aquatech_water_pressure" "world\datapacks\aquatech_water_pressure\" >nul
xcopy /E /I /Y "world_datapack_templates\aquatech_ocean_life" "world\datapacks\aquatech_ocean_life\" >nul
xcopy /E /I /Y "world_datapack_templates\aquatech_resource_rods" "world\datapacks\aquatech_resource_rods\" >nul

if exist "%BACKUP%\datapacks\aquatech_boot_fixes" (
  xcopy /E /I /Y "%BACKUP%\datapacks\aquatech_ocean_life" "world\datapacks\aquatech_ocean_life\" >nul 2>nul
  xcopy /E /I /Y "%BACKUP%\datapacks\aquatech_boot_fixes" "world\datapacks\aquatech_boot_fixes\" >nul
)

echo.
echo [+] Ready. level-type should be skyblockbuilder:skyblock
echo     Start with start_server.bat
echo.
pause
