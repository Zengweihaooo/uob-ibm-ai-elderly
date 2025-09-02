#!/bin/bash

echo "🚀 Starting UOB-IBM AI Elderly Services with Gemini"
echo "=================================================="

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

# Set Google Cloud credentials
CREDS_FILE="/Users/zengweihao/Downloads/keys/organic-totem-467918-a5-d17504cd5eba.json"
if [ -f "$CREDS_FILE" ]; then
    echo "🔐 Using Google credentials from: $CREDS_FILE"
    export GOOGLE_APPLICATION_CREDENTIALS="$CREDS_FILE"
else
    echo "⚠️  Google credentials file not found at $CREDS_FILE"
    echo "   STT/TTS and Gemini cloud features will be disabled."
fi

# Start backend
echo "🔧 Starting backend (Spring Boot with Gemini)..."
cd "$PROJECT_DIR/springboot"
nohup mvn -DskipTests spring-boot:run > "$PROJECT_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
cd "$PROJECT_DIR" # Return to project root

# Start frontend
echo "🌐 Starting frontend (HTTP Server)..."
nohup python3 -m http.server 3000 > "$PROJECT_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!

echo "⏳ Waiting for services to start..."
sleep 20

# Test services
echo "🧪 Testing services..."

# Test backend basic health
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Backend health check passed"
else
    echo "⚠️  Backend health check failed, trying voice API..."
    if curl -s http://localhost:8080/api/voice/status > /dev/null 2>&1; then
        echo "✅ Backend voice API is running"
    else
        echo "❌ Backend failed to start properly"
    fi
fi

# Test Gemini API
if curl -s http://localhost:8080/api/gemini/status > /dev/null 2>&1; then
    echo "✅ Gemini API is ready"
    echo "🤖 Gemini Status:"
    curl -s http://localhost:8080/api/gemini/status | head -3
    echo ""
else
    echo "⚠️  Gemini API not accessible yet (may still be starting)"
fi

# Test frontend
if curl -s http://localhost:3000/ > /dev/null 2>&1; then
    echo "✅ Frontend is running on http://localhost:3000"
else
    echo "❌ Frontend failed to start"
fi

echo ""
echo "🎉 Services started!"
echo "=================================================="
echo "📱 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:8080"
echo "🤖 AI Assistant: http://localhost:3000/src/pages/ai-assistant.html"
echo "📅 Schedule: http://localhost:3000/src/pages/schedule.html"
echo "🐾 Pet: http://localhost:3000/src/pages/pet.html"
echo ""
echo "📋 Process IDs:"
echo "   Backend: $BACKEND_PID"
echo "   Frontend: $FRONTEND_PID"
echo ""
echo "📄 Logs:"
echo "   Backend: tail -f backend.log"
echo "   Frontend: tail -f frontend.log"
echo ""
echo "🛑 To stop services:"
echo "   kill $BACKEND_PID $FRONTEND_PID"
echo "=================================================="
