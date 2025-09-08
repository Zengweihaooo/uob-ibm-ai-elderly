@echo off
echo ========================================
echo Test Normal Business Flow with DynamoDB
echo ========================================
echo.

echo [1] Testing Spring Boot application...
curl -s http://localhost:8080/api/aws-test/health
echo.

echo [2] Testing normal user registration (should write to both SQLite and DynamoDB)...
curl -s -X POST "http://localhost:8080/user/api/register" -d "email=normaluser@example.com"
echo.

echo [3] Testing normal health record creation (should write to both SQLite and DynamoDB)...
curl -s -X POST "http://localhost:8080/api/health/record" -H "Content-Type: application/json" -H "Authorization: Bearer test-token" -d "{\"type\":\"bloodPressure\",\"value\":\"130/85\",\"notes\":\"Normal business flow test\"}"
echo.

echo [4] Testing DynamoDB health record count...
curl -s http://localhost:8080/api/dynamodb/health-record/count
echo.

echo [5] Testing DynamoDB user count...
curl -s http://localhost:8080/api/dynamodb/user/count
echo.

echo [6] Testing CloudWatch metric (should write to both CloudWatch and DynamoDB)...
curl -s -X POST "http://localhost:8080/api/cloudwatch/health-metric?type=heartRate&isAbnormal=false"
echo.

echo [7] Testing final DynamoDB health record count...
curl -s http://localhost:8080/api/dynamodb/health-record/count
echo.

echo ========================================
echo Normal business flow test completed!
echo Check AWS console for data from normal operations
echo ========================================
pause
