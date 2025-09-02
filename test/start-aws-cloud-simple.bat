@echo off
chcp 65001 >nul
echo ========================================
echo   AWS Cloud Service Startup Script
echo   Simple AWS Cloud Implementation Demo
echo ========================================

echo.
echo [1/4] Setting up environment...
set GOOGLE_APPLICATION_CREDENTIALS=%~dp0docs\keys\organic-totem-467918-a5-d17504cd5eba.json
set GOOGLE_CLOUD_PROJECT_ID=organic-totem-467918-a5
set SPRING_PROFILES_ACTIVE=aws,google-cloud

REM AWS Environment Variables (required for SNS topics)
set AWS_ACCESS_KEY_ID=your-access-key-here
set AWS_SECRET_ACCESS_KEY=your-secret-key-here
set AWS_REGION=us-east-1
set AWS_ACCOUNT_ID=123456789012

echo.
echo [2/4] Starting Spring Boot application with AWS profile...
cd springboot
echo Starting application on port 8080...
start "AWS Cloud Service" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=aws,google-cloud"
cd ..

echo.
echo [3/4] Waiting for main service to start...
timeout /t 15 /nobreak >nul

echo.
echo [4/4] Starting voice microservice...
cd voice-command-microservice
echo Starting voice service on port 8090...
start "Voice Microservice" cmd /k "mvn spring-boot:run"
cd ..

echo.
echo ========================================
echo   AWS Cloud Services Started!
echo ========================================
echo.
echo Service URLs:
echo   Main Application: http://localhost:8080
echo   Voice Service: http://localhost:8090
echo   AI Assistant: http://localhost:8080/ai-assistant.html
echo   Voice Test: http://localhost:8080/voice-test.html
echo.
echo AWS Cloud Features:
echo   V DynamoDB data storage
echo   V S3 file storage
echo   V SNS notifications
echo   V Google Cloud TTS/STT
echo.
echo Test AWS Cloud Implementation:
echo   1. Open http://localhost:8080/ai-assistant.html
echo   2. Send message: "Add doctor appointment tomorrow"
echo   3. Check data is stored in AWS DynamoDB
echo   4. Test voice commands with TTS/STT
echo.
echo Press any key to open test pages...
pause >nul

echo Opening test pages...
start http://localhost:8080/ai-assistant.html
start http://localhost:8080/voice-test.html

echo.
echo AWS Cloud services are now running!
echo Check the browser windows for testing.
pause
