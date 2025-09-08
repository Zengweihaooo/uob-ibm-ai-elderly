@echo off
echo ========================================
echo Direct DynamoDB Test
echo ========================================
echo.

echo [1] Testing Spring Boot application...
curl -s http://localhost:8080/api/aws-test/health
echo.

echo [2] Testing DynamoDB table creation...
curl -s -X POST http://localhost:8080/api/aws/init
echo.

echo [3] Waiting for table creation...
timeout /t 15 /nobreak > nul

echo [4] Testing AWS status...
curl -s http://localhost:8080/api/aws/status
echo.

echo [5] Testing CloudWatch...
curl -s -X POST "http://localhost:8080/api/cloudwatch/health-metric?type=test&isAbnormal=false"
echo.

echo ========================================
echo Test completed!
echo Please check AWS console now
echo ========================================
pause
