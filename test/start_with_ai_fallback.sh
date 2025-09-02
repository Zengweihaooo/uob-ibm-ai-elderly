#!/bin/bash

# 智能AI故障转移启动脚本
# 启动主项目和微服务，并演示AI故障转移功能

echo "🚀 启动AI故障转移演示系统"
echo "========================================"
echo

# 配置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查Java环境
check_java() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java 未安装，请先安装 Java 11 或更高版本${NC}"
        exit 1
    fi

    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}✓ Java 版本: $java_version${NC}"
}

# 检查Maven环境
check_maven() {
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ Maven 未安装，请先安装 Maven${NC}"
        exit 1
    fi

    mvn_version=$(mvn -version 2>&1 | head -1 | awk '{print $3}')
    echo -e "${GREEN}✓ Maven 版本: $mvn_version${NC}"
}

# 设置环境变量
setup_environment() {
    echo -e "${BLUE}🔧 设置环境变量...${NC}"

    # 设置Google Cloud凭证
    export GOOGLE_APPLICATION_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
    export GOOGLE_CLOUD_PROJECT_ID="organic-totem-467918-a5"

    echo -e "${GREEN}✓ GOOGLE_APPLICATION_CREDENTIALS: $GOOGLE_APPLICATION_CREDENTIALS${NC}"
    echo -e "${GREEN}✓ GOOGLE_CLOUD_PROJECT_ID: $GOOGLE_CLOUD_PROJECT_ID${NC}"

    # 检查密钥文件是否存在
    if [ ! -f "$GOOGLE_APPLICATION_CREDENTIALS" ]; then
        echo -e "${RED}❌ Google Cloud 密钥文件不存在: $GOOGLE_APPLICATION_CREDENTIALS${NC}"
        echo -e "${YELLOW}请确保已将密钥文件放置在正确位置${NC}"
        exit 1
    fi
}

# 启动主项目
start_main_project() {
    echo -e "${BLUE}🏗️  启动主项目 (Spring Boot)...${NC}"
    cd springboot

    # 清理之前的编译
    echo "清理项目..."
    mvn clean -q

    # 编译项目
    echo "编译项目..."
    if ! mvn compile -q -DskipTests; then
        echo -e "${RED}❌ 主项目编译失败${NC}"
        cd ..
        return 1
    fi

    # 启动服务
    echo -e "${GREEN}启动主项目服务...${NC}"
    nohup mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080" > ../main_project.log 2>&1 &
    MAIN_PID=$!

    echo -e "${GREEN}✓ 主项目启动中 (PID: $MAIN_PID)${NC}"

    cd ..
    return 0
}

# 启动微服务
start_microservice() {
    echo -e "${BLUE}🎯 启动微服务 (Voice Command)...${NC}"
    cd voice-command-microservice

    # 清理之前的编译
    echo "清理项目..."
    mvn clean -q

    # 编译项目
    echo "编译项目..."
    if ! mvn compile -q -DskipTests; then
        echo -e "${RED}❌ 微服务编译失败${NC}"
        cd ..
        return 1
    fi

    # 启动服务
    echo -e "${GREEN}启动微服务...${NC}"
    nohup mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8090" > ../microservice.log 2>&1 &
    MICRO_PID=$!

    echo -e "${GREEN}✓ 微服务启动中 (PID: $MICRO_PID)${NC}"

    cd ..
    return 0
}

# 等待服务启动
wait_for_services() {
    echo -e "${YELLOW}⏳ 等待服务启动...${NC}"

    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        echo -n "检查服务状态 ($attempt/$max_attempts)... "

        # 检查主项目
        if curl -s http://localhost:8080/api/gemini/status > /dev/null 2>&1; then
            echo -e "${GREEN}主项目 ✓${NC}"
            MAIN_READY=true
        else
            echo -e "${RED}主项目 ❌${NC}"
            MAIN_READY=false
        fi

        # 检查微服务
        if curl -s http://localhost:8090/api/ai/status > /dev/null 2>&1; then
            echo -e "${GREEN}微服务 ✓${NC}"
            MICRO_READY=true
        else
            echo -e "${RED}微服务 ❌${NC}"
            MICRO_READY=false
        fi

        if [ "$MAIN_READY" = true ] && [ "$MICRO_READY" = true ]; then
            echo -e "${GREEN}🎉 所有服务已就绪！${NC}"
            return 0
        fi

        sleep 3
        ((attempt++))
    done

    echo -e "${RED}❌ 服务启动超时${NC}"
    return 1
}

