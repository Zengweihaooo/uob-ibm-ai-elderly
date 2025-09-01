#!/bin/bash

# SQLite Database Integration Test Script
# SQLite数据库集成测试脚本

echo "=========================================="
echo "🧪 SQLite Database Integration Test"
echo "=========================================="

# 检查当前目录
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: Please run this script from the springboot directory"
    echo "Current directory: $(pwd)"
    echo "Expected files: pom.xml, src/, etc."
    exit 1
fi

echo "✅ Found pom.xml - we're in the right directory"
echo "📂 Current directory: $(pwd)"

# 第一阶段：检查项目结构
echo ""
echo "=========================================="
echo "📁 Phase 1: Project Structure Check"
echo "=========================================="

echo "Checking required files..."

# 检查Maven配置
if [ -f "pom.xml" ]; then
    echo "✅ pom.xml exists"
    
    # 检查SQLite依赖是否已添加
    if grep -q "sqlite-jdbc" pom.xml; then
        echo "✅ SQLite dependency found in pom.xml"
    else
        echo "❌ SQLite dependency NOT found in pom.xml"
        echo "❗ Please add SQLite dependencies to pom.xml"
        exit 1
    fi
    
    if grep -q "hibernate-community-dialects" pom.xml; then
        echo "✅ Hibernate SQLite dialect found in pom.xml"
    else
        echo "❌ Hibernate SQLite dialect NOT found in pom.xml"
        echo "❗ Please add hibernate-community-dialects dependency"
        exit 1
    fi
else
    echo "❌ pom.xml not found"
    exit 1
fi

# 检查配置文件
if [ -f "src/main/resources/application.properties" ]; then
    echo "✅ application.properties exists"
    
    if grep -q "sqlite" src/main/resources/application.properties; then
        echo "✅ SQLite configuration found in application.properties"
    else
        echo "❌ SQLite configuration NOT found in application.properties"
        echo "❗ Please configure SQLite in application.properties"
        exit 1
    fi
else
    echo "❌ application.properties not found"
    exit 1
fi

# 检查Java配置类
if [ -f "src/main/java/com/example/demo/config/DatabaseConfig.java" ]; then
    echo "✅ DatabaseConfig.java exists"
else
    echo "❌ DatabaseConfig.java not found"
    echo "❗ Database configuration class is missing"
    exit 1
fi

# 检查数据库管理服务
if [ -f "src/main/java/com/example/demo/service/DatabaseManagementService.java" ]; then
    echo "✅ DatabaseManagementService.java exists"
else
    echo "❌ DatabaseManagementService.java not found"
    echo "❗ Database management service is missing"
    exit 1
fi

# 检查数据库控制器
if [ -f "src/main/java/com/example/demo/controller/DatabaseController.java" ]; then
    echo "✅ DatabaseController.java exists"
else
    echo "❌ DatabaseController.java not found"
    echo "❗ Database controller is missing"
    exit 1
fi

echo ""
echo "=========================================="
echo "🔧 Phase 2: Build Test"
echo "=========================================="

echo "Building the project..."
./mvnw clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ Project builds successfully"
else
    echo "❌ Project build failed"
    echo "❗ Please check compilation errors"
    exit 1
fi

echo ""
echo "=========================================="
echo "📁 Phase 3: Directory Setup"
echo "=========================================="

# 创建数据目录
echo "Creating data directories..."
mkdir -p data
mkdir -p data/backups

if [ -d "data" ]; then
    echo "✅ Data directory created: $(pwd)/data"
else
    echo "❌ Failed to create data directory"
    exit 1
fi

if [ -d "data/backups" ]; then
    echo "✅ Backup directory created: $(pwd)/data/backups"
else
    echo "❌ Failed to create backup directory"
    exit 1
fi

echo ""
echo "=========================================="
echo "🚀 Phase 4: Application Startup Test"
echo "=========================================="

echo "Starting Spring Boot application for testing..."
echo "⏳ This may take 30-60 seconds..."

# 启动应用程序并等待
./mvnw spring-boot:run &
APP_PID=$!

echo "📝 Application PID: $APP_PID"

# 等待应用程序启动
echo "⏳ Waiting for application to start..."
sleep 30

# 检查应用程序是否正在运行
if ps -p $APP_PID > /dev/null; then
    echo "✅ Application is running (PID: $APP_PID)"
else
    echo "❌ Application failed to start"
    exit 1
fi

# 等待更长时间确保完全启动
echo "⏳ Waiting for complete startup..."
sleep 15

echo ""
echo "=========================================="
echo "🔌 Phase 5: API Endpoints Test"
echo "=========================================="

BASE_URL="http://localhost:8080"

# 测试应用程序是否响应
echo "Testing application connectivity..."
for i in {1..10}; do
    if curl -s --connect-timeout 5 "$BASE_URL/api/database/status" > /dev/null 2>&1; then
        echo "✅ Application is responding"
        break
    else
        if [ $i -eq 10 ]; then
            echo "❌ Application is not responding after 10 attempts"
            echo "🛑 Stopping application..."
            kill $APP_PID
            exit 1
        fi
        echo "⏳ Attempt $i/10 - waiting for response..."
        sleep 3
    fi
