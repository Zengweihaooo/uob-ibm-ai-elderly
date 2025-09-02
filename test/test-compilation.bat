@echo off
echo ========================================
echo   Testing Compilation Fix
echo ========================================

echo [1/2] Compiling Spring Boot application...
cd springboot
mvn compile -q

if %errorlevel% equ 0 (
    echo ✓ Compilation successful!
    echo.
    echo [2/2] Testing AWS startup...
    echo Starting AWS Cloud services...
    mvn spring-boot:run -Dspring-boot.run.profiles=aws,sms,google-cloud -Dspring-boot.run.jvmArguments="-Dserver.port=8080" > aws-startup.log 2>&1 &
    echo Spring Boot started in background.
    echo.
    echo Waiting for startup...
    timeout /t 15 /nobreak > nul
    
    echo Checking if services are running...
    curl -s http://localhost:8080/actuator/health > nul 2>&1
    if %errorlevel% equ 0 (
        echo ✓ AWS Cloud services are running successfully!
        echo.
        echo Service URLs:
        echo   Main Application: http://localhost:8080
        echo   AI Assistant: http://localhost:8080/ai-assistant.html
        echo   Voice Test: http://localhost:8080/voice-test.html
        echo.
        echo Press any key to stop services...
        pause > nul
    ) else (
        echo ✗ Services failed to start
        echo Check aws-startup.log for details
    )
    
    echo Stopping services...
    taskkill /f /im java.exe > nul 2>&1
) else (
    echo ✗ Compilation failed
    echo Check the error messages above
)

echo Test completed.
