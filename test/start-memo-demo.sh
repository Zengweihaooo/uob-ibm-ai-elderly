#!/bin/bash

# 备忘录功能演示启动脚本
# 作者: AI Assistant
# 日期: 2024

echo "=========================================="
echo "📝 Memo Function Demo Startup Script"
echo "=========================================="

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java not found, please install Java 8 or higher first"
    exit 1
fi

# 检查Java版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
echo "✅ Detected Java version: $JAVA_VERSION"

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven not found, please install Maven first"
    exit 1
fi

echo "✅ Detected Maven"

# 进入Spring Boot项目目录
cd springboot

echo "🔧 Compiling project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed, please check project configuration"
    exit 1
fi

echo "✅ Compilation successful"

echo "🚀 Starting Spring Boot application..."
echo "📝 Memo function will be available at:"
echo "   - Main page: http://localhost:8080/src/pages/memo.html"
echo "   - Test page: http://localhost:8080/src/pages/memo-test.html"
echo ""
echo "Press Ctrl+C to stop server"
echo "=========================================="

# 启动Spring Boot应用
mvn spring-boot:run 