# 🎤 Voice Command Microservice

## 📋 项目概述

这是一个独立的AI语音命令微服务，专门处理语音转文字、AI意图分析和功能执行。该服务完全独立于主项目的springboot目录，可以独立部署和扩展。

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    Voice Command Microservice                │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │  语音输入    │  │ AI意图分析   │  │ 功能路由     │         │
│  │  Voice Input│  │Intent Analysis│  │Function Router│         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                              │                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ 语音转文字   │  │ 功能执行     │  │ 结果反馈     │         │
│  │Speech-to-Text│  │Execution    │  │Feedback     │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  主项目服务      │
                    │  Main Services  │
                    │  (Email, etc.)  │
                    └─────────────────┘
```

## 🚀 核心功能

### 1. **语音转文字**
- 支持多种音频格式
- 多语言支持（中文、英文等）
- 高精度识别

### 2. **AI意图分析**
- 基于OpenAI GPT模型
- 智能理解用户意图
- 动态知识库学习

### 3. **功能路由**
- 自动识别要调用的功能
- 智能参数提取
- 支持功能扩展

### 4. **邮件功能集成**
- 调用主项目邮件服务
- 支持邮件发送、草稿保存
- 智能邮件内容生成

## 🛠️ 技术栈

- **Spring Boot 3.4.7** - 主框架
- **Spring Cloud OpenFeign** - 服务间通信
- **OpenAI GPT** - AI意图分析
- **Google Cloud Speech** - 语音识别
- **Maven** - 依赖管理
- **Java 17** - 运行环境

## 📁 项目结构

```
voice-command-microservice/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── voicecommand/
│       │           ├── VoiceCommandApplication.java
│       │           ├── controller/
│       │           │   └── VoiceCommandController.java
│       │           ├── service/
│       │           │   ├── VoiceCommandService.java
│       │           │   ├── AIIntentAnalysisService.java
│       │           │   └── FunctionRouterService.java
│       │           ├── model/
│       │           │   ├── VoiceCommandRequest.java
│       │           │   ├── VoiceCommandResponse.java
│       │           │   └── IntentAnalysisResult.java
│       │           └── client/
│       │               └── EmailServiceClient.java
│       └── resources/
│           └── application.yml
├── pom.xml
├── start-service.sh
├── start-service.bat
└── README.md
```

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- OpenAI API Key
- Google Cloud 凭证（可选）

### 启动步骤

#### Linux/Mac
```bash
cd voice-command-microservice
chmod +x start-service.sh
./start-service.sh
```

#### Windows
```cmd
cd voice-command-microservice
start-service.bat
```

### 环境变量配置
```bash
export OPENAI_API_KEY="your-openai-api-key"
export GOOGLE_CLOUD_PROJECT_ID="your-project-id"
export GOOGLE_APPLICATION_CREDENTIALS="path/to/credentials.json"
```

## 📡 API接口

### 1. 处理语音命令
```http
POST /api/voice-command/process
Content-Type: multipart/form-data

audio: [音频文件]
languageCode: zh-CN
userId: user123
sessionId: session456
```

### 2. 处理文本命令
```http
POST /api/voice-command/text
Content-Type: application/json

{
  "textCommand": "发送邮件给张三，主题是会议提醒",
  "languageCode": "zh-CN",
  "userId": "user123",
  "sessionId": "session456"
}
```

### 3. 获取执行状态
```http
GET /api/voice-command/status/{executionId}
```

## 🔧 配置说明

### application.yml 主要配置

```yaml
server:
  port: 8081  # 微服务端口

email:
  service:
    url: http://localhost:8080  # 主项目邮件服务地址

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-3.5-turbo

function:
  routing:
    email:
      enabled: true
      priority: 1
```

## 🔌 服务集成

### 与主项目集成
- 通过HTTP API调用主项目服务
- 支持邮件、日程、健康等功能
- 可独立部署和扩展

### 扩展新功能
1. 创建新的FunctionService
2. 注册到FunctionRouter
3. 更新AI知识库
4. 配置服务地址

## 📊 监控和日志

- 详细的执行日志
- 性能监控
- 错误追踪
- 执行状态跟踪

## 🚀 部署

### 本地开发
```bash
mvn spring-boot:run
```

### 生产部署
```bash
mvn clean package
java -jar target/voice-command-service-1.0.0.jar
```

### Docker部署
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/voice-command-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔮 未来计划

- [ ] 支持更多语音格式
- [ ] 增加更多AI模型
- [ ] 支持实时语音流
- [ ] 增加用户权限管理
- [ ] 支持多租户

## 📞 支持

如有问题，请查看：
- 项目文档
- API文档
- 日志文件
- 配置文件

## 📄 许可证

本项目采用 MIT 许可证。
