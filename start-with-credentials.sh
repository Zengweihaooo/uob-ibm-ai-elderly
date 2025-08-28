#!/bin/bash

echo "🚀 Starting UOB-IBM AI Elderly Project with Google Cloud Credentials"
echo "=================================================================="
echo "📁 Project directory: $(pwd)"
echo "🔑 Google Cloud credentials: $(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
echo "🔑 Google Cloud backup credentials: $(pwd)/docs/keys/organic-totem-467918-a5-6497b0d6925f.json"
echo "☁️  Google Cloud project: organic-totem-467918-a5"
echo ""

# 检查密钥文件是否存在
if [ ! -f "docs/keys/organic-totem-467918-a5-d17504cd5eba.json" ]; then
    echo "❌ Google Cloud credentials file not found at: $(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
    exit 1
fi

if [ ! -f "docs/keys/organic-totem-467918-a5-6497b0d6925f.json" ]; then
    echo "❌ Google Cloud backup credentials file not found at: $(pwd)/docs/keys/organic-totem-467918-a5-6497b0d6925f.json"
    exit 1
fi

echo "✅ Google Cloud credentials found and loaded"
echo "✅ Google Cloud backup credentials found and loaded"
echo ""

# 设置Google Cloud凭证环境变量
# 主密钥文件 - 用于AI服务 (Gemini, TTS, STT)
export GOOGLE_APPLICATION_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
export GOOGLE_AI_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
export GOOGLE_SPEECH_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
export GOOGLE_TTS_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"

# 备用密钥文件 - 用于计算引擎和其他服务
export GOOGLE_BACKUP_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-6497b0d6925f.json"
export GOOGLE_COMPUTE_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-6497b0d6925f.json"

export GOOGLE_CLOUD_PROJECT_ID="organic-totem-467918-a5"

echo "🛑 Stopping existing processes..."
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "voice-command-microservice" 2>/dev/null
pkill -f "python3 -m http.server" 2>/dev/null
sleep 2

echo "🔧 Starting backend (Spring Boot)..."
cd springboot
nohup ../springboot/mvnw spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
cd ..

echo "🎤 Starting voice command microservice..."
cd voice-command-microservice
nohup ../springboot/mvnw spring-boot:run > ../microservice.log 2>&1 &
MICROSERVICE_PID=$!
cd ..

echo "🌐 Starting frontend (HTTP Server)..."
nohup python3 -m http.server 3000 > ../frontend.log 2>&1 &
FRONTEND_PID=$!

echo "⏳ Waiting for services to start..."
sleep 10

echo "🧪 Testing services..."
sleep 5

# 测试后端
if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "✅ Backend is running on http://localhost:8080"
else
    echo "❌ Backend failed to start"
fi

# 测试微服务
if curl -s http://localhost:8081/api/voice-command/health > /dev/null 2>&1; then
    echo "✅ Microservice is running on http://localhost:8081"
else
    echo "❌ Microservice failed to start"
fi

# 测试前端
if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Frontend is running on http://localhost:3000"
else
    echo "❌ Frontend failed to start"
fi

echo ""
echo "🎉 Services started with Google Cloud credentials!"
echo "📱 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:8080"
echo "🎤 Microservice: http://localhost:8081"
echo "🧪 Voice Test: http://localhost:3000/voice-test.html"
echo "🤖 AI Assistant: http://localhost:3000/src/pages/ai-assistant.html"
echo ""
echo "📋 Process IDs:"
echo "   Backend: $BACKEND_PID"
echo "   Microservice: $MICROSERVICE_PID"
echo "   Frontend: $FRONTEND_PID"
echo ""
echo "📄 Logs:"
echo "   Backend: tail -f backend.log"
echo "   Microservice: tail -f microservice.log"
echo "   Frontend: tail -f frontend.log"
echo ""
echo "🛑 To stop services:"
echo "   kill $BACKEND_PID $MICROSERVICE_PID $FRONTEND_PID"
echo ""
echo "🔑 Environment variables set:"
echo "   GOOGLE_APPLICATION_CREDENTIALS=$GOOGLE_APPLICATION_CREDENTIALS"
echo "   GOOGLE_AI_CREDENTIALS=$GOOGLE_AI_CREDENTIALS"
echo "   GOOGLE_SPEECH_CREDENTIALS=$GOOGLE_SPEECH_CREDENTIALS"
echo "   GOOGLE_TTS_CREDENTIALS=$GOOGLE_TTS_CREDENTIALS"
echo "   GOOGLE_BACKUP_CREDENTIALS=$GOOGLE_BACKUP_CREDENTIALS"
echo "   GOOGLE_COMPUTE_CREDENTIALS=$GOOGLE_COMPUTE_CREDENTIALS"
echo "   GOOGLE_CLOUD_PROJECT_ID=$GOOGLE_CLOUD_PROJECT_ID"
echo ""
echo "🔑 密钥文件配置说明:"
echo "   📁 主密钥文件: organic-totem-467918-a5-d17504cd5eba.json"
echo "      - 用途: AI服务 (Gemini, TTS, STT)"
echo "      - 服务账号: university-of-bristol@organic-totem-467918-a5.iam.gserviceaccount.com"
echo "      - 特点: 专用服务账号，权限明确"
echo ""
echo "   📁 备用密钥文件: organic-totem-467918-a5-6497b0d6925f.json"
echo "      - 用途: 计算引擎和其他Google Cloud服务"
echo "      - 服务账号: 752873448452-compute@developer.gserviceaccount.com"
echo "      - 特点: 计算引擎默认账号，通用权限"
echo ""
echo "🎯 双密钥策略:"
echo "   - 主密钥: 专门用于AI相关服务，更安全"
echo "   - 备用密钥: 用于其他Google Cloud服务，权限更广泛"
echo "   - 自动故障转移: 如果主密钥失败，系统会尝试使用备用密钥"
