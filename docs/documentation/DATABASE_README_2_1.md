# SQLite Database Integration Guide

## 🗄️ **概述 / Overview**

本项目已集成SQLite数据库，替换原有的内存存储，提供数据持久化功能。
This project has integrated SQLite database to replace in-memory storage and provide data persistence.

## 🚀 **快速开始 / Quick Start**

### Windows系统 / Windows System:
```bash
cd springboot
setup_database.bat
```

### Linux/Mac系统 / Linux/Mac System:
```bash
cd springboot
chmod +x setup_database.sh
./setup_database.sh
```

## 📂 **数据库文件位置 / Database File Location**

- **数据库文件 / Database File**: `data/elderly_companion.db`
- **备份目录 / Backup Directory**: `data/backups/`

## 🔧 **配置说明 / Configuration**

### application.properties 配置:
```properties
# SQLite Database Configuration
spring.datasource.url=jdbc:sqlite:data/elderly_companion.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update

# Database Management
app.database.path=data/elderly_companion.db
app.database.backup.path=data/backups/
app.database.init-schema=true
```

## 📊 **数据库结构 / Database Structure**

### 核心表 / Core Tables:
1. **users** - 用户管理 / User management
2. **schedules** - 日程管理 / Schedule management  
3. **health_records** - 健康记录 / Health records
4. **family_contacts** - 家庭联系人 / Family contacts
5. **emotion_companions** - 情感陪伴AI / Emotion companion AI
6. **important_dates** - 重要日期 / Important dates
7. **chat_messages** - 聊天记录 / Chat messages
8. **podcasts** - 播客缓存 / Podcast cache

### 扩展表 (为CrewAI准备) / Extension Tables (for CrewAI):
1. **uk_user_profiles** - 英国用户配置 / UK user profiles
2. **conversation_contexts** - 对话上下文 / Conversation contexts
3. **llm_request_logs** - LLM请求日志 / LLM request logs
4. **uk_medical_terms** - 英国医疗术语 / UK medical terms

## 🔌 **API接口 / API Endpoints**

### 数据库管理API / Database Management APIs:

#### 1. 获取数据库状态 / Get Database Status
```http
GET /api/database/status
```

#### 2. 创建备份 / Create Backup  
```http
POST /api/database/backup
```

#### 3. 从备份恢复 / Restore from Backup
```http
POST /api/database/restore
Content-Type: application/json

{
  "backup_filename": "elderly_companion_backup_20250805_143022.db"
}
```

#### 4. 验证数据库完整性 / Validate Database
```http
GET /api/database/validate
```

#### 5. 清理旧备份 / Cleanup Old Backups
```http
POST /api/database/cleanup-backups
Content-Type: application/json

{
  "keep_count": 5
}
```

#### 6. 获取数据库信息 / Get Database Info
```http
GET /api/database/info
```

### 现有API保持不变 / Existing APIs Remain Unchanged:
- `/api/schedule/*` - 日程管理 / Schedule management
- `/api/health/*` - 健康记录 / Health records  
- `/api/family/*` - 家庭联系人 / Family contacts
- `/api/pet/*` - 情感陪伴 / Emotion companion
- `/api/chat/*` - 聊天功能 / Chat functionality
- `/user/*` - 用户管理 / User management

## 🧪 **测试 / Testing**

### 运行API测试 / Run API Tests:
```bash
# Linux/Mac
chmod +x test_database_apis.sh
./test_database_apis.sh

# Windows (使用Git Bash或WSL)
bash test_database_apis.sh
```

### 手动测试 / Manual Testing:
```bash
# 检查数据库状态
curl http://localhost:8080/api/database/status

# 创建备份
curl -X POST http://localhost:8080/api/database/backup

# 测试健康记录API
curl -X POST http://localhost:8080/api/health/record \
  -H "Content-Type: application/json" \
  -d '{"type":"bloodPressure","value":"120/80","notes":"Test record"}'
```

## 📁 **项目结构 / Project Structure**

```
springboot/
├── data/                           # 数据库文件目录
│   ├── elderly_companion.db       # 主数据库文件
│   └── backups/                    # 备份目录
├── src/main/
│   ├── java/com/example/demo/
│   │   ├── config/
│   │   │   └── DatabaseConfig.java          # 数据库配置
│   │   ├── controller/
│   │   │   └── DatabaseController.java      # 数据库管理API
│   │   └── service/
│   │       └── DatabaseManagementService.java # 数据库管理服务
│   └── resources/
│       ├── application.properties           # 应用配置
│       └── data.sql                        # 初始数据
├── database_design_with_crewai.sql         # 完整数据库设计
├── setup_database.bat                      # Windows启动脚本
├── setup_database.sh                       # Linux/Mac启动脚本
└── test_database_apis.sh                   # API测试脚本
```

## 🔄 **数据迁移 / Data Migration**

当前版本会自动处理数据迁移:
1. **自动表创建** - Hibernate DDL会根据实体类自动创建表
2. **初始数据** - data.sql会插入必要的初始数据
3. **向后兼容** - 所有现有API保持完全兼容

## 🛡️ **备份策略 / Backup Strategy**

### 自动备份建议 / Automatic Backup Recommendations:
1. **日常备份** - 每天自动创建备份
2. **保留策略** - 保留最近7天的备份
3. **重要操作前** - 手动创建备份

### 备份脚本示例 / Backup Script Example:
```bash
#!/bin/bash
# 每日备份脚本 / Daily backup script
curl -X POST http://localhost:8080/api/database/backup
curl -X POST http://localhost:8080/api/database/cleanup-backups \
  -H "Content-Type: application/json" \
  -d '{"keep_count": 7}'
```

## 🚨 **故障排除 / Troubleshooting**

### 常见问题 / Common Issues:

#### 1. 数据库文件不能创建 / Cannot create database file
```bash
# 检查权限 / Check permissions
ls -la data/
# 手动创建目录 / Manually create directory  
mkdir -p data
```

#### 2. 依赖缺失 / Missing dependencies
```bash
# 重新安装依赖 / Reinstall dependencies
./mvnw clean install
```

#### 3. 端口冲突 / Port conflict
```bash
# 检查端口占用 / Check port usage
netstat -tulpn | grep 8080
# 或修改 application.properties 中的端口
server.port=8081
```

#### 4. 数据库锁定 / Database locked
```bash
# 停止应用程序 / Stop application
# 检查是否有其他进程使用数据库文件
lsof data/elderly_companion.db
```

## 🔮 **未来扩展 / Future Extensions**

本数据库设计已为以下功能预留空间:
1. **CrewAI集成** - 对话上下文管理和LLM请求追踪
2. **英国医疗术语** - 支持英国老年人的医疗表达习惯
3. **多语言支持** - 地区化配置和术语映射
4. **高级分析** - 健康趋势分析和预警系统

## 📞 **支持 / Support**

如有问题，请检查:
1. **日志文件** - 查看控制台输出或日志文件
2. **数据库状态** - 使用 `/api/database/status` 检查
3. **API文档** - 参考上述API接口说明
4. **GitHub Issues** - 提交问题到项目仓库

---

**注意**: 本集成保持所有现有功能不变，只是将数据存储从内存改为SQLite数据库，确保系统的稳定性和数据的持久化。
