# 宠物情绪系统集成说明

## 概述

本项目已成功集成了宠物情绪管理系统，将原有的内存存储方式升级为数据库存储，并实现了 `PetMoodController` 和 `PetController` 的完美集成。同时新增了 **Morning Scheduler** 功能，提供智能的早晨调度服务。

## 🏗️ 系统架构

### 核心组件

1. **PetMood** - 宠物情绪实体类
2. **PetMoodMapper** - 数据访问层
3. **PetMoodService** - 业务逻辑层
4. **PetMoodController** - 情绪管理API控制器
5. **PetController** - 宠物交互主控制器（已集成）
6. **MorningScheduler** - 早晨调度服务 ⭐ 新增
7. **MorningSchedulerController** - 早晨调度API控制器 ⭐ 新增

### 数据流

```
前端请求 → PetMoodController/PetController → PetMoodService → PetMoodMapper → 数据库
                ↓
            缓存层 (ConcurrentHashMap)

定时任务 → MorningScheduler → ScheduleService → 数据库
    ↓
用户请求 → MorningSchedulerController → MorningScheduler → 响应
```

## 📊 数据库设计

### 新增表结构

#### `pet_mood` 表
```sql
CREATE TABLE pet_mood (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    mood_score INTEGER DEFAULT 0, -- 情绪分数 (-100 到 100)
    happiness INTEGER DEFAULT 85, -- 快乐度 (0-100)
    health INTEGER DEFAULT 92,    -- 健康度 (0-100)
    energy INTEGER DEFAULT 78,    -- 精力值 (0-100)
    mood_emoji TEXT DEFAULT '😊', -- 情绪表情
    status TEXT DEFAULT 'Happy & Healthy', -- 状态描述
    level INTEGER DEFAULT 1,      -- 宠物等级
    experience INTEGER DEFAULT 0, -- 经验值
    last_interaction TEXT,        -- 最后交互时间
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);
```

#### `pet_conversation` 表
```sql
CREATE TABLE pet_conversation (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    sender_type TEXT NOT NULL, -- 'user' 或 'pet'
    message TEXT NOT NULL,
    message_type TEXT DEFAULT 'text', -- 'text', 'voice', 'emergency'
    timestamp TEXT DEFAULT CURRENT_TIMESTAMP
);
```

## 🚀 API 端点

### PetMoodController 端点

| 方法 | 端点 | 描述 |
|------|------|------|
| `GET` | `/api/pet/mood/state` | 获取情绪状态和分数 |
| `POST` | `/api/pet/mood/adjust` | 调整情绪分数 |
| `GET` | `/api/pet/mood/status` | 获取完整宠物状态 |
| `POST` | `/api/pet/mood/attributes` | 更新宠物属性 |
| `POST` | `/api/pet/mood/experience` | 增加经验值 |
| `POST` | `/api/pet/mood/reset` | 重置宠物情绪 |
| `DELETE` | `/api/pet/mood/cache` | 清除缓存 |

### PetController 集成端点

| 方法 | 端点 | 描述 |
|------|------|------|
| `GET` | `/api/pet/status` | 获取宠物状态（集成后） |
| `POST` | `/api/pet/interact` | 宠物交互（已集成情绪系统） |
| `POST` | `/api/pet/message` | 发送消息给宠物 |
| `POST` | `/api/pet/voice` | 语音交互 |
| `GET` | `/api/pet/schedule-check` | 日程检查 |
| `POST` | `/api/pet/emergency` | 紧急处理 |
| `GET` | `/api/pet/conversation` | 对话历史 |
| `PUT` | `/api/pet/settings` | 更新设置 |

### MorningSchedulerController 端点 ⭐ 新增

| 方法 | 端点 | 描述 |
|------|------|------|
| `GET` | `/api/morning/greeting/check` | 检查早安问候状态 |
| `POST` | `/api/morning/greeting/trigger` | 手动触发早安问候 |
| `GET` | `/api/morning/intents` | 获取可用意图列表 |
| `POST` | `/api/morning/intent` | 处理单个用户意图 |
| `POST` | `/api/morning/intents/batch` | 批量处理多个意图 |
| `GET` | `/api/morning/suggestions` | 获取早晨日程建议 |
| `GET` | `/api/morning/schedule` | 获取今天早晨日程 |

## 🔧 集成特性

### 1. 统一数据存储
- 从内存存储升级到数据库存储
- 支持数据持久化和多用户隔离
- 保留内存缓存提升性能

