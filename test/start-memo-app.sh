#!/bin/bash

echo "🚀 Starting IBM AI Elderly Memo Application..."

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed or not in PATH"
    exit 1
fi

# 进入Spring Boot目录
cd springboot

echo "📦 Building application..."
mvn clean compile

echo "🌐 Starting Spring Boot application..."
echo "📱 Memo application will be available at: http://localhost:8080/pages/memo.html"
echo "🔌 API endpoints available at: http://localhost:8080/api/*"
echo ""
echo "Press Ctrl+C to stop the application"

# 启动应用
mvn spring-boot:run 