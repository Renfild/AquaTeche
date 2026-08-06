@echo off
title AquaTech Arclight 1.20.1 Server
echo ========================================================
echo        Starting AquaTech Arclight 1.20.1 Server
echo ========================================================
echo.

:: JVM Memory Settings (Default: Min 4GB, Max 8GB)
set MIN_RAM=4G
set MAX_RAM=8G

:: Portable Java 17 Executable
if exist "%~dp0java17\jdk-17.0.10+7\bin\java.exe" (
    set "JAVA_EXE=%~dp0java17\jdk-17.0.10+7\bin\java.exe"
    echo Using portable Java 17: "%~dp0java17\jdk-17.0.10+7\bin\java.exe"
) else (
    set JAVA_EXE=java
    echo Using system Java
)

:: Recommended JVM Flags (Aikar's Flags for G1GC + Java 17 Performance)
set JVM_FLAGS=-Xms%MIN_RAM% -Xmx%MAX_RAM% -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -Dfile.encoding=UTF-8

"%JAVA_EXE%" %JVM_FLAGS% -jar Arclight-1.20.1.jar nogui
pause
