#!/bin/bash

echo "🧪 Testing Memo Features..."

# 检查应用是否运行
echo "📡 Checking if application is running..."
if curl -s http://localhost:8080/api/memo/list > /dev/null; then
    echo "✅ Application is running"
else
    echo "❌ Application is not running. Please start it first:"
    echo "   ./start-memo-app.sh"
    exit 1
fi

# 测试PIN码设置
echo "🔐 Testing PIN code setting..."
PIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/memo/set-pin \
    -H "Content-Type: application/json" \
    -d '{"pinCode":"1234"}')

if echo "$PIN_RESPONSE" | grep -q '"success":true'; then
    echo "✅ PIN code setting works"
else
    echo "❌ PIN code setting failed: $PIN_RESPONSE"
fi

# 测试创建备忘录
echo "📝 Testing memo creation..."
MEMO_RESPONSE=$(curl -s -X POST http://localhost:8080/api/memo/create \
    -H "Content-Type: application/json" \
    -d '{"title":"Test Memo","content":"This is a test memo","type":"general"}')

if echo "$MEMO_RESPONSE" | grep -q '"success":true'; then
    echo "✅ Memo creation works"
else
    echo "❌ Memo creation failed: $MEMO_RESPONSE"
fi

# 测试创建重要备忘录
echo "❗ Testing important memo creation..."
IMPORTANT_MEMO_RESPONSE=$(curl -s -X POST http://localhost:8080/api/memo/create \
    -H "Content-Type: application/json" \
    -d '{"title":"Important Test","content":"This is an important memo","type":"important"}')

if echo "$IMPORTANT_MEMO_RESPONSE" | grep -q '"success":true'; then
    echo "✅ Important memo creation works"
else
    echo "❌ Important memo creation failed: $IMPORTANT_MEMO_RESPONSE"
fi

# 测试获取备忘录列表
echo "📋 Testing memo list retrieval..."
LIST_RESPONSE=$(curl -s http://localhost:8080/api/memo/list)

if echo "$LIST_RESPONSE" | grep -q '"success":true'; then
    echo "✅ Memo list retrieval works"
else
    echo "❌ Memo list retrieval failed: $LIST_RESPONSE"
fi

# 测试统计信息
echo "📊 Testing statistics..."
STATS_RESPONSE=$(curl -s http://localhost:8080/api/memo/statistics)

if echo "$STATS_RESPONSE" | grep -q '"success":true'; then
    echo "✅ Statistics retrieval works"
else
    echo "❌ Statistics retrieval failed: $STATS_RESPONSE"
fi

echo ""
echo "🎉 Feature testing completed!"
echo ""
echo "📱 To test the full user experience:"
echo "   1. Open http://localhost:8080/pages/memo.html"
echo "   2. Set your PIN code on first login"
echo "   3. Create some memos and important memos"
echo "   4. Test PIN code verification for important memos"
echo ""
echo "🔧 API endpoints available:"
echo "   - GET  /api/memo/list"
echo "   - POST /api/memo/create"
echo "   - POST /api/memo/set-pin"
echo "   - GET  /api/memo/statistics" 