#!/bin/bash

# 宠物情绪系统集成测试脚本
# 测试PetMoodController和PetController的集成功能

echo "🐱 开始测试宠物情绪系统集成..."
echo "=================================="

BASE_URL="http://localhost:8080"
USER_ID=1

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试函数
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "\n${BLUE}测试: $description${NC}"
    echo "端点: $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    # 分离响应体和状态码
    http_status=$(echo "$response" | tail -n1 | sed 's/.*HTTP_STATUS://')
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$http_status" -ge 200 ] && [ "$http_status" -lt 300 ]; then
        echo -e "${GREEN}✅ 成功 (HTTP $http_status)${NC}"
        echo "响应: $response_body" | jq '.' 2>/dev/null || echo "响应: $response_body"
    else
        echo -e "${RED}❌ 失败 (HTTP $http_status)${NC}"
        echo "响应: $response_body"
    fi
    
    echo "---"
}

# 等待服务启动
wait_for_service() {
    echo "等待服务启动..."
    for i in {1..30}; do
        if curl -s "$BASE_URL/actuator/health" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ 服务已启动${NC}"
            return 0
        fi
        echo -n "."
        sleep 1
    done
    echo -e "\n${RED}❌ 服务启动超时${NC}"
    return 1
}

# 主测试流程
main() {
    # 检查服务是否运行
    if ! wait_for_service; then
        exit 1
    fi
    
    echo -e "\n${YELLOW}开始测试宠物情绪系统...${NC}"
    
    # 1. 测试获取情绪状态
    test_endpoint "GET" "/api/pet/mood/state?userId=$USER_ID" "" \
        "获取宠物情绪状态"
    
    # 2. 测试调整情绪分数
    test_endpoint "POST" "/api/pet/mood/adjust?userId=$USER_ID" \
        '{"delta": 15}' \
        "增加宠物情绪分数 (+15)"
    
    # 3. 测试获取完整宠物状态
    test_endpoint "GET" "/api/pet/mood/status?userId=$USER_ID" "" \
        "获取完整宠物状态"
    
    # 4. 测试更新宠物属性
    test_endpoint "POST" "/api/pet/mood/attributes?userId=$USER_ID" \
        '{"happiness": 90, "health": 95, "energy": 85}' \
        "更新宠物属性"
    
    # 5. 测试增加经验值
    test_endpoint "POST" "/api/pet/mood/experience?userId=$USER_ID" \
        '{"exp": 25}' \
        "增加宠物经验值 (+25)"
    
    # 6. 测试宠物交互（喂食）
    test_endpoint "POST" "/api/pet/interact" \
        '{"type": "feed", "message": "Time to eat!"}' \
        "宠物交互 - 喂食"
    
    # 7. 测试宠物交互（玩耍）
    test_endpoint "POST" "/api/pet/interact" \
        '{"type": "play", "message": "Let\'s play!"}' \
        "宠物交互 - 玩耍"
    
    # 8. 测试获取宠物状态
    test_endpoint "GET" "/api/pet/status" "" \
        "获取宠物状态（集成后）"
    
    # 9. 测试发送消息给宠物
    test_endpoint "POST" "/api/pet/message" \
        '{"message": "Hello, how are you today?", "type": "text"}' \
        "发送消息给宠物"
    
    # 10. 测试重置宠物情绪
    test_endpoint "POST" "/api/pet/mood/reset?userId=$USER_ID" "" \
        "重置宠物情绪到默认值"
    
    # 11. 测试获取最终状态
    test_endpoint "GET" "/api/pet/mood/status?userId=$USER_ID" "" \
        "获取重置后的宠物状态"
    
    echo -e "\n${GREEN}🎉 所有测试完成！${NC}"
    echo "=================================="
}

# 错误处理
error_handler() {
    echo -e "\n${RED}❌ 测试过程中发生错误${NC}"
    echo "错误详情: $1"
    exit 1
}

# 设置错误处理
trap 'error_handler "$BASH_COMMAND"' ERR

# 检查依赖
if ! command -v curl &> /dev/null; then
    echo -e "${RED}❌ 需要安装 curl${NC}"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}⚠️  建议安装 jq 以获得更好的输出格式${NC}"
fi

# 运行测试
main "$@"
