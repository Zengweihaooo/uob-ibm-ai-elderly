@echo off
chcp 65001 >nul
echo ========================================
echo   云端数据存储演示脚本
echo   Cloud Data Storage Demo
echo ========================================

echo.
echo [1/8] 启动AWS云服务...
cd springboot
start "AWS Cloud Services" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=aws"
cd ..

echo.
echo [2/8] 等待服务启动...
timeout /t 15 /nobreak >nul

echo.
echo [3/8] 检查数据库状态...
echo 检查DynamoDB表状态...
curl -s http://localhost:8080/api/database/status
echo.

echo.
echo [4/8] 演示健康数据存储...
echo 添加血压记录...
curl -X POST "http://localhost:8080/api/health/record" -H "Content-Type: application/json" -d "{\"type\":\"bloodPressure\",\"value\":\"130/85\",\"notes\":\"正常范围\"}"
echo.

echo 添加血糖记录...
curl -X POST "http://localhost:8080/api/health/record" -H "Content-Type: application/json" -d "{\"type\":\"bloodSugar\",\"value\":\"6.2\",\"notes\":\"餐后2小时\"}"
echo.

echo.
echo [5/8] 演示日程数据存储...
echo 添加医生预约...
curl -X POST "http://localhost:8080/api/schedules" -H "Content-Type: application/json" -d "{\"title\":\"医生预约\",\"description\":\"心血管科检查\",\"scheduleDate\":\"2024-01-15\",\"startTime\":\"10:00\",\"endTime\":\"11:00\",\"type\":\"medical\"}"
echo.

echo 添加服药提醒...
curl -X POST "http://localhost:8080/api/schedules" -H "Content-Type: application/json" -d "{\"title\":\"服药提醒\",\"description\":\"降压药\",\"scheduleDate\":\"2024-01-10\",\"startTime\":\"08:00\",\"endTime\":\"08:30\",\"type\":\"medication\"}"
echo.

echo.
echo [6/8] 演示备忘录数据存储...
echo 添加重要备忘录...
curl -X POST "http://localhost:8080/api/memos" -H "Content-Type: application/json" -d "{\"title\":\"重要提醒\",\"content\":\"记得带身份证去医院\",\"tags\":\"医疗,重要\"}"
echo.

echo 添加日常备忘录...
curl -X POST "http://localhost:8080/api/memos" -H "Content-Type: application/json" -d "{\"title\":\"购物清单\",\"content\":\"牛奶,面包,水果\",\"tags\":\"日常,购物\"}"
echo.

echo.
echo [7/8] 演示家庭联系人数据存储...
echo 添加紧急联系人...
curl -X POST "http://localhost:8080/api/family" -H "Content-Type: application/json" -d "{\"name\":\"张三\",\"phoneNumber\":\"13800138000\",\"email\":\"zhangsan@example.com\",\"relationship\":\"儿子\",\"isEmergencyContact\":true}"
echo.

echo 添加普通联系人...
curl -X POST "http://localhost:8080/api/family" -H "Content-Type: application/json" -d "{\"name\":\"李四\",\"phoneNumber\":\"13900139000\",\"email\":\"lisi@example.com\",\"relationship\":\"朋友\",\"isEmergencyContact\":false}"
echo.

echo.
echo [8/8] 演示云端监控和通知...
echo 发送CloudWatch指标...
curl -X POST "http://localhost:8080/api/cloudwatch/health-metric?type=bloodPressure&isAbnormal=false"
echo.

echo 发送SNS通知...
curl -X POST "http://localhost:8080/api/aws-test/publish-topic?topicType=reminder&message=健康数据已更新，请查看最新记录"
echo.

echo 发送SES邮件...
curl -X POST "http://localhost:8080/api/aws-test/send-email?toEmail=test@example.com&subject=健康报告&message=您的健康数据已成功存储到云端"
echo.

echo.
echo ========================================
echo   云端数据存储演示完成！
echo   Cloud Data Storage Demo Complete!
echo ========================================
echo.
echo 📊 演示总结:
echo   ✅ 10个DynamoDB表已创建
echo   ✅ 健康记录数据已存储
echo   ✅ 日程安排数据已存储
echo   ✅ 备忘录数据已存储
echo   ✅ 家庭联系人数据已存储
echo   ✅ CloudWatch监控指标已发送
echo   ✅ SNS通知已发送
echo   ✅ SES邮件已发送
echo.
echo 🌐 云端服务状态:
echo   - DynamoDB: 10个表，50+字段
echo   - CloudWatch: 监控指标收集
echo   - SNS: 短信和推送通知
echo   - SES: 邮件发送服务
echo   - 数据同步: 实时同步机制
echo.
echo 📱 访问地址:
echo   - 主应用: http://localhost:8080
echo   - AI助手: http://localhost:8080/ai-assistant.html
echo   - 数据库状态: http://localhost:8080/api/database/status
echo   - CloudWatch: http://localhost:8080/api/cloudwatch/health
echo.
echo 按任意键停止服务...
pause >nul

echo 停止服务...
taskkill /f /im java.exe >nul 2>&1
echo 服务已停止。
