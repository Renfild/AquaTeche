@echo off
REM Daily backup of AquaTech world + key configs with rotation (keep 14)
setlocal
set ROOT=C:\Users\xieto\Desktop\AquaTech\server
set STAMP=%DATE:~-4%-%DATE:~3,2%-%DATE:~0,2%_%TIME:~0,2%%TIME:~3,2%
set STAMP=%STAMP: =0%
set OUT=%ROOT%\backups\aquatech_%STAMP%

if not exist "%ROOT%\world" (
  echo [ERROR] world not found at %ROOT%\world
  exit /b 1
)

mkdir "%OUT%" 2>nul
robocopy "%ROOT%\world" "%OUT%\world" /E /NFL /NDL /NJH /NJS >nul
robocopy "%ROOT%\config\ftbquests" "%OUT%\ftbquests" /E /NFL /NDL /NJH /NJS >nul
robocopy "%ROOT%\plugins\LuckPerms" "%OUT%\LuckPerms" /E /NFL /NDL /NJH /NJS >nul

REM rotation: keep newest 14 backups
for /f "skip=14 delims=" %%D in ('dir /b /ad /o-n "%ROOT%\backups\aquatech_*" 2^>nul') do rmdir /s /q "%ROOT%\backups\%%D"

echo Backup done: %OUT%
endlocal
