#!/bin/bash

# 启用Gemini AI的启动脚本
# 启动系统并确保Gemini AI服务正常工作

echo "🚀 启动Gemini AI系统"
echo "======================="
echo

# 配置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查Google Cloud密钥文件
check_credentials() {
    echo -e "${BLUE}🔍 检查Google Cloud凭证...${NC}"

    local creds_file="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"

    if [ ! -f "$creds_file" ]; then
        echo -e "${RED}❌ Google Cloud密钥文件不存在: $creds_file${NC}"
        echo -e "${YELLOW}请确保密钥文件已正确放置${NC}"
        exit 1
    fi

    # 验证JSON格式
    if ! jq . "$creds_file" > /dev/null 2>&1; then
        echo -e "${RED}❌ Google Cloud密钥文件格式无效${NC}"
        exit 1
    fi

    echo -e "${GREEN}✓ Google Cloud密钥文件有效${NC}"
}

# 启用Gemini模式
enable_gemini_mode() {
    echo -e "${BLUE}🎯 启用Gemini AI模式...${NC}"

    # 等待微服务启动
    local max_attempts=20
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8090/api/ai/status > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 微服务已就绪${NC}"

            # 启用微服务AI模式（Gemini）
            echo "启用微服务AI模式..."
            response=$(curl -s -X POST http://localhost:8090/api/ai/mode \
                -H "Content-Type: application/json" \
                -d '{"microserviceMode": true}')

            if echo "$response" | grep -q "success.*true"; then
                echo -e "${GREEN}✓ Gemini AI模式已启用${NC}"
                return 0
            else
                echo -e "${YELLOW}⚠️  Gemini模式启用响应: $response${NC}"
            fi
        fi

        echo "等待微服务启动 ($attempt/$max_attempts)..."
        sleep 2
        ((attempt++))
    done

    echo -e "${RED}❌ 无法启用Gemini模式${NC}"
    return 1
}

# 测试Gemini AI功能
test_gemini() {
    echo -e "${BLUE}🧪 测试Gemini AI功能...${NC}"

    # 测试消息
    local test_message="你好，Gemini！请用中文介绍一下你自己。"

    echo "发送测试消息: '$test_message'"

    # 调用AI测试接口
    response=$(curl -s -X POST http://localhost:8090/api/ai/test \
        -H "Content-Type: application/json" \
        -d "{\"message\": \"$test_message\"}" \
        --connect-timeout 10 \
        --max-time 30)

    if [ $? -eq 0 ] && echo "$response" | grep -q "success.*true"; then
        echo -e "${GREEN}✓ Gemini AI测试成功${NC}"

        # 提取回复内容
        reply=$(echo "$response" | grep -o '"response":"[^"]*"' | cut -d'"' -f4)
        if [ ! -z "$reply" ]; then
            echo -e "${BLUE}🤖 Gemini回复:${NC}"
            echo "$reply"
        fi

        return 0
    else
        echo -e "${RED}❌ Gemini AI测试失败${NC}"
        echo "错误响应: $response"
        return 1
    fi
}

# 显示Gemini状态
show_gemini_status() {
    echo -e "${BLUE}📊 Gemini AI状态信息${NC}"

    # 获取AI状态
    status_response=$(curl -s http://localhost:8090/api/ai/status)
    if [ $? -eq 0 ]; then
        echo "AI服务状态:"
        echo "$status_response" | jq . 2>/dev/null || echo "$status_response"
    fi

    # 获取AI配置
    echo
    config_response=$(curl -s http://localhost:8090/api/ai/config)
    if [ $? -eq 0 ]; then
        echo "AI服务配置:"
        echo "$config_response" | jq . 2>/dev/null || echo "$config_response"
    fi
}

# 主函数
main() {
    echo "检查系统环境..."
    check_credentials

    echo
    echo -e "${BLUE}启动系统服务...${NC}"

    # 启动系统（使用现有的智能启动脚本）
    if [ -f "./start_with_ai_fallback.sh" ]; then
        echo "使用智能启动脚本..."
        # 这里我们不直接调用start_with_ai_fallback.sh，因为它会进入交互模式
        # 而是手动执行启动流程
    fi

    # 设置环境变量
    export GOOGLE_APPLICATION_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
    export GOOGLE_CLOUD_PROJECT_ID="organic-totem-467918-a5"

    # 启动主项目
    echo -e "${YELLOW}启动主项目...${NC}"
    cd springboot
    nohup mvn spring-boot:run -q > ../main_project_gemini.log 2>&1 &
    MAIN_PID=$!
    cd ..

    # 启动微服务
    echo -e "${YELLOW}启动微服务...${NC}"
    cd voice-command-microservice
    nohup mvn spring-boot:run -q > ../microservice_gemini.log 2>&1 &
    MICRO_PID=$!
    cd ..

    echo -e "${GREEN}✓ 服务启动中 (主项目PID: $MAIN_PID, 微服务PID: $MICRO_PID)${NC}"

    # 等待服务启动
    echo
    echo -e "${YELLOW}等待服务启动...${NC}"
    sleep 15

    # 启用Gemini模式
    if enable_gemini_mode; then
        echo
        # 测试Gemini功能
        if test_gemini; then
            echo
            echo -e "${GREEN}🎉 Gemini AI已成功启用！${NC}"
            echo
            show_gemini_status

            echo
            echo -e "${BLUE}📖 使用说明${NC}"
            echo "==================="
            echo -e "${GREEN}Gemini AI服务已就绪！${NC}"
            echo
            echo "🌐 Web界面:"
            echo "   主项目: http://localhost:8080"
            echo "   微服务: http://localhost:8090"
            echo
            echo "🔧 API端点:"
            echo "   GET  http://localhost:8090/api/ai/status  - 查看AI状态"
            echo "   POST http://localhost:8090/api/ai/test    - 测试AI功能"
            echo "   POST http://localhost:8090/api/voice/process - 语音命令处理"
            echo
            echo "📄 日志文件:"
            echo "   tail -f main_project_gemini.log     # 主项目日志"
            echo "   tail -f microservice_gemini.log     # 微服务日志"
            echo
            echo -e "${YELLOW}按 Ctrl+C 停止服务${NC}"

            # 保持运行
            echo
            echo -e "${GREEN}系统运行中...${NC}"
            while true; do
                sleep 1
            done
        else
            echo -e "${RED}❌ Gemini测试失败，请检查配置${NC}"
            exit 1
        fi
    else
        echo -e "${RED}❌ 无法启用Gemini模式${NC}"
        exit 1
    fi
}

# 清理函数
cleanup() {
    echo
    echo -e "${YELLOW}🧹 停止Gemini AI服务...${NC}"

    # 停止相关进程
    pkill -f "spring-boot:run" 2>/dev/null
    pkill -f "mvn.*spring-boot:run" 2>/dev/null

    echo -e "${GREEN}✓ 服务已停止${NC}"
    exit 0
}

# 设置清理函数
trap cleanup SIGINT SIGTERM

# 执行主函数
main "$@"
