#!/bin/bash

# 🚀 UOB-IBM AI Elderly Project Backend Startup Script
# 使用方法: ./start_backend.sh

echo "🚀 Starting UOB-IBM AI Elderly Backend..."
echo "========================================"

# 记录脚本目录绝对路径
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 进入springboot目录
cd "$SCRIPT_DIR/springboot"

# 检查是否在正确目录
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: pom.xml not found. Please make sure you're in the correct directory."
    exit 1
fi

# 检查端口8080是否被占用，并尽力结束相关进程
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Port 8080 is in use. Attempting to stop existing process..."
    # 结束 Maven 启动器
    pkill -f "spring-boot:run" || true
    # 结束 DemoApplication
    pkill -f "com.example.demo.DemoApplication" || true
    # 精确杀掉占用 8080 的进程
    PIDS=$(lsof -ti:8080) || true
    if [ -n "$PIDS" ]; then
        echo "🔪 Killing PIDs: $PIDS"
        kill -9 $PIDS || true
    fi
    sleep 2
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

# 注入 Google Cloud 服务账号（如存在）
KEY_FILE="$SCRIPT_DIR/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
if [ -f "$KEY_FILE" ]; then
  export GOOGLE_APPLICATION_CREDENTIALS="$KEY_FILE"
  echo "🔐 Using Google credentials: $GOOGLE_APPLICATION_CREDENTIALS"
else
  echo "⚠️  Google credentials not found at $KEY_FILE. STT/TTS cloud features will be disabled."
fi

# 启动Spring Boot
mvn -DskipTests spring-boot:run