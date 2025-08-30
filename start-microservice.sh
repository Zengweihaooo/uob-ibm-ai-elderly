#!/bin/bash

echo "🎤 Starting Voice Command Microservice"
echo "====================================="

# Get the absolute path of the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

echo "📁 Project directory: $PROJECT_DIR"

# Ensure we're in the correct directory
cd "$PROJECT_DIR" || exit 1

# Kill any existing microservice processes
echo "🛑 Stopping existing microservice processes..."
pkill -f "voice-command-microservice" || true
sleep 3

# Start microservice
echo "🚀 Starting microservice..."
cd "$PROJECT_DIR/voice-command-microservice"
nohup ../springboot/mvnw spring-boot:run > "$PROJECT_DIR/microservice.log" 2>&1 &
MICROSERVICE_PID=$!
cd "$PROJECT_DIR"

echo "⏳ Waiting for microservice to start..."
sleep 15

# Test microservice
echo "🧪 Testing microservice..."
if curl -s http://localhost:8090/api/voice-command/health > /dev/null 2>&1; then
    echo "✅ Microservice is running on http://localhost:8090"
    echo "🎉 Microservice started successfully!"
    echo "📋 Process ID: $MICROSERVICE_PID"
    echo "📄 Log: tail -f microservice.log"
    echo "🛑 To stop: kill $MICROSERVICE_PID"
else
    echo "❌ Microservice failed to start - check microservice.log"
    exit 1
fi
