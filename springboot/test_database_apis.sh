#!/bin/bash

# SQLite Database API Testing Script
# SQLite数据库API测试脚本

BASE_URL="http://localhost:8080"

echo "==================================="
echo "Testing SQLite Database APIs"
echo "==================================="

# Check if server is running
echo "1. Checking if server is running..."
curl -s "$BASE_URL/api/database/status" > /dev/null
if [ $? -ne 0 ]; then
    echo "❌ Server is not running. Please start the application first."
    echo "Run: ./setup_database.sh"
    exit 1
fi
echo "✅ Server is running"

# Test database status
echo ""
echo "2. Testing database status..."
response=$(curl -s "$BASE_URL/api/database/status")
echo "Response: $response"

# Test database info
echo ""
echo "3. Testing database info..."
response=$(curl -s "$BASE_URL/api/database/info")
echo "Response: $response"

# Test database validation
echo ""
echo "4. Testing database validation..."
response=$(curl -s "$BASE_URL/api/database/validate")
echo "Response: $response"

# Test backup creation
echo ""
echo "5. Testing backup creation..."
response=$(curl -s -X POST "$BASE_URL/api/database/backup")
echo "Response: $response"

# Test existing APIs to ensure they still work
echo ""
echo "6. Testing existing user API..."
response=$(curl -s "$BASE_URL/user/stats")
echo "Response: $response"

echo ""
echo "7. Testing existing health API..."
response=$(curl -s -X POST "$BASE_URL/api/health/record" \
  -H "Content-Type: application/json" \
  -d '{"type":"bloodPressure","value":"120/80","notes":"Test record"}')
echo "Response: $response"

echo ""
echo "==================================="
echo "Database API Testing Complete!"
echo "==================================="
