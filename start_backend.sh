#!/bin/bash

# 🚀 UOB-IBM AI Elderly Project Backend Startup Script
# 使用方法: ./start_backend.sh

echo "🚀 Starting UOB-IBM AI Elderly Backend..."
echo "========================================"

# 进入springboot目录
cd "$(dirname "$0")/springboot"

# 检查是否在正确目录
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: pom.xml not found. Please make sure you're in the correct directory."
    exit 1
fi

# 检查端口8080是否被占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Port 8080 is already in use. Stopping existing process..."
    pkill -f "spring-boot:run"
    sleep 3
fi

echo "🔧 Starting Spring Boot application..."
echo "📍 Directory: $(pwd)"
echo "⏰ Starting at: $(date)"
echo ""
echo "🌐 Once started, you can access:"
echo "   Main Page: http://localhost:5500/index.html"
echo "   Schedule:  http://localhost:5500/src/pages/schedule.html"
echo "   Register:  http://localhost:5500/src/pages/register.html"
echo ""
echo "📱 Backend APIs will be available at: http://localhost:8080/api/"
echo ""
echo "⏹️  To stop the server, press Ctrl+C"
echo "========================================"
echo ""

# 启动Spring Boot
mvn spring-boot:run 