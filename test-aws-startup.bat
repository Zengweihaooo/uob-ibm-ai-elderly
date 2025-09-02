@echo off
echo ========================================
echo   Testing AWS Cloud Service Startup
echo ========================================

echo [1/3] Starting Spring Boot application with AWS profile...
cd springboot
mvn spring-boot:run -Dspring-boot.run.profiles=aws,sms,google-cloud -Dspring-boot.run.jvmArguments="-Dserver.port=8080" > aws-test.log 2>&1 &
echo Spring Boot started in background, PID: %!

echo [2/3] Waiting for application to start...
timeout /t 10 /nobreak > nul

echo [3/3] Checking if application is running...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ AWS Cloud services are running successfully!
    echo.
    echo Service URLs:
    echo   Main Application: http://localhost:8080
    echo   AI Assistant: http://localhost:8080/ai-assistant.html
    echo   Voice Test: http://localhost:8080/voice-test.html
    echo.
    echo Test AWS Cloud Implementation:
    echo   1. Open http://localhost:8080/ai-assistant.html
    echo   2. Send message: "Add doctor appointment tomorrow"
    echo   3. Check data is stored in AWS DynamoDB
    echo   4. Test voice commands with TTS/STT
    echo.
    echo Press any key to stop the services...
    pause > nul
) else (
    echo ✗ AWS Cloud services failed to start
    echo Check aws-test.log for details
)

echo Stopping services...
taskkill /f /im java.exe > nul 2>&1
echo AWS Cloud services stopped.
