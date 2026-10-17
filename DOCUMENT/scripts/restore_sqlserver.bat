@echo off
chcp 65001 >nul
echo [THE4BOOKSTORE] Dang tien hanh khoi phuc CSDL QL_NhaSach tren SQL Server...
powershell -ExecutionPolicy Bypass -File "%~dp0restore_sqlserver.ps1"
pause
