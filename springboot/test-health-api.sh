#!/bin/bash

# HealthController API Test Script
echo "=== HealthController API Test ==="

BASE_URL="http://localhost:8080/api/health"
AUTH_HEADER="Authorization: Bearer test-token"

echo ""
echo "1. Testing normal blood pressure record..."
curl -X POST $BASE_URL/record \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"type": "bloodPressure", "value": "120/80"}' \
  | jq '.'

echo ""
echo "2. Testing abnormal blood pressure record (will trigger email notification)..."
curl -X POST $BASE_URL/record \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"type": "bloodPressure", "value": "160/95"}' \
  | jq '.'

echo ""
echo "3. Testing abnormal blood sugar record (will trigger email notification)..."
curl -X POST $BASE_URL/record \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"type": "bloodSugar", "value": "250"}' \
  | jq '.'

echo ""
echo "4. Testing normal steps record..."
curl -X POST $BASE_URL/record \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"type": "steps", "value": "8000"}' \
  | jq '.'

echo ""
echo "5. Getting today's health records..."
curl -X GET $BASE_URL/today \
  -H "$AUTH_HEADER" \
  | jq '.'

echo ""
echo "6. Getting health statistics..."
curl -X GET $BASE_URL/stats \
  -H "$AUTH_HEADER" \
  | jq '.'

echo ""
echo "=== Test Completed ===" 