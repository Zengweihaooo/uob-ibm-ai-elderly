@echo off
echo Testing JWT Token Generation...
echo.

echo 1. Generating token for user1...
curl -X POST http://localhost:8080/api/test/jwt/generate -H "Content-Type: application/json" -d "{\"username\": \"user1\", \"userId\": 1}"
echo.
echo.

echo 2. Generating token for user2...
curl -X POST http://localhost:8080/api/test/jwt/generate -H "Content-Type: application/json" -d "{\"username\": \"user2\", \"userId\": 2}"
echo.
echo.

echo 3. Testing Family API without token (should fail)...
curl -X GET http://localhost:8080/api/family/contacts
echo.
echo.

echo JWT Test completed!
pause
