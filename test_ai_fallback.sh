#!/bin/bash

# AI故障转移测试脚本
# 测试微服务和主项目的AI调用故障转移机制

echo "=== AI故障转移测试脚本 ==="
echo "测试时间: $(date)"
echo

# 配置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_ai_service() {
    local service_name=$1
    local url=$2
    local test_message=$3

    echo -e "${YELLOW}测试 $service_name...${NC}"
    echo "URL: $url"
    echo "测试消息: $test_message"

    # 发送测试请求
    response=$(curl -s -X POST "$url" \
        -H "Content-Type: application/json" \
        -d "{\"message\": \"$test_message\"}" \
        --connect-timeout 10 \
        --max-time 30)

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $service_name 响应成功${NC}"
        echo "响应内容: $response"
        echo
        return 0
    else
        echo -e "${RED}✗ $service_name 响应失败${NC}"
        echo "错误信息: $response"
        echo
        return 1
    fi
}

test_ai_status() {
    local service_name=$1
    local url=$2

    echo -e "${YELLOW}检查 $service_name 状态...${NC}"
    echo "URL: $url"

    response=$(curl -s "$url" \
        --connect-timeout 5 \
        --max-time 10)

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $service_name 状态正常${NC}"
        echo "状态信息: $response"
        echo
        return 0
    else
        echo -e "${RED}✗ $service_name 状态异常${NC}"
        echo "错误信息: $response"
        echo
        return 1
    fi
}

# 1. 检查服务是否运行
echo "=== 步骤1: 检查服务状态 ==="

# 检查主项目
test_ai_status "主项目AI服务" "http://localhost:8080/api/gemini/status"

# 检查微服务
test_ai_status "微服务AI状态" "http://localhost:8090/api/ai/status"

echo "=== 步骤2: 测试AI聊天功能 ==="

# 测试消息
TEST_MESSAGE="你好，请介绍一下自己"

# 测试主项目AI
test_ai_service "主项目AI" "http://localhost:8080/api/gemini/chat" "$TEST_MESSAGE"

# 测试微服务AI
test_ai_service "微服务AI" "http://localhost:8090/api/ai/test" "$TEST_MESSAGE"

echo "=== 步骤3: 测试故障转移机制 ==="

echo -e "${YELLOW}模拟主项目AI服务不可用...${NC}"

# 临时停止主项目（如果正在运行）
MAIN_PROJECT_PID=$(lsof -t -i:8080 2>/dev/null)
if [ ! -z "$MAIN_PROJECT_PID" ]; then
    echo "停止主项目服务 (PID: $MAIN_PROJECT_PID)..."
    kill $MAIN_PROJECT_PID
    sleep 3
fi

# 测试微服务是否能回退到本地模式
test_ai_service "微服务故障转移" "http://localhost:8090/api/ai/test" "$TEST_MESSAGE"

echo "=== 步骤4: 测试结果总结 ==="

echo -e "${GREEN}测试完成！${NC}"
echo "如果微服务在主项目不可用时仍能正常响应，则故障转移机制工作正常。"
echo
echo "=== 故障转移流程 ==="
echo "1. 微服务尝试调用自己的AI服务"
echo "2. 如果失败，回退到主项目AI服务"
echo "3. 如果主项目也不可用，使用本地fallback回复"
echo
echo "=== 配置说明 ==="
echo "微服务默认使用主项目AI，确保高可用性"
echo "可以通过以下端点切换模式："
echo "POST http://localhost:8090/api/ai/mode"
echo "Body: {\"microserviceMode\": true}"
echo
echo "测试时间: $(date)"
