@echo off
echo ========================================
echo 运行 FamilyController 单元测试
echo ========================================

echo.
echo 1. 运行FamilyController单元测试 (使用Mock)...
./mvnw test -Dtest=FamilyControllerTest

echo.
echo 2. 运行所有测试...
./mvnw test

echo.
echo FamilyController测试完成！
pause 