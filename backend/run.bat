@echo off
title Student Management - Backend Server
cd /d "%~dp0"
echo Starting Backend...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-backend.ps1"
pause
