@echo off
chcp 65001 >nul
echo ========================================
echo   Google Cloud Service Test Script
echo   Test TTS/STT and AI Service Connection
echo ========================================

echo.
echo [1/3] Loading environment configuration...
call setup-hybrid-cloud.bat

echo.
echo [2/3] Testing Google Cloud connection...
echo Test Project: %GOOGLE_CLOUD_PROJECT_ID%
echo Credentials File: %GOOGLE_APPLICATION_CREDENTIALS%

echo.
echo Testing Google Cloud service connection...
cd springboot

REM Check if GoogleCloudConnectionTest exists
if exist "src\test\java\com\example\demo\test\GoogleCloudConnectionTest.java" (
    echo Running Google Cloud connection test...
    mvn test -Dtest=GoogleCloudConnectionTest -Dspring.profiles.active=google-cloud
    if %ERRORLEVEL% EQU 0 (
        echo V Google Cloud connection test successful
    ) else (
        echo X Google Cloud connection test failed
        echo Please check:
        echo   1. Key file is correct
        echo   2. Network connection is normal
        echo   3. Google Cloud project is activated
    )
) else (
    echo GoogleCloudConnectionTest not found, skipping test...
    echo V Skipping Google Cloud test (test class not found)
)

cd ..

echo.
echo [3/3] Starting test pages...
echo Opening browser to test TTS/STT functionality...
start http://localhost:8080/voice-test.html
start http://localhost:8080/ai-assistant.html

echo.
echo Test completed!
echo Please test voice functionality in browser
pause
