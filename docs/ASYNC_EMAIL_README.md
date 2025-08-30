# 📧 异步邮箱功能使用说明

## 🎯 **概述**

本项目已实现异步邮箱功能，解决使用同一个app邮箱发送所有邮件时的性能问题。

## ⚡ **为什么需要异步？**

### **同步发送的问题：**
- ❌ 邮件发送会阻塞用户请求（等待几秒到几十秒）
- ❌ 用户需要等待邮件发送完成才能得到响应
- ❌ 无法同时处理多个邮件发送请求
- ❌ 单个邮件发送失败会影响其他操作

### **异步发送的优势：**
- ✅ 邮件发送在后台进行，不阻塞主线程
- ✅ 用户立即得到响应，提高用户体验
- ✅ 可以同时处理多个邮件发送请求
- ✅ 错误隔离，单个邮件失败不影响其他操作

## 🔧 **技术实现**

### **1. 异步配置类**
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        // 邮件发送专用线程池
        // 核心线程数：2个
        // 最大线程数：5个
        // 队列容量：100个任务
    }
}
```

### **2. 异步邮件方法**
所有邮件发送方法都有对应的异步版本：

| 同步方法 | 异步方法 | 说明 |
|---------|---------|------|
| `sendCustomEmail()` | `sendCustomEmailAsync()` | 发送自定义邮件 |
| `sendVerificationEmail()` | `sendVerificationEmailAsync()` | 发送验证邮件 |
| `sendHealthAlertEmail()` | `sendHealthAlertEmailAsync()` | 发送健康提醒 |
| `sendDailyHealthCheckReminderEmail()` | `sendDailyHealthCheckReminderEmailAsync()` | 发送每日健康检查提醒 |
| `sendDailyPlanReminderEmail()` | `sendDailyPlanReminderEmailAsync()` | 发送每日计划提醒 |
| `sendImportantDateReminderEmail()` | `sendImportantDateReminderEmailAsync()` | 发送重要日期提醒 |
| `sendEmailsToAll()` | `sendEmailsToAllAsync()` | 批量发送邮件 |

## 📖 **使用方法**

### **1. 注入EmailService**
```java
@Autowired
private EmailService emailService;
```

### **2. 调用异步方法**
```java
// 异步发送邮件（推荐）
emailService.sendCustomEmailAsync(
    "user@example.com", 
    "测试邮件", 
    "这是一封测试邮件", 
    "AI助手"
);

// 同步发送邮件（不推荐，会阻塞）
emailService.sendCustomEmail(
    "user@example.com", 
    "测试邮件", 
    "这是一封测试邮件", 
    "AI助手"
);
```

### **3. 微服务中的使用**
微服务通过 `EmailServiceClient` 调用主后端的异步邮件服务：

```java
// 在FunctionRouterService中
@Async("emailTaskExecutor")
public void executeSendEmailAsync(IntentAnalysisResult intent) {
    // 异步执行邮件发送
    emailServiceClient.sendEmailAsync(...);
}
```

## 🚀 **性能优化建议**

### **1. 批量邮件发送**
```java
// 使用异步批量发送
emailService.sendEmailsToAllAsync();
```

### **2. 错误处理**
异步方法中的异常不会抛出，只记录日志：
```java
@Async("emailTaskExecutor")
public void sendEmailAsync(String to, String subject, String content) {
    try {
        // 邮件发送逻辑
    } catch (Exception e) {
        // 记录错误日志，不抛出异常
        log.error("邮件发送失败: {}", e.getMessage());
    }
}
```

### **3. 监控和日志**
- 查看线程池状态：`/actuator/threaddump`
- 监控邮件发送队列：日志中的 `email-` 前缀线程
- 设置合理的超时时间：避免邮件发送卡死

## 📊 **线程池配置**

### **邮件专用线程池 (emailTaskExecutor)**
- **核心线程数**: 2个
- **最大线程数**: 5个  
- **队列容量**: 100个任务
- **线程名前缀**: `email-`
- **空闲线程存活时间**: 60秒

### **通用任务线程池 (taskExecutor)**
- **核心线程数**: 3个
- **最大线程数**: 10个
- **队列容量**: 200个任务
- **线程名前缀**: `task-`

## 🔍 **故障排查**

### **常见问题：**

1. **邮件发送缓慢**
   - 检查线程池是否满载
   - 查看邮件服务器连接状态
   - 检查网络延迟

2. **邮件发送失败**
   - 查看异步方法日志
   - 检查邮件服务器配置
   - 验证收件人地址格式

3. **线程池耗尽**
   - 增加最大线程数
   - 优化邮件发送逻辑
   - 添加熔断机制

### **日志示例：**
```
Async email sent successfully to: user@example.com with subject: 测试邮件
Async email sending failed to: invalid@email => Invalid email address
```

## 📝 **最佳实践**

1. **优先使用异步方法**：`sendXXXAsync()`
2. **合理设置邮件内容**：避免过大的附件或内容
3. **添加重试机制**：对于重要邮件
4. **监控邮件队列**：防止积压过多任务
5. **优雅关闭**：等待所有邮件发送完成

## 🔄 **迁移指南**

### **从同步到异步：**

```java
// 旧代码（同步）
public void sendWelcomeEmail(String email) {
    emailService.sendCustomEmail(email, "欢迎", "欢迎使用我们的服务", "系统");
    // 这里会阻塞等待邮件发送完成
}

// 新代码（异步）
public void sendWelcomeEmail(String email) {
    emailService.sendCustomEmailAsync(email, "欢迎", "欢迎使用我们的服务", "系统");
    // 立即返回，邮件在后台发送
}
```

---

**🎉 异步邮箱功能已完全集成到项目中，现在可以享受高性能的邮件发送体验！**
