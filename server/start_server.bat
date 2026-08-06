@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title AquaTech Mohist 1.20.1 Server

echo ========================================================
echo        Starting AquaTech Mohist 1.20.1 Server
echo ========================================================
echo.

if not exist "%~dp0Mohist-1.20.1.jar" (
    echo [!] ERROR: Mohist-1.20.1.jar not found in:
    echo     %~dp0
    echo.
    pause
    exit /b 1
)

echo [*] Cleaning up old AquaTech server instances...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0kill_old_server.ps1"
timeout /t 2 /nobreak >nul

if exist "%~dp0world\session.lock" del /f "%~dp0world\session.lock" >nul 2>&1
if exist "%~dp0world_nether\session.lock" del /f "%~dp0world_nether\session.lock" >nul 2>&1
if exist "%~dp0world_the_end\session.lock" del /f "%~dp0world_the_end\session.lock" >nul 2>&1
echo [+] Ready to start!
echo.

set MIN_RAM=8G
set MAX_RAM=16G

if exist "%~dp0java17\jdk-17.0.10+7\bin\java.exe" (
    set "JAVA_EXE=%~dp0java17\jdk-17.0.10+7\bin\java.exe"
    echo Using portable Java 17: "%~dp0java17\jdk-17.0.10+7\bin\java.exe"
) else (
    set "JAVA_EXE=java"
    echo Using system Java
)

set "JVM_FLAGS=-Xms%MIN_RAM% -Xmx%MAX_RAM% -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -Dfile.encoding=UTF-8"

echo [*] Starting Mohist...
echo.
"%JAVA_EXE%" %JVM_FLAGS% -jar "%~dp0Mohist-1.20.1.jar" nogui
set "EXITCODE=%ERRORLEVEL%"
echo.
echo [*] Server process ended with exit code %EXITCODE%
pause
endlocal
exit /b %EXITCODE%
