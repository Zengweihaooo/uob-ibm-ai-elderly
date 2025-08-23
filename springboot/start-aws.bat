@echo off
echo ========================================
echo IBM AI Elderly Project - AWS Environment
echo ========================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java not found. Please install Java 17 or later.
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven not found. Please install Maven.
    pause
    exit /b 1
)

echo ✅ Java and Maven environment checked.

REM 设置AWS环境变量
echo Setting AWS environment variables...
set SPRING_PROFILES_ACTIVE=aws

REM 检查AWS凭证
if "%AWS_ACCESS_KEY_ID%"=="" (
    echo ⚠️  AWS_ACCESS_KEY_ID not set. Please set your AWS credentials.
    echo You can set them in your environment or create a .env file.
    echo.
    echo Example:
    echo set AWS_ACCESS_KEY_ID=your-access-key
    echo set AWS_SECRET_ACCESS_KEY=your-secret-key
    echo set AWS_REGION=us-east-1
    echo.
    pause
    exit /b 1
)

if "%AWS_SECRET_ACCESS_KEY%"=="" (
    echo ⚠️  AWS_SECRET_ACCESS_KEY not set.
    pause
    exit /b 1
)

if "%AWS_REGION%"=="" (
    echo ⚠️  AWS_REGION not set. Using default: us-east-1
    set AWS_REGION=us-east-1
)

echo ✅ AWS credentials configured.
echo.

REM 编译项目
echo 🔨 Building project...
call mvn clean compile -q
if errorlevel 1 (
    echo ❌ Build failed.
    pause
    exit /b 1
)

echo ✅ Build successful.
echo.

REM 启动应用
echo 🚀 Starting application with AWS profile...
echo Profile: %SPRING_PROFILES_ACTIVE%
echo Region: %AWS_REGION%
echo.

call mvn spring-boot:run -Dspring-boot.run.profiles=aws

echo.
echo Application stopped.
pause

