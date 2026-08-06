@echo off
REM Backup AquaTech world + configs (run from server/ or schedule via Task Scheduler)
set ROOT=%~dp0
set STAMP=%DATE:~-4%%DATE:~3,2%%DATE:~0,2%_%TIME:~0,2%%TIME:~3,2%
set STAMP=%STAMP: =0%
set OUT=%ROOT%backups\aquatech_%STAMP%
mkdir "%OUT%" 2>nul
robocopy "%ROOT%world" "%OUT%\world" /E /XD session.lock /NFL /NDL /NJH /NJS
robocopy "%ROOT%config\ftbquests" "%OUT%\ftbquests" /E /NFL /NDL /NJH /NJS
robocopy "%ROOT%plugins\LuckPerms" "%OUT%\LuckPerms" /E /NFL /NDL /NJH /NJS
echo Backup done: %OUT%
