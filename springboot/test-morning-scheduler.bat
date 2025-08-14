@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM Morning Scheduler 功能测试脚本 (Windows版本)
REM 测试MorningScheduler的各种功能

echo 🌅 开始测试 Morning Scheduler 功能...
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
echo 🟡 开始测试 Morning Scheduler 功能...

REM 1. 检查早安问候状态
call :test_endpoint "GET" "/api/morning/greeting/check?userId=%USER_ID%" "" "检查用户是否有早安问候"

REM 2. 手动触发早安问候
call :test_endpoint "POST" "/api/morning/greeting/trigger?userId=%USER_ID%" "" "手动触发早安问候"

REM 3. 再次检查早安问候状态
call :test_endpoint "GET" "/api/morning/greeting/check?userId=%USER_ID%" "" "再次检查早安问候状态"

REM 4. 获取可用的意图列表
call :test_endpoint "GET" "/api/morning/intents" "" "获取可用的用户意图列表"

REM 5. 处理单个意图 - 安排播客
call :test_endpoint "POST" "/api/morning/intent?userId=%USER_ID%" "{\"intent\": \"SCHEDULE_PODCAST\"}" "处理用户意图 - 安排播客"

REM 6. 处理单个意图 - 提醒散步
call :test_endpoint "POST" "/api/morning/intent?userId=%USER_ID%" "{\"intent\": \"REMIND_WALK\"}" "处理用户意图 - 提醒散步"

REM 7. 处理单个意图 - 联系家人
call :test_endpoint "POST" "/api/morning/intent?userId=%USER_ID%" "{\"intent\": \"MESSAGE_FAMILY\"}" "处理用户意图 - 联系家人"

REM 8. 处理单个意图 - 晨练安排
call :test_endpoint "POST" "/api/morning/intent?userId=%USER_ID%" "{\"intent\": \"MORNING_EXERCISE\"}" "处理用户意图 - 晨练安排"

REM 9. 批量处理多个意图
call :test_endpoint "POST" "/api/morning/intents/batch?userId=%USER_ID%" "{\"intents\": [\"BREAKFAST_REMINDER\", \"MORNING_EXERCISE\"]}" "批量处理多个意图"

REM 10. 获取早晨日程建议
call :test_endpoint "GET" "/api/morning/suggestions?userId=%USER_ID%" "" "获取用户的早晨日程建议"

REM 11. 获取今天的早晨日程
call :test_endpoint "GET" "/api/morning/schedule?userId=%USER_ID%" "" "获取用户今天的早晨日程"

REM 12. 测试无效意图
call :test_endpoint "POST" "/api/morning/intent?userId=%USER_ID%" "{\"intent\": \"INVALID_INTENT\"}" "测试无效意图处理"

REM 13. 测试空意图
call :test_endpoint "POST" "/api/morning/intent?userId=%USER_ID%" "{\"intent\": \"\"}" "测试空意图处理"

echo.
echo 🎉 Morning Scheduler 功能测试完成！
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
