#!/bin/bash

echo "========================================"
echo "IBM AI Elderly Project - AWS Environment"
echo "========================================"
echo

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17 or later."
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven."
    exit 1
fi

echo "✅ Java and Maven environment checked."

# 设置AWS环境变量
echo "Setting AWS environment variables..."
export SPRING_PROFILES_ACTIVE=aws

# 检查AWS凭证
if [ -z "$AWS_ACCESS_KEY_ID" ]; then
    echo "⚠️  AWS_ACCESS_KEY_ID not set. Please set your AWS credentials."
    echo "You can set them in your environment or create a .env file."
    echo
    echo "Example:"
    echo "export AWS_ACCESS_KEY_ID=your-access-key"
    echo "export AWS_SECRET_ACCESS_KEY=your-secret-key"
    echo "export AWS_REGION=us-east-1"
    echo
    exit 1
fi

if [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    echo "⚠️  AWS_SECRET_ACCESS_KEY not set."
    exit 1
fi

if [ -z "$AWS_REGION" ]; then
    echo "⚠️  AWS_REGION not set. Using default: us-east-1"
    export AWS_REGION=us-east-1
fi

echo "✅ AWS credentials configured."
echo

# 编译项目
echo "🔨 Building project..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ Build failed."
    exit 1
fi

echo "✅ Build successful."
echo

# 启动应用
echo "🚀 Starting application with AWS profile..."
echo "Profile: $SPRING_PROFILES_ACTIVE"
echo "Region: $AWS_REGION"
echo

mvn spring-boot:run -Dspring-boot.run.profiles=aws

echo
echo "Application stopped."

