@echo off
echo 🚀 Starting Voice Command Microservice...
echo ==========================================

REM 检查Java版本
echo 📋 Checking Java version...
java -version

REM 检查Maven是否安装
echo 📋 Checking Maven...
mvn -version
if %errorlevel% neq 0 (
    echo ❌ Maven not found. Please install Maven first.
    pause
    exit /b 1
)

REM 设置环境变量
echo 🔧 Setting environment variables...
set OPENAI_API_KEY=%OPENAI_API_KEY:your-openai-api-key%
set GOOGLE_CLOUD_PROJECT_ID=%GOOGLE_CLOUD_PROJECT_ID:your-project-id%
set GOOGLE_APPLICATION_CREDENTIALS=%GOOGLE_APPLICATION_CREDENTIALS:path/to/credentials.json%

echo 📧 Email service URL: http://localhost:8080
echo 🔊 Voice command service will run on: http://localhost:8081
echo 🤖 OpenAI API Key: %OPENAI_API_KEY:~0,10%...
echo ☁️  Google Cloud Project: %GOOGLE_CLOUD_PROJECT_ID%

REM 清理并编译
echo 🔨 Cleaning and compiling...
mvn clean compile

REM 启动服务
echo 🚀 Starting service...
mvn spring-boot:run

echo ✅ Service started successfully!
echo 🌐 Access the service at: http://localhost:8081
echo 📚 API Documentation: http://localhost:8081/swagger-ui.html

pause