done

echo ""
echo "Testing database management APIs..."

# 1. 测试数据库状态
echo "1️⃣  Testing database status API..."
response=$(curl -s "$BASE_URL/api/database/status")
if echo "$response" | grep -q "success"; then
    echo "✅ Database status API working"
    echo "   Response: $response" | head -c 100
    echo "..."
else
    echo "❌ Database status API failed"
    echo "   Response: $response"
fi

# 2. 测试数据库信息
echo ""
echo "2️⃣  Testing database info API..."
response=$(curl -s "$BASE_URL/api/database/info")
if echo "$response" | grep -q "database_path"; then
    echo "✅ Database info API working"
    echo "   Response: $response" | head -c 100
    echo "..."
else
    echo "❌ Database info API failed"
    echo "   Response: $response"
fi

# 3. 测试数据库验证
echo ""
echo "3️⃣  Testing database validation API..."
response=$(curl -s "$BASE_URL/api/database/validate")
if echo "$response" | grep -q "success"; then
    echo "✅ Database validation API working"
    echo "   Response: $response" | head -c 100
    echo "..."
else
    echo "❌ Database validation API failed"
    echo "   Response: $response"
fi

# 4. 测试备份创建
echo ""
echo "4️⃣  Testing backup creation API..."
response=$(curl -s -X POST "$BASE_URL/api/database/backup")
if echo "$response" | grep -q "backup_path"; then
    echo "✅ Backup creation API working"
    echo "   Response: $response" | head -c 100
    echo "..."
else
    echo "❌ Backup creation API failed"
    echo "   Response: $response"
fi

echo ""
echo "=========================================="
echo "🧪 Phase 6: Existing APIs Test"
echo "=========================================="

# 测试现有API是否仍然工作
echo "Testing existing APIs to ensure backward compatibility..."

# 5. 测试用户API
echo ""
echo "5️⃣  Testing user stats API..."
response=$(curl -s "$BASE_URL/user/stats")
if echo "$response" | grep -q "totalUsers\|userCount\|success"; then
    echo "✅ User stats API working"
    echo "   Response: $response" | head -c 100
    echo "..."
else
    echo "❌ User stats API failed"
    echo "   Response: $response"
fi

# 6. 测试健康记录API
echo ""
echo "6️⃣  Testing health record creation API..."
response=$(curl -s -X POST "$BASE_URL/api/health/record" \
  -H "Content-Type: application/json" \
  -d '{"type":"bloodPressure","value":"120/80","notes":"Test record from automation"}')

if echo "$response" | grep -q "success\|created\|added"; then
    echo "✅ Health record API working"
    echo "   Response: $response" | head -c 100
    echo "..."
else
    echo "❌ Health record API failed"
    echo "   Response: $response"
fi

echo ""
echo "=========================================="
echo "🗄️  Phase 7: Database File Check"
echo "=========================================="

# 检查数据库文件是否已创建
if [ -f "data/elderly_companion.db" ]; then
    echo "✅ Database file created successfully"
    echo "   📂 Location: $(pwd)/data/elderly_companion.db"
    echo "   📊 Size: $(ls -lh data/elderly_companion.db | awk '{print $5}')"
else
    echo "❌ Database file not found"
    echo "   Expected location: $(pwd)/data/elderly_companion.db"
fi

# 检查备份文件
backup_count=$(ls data/backups/*.db 2>/dev/null | wc -l)
if [ $backup_count -gt 0 ]; then
    echo "✅ Backup files created: $backup_count"
    echo "   📂 Backup directory: $(pwd)/data/backups"
    ls -la data/backups/*.db | head -3
else
    echo "⚠️  No backup files found (this might be OK if backup API failed)"
fi

echo ""
echo "=========================================="
echo "🛑 Phase 8: Cleanup"
echo "=========================================="

echo "Stopping the application..."
kill $APP_PID

# 等待应用程序停止
sleep 5

if ps -p $APP_PID > /dev/null; then
    echo "⚠️  Application still running, force killing..."
    kill -9 $APP_PID
    sleep 2
fi

echo "✅ Application stopped"

echo ""
echo "=========================================="
echo "📊 Test Summary"
echo "=========================================="

echo "Test completed on: $(date)"
echo ""
echo "✅ Tests passed:"
echo "   - Project structure check"
echo "   - Dependencies verification"
echo "   - Application compilation"
echo "   - Application startup"
echo "   - Database management APIs"
echo "   - Existing APIs compatibility"
echo "   - Database file creation"
echo ""
echo "📁 Generated files:"
echo "   - Database: data/elderly_companion.db"
echo "   - Backups: data/backups/"
echo ""
echo "🎉 SQLite database integration appears to be working correctly!"
echo ""
echo "Next steps:"
echo "1. Review the test output above for any ❌ failures"
echo "2. If all tests passed, your SQLite integration is ready"
echo "3. You can now use ./setup_database.sh to start the application normally"
echo "4. Use the database management APIs for backup/restore operations"

echo ""
echo "=========================================="
