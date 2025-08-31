#!/bin/bash

# SMS集成测试脚本
# 用于测试IBM AI老年人项目的SMS功能

echo "🚀 启动SMS集成测试"
echo "=================================="
echo "时间: $(date)"
echo ""

# 检查Spring Boot应用是否运行
check_service() {
    echo "🔍 检查Spring Boot服务状态..."
    if curl -s -f http://localhost:8080/api/sms/statistics > /dev/null 2>&1; then
        echo "✅ Spring Boot服务正在运行"
        return 0
    else
        echo "❌ Spring Boot服务未运行"
        return 1
    fi
}

# 启动Spring Boot应用（如果需要）
start_service() {
    echo "🚀 启动Spring Boot应用..."
    cd springboot
    if [ -f "mvnw" ]; then
        ./mvnw spring-boot:run &
    elif [ -f "mvnw.cmd" ]; then
        ./mvnw.cmd spring-boot:run &
    else
        mvn spring-boot:run &
    fi
    
    echo "⏳ 等待服务启动..."
    for i in {1..30}; do
        if curl -s -f http://localhost:8080/api/sms/statistics > /dev/null 2>&1; then
            echo "✅ 服务启动成功"
            cd ..
            return 0
        fi
        sleep 2
        echo "等待中... ($i/30)"
    done
    
    echo "❌ 服务启动超时"
    cd ..
    return 1
}

# 测试SMS发送功能
test_sms_send() {
    echo ""
    echo "📱 测试SMS发送功能..."
    
    response=$(curl -s -X POST http://localhost:8080/api/sms/send \
        -H "Content-Type: application/json" \
        -d '{
            "phoneNumber": "+8613800138000",
            "message": "SMS集成测试消息",
            "messageType": "TEST"
        }')
    
    if echo "$response" | grep -q '"success":true'; then
        echo "✅ SMS发送测试通过"
        echo "响应: $response"
    else
        echo "❌ SMS发送测试失败"
        echo "响应: $response"
    fi
}

# 测试健康警报SMS
test_health_alert() {
    echo ""
    echo "🏥 测试健康警报SMS..."
    
    response=$(curl -s -X POST http://localhost:8080/api/sms/health-alert \
        -H "Content-Type: application/json" \
        -d '{
            "phoneNumber": "+8613800138000",
            "healthData": "测试健康数据：血压正常"
        }')
    
    if echo "$response" | grep -q '"success":true'; then
        echo "✅ 健康警报SMS测试通过"
        echo "响应: $response"
    else
        echo "❌ 健康警报SMS测试失败"
        echo "响应: $response"
    fi
}

# 测试紧急SMS
test_emergency() {
    echo ""
    echo "🚨 测试紧急SMS..."
    
    response=$(curl -s -X POST http://localhost:8080/api/sms/emergency \
        -H "Content-Type: application/json" \
        -d '{
            "phoneNumber": "+8613800138000",
            "emergencyInfo": "测试紧急情况：系统测试"
        }')
    
    if echo "$response" | grep -q '"success":true'; then
        echo "✅ 紧急SMS测试通过"
        echo "响应: $response"
    else
        echo "❌ 紧急SMS测试失败"
        echo "响应: $response"
    fi
}

# 测试SMS历史记录
test_sms_history() {
    echo ""
    echo "📋 测试SMS历史记录..."
    
    response=$(curl -s http://localhost:8080/api/sms/history)
    
    if echo "$response" | grep -q '"success":true'; then
        echo "✅ SMS历史记录测试通过"
        total=$(echo "$response" | grep -o '"total":[0-9]*' | cut -d':' -f2)
        echo "历史记录数量: $total"
    else
        echo "❌ SMS历史记录测试失败"
        echo "响应: $response"
    fi
}

# 测试SMS统计信息
test_sms_statistics() {
    echo ""
    echo "📊 测试SMS统计信息..."
    
    response=$(curl -s http://localhost:8080/api/sms/statistics)
    
    if echo "$response" | grep -q '"success":true'; then
        echo "✅ SMS统计信息测试通过"
        echo "响应: $response"
    else
        echo "❌ SMS统计信息测试失败"
        echo "响应: $response"
    fi
}

# 主函数
main() {
    # 检查curl是否可用
    if ! command -v curl &> /dev/null; then
        echo "❌ curl命令未找到，请先安装curl"
        exit 1
    fi
    
    # 检查服务状态
    if ! check_service; then
        echo "⚠️  Spring Boot服务未运行，尝试启动..."
        if ! start_service; then
            echo "❌ 无法启动Spring Boot服务，请手动启动后重试"
            exit 1
        fi
    fi
    
    # 执行所有测试
    test_sms_send
    sleep 1
    
    test_health_alert
    sleep 1
    
    test_emergency
    sleep 1
    
    test_sms_history
    sleep 1
    
    test_sms_statistics
    
    echo ""
    echo "=================================="
    echo "🎉 SMS集成测试完成！"
    echo "时间: $(date)"
    echo "=================================="
}

# 运行主函数
main
