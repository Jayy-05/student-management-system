@echo off
title Student Management System
cd /d "%~dp0"
echo ==========================================================
echo  Starting Student Management System
echo ==========================================================
echo.
echo [1/2] Starting Backend Server...
start "Student Management - Backend" cmd /k "cd /d "%~dp0backend" && powershell -NoProfile -ExecutionPolicy Bypass -File "run-backend.ps1""

echo [2/2] Starting Frontend Server...
start "Student Management - Frontend" cmd /k "cd /d "%~dp0frontend" && powershell -NoProfile -ExecutionPolicy Bypass -File "start-frontend.ps1""

echo.
echo Both servers have been launched in separate windows!
echo - Backend: http://localhost:8080
echo - Frontend: http://localhost:5500
echo.
pause
