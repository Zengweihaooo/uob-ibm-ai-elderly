#!/bin/bash

# UOB-IBM AI Elderly Project Quick Start Script
# This script starts all necessary services for the project

echo "🚀 Starting UOB-IBM AI Elderly Project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        print_error "Java version $java_version is too old. Please install Java 17 or higher."
        exit 1
    fi
    
    print_success "Java version check passed"
}

# Check if Python is installed
check_python() {
    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is not installed. Please install Python 3."
        exit 1
    fi
    
    print_success "Python 3 check passed"
}

# Kill existing processes on ports 8080 and 3000
kill_existing_processes() {
    print_status "Checking for existing processes..."
    
    # Kill process on port 8080 (backend)
    if lsof -ti:8080 > /dev/null 2>&1; then
        print_warning "Killing existing process on port 8080"
        lsof -ti:8080 | xargs kill -9
    fi
    
    # Kill process on port 3000 (frontend)
    if lsof -ti:3000 > /dev/null 2>&1; then
        print_warning "Killing existing process on port 3000"
        lsof -ti:3000 | xargs kill -9
    fi
    
    sleep 2
}

# Start backend service
start_backend() {
    print_status "Starting backend service (Spring Boot)..."
    
    cd springboot
    
    # Check if Maven wrapper exists
    if [ ! -f "./mvnw" ]; then
        print_error "Maven wrapper not found. Please ensure you're in the correct directory."
        exit 1
    fi
    
    # Make mvnw executable
    chmod +x ./mvnw
    
    # Start Spring Boot application
    nohup ./mvnw spring-boot:run > ../backend.log 2>&1 &
    BACKEND_PID=$!
    
    cd ..
    
    print_status "Backend service starting (PID: $BACKEND_PID)"
    print_status "Backend logs: backend.log"
}

# Start frontend service
start_frontend() {
    print_status "Starting frontend service (HTTP Server)..."
    
    # Start Python HTTP server
    nohup python3 -m http.server 3000 > frontend.log 2>&1 &
    FRONTEND_PID=$!
    
    print_status "Frontend service starting (PID: $FRONTEND_PID)"
    print_status "Frontend logs: frontend.log"
}

# Wait for services to start
wait_for_services() {
    print_status "Waiting for services to start..."
    
    # Wait for backend
    local backend_ready=false
    for i in {1..30}; do
        if curl -s http://localhost:8080/user/api/register > /dev/null 2>&1; then
            backend_ready=true
            break
        fi
        sleep 1
    done
    
    if [ "$backend_ready" = true ]; then
        print_success "Backend service is ready"
    else
        print_error "Backend service failed to start"
        exit 1
    fi
    
    # Wait for frontend
    local frontend_ready=false
    for i in {1..10}; do
        if curl -s http://localhost:3000 > /dev/null 2>&1; then
            frontend_ready=true
            break
        fi
        sleep 1
    done
    
    if [ "$frontend_ready" = true ]; then
        print_success "Frontend service is ready"
    else
        print_error "Frontend service failed to start"
        exit 1
    fi
}

# Run health check
run_health_check() {
    print_status "Running health check..."
    
    # Test backend API
    if curl -s -X POST "http://localhost:8080/user/api/register" \
        -d "email=healthcheck@test.com" \
        -H "Content-Type: application/x-www-form-urlencoded" > /dev/null 2>&1; then
        print_success "Backend API is responding"
    else
        print_error "Backend API is not responding"
        return 1
    fi
    
    # Test frontend
    if curl -s http://localhost:3000 > /dev/null 2>&1; then
        print_success "Frontend is accessible"
    else
        print_error "Frontend is not accessible"
        return 1
    fi
    
    return 0
}

# Display service information
display_info() {
    echo ""
    echo "🎉 All services are running successfully!"
    echo ""
    echo "📱 Service Information:"
    echo "   Backend (Spring Boot): http://localhost:8080"
    echo "   Frontend (HTTP Server): http://localhost:3000"
    echo ""
    echo "🌐 Access URLs:"
    echo "   Main Page: http://localhost:3000/"
    echo "   Register Page: http://localhost:3000/src/pages/register.html"
    echo "   Test Page: http://localhost:3000/test_register.html"
    echo ""
    echo "📋 Useful Commands:"
    echo "   View backend logs: tail -f backend.log"
    echo "   View frontend logs: tail -f frontend.log"
    echo "   Run tests: python3 test_complete_registration.py"
    echo "   Stop services: ./stop_all.sh"
    echo ""
    echo "🔧 Process IDs:"
    echo "   Backend PID: $BACKEND_PID"
    echo "   Frontend PID: $FRONTEND_PID"
    echo ""
}

# Main execution
main() {
    echo "=========================================="
    echo "UOB-IBM AI Elderly Project Quick Start"
    echo "=========================================="
    echo ""
    
    # Check prerequisites
    check_java
    check_python
    
    # Kill existing processes
    kill_existing_processes
    
    # Start services
    start_backend
    start_frontend
    
    # Wait for services
    wait_for_services
    
    # Health check
    if run_health_check; then
        display_info
        print_success "System is ready for use!"
    else
        print_error "Health check failed. Please check the logs."
        exit 1
    fi
}

# Run main function
main "$@"
