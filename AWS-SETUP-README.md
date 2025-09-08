# AWS云服务配置说明

## 📁 配置文件结构

### `application.properties` (主配置文件)
- ✅ 包含所有基础配置
- ✅ 默认使用本地服务 (SQLite + SMTP)
- ✅ 包含AWS服务配置但不包含敏感信息

### `application-aws.properties` (AWS专用配置)
- ✅ 只包含AWS特定的覆盖配置
- ✅ 使用环境变量存储敏感信息
- ✅ 简洁明了，易于维护

## 🚀 启动方式

### 方式1: 使用批处理脚本 (推荐)

#### 本地开发模式
```bash
# 双击运行
start-local.bat
```
- 使用SQLite数据库
- 使用本地SMTP服务
- 无需AWS凭证

#### AWS云服务模式
```bash
# 1. 首先设置AWS凭证
set-aws-credentials.bat

# 2. 然后启动AWS模式
start-aws.bat
```
- 使用AWS SNS发送短信
- 使用AWS SES发送邮件
- 使用DynamoDB存储数据

### 方式2: 手动命令行

#### 本地模式
```bash
cd springboot
mvn spring-boot:run
```

#### AWS模式
```bash
# 设置环境变量
set AWS_ACCESS_KEY_ID=AKIAUZAXTVWHF6W47WMS
set AWS_SECRET_ACCESS_KEY=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

# 启动AWS模式
cd springboot
mvn spring-boot:run -Dspring-boot.run.profiles=aws
```

## 🔧 测试AWS服务

启动AWS模式后，可以测试以下端点：

### 健康检查
```bash
curl http://localhost:8080/api/aws-test/health
```

### 测试邮件发送
```bash
curl -X POST "http://localhost:8080/api/aws-test/send-email?toEmail=test@example.com&subject=Test%20AWS%20Email&message=This%20is%20a%20test%20email%20from%20AWS%20SES"
```

### 测试SNS主题发布
```bash
curl -X POST "http://localhost:8080/api/aws-test/publish-topic?topicType=reminder&message=Test%20reminder%20message"
```

## 🔒 安全注意事项

1. **不要将AWS凭证提交到代码库**
2. **使用环境变量存储敏感信息**
3. **定期轮换访问密钥**
4. **遵循最小权限原则**

## 📋 当前AWS配置

- **区域**: us-east-1
- **SNS主题**: 
  - 健康提醒: `elderly-companion-health-alerts`
  - 紧急通知: `elderly-companion-system-notifications`
- **SES发送邮箱**: `noreply@elderly-companion.com`
- **DynamoDB表**: 多个表用于不同数据存储

## 🆘 故障排除

### 常见问题

1. **AWS凭证错误**
   - 检查环境变量是否正确设置
   - 验证AWS凭证是否有效

2. **SNS发送失败**
   - 检查SNS主题ARN是否正确
   - 验证IAM权限

3. **SES发送失败**
   - 检查发送邮箱是否已验证
   - 验证SMTP凭证

4. **DynamoDB连接失败**
   - 检查表名是否正确
   - 验证IAM权限
