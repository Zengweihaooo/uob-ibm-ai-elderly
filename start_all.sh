#!/bin/bash

echo "🚀 Starting UOB-IBM AI Elderly Project"
echo "======================================"

# Get the absolute path of the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

echo "📁 Project directory: $PROJECT_DIR"

# Ensure we're in the correct directory
cd "$PROJECT_DIR" || exit 1

# Kill any existing processes
echo "🛑 Stopping existing processes..."
pkill -f "spring-boot:run" || true
pkill -f "python3 -m http.server" || true
sleep 3

# Set Google Cloud credentials from the specified path
CREDS_FILE="/Users/zengweihao/Downloads/keys/organic-totem-467918-a5-d17504cd5eba.json"
if [ -f "$CREDS_FILE" ]; then
    echo "🔐 Using Google credentials from: $CREDS_FILE"
    export GOOGLE_APPLICATION_CREDENTIALS="$CREDS_FILE"
else
    echo "⚠️  Google credentials file not found at $CREDS_FILE. STT/TTS cloud features will be disabled."
fi

# Start backend
echo "🔧 Starting backend (Spring Boot)..."
cd "$PROJECT_DIR/springboot"
nohup ./mvnw -DskipTests spring-boot:run > "$PROJECT_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
cd "$PROJECT_DIR"

# Start voice command microservice
echo "🎤 Starting voice command microservice..."
cd "$PROJECT_DIR/voice-command-microservice"
nohup ../springboot/mvnw spring-boot:run > "$PROJECT_DIR/microservice.log" 2>&1 &
MICROSERVICE_PID=$!
cd "$PROJECT_DIR"

# Start frontend
echo "🌐 Starting frontend (HTTP Server)..."
nohup python3 -m http.server 3000 > "$PROJECT_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!

echo "⏳ Waiting for services to start..."
sleep 15

# Test services
echo "🧪 Testing services..."

# Test backend
if curl -s http://localhost:8080/api/voice/status > /dev/null 2>&1; then
    echo "✅ Backend is running on http://localhost:8080"
else
    echo "❌ Backend failed to start - check backend.log"
fi

# Test microservice
if curl -s http://localhost:8090/api/voice-command/health > /dev/null 2>&1; then
    echo "✅ Microservice is running on http://localhost:8090"
else
    echo "❌ Microservice failed to start - check microservice.log"
fi

# Test frontend
if curl -s http://localhost:3000/ > /dev/null 2>&1; then
    echo "✅ Frontend is running on http://localhost:3000"
else
    echo "❌ Frontend failed to start - check frontend.log"
fi

echo ""
echo "🎉 Services started!"
echo "📱 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:8080"
echo "🎤 Microservice: http://localhost:8090"
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