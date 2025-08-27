# AWS Cloud Integration Guide

## 🚀 概述

本项目已成功集成AWS云服务，支持本地SQLite和云端DynamoDB双重存储，实现渐进式数据迁移和云端扩展。

## ✨ 主要特性

### **1. 混合数据架构**
- **本地存储**: SQLite数据库，快速响应，离线可用
- **云端存储**: DynamoDB，高可用，多设备同步
- **智能同步**: 后台自动同步，不影响用户体验
- **渐进迁移**: 支持从本地到云端的平滑迁移

### **2. 多渠道通知**
- **短信通知**: AWS SNS SMS服务
- **邮件通知**: AWS SES邮件服务
- **推送通知**: SNS主题订阅
- **紧急通知**: 高优先级通知处理

### **3. 云服务集成**
- **存储服务**: S3文件存储，CloudFront CDN
- **数据库服务**: DynamoDB NoSQL，RDS关系型
- **消息服务**: SNS通知，SQS队列
- **监控服务**: CloudWatch监控，X-Ray追踪

## 🏗️ 架构设计

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │   AWS Cloud     │
│   (Web/Mobile)  │◄──►│   Application   │◄──►│   Services      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                        │
                              ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Local SQLite  │    │   DynamoDB      │
                       │   (Cache)       │    │   (Primary)     │
                       └─────────────────┘    └─────────────────┘
```

## 🔧 安装和配置

### **1. 环境要求**
- Java 17+
- Maven 3.6+
- AWS账户和凭证

### **2. AWS凭证配置**

#### **方法1: 环境变量**
```bash
# Windows
set AWS_ACCESS_KEY_ID=your-access-key
set AWS_SECRET_ACCESS_KEY=your-secret-key
set AWS_REGION=us-east-1

# Linux/Mac
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

#### **方法2: AWS配置文件**
```bash
# 创建 ~/.aws/credentials
[default]
aws_access_key_id = your-access-key
aws_secret_access_key = your-secret-key

# 创建 ~/.aws/config
[default]
region = us-east-1
```

### **3. 启动应用**

#### **本地模式（SQLite）**
```bash
# Windows
start_local.bat

# Linux/Mac
./start_local.sh
```

#### **AWS模式（DynamoDB）**
```bash
# Windows
start-aws.bat

# Linux/Mac
chmod +x start-aws.sh
./start-aws.sh
```

## 📊 配置说明

### **1. 数据库选择**
```properties
# 使用SQLite（本地模式）
app.database.repository=sqlitePetMoodRepository

# 使用DynamoDB（AWS模式）
app.database.repository=dynamoDBPetMoodRepository
```

### **2. AWS服务配置**
```properties
# AWS基础配置
aws.region=us-east-1
aws.access-key-id=${AWS_ACCESS_KEY_ID}
aws.secret-access-key=${AWS_SECRET_ACCESS_KEY}

# DynamoDB配置
aws.dynamodb.table.pet-mood=pet_mood
aws.dynamodb.table.schedules=schedules

# SNS配置
aws.sns.topic.reminders=${AWS_SNS_TOPIC_REMINDERS}
aws.sns.topic.emergency=${AWS_SNS_TOPIC_EMERGENCY}

# S3配置
aws.s3.bucket=elderly-companion-bucket
```

## 🚀 使用指南

### **1. 数据迁移**

#### **从本地迁移到云端**
```java
@Autowired
private HybridPetMoodService hybridService;

// 执行数据迁移
hybridService.migrateToCloud();
```

#### **渐进式同步**
```java
// 数据会自动在后台同步
PetMood petMood = hybridService.getOrInitPetMood(userId);
// 本地更新后，云端会自动同步
```

### **2. 云端通知**

#### **发送提醒通知**
```java
@Autowired
private AWSSNSNotificationService snsService;

// 发送提醒
snsService.sendReminderNotification(user, "health_check", "请提交健康数据");
```

#### **发送紧急通知**
```java
// 发送紧急通知
snsService.sendEmergencyNotification(user, "medication", "请立即服药！");
```

### **3. 多设备同步**

#### **设备状态同步**
```java
// 用户在不同设备上的数据会自动同步
// 通过DynamoDB实现实时同步
```

## 📈 性能优化

