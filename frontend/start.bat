@echo off
title Student Management - Frontend Server
cd /d "%~dp0"
echo Starting Frontend...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-frontend.ps1"
pause
