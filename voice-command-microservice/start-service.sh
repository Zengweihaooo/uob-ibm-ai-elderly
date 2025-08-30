#!/bin/bash

echo "🚀 Starting Voice Command Microservice..."
echo "=========================================="

# 检查Java版本
echo "📋 Checking Java version..."
java -version

# 检查Maven是否安装
echo "📋 Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven first."
    exit 1
fi

# 设置环境变量
echo "🔧 Setting environment variables..."
export OPENAI_API_KEY="${OPENAI_API_KEY:-your-openai-api-key}"
export GOOGLE_CLOUD_PROJECT_ID="${GOOGLE_CLOUD_PROJECT_ID:-your-project-id}"
export GOOGLE_APPLICATION_CREDENTIALS="${GOOGLE_APPLICATION_CREDENTIALS:-path/to/credentials.json}"

echo "📧 Email service URL: http://localhost:8080"
echo "🔊 Voice command service will run on: http://localhost:8081"
echo "🤖 OpenAI API Key: ${OPENAI_API_KEY:0:10}..."
echo "☁️  Google Cloud Project: ${GOOGLE_CLOUD_PROJECT_ID}"

# 清理并编译
echo "🔨 Cleaning and compiling..."
mvn clean compile

# 启动服务
echo "🚀 Starting service..."
mvn spring-boot:run

echo "✅ Service started successfully!"
echo "🌐 Access the service at: http://localhost:8081"
echo "📚 API Documentation: http://localhost:8081/swagger-ui.html"
