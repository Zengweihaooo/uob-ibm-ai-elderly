#!/bin/bash

# Email Sending Test Script
# 邮件发送测试脚本

echo "==================================="
echo "Email Sending Test for IBM AI Elderly Care System"
echo "==================================="

# Check if the application is running
echo "Checking if Spring Boot application is running..."
if ! curl -s http://localhost:8080/api/database/status > /dev/null; then
    echo "❌ Spring Boot application is not running on port 8080"
    echo "Please start the application first with: ./mvnw spring-boot:run"
    exit 1
fi

echo "✅ Spring Boot application is running"
echo ""

# Test email configuration
echo "Testing email sending functionality..."
echo ""

# Replace with a real test email address
TEST_EMAIL="test@example.com"

echo "📧 Testing email registration with: $TEST_EMAIL"
echo ""

# Test user registration (which will trigger email sending)
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$TEST_EMAIL\"}" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "📋 Check the console output of your Spring Boot application for:"
echo "   ✅ 'Verification email sent successfully to: $TEST_EMAIL'"
echo "   ❌ 'Failed to send verification email to: $TEST_EMAIL'"
echo ""

echo "🔧 If email sending fails, check:"
echo "   1. SMTP configuration in application.properties"
echo "   2. Network connectivity to smtp.163.com:465"
echo "   3. Email authorization code validity"
echo ""

echo "==================================="
echo "Test completed. Check console output for results."
echo "==================================="