# 演示AI故障转移
demonstrate_fallback() {
    echo
    echo -e "${BLUE}🎭 演示AI故障转移功能${NC}"
    echo "========================================"

    # 1. 测试正常情况
    echo -e "${YELLOW}1. 测试正常AI调用...${NC}"
    curl -s -X POST http://localhost:8090/api/ai/test \
        -H "Content-Type: application/json" \
        -d '{"message": "你好，请介绍一下自己"}' | jq . 2>/dev/null || echo "响应: $(curl -s -X POST http://localhost:8090/api/ai/test -H "Content-Type: application/json" -d '{"message": "你好，请介绍一下自己"}')"

    echo

    # 2. 检查AI服务状态
    echo -e "${YELLOW}2. 检查AI服务状态...${NC}"
    curl -s http://localhost:8090/api/ai/status | jq . 2>/dev/null || echo "状态: $(curl -s http://localhost:8090/api/ai/status)"

    echo

    # 3. 模拟故障转移
    echo -e "${YELLOW}3. 模拟主项目AI服务不可用...${NC}"

    # 临时停止主项目
    MAIN_PROJECT_PID=$(lsof -t -i:8080 2>/dev/null)
    if [ ! -z "$MAIN_PROJECT_PID" ]; then
        echo "停止主项目服务..."
        kill $MAIN_PROJECT_PID
        sleep 2
    fi

    # 测试微服务是否能自动回退
    echo -e "${YELLOW}测试微服务故障转移...${NC}"
    curl -s -X POST http://localhost:8090/api/ai/test \
        -H "Content-Type: application/json" \
        -d '{"message": "主项目停止后的测试消息"}' | jq . 2>/dev/null || echo "响应: $(curl -s -X POST http://localhost:8090/api/ai/test -H "Content-Type: application/json" -d '{"message": "主项目停止后的测试消息"}')"

    echo
    echo -e "${GREEN}✅ 故障转移演示完成！${NC}"
    echo -e "${BLUE}📋 演示说明:${NC}"
    echo "1. 正常情况下，微服务会调用主项目的AI服务"
    echo "2. 当主项目不可用时，微服务会使用本地fallback回复"
    echo "3. 系统会自动检测服务状态并选择最佳的AI服务"
}

# 显示使用说明
show_usage() {
    echo
    echo -e "${BLUE}📖 使用说明${NC}"
    echo "========================================"
    echo -e "${GREEN}服务已启动，可以通过以下方式使用：${NC}"
    echo
    echo "1. 🌐 Web界面:"
    echo "   主项目: http://localhost:8080"
    echo "   微服务: http://localhost:8090"
    echo
    echo "2. 🔧 API端点:"
    echo "   GET  http://localhost:8090/api/ai/status  - 查看AI服务状态"
    echo "   POST http://localhost:8090/api/ai/test    - 测试AI功能"
    echo "   POST http://localhost:8090/api/ai/mode    - 切换AI模式"
    echo
    echo "3. 🧪 测试脚本:"
    echo "   ./test_ai_fallback.sh"
    echo
    echo "4. 📄 日志文件:"
    echo "   tail -f main_project.log     # 主项目日志"
    echo "   tail -f microservice.log     # 微服务日志"
    echo
    echo -e "${YELLOW}按 Ctrl+C 停止所有服务${NC}"
}

# 清理函数
cleanup() {
    echo
    echo -e "${YELLOW}🧹 清理服务...${NC}"

    # 停止所有相关进程
    pkill -f "spring-boot:run" 2>/dev/null
    pkill -f "mvn.*spring-boot:run" 2>/dev/null

    # 等待进程停止
    sleep 2

    echo -e "${GREEN}✓ 服务已停止${NC}"
    exit 0
}

# 主函数
main() {
    # 设置清理函数
    trap cleanup SIGINT SIGTERM

    echo "检查环境..."
    check_java
    check_maven

    setup_environment

    echo
    echo -e "${BLUE}开始启动服务...${NC}"

    # 启动主项目
    if ! start_main_project; then
        echo -e "${RED}❌ 主项目启动失败${NC}"
        exit 1
    fi

    # 启动微服务
    if ! start_microservice; then
        echo -e "${RED}❌ 微服务启动失败${NC}"
        exit 1
    fi

    # 等待服务就绪
    if ! wait_for_services; then
        echo -e "${RED}❌ 服务启动超时${NC}"
        exit 1
    fi

    # 演示功能
    demonstrate_fallback

    # 显示使用说明
    show_usage

    # 保持运行
    echo
    echo -e "${GREEN}系统运行中... 按 Ctrl+C 退出${NC}"
    while true; do
        sleep 1
    done
}

# 执行主函数
main "$@"
