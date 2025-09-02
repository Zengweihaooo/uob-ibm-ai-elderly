@echo off
chcp 65001 >nul
echo ========================================
echo   Hybrid Cloud Deployment Script
echo   Google Cloud + AWS Complete Deployment
echo ========================================

echo.
echo [1/6] Loading environment configuration...
call setup-hybrid-cloud.bat

echo.
echo [2/6] Building Spring Boot application...
cd springboot
echo Building main application...
mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo X Main application build failed
    pause
    exit /b 1
)
echo V Main application build successful
cd ..

echo.
echo [3/6] Building voice command microservice...
cd voice-command-microservice
echo Building voice microservice...
mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo X Voice microservice build failed
    pause
    exit /b 1
)
echo V Voice microservice build successful
cd ..

echo.
echo [4/6] Deploying AWS infrastructure...
echo Deploying AWS services...
call deploy-to-aws-simple.bat
if %ERRORLEVEL% NEQ 0 (
    echo X AWS deployment failed
    pause
    exit /b 1
)
echo V AWS infrastructure deployment successful

echo.
echo [5/6] Testing Google Cloud connection...
echo Testing Google Cloud services...
call test-google-services-fixed.bat
if %ERRORLEVEL% NEQ 0 (
    echo X Google Cloud test failed
    pause
    exit /b 1
)
echo V Google Cloud service test successful

echo.
echo [6/6] Starting hybrid cloud services...
echo Starting hybrid cloud architecture...
call start-hybrid-cloud.bat

echo.
echo ========================================
echo   Hybrid Cloud Deployment Complete!
echo ========================================
echo.
echo Service Status:
echo   V Google Cloud AI Services (TTS/STT/Gemini)
echo   V AWS Infrastructure (S3/DynamoDB/SNS/SES)
echo   V Spring Boot Main Application (Port 8080)
echo   V Voice Command Microservice (Port 8090)
echo.
echo Access URLs:
echo   Main Application: http://localhost:8080
echo   Voice Service: http://localhost:8090
echo   AI Assistant: http://localhost:8080/ai-assistant.html
echo   Voice Test: http://localhost:8080/voice-test.html
echo.
echo Function Verification:
echo   1. Test voice recognition and synthesis
echo   2. Test AI conversation functionality
echo   3. Verify AWS data storage
echo   4. Test email and notification services
echo.
pause
