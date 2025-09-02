#!/bin/bash

echo "🛑 Stopping UOB-IBM AI Elderly Project Services"
echo "================================================"

# Get the absolute path of the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

echo "📁 Project directory: $PROJECT_DIR"

# Stop all Spring Boot processes
echo "🔄 Stopping Spring Boot services..."
pkill -f "spring-boot:run" || true

# Stop frontend HTTP server
echo "🔄 Stopping frontend server..."
pkill -f "python3 -m http.server" || true

# Wait for processes to stop
echo "⏳ Waiting for processes to stop..."
sleep 3

# Check if ports are still in use
echo "📊 Checking port status..."

# Check backend port 8080
if lsof -i :8080 > /dev/null 2>&1; then
    echo "⚠️  Port 8080 still in use, force killing..."
    lsof -ti :8080 | xargs kill -9
else
    echo "✅ Port 8080 is free"
fi

# Check microservice port 8090
if lsof -i :8090 > /dev/null 2>&1; then
    echo "⚠️  Port 8090 still in use, force killing..."
    lsof -ti :8090 | xargs kill -9
else
    echo "✅ Port 8090 is free"
fi

# Check frontend port 3000
if lsof -i :3000 > /dev/null 2>&1; then
    echo "⚠️  Port 3000 still in use, force killing..."
    lsof -ti :3000 | xargs kill -9
else
    echo "✅ Port 3000 is free"
fi

echo ""
echo "🎊 All services stopped!"
echo "================================================"
echo "📋 To restart services:"
echo "   ./start_all.sh"
echo ""
echo "📋 To start individual services:"
echo "   Backend only: ./start_backend.sh"
echo "   Frontend only: ./start_frontend.sh"
echo "   Microservice only: ./start-microservice.sh"
echo ""
