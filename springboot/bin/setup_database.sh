#!/bin/bash

# Database Setup and Testing Script
# 数据库设置和测试脚本

echo "==================================="
echo "SQLite Database Setup for IBM AI Elderly Care System"
echo "==================================="

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "Error: Please run this script from the springboot directory"
    exit 1
fi

# Create data directory if it doesn't exist
echo "Creating data directories..."
mkdir -p data
mkdir -p data/backups

echo "Data directories created:"
echo "- Database: $(pwd)/data/"
echo "- Backups: $(pwd)/data/backups/"

# Build the project
echo ""
echo "Building the project..."
./mvnw clean compile

if [ $? -ne 0 ]; then
    echo "Error: Failed to build project"
    exit 1
fi

echo "Project built successfully!"

# Start the application
echo ""
echo "Starting the Spring Boot application..."
echo "The database will be automatically created on first startup."
echo ""
echo "API Endpoints available:"
echo "- Database Status: GET  http://localhost:8080/api/database/status"
echo "- Create Backup:   POST http://localhost:8080/api/database/backup"
echo "- Database Info:   GET  http://localhost:8080/api/database/info"
echo ""
echo "Press Ctrl+C to stop the application"
echo "==================================="

./mvnw spring-boot:run
