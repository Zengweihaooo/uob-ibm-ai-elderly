#!/bin/bash

echo "🌐 Starting Frontend Server..."
echo "=============================="

# Get the absolute path of the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "📁 Project directory: $SCRIPT_DIR"

# Ensure we're in the correct directory
cd "$SCRIPT_DIR" || exit 1

# Kill any existing processes on port 3000
echo "🛑 Stopping existing processes on port 3000..."
pkill -f "python3 -m http.server 3000" || true
sleep 2

# Start frontend server
echo "🚀 Starting frontend HTTP server on port 3000..."
echo "📍 Serving from: $(pwd)"
echo ""
echo "🌐 Frontend URLs:"
echo "   Main Page: http://localhost:3000/index.html"
echo "   Schedule:  http://localhost:3000/src/pages/schedule.html"
echo "   AI Assistant: http://localhost:3000/src/pages/ai-assistant.html"
echo "   Voice Test: http://localhost:3000/voice-test.html"
echo ""
echo "⏹️  To stop the server, press Ctrl+C"
echo "=============================="
echo ""

python3 -m http.server 3000