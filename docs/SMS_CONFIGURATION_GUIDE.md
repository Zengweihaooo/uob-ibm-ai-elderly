# SMS功能配置与使用指南

## 概述

本文档介绍如何配置和使用IBM AI老年人项目的SMS短信发送功能。系统集成了Twilio SMS服务，支持发送多种类型的短信通知。

## 特性

- ✅ 支持Twilio SMS服务集成
- ✅ 配置文件管理，避免硬编码
- ✅ 手机号码格式验证
- ✅ 消息长度限制
- ✅ 测试号码保护机制
- ✅ 详细的日志记录
- ✅ 多种消息类型支持
- ✅ 模拟和真实发送模式

## 配置步骤

### 1. 获取Twilio账号信息

1. 访问 [Twilio控制台](https://console.twilio.com/)
2. 注册或登录Twilio账号
3. 在Dashboard页面获取以下信息：
   - **Account SID**: 类似 `AC426e1f0d763cae4d3f3df12aa86e7585`
   - **Auth Token**: 类似 `80ae8a561906bf23027a3fd67a9a79f1`
4. 购买或验证发送方手机号码

### 2. 配置SMS设置

系统使用两个配置文件：

#### 主配置文件 (`application.properties`)
```properties
# SMS基础配置
app.sms.mock=false
app.sms.provider=twilio

# 包含SMS专用配置
spring.profiles.include=sms
```

#### SMS专用配置文件 (`application-sms.properties`)
```properties
# Twilio账号配置
app.sms.twilio.account-sid=AC426e1f0d763cae4d3f3df12aa86e7585
app.sms.twilio.auth-token=80ae8a561906bf23027a3fd67a9a79f1
app.sms.twilio.from-number=+15551234567

# 验证和限制配置
app.sms.phone.validation.pattern=^\\+[1-9]\\d{1,14}$
app.sms.max.message.length=160
app.sms.test.phone.numbers=+15555551234,+15555555678
app.sms.debug.enabled=true
```

### 3. 安全建议

⚠️ **重要安全提示：**

1. **不要将真实的Account SID和Auth Token提交到代码仓库**
2. 将 `application-sms.properties` 添加到 `.gitignore`
3. 在生产环境中使用环境变量：
   ```bash
   export SMS_ACCOUNT_SID=your_real_account_sid
   export SMS_AUTH_TOKEN=your_real_auth_token
   ```
4. 定期轮换Auth Token

## API接口

### 1. 发送测试短信
```http
POST /api/sms/send
Content-Type: application/json

{
  "phoneNumber": "+8613800138000",
  "message": "这是一条测试短信",
  "messageType": "TEST"
}
```

### 2. 发送健康警报
```http
POST /api/sms/health-alert
Content-Type: application/json

{
  "phoneNumber": "+8613800138000",
  "healthData": "血压异常：160/95 mmHg"
}
```

### 3. 发送紧急通知
```http
POST /api/sms/emergency
Content-Type: application/json

{
  "phoneNumber": "+8613800138000",
  "emergencyInfo": "用户摔倒检测警报"
}
```

### 4. 查看发送历史
```http
GET /api/sms/history
```

### 5. 查看统计信息
```http
GET /api/sms/statistics
```

## 响应格式

### 成功响应
```json
{
  "success": true,
  "message": "SMS发送请求已处理",
  "smsResult": {
    "success": true,
    "messageId": "SM1234567890abcdef",
    "status": "sent",
    "provider": "twilio"
  }
}
```

### 错误响应
```json
{
  "success": false,
  "message": "手机号码格式无效: 123456，请使用国际格式（如 +1234567890）"
}
```

## 手机号码格式

系统支持国际格式的手机号码：

### 有效格式示例
- `+8613800138000` (中国)
- `+14155552671` (美国)
- `+442071234567` (英国)
- `+81123456789` (日本)

### 自动格式化
系统会自动格式化中国手机号码：
- 输入：`13800138000`
- 自动转换为：`+8613800138000`

## 测试功能

### 使用测试脚本
```bash
# 运行完整测试
python test_sms_functionality.py
```

### 手动测试
1. 启动Spring Boot应用
2. 使用Postman或curl发送请求
3. 检查控制台日志和响应

### 测试号码保护
配置在 `app.sms.test.phone.numbers` 中的号码将自动使用模拟模式，避免意外发送真实短信。

## 配置选项详解

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `app.sms.mock` | `true` | 是否启用模拟模式 |
| `app.sms.provider` | `mock` | SMS服务提供商 |
| `app.sms.twilio.account-sid` | - | Twilio账号SID |
| `app.sms.twilio.auth-token` | - | Twilio认证令牌 |
| `app.sms.twilio.from-number` | - | 发送方号码 |
| `app.sms.phone.validation.pattern` | `^\\+[1-9]\\d{1,14}$` | 手机号验证正则 |
| `app.sms.max.message.length` | `160` | 最大消息长度 |
| `app.sms.test.phone.numbers` | - | 测试号码列表 |
| `app.sms.debug.enabled` | `false` | 调试模式开关 |

## 故障排除

### 常见问题

1. **"Twilio配置不完整"错误**
   - 检查Account SID和Auth Token是否正确配置
   - 确认没有多余的空格或特殊字符

2. **"手机号码格式无效"错误**
   - 确保使用国际格式（+国家代码+号码）
   - 检查号码长度是否在有效范围内

3. **"SMS发送失败"错误**
   - 检查网络连接
   - 验证Twilio账号余额
   - 确认发送方号码已验证

4. **连接超时**
   - 检查防火墙设置
   - 验证Twilio服务状态

### 调试步骤

1. 启用调试模式：`app.sms.debug.enabled=true`
2. 查看控制台日志
3. 使用测试号码验证配置
4. 检查Twilio控制台的发送记录

## 最佳实践

1. **生产环境配置**
   - 使用环境变量存储敏感信息
   - 启用日志监控
   - 设置适当的速率限制

2. **安全考虑**
   - 定期轮换API密钥
   - 限制发送频率
   - 验证接收方号码

3. **成本控制**
   - 监控SMS发送量
   - 设置月度预算警报
   - 优化消息内容长度

## 相关文档

- [Twilio SMS API文档](https://www.twilio.com/docs/sms)
- [Spring Boot配置指南](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
- [项目总体架构文档](./TECHNICAL_ARCHITECTURE.md)

## 更新日志

- **v1.0** - 初始版本，基础SMS功能
- **v1.1** - 添加配置文件支持，增强安全性
- **v1.2** - 集成Twilio SDK，支持真实发送

---

*最后更新时间：2025年1月*
