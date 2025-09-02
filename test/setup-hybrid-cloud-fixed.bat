@echo off
chcp 65001 >nul
echo ========================================
echo   Hybrid Cloud Environment Setup Script
echo   Google Cloud + AWS Hybrid Cloud Setup
echo ========================================

echo.
echo [1/5] Setting Google Cloud environment variables...
set GOOGLE_APPLICATION_CREDENTIALS=%~dp0docs\keys\organic-totem-467918-a5-d17504cd5eba.json
set GOOGLE_CLOUD_PROJECT_ID=organic-totem-467918-a5
set GOOGLE_AI_CREDENTIALS=%~dp0docs\keys\organic-totem-467918-a5-d17504cd5eba.json
set GOOGLE_SPEECH_CREDENTIALS=%~dp0docs\keys\organic-totem-467918-a5-d17504cd5eba.json
set GOOGLE_TTS_CREDENTIALS=%~dp0docs\keys\organic-totem-467918-a5-d17504cd5eba.json
set GOOGLE_BACKUP_CREDENTIALS=%~dp0docs\keys\organic-totem-467918-a5-6497b0d6925f.json

echo [2/5] Setting AWS environment variables...
echo Please ensure AWS credentials are configured:
echo   - AWS_ACCESS_KEY_ID
echo   - AWS_SECRET_ACCESS_KEY
echo   - AWS_DEFAULT_REGION=us-east-1

echo.
echo [3/5] Verifying key files...
if exist "docs\keys\organic-totem-467918-a5-d17504cd5eba.json" (
    echo V Main key file exists
) else (
    echo X Main key file missing
    pause
    exit /b 1
)

if exist "docs\keys\organic-totem-467918-a5-6497b0d6925f.json" (
    echo V Backup key file exists
) else (
    echo X Backup key file missing
    pause
    exit /b 1
)

echo.
echo [4/5] Setting Spring Boot configuration files...
set SPRING_PROFILES_ACTIVE=aws,google-cloud

echo.
echo [5/5] Environment configuration complete!
echo.
echo Current Configuration:
echo   Google Cloud Project: %GOOGLE_CLOUD_PROJECT_ID%
echo   Google Credentials: %GOOGLE_APPLICATION_CREDENTIALS%
echo   Spring Profiles: %SPRING_PROFILES_ACTIVE%
echo.
echo Next Steps:
echo   1. Run start-hybrid-cloud.bat to start services
echo   2. Or run deploy-to-aws-simple.bat to deploy AWS infrastructure
echo.
pause
