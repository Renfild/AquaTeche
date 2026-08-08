@echo off
chcp 65001 > nul
title AquaTech - Upload to GitHub Release
echo.
echo  ============================================================
echo   AquaTech Launcher  ^|  Deploy + GitHub Release Upload
echo  ============================================================
echo.
echo  Что произойдет:
echo   1. Файлы модов скопируются в dist/AquaTech-Client/
echo   2. Создастся manifest.json (MD5 + URL всех файлов)
echo   3. Все файлы загрузятся в GitHub Release (modpack-latest)
echo   4. manifest.json запушится в репозиторий
echo.
echo  Друзья получат обновление при следующем запуске лаунчера!
echo.
echo  Токен GitHub нужен в файле .gh_token или в env GITHUB_TOKEN
echo.
python tools\upload_to_github.py
echo.
pause
