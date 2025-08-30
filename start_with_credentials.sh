#!/bin/bash

# IBM AI Elderly Project - 启动脚本（带Google Cloud凭据）
# Start script with Google Cloud credentials

echo "=== IBM AI Elderly Project Startup ==="

# 设置Google Cloud凭据
export GOOGLE_APPLICATION_CREDENTIALS="/Users/zengweihao/Downloads/keys/organic-totem-467918-a5-d17504cd5eba.json"
echo "✅ TTS/STT凭据已设置: $GOOGLE_APPLICATION_CREDENTIALS"

# 检查凭据文件是否存在
if [ ! -f "$GOOGLE_APPLICATION_CREDENTIALS" ]; then
    echo "❌ 错误: 凭据文件不存在: $GOOGLE_APPLICATION_CREDENTIALS"
    exit 1
fi

echo "✅ 凭据文件存在且可读"

# 停止现有服务
echo "🛑 停止现有服务..."
pkill -f "demo-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
pkill -f "live-server" 2>/dev/null || true
sleep 3

# 启动后端服务
echo "🚀 启动后端服务..."
cd springboot
java -jar target/demo-0.0.1-SNAPSHOT.jar > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo "✅ 后端服务已启动 (PID: $BACKEND_PID)"

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 10

# 测试后端服务
echo "🧪 测试后端服务..."
if curl -s http://localhost:8080/api/voice/status > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务启动失败"
fi

# 启动微服务
echo "🎤 启动Voice Command微服务..."
cd voice-command-microservice
nohup ../springboot/mvnw spring-boot:run > /tmp/microservice.log 2>&1 &
MICROSERVICE_PID=$!
echo "✅ 微服务已启动 (PID: $MICROSERVICE_PID)"

# 等待微服务启动
echo "⏳ 等待微服务启动..."
sleep 10

# 测试微服务
echo "🧪 测试微服务..."
if curl -s http://localhost:8090/api/voice-command/health > /dev/null; then
    echo "✅ 微服务正常运行"
else
    echo "❌ 微服务启动失败"
fi

# 启动前端服务
echo "🌐 启动前端服务..."
cd ..
npx live-server --port=5500 --host=127.0.0.1 --no-browser --cors > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "✅ 前端服务已启动 (PID: $FRONTEND_PID)"

echo ""
echo "🎉 所有服务已启动完成！"
echo ""
echo "📱 访问地址："
echo "   🏠 主页: http://127.0.0.1:5500/"
echo "   🔐 登录: http://127.0.0.1:5500/src/pages/register.html"
echo "   🤖 AI助手: http://127.0.0.1:5500/src/pages/ai-assistant.html"
echo ""
echo "🔧 后端API："
echo "   📊 状态: http://localhost:8080/api/voice/status"
echo "   🎙️  语音识别: http://localhost:8080/api/voice/stt"
echo "   🔊 文字转语音: http://localhost:8080/api/voice/tts"
echo ""
echo "🎤 微服务API："
echo "   🤖 AI意图分析: http://localhost:8090/api/voice-command/text"
echo "   🎙️  语音处理: http://localhost:8090/api/voice-command/process"
echo "   💚 健康检查: http://localhost:8090/api/voice-command/health"
echo ""
echo "📋 进程信息："
echo "   后端PID: $BACKEND_PID"
echo "   微服务PID: $MICROSERVICE_PID"
echo "   前端PID: $FRONTEND_PID"
echo ""
echo "📜 日志文件："
echo "   后端日志: /tmp/backend.log"
echo "   微服务日志: /tmp/microservice.log"
echo "   前端日志: /tmp/frontend.log"
echo ""
echo "⚠️  注意: 请保持终端窗口打开以维持服务运行"
