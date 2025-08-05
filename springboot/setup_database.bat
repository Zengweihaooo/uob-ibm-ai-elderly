@echo off
:: Database Setup and Testing Script for Windows
:: Windows数据库设置和测试脚本

echo ===================================
echo SQLite Database Setup for IBM AI Elderly Care System
echo ===================================

:: Check if we're in the right directory
if not exist "pom.xml" (
    echo Error: Please run this script from the springboot directory
    pause
    exit /b 1
)

:: Create data directory if it doesn't exist
echo Creating data directories...
if not exist "data" mkdir data
if not exist "data\backups" mkdir "data\backups"

echo Data directories created:
echo - Database: %cd%\data\
echo - Backups: %cd%\data\backups\

:: Build the project
echo.
echo Building the project...
call mvnw.cmd clean compile

if %errorlevel% neq 0 (
    echo Error: Failed to build project
    pause
    exit /b 1
)

echo Project built successfully!

:: Start the application
echo.
echo Starting the Spring Boot application...
echo The database will be automatically created on first startup.
echo.
echo API Endpoints available:
echo - Database Status: GET  http://localhost:8080/api/database/status
echo - Create Backup:   POST http://localhost:8080/api/database/backup
echo - Database Info:   GET  http://localhost:8080/api/database/info
echo.
echo Press Ctrl+C to stop the application
echo ===================================

call mvnw.cmd spring-boot:run
