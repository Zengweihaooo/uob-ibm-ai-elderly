# 重要日期提醒功能说明

## 功能概述

重要日期提醒功能为IBM AI老年人陪伴系统添加了智能邮件提醒功能，可以在重要日期的前一周和前一天自动发送邮件提醒给用户。

## 主要特性

### 🎯 核心功能
- **双重提醒机制**: 在重要日期前一周和前一天分别发送邮件提醒
- **智能日期计算**: 自动处理年度重复的重要日期（如生日、纪念日）
- **邮件模板**: 使用美观的HTML邮件模板，包含不同的提醒内容
- **定时任务**: 每天上午8点自动检查并发送待发送的提醒邮件

### 📧 邮件提醒类型
1. **一周前提醒**: 提醒用户准备相关安排和礼物
2. **一天前提醒**: 提醒用户确认当天的具体安排

### 📊 管理功能
- 添加、编辑、删除重要日期
- 查看即将到来的重要日期
- 获取重要日期统计信息
- 手动触发邮件提醒
- 添加默认节日（如新年、圣诞节等）

## 技术实现

### 核心组件

#### 1. 数据模型 (`ImportantDate.java`)
```java
public class ImportantDate {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDate date;
    private String type; // birthday, anniversary, holiday, custom
    private String repeatCycle; // none, yearly
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 邮件提醒跟踪
    private LocalDateTime weekReminderSent;
    private LocalDateTime dayReminderSent;
}
```

#### 2. 服务层 (`ImportantDateService.java`)
- 管理重要日期的CRUD操作
- 计算下一次出现的重要日期
- 发送邮件提醒
- 获取需要提醒的重要日期

#### 3. 邮件服务 (`EmailService.java`)
- 发送重要日期提醒邮件
- 使用Thymeleaf模板生成HTML邮件
- 支持不同的提醒类型（周提醒/日提醒）

#### 4. 定时任务 (`ScheduleMonitorService.java`)
```java
@Scheduled(cron = "0 0 8 * * ?") // 每天上午8点执行
public void checkImportantDateReminders() {
    importantDateService.sendAllPendingReminders();
}
```

### 邮件模板

使用 `importantDateReminderTemplate.html` 模板，包含：
- 响应式设计，适配移动设备
- 根据提醒类型显示不同的内容
- 美观的UI设计，适合老年人使用

## API接口

### 重要日期管理

#### 添加重要日期
```http
POST /api/important-dates/add
Content-Type: application/json

{
    "userId": 1,
    "title": "生日",
    "date": "2025-01-15",
    "type": "birthday",
    "description": "我的生日"
}
```

#### 获取用户的重要日期
```http
GET /api/important-dates/user/{userId}
```

#### 获取即将到来的重要日期
```http
GET /api/important-dates/user/{userId}/upcoming
```

#### 获取统计信息
```http
GET /api/important-dates/user/{userId}/stats
```

#### 更新重要日期
```http
PUT /api/important-dates/{id}
Content-Type: application/json

{
    "title": "更新的标题",
    "date": "2025-01-15",
    "type": "birthday",
    "description": "更新的描述",
    "repeatCycle": "yearly",
    "enabled": true
}
```

#### 删除重要日期
```http
DELETE /api/important-dates/{id}
```

#### 切换重要日期状态
```http
POST /api/important-dates/{id}/toggle
```

### 邮件提醒管理

#### 手动发送提醒
```http
POST /api/important-dates/send-reminders
```

#### 添加默认节日
```http
POST /api/important-dates/user/{userId}/default-holidays
```

## 测试页面

访问 `http://localhost:8080/important-date-test` 可以打开测试页面，包含：

1. **添加重要日期**: 测试添加新的重要日期
2. **查看重要日期**: 查看用户的所有重要日期
3. **即将到来的日期**: 查看即将到来的重要日期
4. **统计信息**: 查看重要日期的统计信息
5. **发送提醒**: 手动触发邮件提醒
6. **添加默认节日**: 添加系统预设的节日

## 配置说明

### 邮件配置
在 `application.properties` 中配置邮件服务器：

```properties
# SMTP Email Configuration
spring.mail.host=smtp.163.com
spring.mail.port=465
spring.mail.username=your-email@163.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.default-encoding=UTF-8
```

### 定时任务配置
定时任务默认在每天上午8点执行，可以通过修改cron表达式调整：

```java
@Scheduled(cron = "0 0 8 * * ?") // 每天上午8点
```

## 使用流程

### 1. 添加重要日期
用户可以通过API或测试页面添加重要日期，系统会记录：
- 日期信息
- 重复周期（年度重复）
- 提醒状态

### 2. 自动提醒
系统每天上午8点自动检查：
- 一周后需要提醒的重要日期
- 一天后需要提醒的重要日期
- 发送相应的邮件提醒

### 3. 邮件内容
- **一周前提醒**: 包含准备建议和安排提示
- **一天前提醒**: 包含确认清单和最后提醒

### 4. 状态跟踪
系统会记录每封提醒邮件的发送时间，避免重复发送：
- `weekReminderSent`: 周提醒发送时间
- `dayReminderSent`: 日提醒发送时间

## 注意事项

1. **邮件发送**: 确保邮件服务器配置正确，测试邮件发送功能
2. **用户邮箱**: 确保用户有有效的邮箱地址
3. **时区设置**: 系统使用服务器本地时区
4. **重复发送**: 系统会避免重复发送同一提醒
5. **数据持久化**: 当前使用内存存储，生产环境建议使用数据库

## 扩展功能

### 可能的扩展
1. **SMS提醒**: 添加短信提醒功能
2. **推送通知**: 集成移动端推送通知
3. **自定义提醒时间**: 允许用户自定义提醒时间
4. **多语言支持**: 支持多种语言的邮件模板
5. **提醒历史**: 记录和查看提醒历史

### 数据库集成
当前使用内存存储，可以轻松集成到现有数据库：

```sql
-- 重要日期表
CREATE TABLE important_dates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    type TEXT NOT NULL,
    repeat_cycle TEXT DEFAULT 'yearly',
    enabled BOOLEAN DEFAULT 1,
    week_reminder_sent DATETIME,
    day_reminder_sent DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

## 故障排除

### 常见问题

1. **邮件发送失败**
   - 检查邮件服务器配置
   - 确认邮箱地址有效
   - 查看应用日志

2. **定时任务不执行**
   - 确认 `@EnableScheduling` 已启用
   - 检查cron表达式格式
   - 查看应用启动日志

3. **重要日期计算错误**
   - 检查日期格式
   - 确认时区设置
   - 验证重复周期逻辑

### 日志查看
系统会在控制台输出详细的日志信息：
- 邮件发送状态
- 定时任务执行情况
- 错误信息

## 总结

重要日期提醒功能为老年人提供了贴心的提醒服务，通过智能的邮件提醒机制，帮助用户不会错过重要的日期。系统设计考虑了老年人的使用习惯，提供了简单易用的界面和清晰明确的提醒内容。 