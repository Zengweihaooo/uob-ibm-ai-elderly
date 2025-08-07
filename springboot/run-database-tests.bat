@echo off
REM Database Integration Test Runner
REM 数据库集成测试运行器
REM 
REM 运行SQLite数据库相关的所有测试
REM Runs all SQLite database related tests

echo ==============================================
echo SQLite Database Integration Test Runner
echo 数据库集成测试运行器
echo ==============================================
echo.

echo 正在运行数据库测试... / Running database tests...
echo.

REM 切换到springboot目录
cd /d "%~dp0"

REM 运行特定的数据库测试
echo 1. 运行数据库管理服务单元测试 / Running database management service unit tests
call mvnw.cmd test -Dtest=DatabaseManagementServiceTest
if %ERRORLEVEL% neq 0 (
    echo 错误: 数据库管理服务测试失败 / ERROR: Database management service tests failed
    pause
    exit /b 1
)

echo.
echo 2. 运行数据库集成测试（类型安全版本） / Running database integration tests (type safe version)
call mvnw.cmd test -Dtest=DatabaseIntegrationTestTypeSafe
if %ERRORLEVEL% neq 0 (
    echo 错误: 数据库集成测试失败 / ERROR: Database integration tests failed
    pause
    exit /b 1
)

echo.
echo 3. 运行所有数据库相关测试 / Running all database related tests
call mvnw.cmd test -Dtest="*Database*"
if %ERRORLEVEL% neq 0 (
    echo 错误: 数据库测试套件失败 / ERROR: Database test suite failed
    pause
    exit /b 1
)

echo.
echo ==============================================
echo 测试完成! / Tests completed!
echo ==============================================
echo.
echo 所有数据库测试已成功通过 / All database tests passed successfully
echo.
echo 测试覆盖范围 / Test coverage:
echo - 数据库文件创建和初始化 / Database file creation and initialization
echo - 数据库状态检查 / Database status checking
echo - 数据库验证 / Database validation  
echo - 备份创建和恢复 / Backup creation and restoration
echo - API端点测试 / API endpoint testing
echo - 错误处理 / Error handling
echo.

pause
