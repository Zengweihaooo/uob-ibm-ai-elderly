@echo off
echo ========================================
echo Complete DynamoDB Test
echo ========================================
echo.

echo [1] Testing Spring Boot application...
curl -s http://localhost:8080/api/aws-test/health
echo.

echo [2] Testing CloudWatch metric (writes to CloudWatch)...
curl -s -X POST "http://localhost:8080/api/cloudwatch/health-metric?type=bloodPressure&isAbnormal=false"
echo.

echo [3] Testing DynamoDB health record save...
curl -s -X POST "http://localhost:8080/api/dynamodb/health-record?type=bloodPressure&value=120/80&notes=Test record&isAbnormal=false"
echo.

echo [4] Testing DynamoDB user save...
curl -s -X POST "http://localhost:8080/api/dynamodb/user?email=test@example.com&name=Test User&role=USER"
echo.

echo [5] Testing DynamoDB health record count...
curl -s http://localhost:8080/api/dynamodb/health-record/count
echo.

echo [6] Testing DynamoDB user count...
curl -s http://localhost:8080/api/dynamodb/user/count
echo.

echo [7] Testing DynamoDB health records list...
curl -s http://localhost:8080/api/dynamodb/health-record/all
echo.

echo [8] Testing DynamoDB users list...
curl -s http://localhost:8080/api/dynamodb/user/all
echo.

echo [9] Testing SNS notification...
curl -s -X POST "http://localhost:8080/api/aws-test/send-sms?phoneNumber=+1234567890&message=Test message"
echo.

echo ========================================
echo Complete DynamoDB test finished!
echo Check AWS console for data
echo ========================================
pause
