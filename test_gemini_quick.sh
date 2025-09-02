#!/bin/bash

# Gemini AI快速测试脚本

echo "🧪 Gemini AI快速测试"
echo "===================="

# 检查服务是否运行
check_services() {
    echo "检查服务状态..."

    # 检查主项目
    if curl -s http://localhost:8080/api/gemini/status > /dev/null 2>&1; then
        echo "✅ 主项目运行中"
    else
        echo "❌ 主项目未运行"
        return 1
    fi

    # 检查微服务
    if curl -s http://localhost:8090/api/ai/status > /dev/null 2>&1; then
        echo "✅ 微服务运行中"
    else
        echo "❌ 微服务未运行"
        return 1
    fi

    return 0
}

# 测试Gemini AI
test_gemini() {
    echo
    echo "测试Gemini AI聊天..."

    response=$(curl -s -X POST http://localhost:8090/api/ai/test \
        -H "Content-Type: application/json" \
        -d '{"message": "你好，Gemini！请简单介绍一下你自己。"}' \
        --connect-timeout 10 \
        --max-time 30)

    if [ $? -eq 0 ]; then
        echo "✅ Gemini测试成功"

        # 显示回复
        echo "🤖 Gemini回复:"
        echo "$response" | grep -o '"response":"[^"]*"' | cut -d'"' -f4 | head -1
        echo

        # 显示完整响应
        echo "完整响应:"
        echo "$response"
    else
        echo "❌ Gemini测试失败"
        echo "响应: $response"
        return 1
    fi
}

# 显示状态
show_status() {
    echo
    echo "AI服务状态:"

    status=$(curl -s http://localhost:8090/api/ai/status)
    if [ $? -eq 0 ]; then
        echo "$status" | jq . 2>/dev/null || echo "$status"
    else
        echo "无法获取状态信息"
    fi
}

# 主函数
main() {
    if check_services; then
        test_gemini
        show_status

        echo
        echo "🎉 Gemini AI测试完成！"
        echo
        echo "💡 使用提示:"
        echo "• 访问 http://localhost:8090 测试语音命令"
        echo "• 查看日志: tail -f microservice_gemini.log"
    else
        echo
        echo "❌ 服务未运行，请先启动系统:"
        echo "   ./start_with_gemini.sh"
        exit 1
    fi
}

main "$@"