### **1. 缓存策略**
- **本地缓存**: SQLite作为一级缓存
- **云端缓存**: DynamoDB DAX加速
- **应用缓存**: Spring Cache集成

### **2. 异步处理**
- **后台同步**: 数据同步不影响主流程
- **批量操作**: 支持批量数据迁移
- **重试机制**: 网络异常自动重试

### **3. 连接池优化**
```properties
# 本地模式
spring.datasource.hikari.maximum-pool-size=1

# AWS模式
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

## 🔒 安全配置

### **1. IAM权限**
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "dynamodb:GetItem",
                "dynamodb:PutItem",
                "dynamodb:UpdateItem",
                "dynamodb:DeleteItem",
                "dynamodb:Query",
                "dynamodb:Scan"
            ],
            "Resource": "arn:aws:dynamodb:*:*:table/pet_mood"
        },
        {
            "Effect": "Allow",
            "Action": [
                "sns:Publish",
                "sns:Subscribe"
            ],
            "Resource": "arn:aws:sns:*:*:elderly-*"
        }
    ]
}
```

### **2. 数据加密**
- **传输加密**: TLS 1.2+
- **存储加密**: DynamoDB AES-256
- **密钥管理**: AWS KMS集成

## 🧪 测试指南

### **1. 单元测试**
```bash
# 运行所有测试
mvn test

# 运行AWS相关测试
mvn test -Dtest=*AWS*
```

### **2. 集成测试**
```bash
# 本地集成测试
mvn test -Dspring.profiles.active=test

# AWS集成测试
mvn test -Dspring.profiles.active=aws-test
```

### **3. 性能测试**
```bash
# 数据迁移性能测试
mvn test -Dtest=MigrationPerformanceTest

# 云端同步性能测试
mvn test -Dtest=CloudSyncPerformanceTest
```

## 🚨 故障排除

### **1. 常见问题**

#### **AWS凭证错误**
```
Error: Unable to load AWS credentials
Solution: 检查环境变量或配置文件
```

#### **DynamoDB连接失败**
```
Error: Unable to connect to DynamoDB
Solution: 检查网络和IAM权限
```

#### **SNS发送失败**
```
Error: SNS publish failed
Solution: 检查主题ARN和权限
```

### **2. 日志查看**
```properties
# 启用AWS SDK调试日志
logging.level.software.amazon.awssdk=DEBUG

# 启用应用调试日志
logging.level.com.example.demo=DEBUG
```

### **3. 监控指标**
- **CloudWatch**: 应用性能监控
- **X-Ray**: 请求追踪分析
- **自定义指标**: 业务指标监控

## 🔮 未来扩展

### **1. 计划功能**
- [ ] **AI服务集成**: Amazon Bedrock, SageMaker
- [ ] **语音服务**: Amazon Transcribe, Polly
- [ ] **图像识别**: Amazon Rekognition
- [ ] **自然语言**: Amazon Comprehend

### **2. 架构升级**
- [ ] **微服务化**: ECS + API Gateway
- [ ] **无服务器**: Lambda + EventBridge
- [ ] **容器化**: EKS + Docker
- [ ] **边缘计算**: CloudFront + Lambda@Edge

### **3. 成本优化**
- [ ] **预留实例**: RDS, ElastiCache
- [ ] **自动扩缩容**: Auto Scaling
- [ ] **成本监控**: Cost Explorer
- [ ] **资源优化**: Trusted Advisor

## 📞 技术支持

### **1. 文档资源**
- [AWS官方文档](https://docs.aws.amazon.com/)
- [Spring Boot AWS集成](https://spring.io/projects/spring-cloud-aws)
- [项目Wiki](https://github.com/your-repo/wiki)

### **2. 社区支持**
- [GitHub Issues](https://github.com/your-repo/issues)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/aws-spring-boot)
- [AWS开发者论坛](https://forums.aws.amazon.com/)

### **3. 联系方式**
- **项目维护者**: Lepeng Zhou
- **邮箱**: lepeng@example.com
- **GitHub**: [@lepengz233](https://github.com/lepengz233)

---

**版本**: 1.0  
**最后更新**: 2025-01-XX  
**作者**: Lepeng Zhou  
**状态**: ✅ AWS集成完成，生产就绪

