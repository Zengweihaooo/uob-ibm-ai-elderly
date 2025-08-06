#!/bin/bash

# Listen Notes API 测试脚本

API_KEY="a3432f0d55d940e3bbe3d18f7acdeea6"
BASE_URL="https://listen-api.listennotes.com/api/v2"

echo "🔍 测试 Listen Notes API 连接..."
echo "API密钥: $API_KEY"
echo "基础URL: $BASE_URL"
echo ""

# 测试1: 搜索播客
echo "📡 测试1: 搜索播客 (health)"
curl -s -H "X-ListenAPI-Key: $API_KEY" \
     "$BASE_URL/search?q=health&type=podcast&limit=1" | jq '.count // "Error: API调用失败"'

echo ""
echo ""

# 测试2: 检查API密钥状态
echo "🔑 测试2: 检查API密钥状态"
response=$(curl -s -w "%{http_code}" -H "X-ListenAPI-Key: $API_KEY" \
     "$BASE_URL/search?q=test&type=podcast&limit=1")

http_code="${response: -3}"
body="${response%???}"

echo "HTTP状态码: $http_code"

if [ "$http_code" = "200" ]; then
    echo "✅ API密钥有效"
elif [ "$http_code" = "401" ]; then
    echo "❌ API密钥无效或已过期"
elif [ "$http_code" = "429" ]; then
    echo "⚠️  请求频率超限"
else
    echo "❓ 未知错误: $http_code"
fi

echo ""
echo ""

# 测试3: 检查热门播客端点是否存在
echo "🔥 测试3: 检查热门播客端点"
curl -s -H "X-ListenAPI-Key: $API_KEY" \
     "$BASE_URL/podcasts/trending" | jq '.error // "端点不存在或返回错误"'

echo ""
echo ""

echo "📋 测试完成！"
echo ""
echo "💡 建议:"
echo "1. 如果API密钥无效，请访问 https://www.listennotes.com/api/ 获取新密钥"
echo "2. 如果请求被限制，请检查请求频率或升级计划"
echo "3. 如果端点不存在，请查看官方文档" 