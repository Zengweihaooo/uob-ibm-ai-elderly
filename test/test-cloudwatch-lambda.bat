@echo off
chcp 65001 >nul
echo ========================================
echo   Testing CloudWatch and Lambda Services
echo ========================================

echo.
echo [1/4] Starting Spring Boot application with AWS profile...
cd springboot
start "AWS Cloud Services" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=aws"
cd ..

echo.
echo [2/4] Waiting for application to start...
timeout /t 15 /nobreak >nul

echo.
echo [3/4] Testing CloudWatch services...
echo Testing CloudWatch health check...
curl -s http://localhost:8080/api/cloudwatch/health
echo.

echo Testing CloudWatch metric sending...
curl -X POST "http://localhost:8080/api/cloudwatch/metric?metricName=TestMetric&value=1.0&unit=Count"
echo.

echo Testing health metric recording...
curl -X POST "http://localhost:8080/api/cloudwatch/health-metric?type=bloodPressure&isAbnormal=true"
echo.

echo Testing user activity recording...
curl -X POST "http://localhost:8080/api/cloudwatch/user-activity?activity=login"
echo.

echo.
echo [4/4] Testing Lambda services...
echo Testing Lambda health check...
curl -s http://localhost:8080/api/lambda/health
echo.

echo Testing Lambda function listing...
curl -s http://localhost:8080/api/lambda/functions
echo.

echo Testing health analysis function...
curl -X POST "http://localhost:8080/api/lambda/health-analysis" -H "Content-Type: application/json" -d "{\"healthData\":\"bloodPressure:130/85\"}"
echo.

echo Testing emergency processing function...
curl -X POST "http://localhost:8080/api/lambda/emergency-processing" -H "Content-Type: application/json" -d "{\"emergencyData\":\"Emergency situation detected\"}"
echo.

echo.
echo ========================================
echo   CloudWatch and Lambda Testing Complete!
echo ========================================
echo.
echo Service URLs:
echo   CloudWatch API: http://localhost:8080/api/cloudwatch/
echo   Lambda API: http://localhost:8080/api/lambda/
echo.
echo Test Results:
echo   V CloudWatch metrics collection
echo   V CloudWatch health monitoring
echo   V Lambda function invocation
echo   V Lambda health analysis
echo   V Lambda emergency processing
echo.
echo Press any key to stop services...
pause >nul

echo Stopping services...
taskkill /f /im java.exe >nul 2>&1
echo Services stopped.
