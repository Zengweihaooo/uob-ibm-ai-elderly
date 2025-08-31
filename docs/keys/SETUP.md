# 🔐 凭据配置指南

## 📝 快速设置

### 1. 复制配置模板
```bash
cp twilio-config.json.example twilio-config.json
```

### 2. 编辑配置文件
打开 `twilio-config.json`，替换以下内容：

```json
{
  "account_sid": "你的_Twilio_账户_SID",
  "auth_token": "你的_Twilio_授权_令牌", 
  "from_number": "你的_Twilio_电话号码"
}
```

### 3. 获取Twilio凭据

1. 访问 [Twilio Console](https://console.twilio.com/)
2. 注册/登录账户
3. 在Dashboard页面找到：
   - **Account SID**
   - **Auth Token** 
4. 购买或验证一个电话号码

## 🚀 无配置启动

**好消息**：即使不配置，项目也能正常运行！

- SMS功能会自动使用模拟模式
- 在控制台显示短信内容
- 所有其他功能正常工作

## 🔒 安全说明

- `twilio-config.json` 被 `.gitignore` 忽略
- 真实凭据不会被推送到Git仓库
- 只有 `.example` 文件会被版本控制

## ❓ 常见问题

**Q: 找不到配置文件？**
A: 复制 `twilio-config.json.example` 为 `twilio-config.json`

**Q: 没有Twilio账户怎么办？**  
A: 项目会自动使用Mock模式，无需真实凭据

**Q: 配置后还是模拟模式？**
A: 检查 `application.properties` 中的 `app.sms.mock=true` 设置
