@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 宠物情绪系统集成测试脚本 (Windows版本)
REM 测试PetMoodController和PetController的集成功能

echo 🐱 开始测试宠物情绪系统集成...
echo ==================================

set BASE_URL=http://localhost:8080
set USER_ID=1

REM 测试函数
:test_endpoint
set method=%1
set endpoint=%2
set data=%3
set description=%4

echo.
echo 🔵 测试: %description%
echo 端点: %method% %endpoint%

if "%method%"=="GET" (
    curl -s -w "HTTP_STATUS:%%{http_code}" "%BASE_URL%%endpoint%" > temp_response.txt 2>nul
) else (
    echo %data% > temp_data.json
    curl -s -w "HTTP_STATUS:%%{http_code}" -X "%method%" -H "Content-Type: application/json" -d @temp_data.json "%BASE_URL%%endpoint%" > temp_response.txt 2>nul
    del temp_data.json
)

REM 读取响应
set /p response=<temp_response.txt
del temp_response.txt

REM 检查HTTP状态码
for /f "tokens=2 delims=:" %%a in ("%response%") do set http_status=%%a
set http_status=!http_status:HTTP_STATUS=!

REM 提取响应体
for /f "tokens=1 delims=:" %%a in ("%response%") do set response_body=%%a

REM 检查是否成功
if !http_status! geq 200 if !http_status! lss 300 (
    echo ✅ 成功 (HTTP !http_status!)
    echo 响应: !response_body!
) else (
    echo ❌ 失败 (HTTP !http_status!)
    echo 响应: !response_body!
)

echo ---
goto :eof

REM 等待服务启动
:wait_for_service
echo 等待服务启动...
for /l %%i in (1,1,30) do (
    curl -s "%BASE_URL%/actuator/health" >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 服务已启动
        goto :eof
    )
    echo -n .
    timeout /t 1 /nobreak >nul
)
echo.
echo ❌ 服务启动超时
exit /b 1

REM 主测试流程
:main
REM 检查服务是否运行
call :wait_for_service
if !errorlevel! neq 0 exit /b 1

echo.
echo 🟡 开始测试宠物情绪系统...

REM 1. 测试获取情绪状态
call :test_endpoint "GET" "/api/pet/mood/state?userId=%USER_ID%" "" "获取宠物情绪状态"

REM 2. 测试调整情绪分数
call :test_endpoint "POST" "/api/pet/mood/adjust?userId=%USER_ID%" "{\"delta\": 15}" "增加宠物情绪分数 (+15)"

REM 3. 测试获取完整宠物状态
call :test_endpoint "GET" "/api/pet/mood/status?userId=%USER_ID%" "" "获取完整宠物状态"

REM 4. 测试更新宠物属性
call :test_endpoint "POST" "/api/pet/mood/attributes?userId=%USER_ID%" "{\"happiness\": 90, \"health\": 95, \"energy\": 85}" "更新宠物属性"

REM 5. 测试增加经验值
call :test_endpoint "POST" "/api/pet/mood/experience?userId=%USER_ID%" "{\"exp\": 25}" "增加宠物经验值 (+25)"

REM 6. 测试宠物交互（喂食）
call :test_endpoint "POST" "/api/pet/interact" "{\"type\": \"feed\", \"message\": \"Time to eat!\"}" "宠物交互 - 喂食"

REM 7. 测试宠物交互（玩耍）
call :test_endpoint "POST" "/api/pet/interact" "{\"type\": \"play\", \"message\": \"Let's play!\"}" "宠物交互 - 玩耍"

REM 8. 测试获取宠物状态
call :test_endpoint "GET" "/api/pet/status" "" "获取宠物状态（集成后）"

REM 9. 测试发送消息给宠物
call :test_endpoint "POST" "/api/pet/message" "{\"message\": \"Hello, how are you today?\", \"type\": \"text\"}" "发送消息给宠物"

REM 10. 测试重置宠物情绪
call :test_endpoint "POST" "/api/pet/mood/reset?userId=%USER_ID%" "" "重置宠物情绪到默认值"

REM 11. 测试获取最终状态
call :test_endpoint "GET" "/api/pet/mood/status?userId=%USER_ID%" "" "获取重置后的宠物状态"

echo.
echo 🎉 所有测试完成！
echo ==================================
goto :eof

REM 检查依赖
:check_dependencies
curl --version >nul 2>&1
if !errorlevel! neq 0 (
    echo ❌ 需要安装 curl
    echo 请从 https://curl.se/windows/ 下载并安装
    pause
    exit /b 1
)

echo ✅ 依赖检查通过
goto :eof

REM 运行测试
echo 检查依赖...
call :check_dependencies
call :main

pause
