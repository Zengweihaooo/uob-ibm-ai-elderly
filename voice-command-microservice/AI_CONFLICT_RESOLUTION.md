# AI功能冲突解决方案

## 问题描述

在项目中存在两个AI功能：
1. **主项目AI对话功能** (`GeminiController`) - 提供通用AI对话
2. **微服务AI功能调用** (`AIIntentAnalysisService`) - 专门处理功能调用

这两个功能存在冲突，微服务试图将所有对话都转换为功能调用，导致普通聊天被误识别。

## 解决方案

### 双路径处理机制

实现了智能意图预判和双路径处理：

```
用户输入
    │
    ▼
意图预判 (isFunctionCallIntent)
    │
    ├─ 功能调用意图 ──→ 微服务AI分析 ──→ 功能执行
    │
    └─ 普通对话意图 ──→ 主项目AI对话 ──→ 返回对话结果
```

### 核心组件

#### 1. MainProjectAIClient
- 负责调用主项目的AI对话服务
- 提供健康检查功能
- 处理网络异常和错误

#### 2. 意图预判逻辑
```java
private boolean isFunctionCallIntent(String userText) {
    // 功能调用关键词检测
    List<String> functionKeywords = Arrays.asList(
        "发送邮件", "发邮件", "send email", "邮件",
        "查看日程", "添加日程", "schedule", "日程",
        "健康检查", "health check", "健康",
        "联系人", "contact", "查找",
        "重要日期", "important date", "生日"
    );
    
    return functionKeywords.stream().anyMatch(lowerText::contains);
}
```

#### 3. 双路径处理
- **功能调用路径**: 使用微服务的AI分析，执行具体功能
- **普通对话路径**: 调用主项目的AI对话，返回自然语言回复

### 容错机制

1. **置信度检查**: 功能调用置信度低于0.7时自动回退到普通对话
2. **异常处理**: 微服务AI失败时自动切换到主项目AI
3. **服务健康检查**: 定期检查主项目AI服务可用性

### 配置

在 `application.yml` 中配置主项目URL：
```yaml
main-project:
  service:
    url: http://localhost:8080
    timeout: 30000
```

### 优势

1. **智能分流**: 根据用户意图自动选择处理路径
2. **容错机制**: 多重备选方案确保服务可用性
3. **用户体验**: 无论什么类型的输入都能得到合适的响应
4. **系统稳定性**: 服务间解耦，互不影响

### 测试

运行测试验证意图预判逻辑：
```bash
mvn test -Dtest=VoiceCommandServiceTest#testIsFunctionCallIntent
```

### 使用示例

**功能调用示例**:
- 输入: "发送邮件给张三"
- 路径: 功能调用路径
- 结果: 执行邮件发送功能

**普通对话示例**:
- 输入: "今天天气怎么样"
- 路径: 普通对话路径  
- 结果: 返回AI对话回复
