@echo off
title AquaTech Sync Server
cd /d "%~dp0"
echo =====================================
echo  AquaTech Sync Server
echo  Serving: dist\AquaTech-Client\
echo  Default port: 8765 (not 8080 — NVIDIA Broadcast)
echo =====================================
echo.
echo [INFO] Перед стартом обнови сборку: python tools\publish_client_pack.py
echo [INFO] В Playit.gg нужен TCP-тоннель на порт из консоли (обычно 8765)
echo [INFO] URL из Playit вставь друзьям в лаунчер: «URL обновлений»
echo [INFO] Ctrl+C — остановка.
echo.
python tools\start_sync_server.py %*
pause
