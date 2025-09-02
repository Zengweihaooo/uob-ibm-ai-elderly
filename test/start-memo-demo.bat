@echo off
chcp 65001 >nul

echo ==========================================
echo 📝 Memo Function Demo Startup Script
echo ==========================================

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Error: Java not found, please install Java 8 or higher first
    pause
    exit /b 1
)

REM 检查Java版本
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo ✅ Detected Java version: %JAVA_VERSION%

REM 检查Maven是否安装
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Error: Maven not found, please install Maven first
    pause
    exit /b 1
)

echo ✅ Detected Maven

REM 进入Spring Boot项目目录
cd springboot

echo 🔧 Compiling project...
mvn clean compile

if %errorlevel% neq 0 (
    echo ❌ Compilation failed, please check project configuration
    pause
    exit /b 1
)

echo ✅ Compilation successful

echo 🚀 Starting Spring Boot application...
echo 📝 Memo function will be available at:
echo    - Main page: http://localhost:8080/src/pages/memo.html
echo    - Test page: http://localhost:8080/src/pages/memo-test.html
echo.
echo Press Ctrl+C to stop server
echo ==========================================

REM 启动Spring Boot应用
mvn spring-boot:run

pause 