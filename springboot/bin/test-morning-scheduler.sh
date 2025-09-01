#!/bin/bash

# Morning Scheduler 功能测试脚本
# 测试MorningScheduler的各种功能

echo "🌅 开始测试 Morning Scheduler 功能..."
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
    
    echo -e "\n${YELLOW}开始测试 Morning Scheduler 功能...${NC}"
    
    # 1. 检查早安问候状态
    test_endpoint "GET" "/api/morning/greeting/check?userId=$USER_ID" "" \
        "检查用户是否有早安问候"
    
    # 2. 手动触发早安问候
    test_endpoint "POST" "/api/morning/greeting/trigger?userId=$USER_ID" "" \
        "手动触发早安问候"
    
    # 3. 再次检查早安问候状态
    test_endpoint "GET" "/api/morning/greeting/check?userId=$USER_ID" "" \
        "再次检查早安问候状态"
    
    # 4. 获取可用的意图列表
    test_endpoint "GET" "/api/morning/intents" "" \
        "获取可用的用户意图列表"
    
    # 5. 处理单个意图 - 安排播客
    test_endpoint "POST" "/api/morning/intent?userId=$USER_ID" \
        '{"intent": "SCHEDULE_PODCAST}' \
        "处理用户意图 - 安排播客"
    
    # 6. 处理单个意图 - 提醒散步
    test_endpoint "POST" "/api/morning/intent?userId=$USER_ID" \
        '{"intent": "REMIND_WALK}' \
        "处理用户意图 - 提醒散步"
    
    # 7. 处理单个意图 - 联系家人
    test_endpoint "POST" "/api/morning/intent?userId=$USER_ID" \
        '{"intent": "MESSAGE_FAMILY}' \
        "处理用户意图 - 联系家人"
    
    # 8. 处理单个意图 - 晨练安排
    test_endpoint "POST" "/api/morning/intent?userId=$USER_ID" \
        '{"intent": "MORNING_EXERCISE}"' \
        "处理用户意图 - 晨练安排"
    
    # 9. 批量处理多个意图
    test_endpoint "POST" "/api/morning/intents/batch?userId=$USER_ID" \
        '{"intents": ["BREAKFAST_REMINDER", "MORNING_EXERCISE"]}' \
        "批量处理多个意图"
    
    # 10. 获取早晨日程建议
    test_endpoint "GET" "/api/morning/suggestions?userId=$USER_ID" "" \
        "获取用户的早晨日程建议"
    
    # 11. 获取今天的早晨日程
    test_endpoint "GET" "/api/morning/schedule?userId=$USER_ID" "" \
        "获取用户今天的早晨日程"
    
    # 12. 测试无效意图
    test_endpoint "POST" "/api/morning/intent?userId=$USER_ID" \
        '{"intent": "INVALID_INTENT"}' \
        "测试无效意图处理"
    
    # 13. 测试空意图
    test_endpoint "POST" "/api/morning/intent?userId=$USER_ID" \
        '{"intent": ""}' \
        "测试空意图处理"
    
    echo -e "\n${GREEN}🎉 Morning Scheduler 功能测试完成！${NC}"
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
