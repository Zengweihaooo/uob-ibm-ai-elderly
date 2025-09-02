@echo off
REM 提交日志创建脚本 / Commit Log Creation Script
REM 自动创建基于模板的提交日志文件
REM Automatically creates commit log files based on template

echo ==============================================
echo 提交日志创建器 / Commit Log Creator
echo ==============================================
echo.

REM 获取当前日期
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "TODAY=%YYYY%-%MM%-%DD%"

echo 今天的日期: %TODAY%
echo Today's date: %TODAY%
echo.

REM 提示用户输入提交描述
set /p COMMIT_DESC="请输入提交描述 / Please enter commit description: "

if "%COMMIT_DESC%"=="" (
    echo 错误: 必须提供提交描述 / ERROR: Commit description is required
    pause
    exit /b 1
)

REM 创建文件名（替换空格为连字符）
set "FILENAME=%COMMIT_DESC: =-%"
set "FILENAME=%FILENAME:/=-%"
set "FILENAME=%FILENAME:\=-%"
set "FULL_FILENAME=%TODAY%_%FILENAME%.md"

echo.
echo 将创建文件: %FULL_FILENAME%
echo Will create file: %FULL_FILENAME%
echo.

REM 检查文件是否已存在
if exist "commit-logs\%FULL_FILENAME%" (
    echo 警告: 文件已存在 / WARNING: File already exists
    set /p OVERWRITE="是否覆盖? (y/n) / Overwrite? (y/n): "
    if /i not "%OVERWRITE%"=="y" (
        echo 操作取消 / Operation cancelled
        pause
        exit /b 0
    )
)

REM 复制模板并替换日期
copy "commit-logs\_TEMPLATE.md" "commit-logs\%FULL_FILENAME%" >nul

REM 使用PowerShell替换模板中的日期占位符
powershell -Command "(Get-Content 'commit-logs\%FULL_FILENAME%') -replace 'YYYY-MM-DD', '%TODAY%' | Set-Content 'commit-logs\%FULL_FILENAME%'"

echo.
echo ✅ 提交日志文件已创建: commit-logs\%FULL_FILENAME%
echo ✅ Commit log file created: commit-logs\%FULL_FILENAME%
echo.
echo 请编辑该文件添加具体的变更内容
echo Please edit the file to add specific change details
echo.

REM 询问是否打开文件
set /p OPEN_FILE="是否现在打开文件? (y/n) / Open file now? (y/n): "
if /i "%OPEN_FILE%"=="y" (
    start "" "commit-logs\%FULL_FILENAME%"
)

echo.
echo 操作完成 / Operation completed
pause
