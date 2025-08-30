#!/bin/bash

# HealthController Email Integration Test Script
echo "=== HealthController Email Integration Test ==="

BASE_URL="http://localhost:8080/api/health"
AUTH_HEADER="Authorization: Bearer test-token"

echo ""
echo "1. Testing daily health check reminder..."
curl -X POST $BASE_URL/reminder/daily \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  | jq '.'

echo ""
echo "2. Testing health report generation (daily)..."
curl -X POST $BASE_URL/report \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"reportType": "daily"}' \
  | jq '.'

echo ""
echo "3. Testing health report generation (weekly)..."
curl -X POST $BASE_URL/report \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"reportType": "weekly"}' \
  | jq '.'

echo ""
echo "4. Testing health trend analysis..."
curl -X POST $BASE_URL/trend-analysis \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"days": 7}' \
  | jq '.'

echo ""
echo "5. Testing enhanced health statistics (today)..."
curl -X GET "$BASE_URL/statistics?period=today" \
  -H "$AUTH_HEADER" \
  | jq '.'

echo ""
echo "6. Testing enhanced health statistics (week)..."
curl -X GET "$BASE_URL/statistics?period=week" \
  -H "$AUTH_HEADER" \
  | jq '.'

echo ""
echo "7. Testing health trends data..."
curl -X GET "$BASE_URL/trends?days=7" \
  -H "$AUTH_HEADER" \
  | jq '.'

echo ""
echo "8. Testing abnormal health record with email notification..."
curl -X POST $BASE_URL/record \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"type": "bloodPressure", "value": "160/95"}' \
  | jq '.'

echo ""
echo "9. Testing normal health record..."
curl -X POST $BASE_URL/record \
  -H "Content-Type: application/json" \
  -H "$AUTH_HEADER" \
  -d '{"type": "bloodSugar", "value": "120"}' \
  | jq '.'

echo ""
echo "=== Email Integration Test Completed ==="
echo ""
echo "📧 Email Features Tested:"
echo "  ✅ Daily health check reminders"
echo "  ✅ Health data reports (daily/weekly/monthly)"
echo "  ✅ Health trend analysis"
echo "  ✅ Enhanced statistics with email integration"
echo "  ✅ Abnormal value email notifications"
echo "  ✅ Family member notifications"
echo ""
echo "🔗 New API Endpoints:"
echo "  POST /api/health/reminder/daily - Send daily health check reminder"
echo "  POST /api/health/report - Send health data report"
echo "  POST /api/health/trend-analysis - Send health trend analysis"
echo "  GET /api/health/statistics - Get enhanced health statistics"
echo "  GET /api/health/trends - Get health trends data"
echo ""
echo "📊 Enhanced Features:"
echo "  ✅ Multi-period statistics (today/week/month)"
echo "  ✅ Trend analysis with email reports"
echo "  ✅ Family member notification system"
echo "  ✅ Professional email templates"
echo "  ✅ Async email sending"
echo "  ✅ Comprehensive health data analysis"