### 2. 智能情绪计算
- 基于快乐度、健康度、精力值自动计算情绪分数
- 情绪分数范围：-100 到 100
- 自动更新情绪表情和状态描述

### 3. 经验值系统
- 每次交互增加经验值
- 自动等级提升机制
- 每级需要 100 经验值

### 4. 忽视检测
- 超过3小时未交互自动降低快乐度
- 触发悲伤表情和主动消息
- 智能情绪状态管理

### 5. 早晨调度系统 ⭐ 新增
- 自动早安问候（每天8点）
- 智能意图处理（播客、散步、家人联系等）
- 个性化日程建议
- 与宠物情绪系统完美集成

## 📝 使用示例

### 宠物情绪管理
```bash
# 获取宠物情绪状态
curl -X GET "http://localhost:8080/api/pet/mood/state?userId=1"

# 调整情绪分数
curl -X POST "http://localhost:8080/api/pet/mood/adjust?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"delta": 15}'
```

### 宠物交互
```bash
# 宠物交互（喂食）
curl -X POST "http://localhost:8080/api/pet/interact" \
  -H "Content-Type: application/json" \
  -d '{"type": "feed", "message": "Time to eat!"}'
```

### 早晨调度功能 ⭐ 新增
```bash
# 手动触发早安问候
curl -X POST "http://localhost:8080/api/morning/greeting/trigger?userId=1"

# 处理用户意图
curl -X POST "http://localhost:8080/api/morning/intent?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"intent": "SCHEDULE_PODCAST"}'

# 获取早晨建议
curl -X GET "http://localhost:8080/api/morning/suggestions?userId=1"
```

## 🧪 测试

### 运行测试脚本

#### 宠物情绪系统测试
```bash
# Linux/Mac
cd springboot
chmod +x test-pet-mood-integration.sh
./test-pet-mood-integration.sh

# Windows
cd springboot
test-pet-mood-integration.bat
```

#### 早晨调度系统测试 ⭐ 新增
```bash
# Linux/Mac
cd springboot
chmod +x test-morning-scheduler.sh
./test-morning-scheduler.sh

# Windows
cd springboot
test-morning-scheduler.bat
```

### 测试覆盖范围
- ✅ 情绪状态管理
- ✅ 属性更新
- ✅ 经验值系统
- ✅ 宠物交互
- ✅ 忽视检测
- ✅ 缓存管理
- ✅ 错误处理
- ✅ 早晨调度功能 ⭐ 新增
- ✅ 意图处理系统 ⭐ 新增
- ✅ 定时任务管理 ⭐ 新增

## 🔄 迁移说明

### 从旧版本升级
1. 运行新的数据库脚本创建表
2. 重启应用服务
3. 系统自动初始化新用户数据
4. 旧的内存数据将不再使用
5. 早晨调度功能自动启用

### 兼容性
- 保持原有API接口兼容
- 新增功能不影响现有功能
- 支持渐进式迁移

## 🚨 注意事项

### 性能优化
- 使用内存缓存减少数据库查询
- 批量操作优化
- 异步处理长时间任务
- 定时任务优化（避免重复执行）

### 数据一致性
- 事务管理确保数据完整性
- 缓存与数据库同步机制
- 异常情况下的数据恢复
- 重复创建检查

### 安全性
- 用户ID验证
- 输入参数验证
- SQL注入防护
- 定时任务安全控制

## 🔮 未来扩展

### 计划功能
- [ ] 宠物个性化设置
- [ ] 情绪历史记录
- [ ] 社交功能（宠物间互动）
- [ ] 成就系统
- [ ] 数据分析报告
- [ ] 语音播报集成 ⭐ 新增
- [ ] 天气信息集成 ⭐ 新增
- [ ] 智能时间推荐 ⭐ 新增

### 技术改进
- [ ] Redis缓存支持
- [ ] 消息队列集成
- [ ] 微服务架构
- [ ] 容器化部署
- [ ] 机器学习推荐 ⭐ 新增

## 📞 技术支持

如有问题或建议，请联系开发团队或查看项目文档。

---

**版本**: 3.0  
**最后更新**: 2025-01-XX  
**作者**: Lepeng Zhou  
**状态**: ✅ 生产就绪

## 📚 相关文档

- [Morning Scheduler 功能说明](./MORNING_SCHEDULER_README.md) - 早晨调度系统详细说明
- [项目架构文档](./TECHNICAL_ARCHITECTURE.md) - 整体技术架构
- [数据库设计文档](./DATABASE_README.md) - 数据库结构说明
