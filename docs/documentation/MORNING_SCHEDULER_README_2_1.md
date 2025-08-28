# Morning Scheduler 功能说明

## 概述

Morning Scheduler 是一个智能的早晨调度系统，专门为老年用户设计，提供个性化的早安问候、日程安排和健康提醒服务。该系统与宠物情绪管理系统完美集成，为用户创造温馨的早晨体验。

## 🏗️ 系统架构

### 核心组件

1. **MorningScheduler** - 核心调度服务
2. **MorningSchedulerController** - REST API 控制器
3. **ScheduleService** - 日程管理服务（已扩展）
4. **PetMoodService** - 宠物情绪服务（集成）

### 数据流

```
定时任务 → MorningScheduler → ScheduleService → 数据库
    ↓
用户请求 → MorningSchedulerController → MorningScheduler → 响应
```

## ⏰ 定时功能

### 自动早安问候
- **执行时间**: 每天早上 8:00（可配置）
- **功能**: 自动为活跃用户创建早安问候
- **集成**: 同时更新宠物情绪，增加快乐度

### 配置选项
```properties
# 配置文件中可调整的参数
pet.wakeupHour=8                    # 早安问候时间
pet.defaultPodcastTime=15:00        # 默认播客时间
pet.morning.cron=*/10 * * * * *     # 测试模式：每10秒执行一次
# 生产环境建议：0 0 8 * * * (每天8点执行)
```

## 🚀 API 端点

### 早安问候管理

| 方法 | 端点 | 描述 |
|------|------|------|
| `GET` | `/api/morning/greeting/check` | 检查用户是否有早安问候 |
| `POST` | `/api/morning/greeting/trigger` | 手动触发早安问候 |

### 意图处理

| 方法 | 端点 | 描述 |
|------|------|------|
| `GET` | `/api/morning/intents` | 获取可用的用户意图列表 |
| `POST` | `/api/morning/intent` | 处理单个用户意图 |
| `POST` | `/api/morning/intents/batch` | 批量处理多个意图 |

### 日程管理

| 方法 | 端点 | 描述 |
|------|------|------|
| `GET` | `/api/morning/schedule` | 获取今天的早晨日程 |
| `GET` | `/api/morning/suggestions` | 获取早晨日程建议 |

## 🎯 支持的意图类型

### 基础意图
- **SCHEDULE_PODCAST** - 安排播客时间
- **REMIND_WALK** - 提醒散步20分钟
- **MESSAGE_FAMILY** - 联系家人

### 扩展意图
- **MORNING_EXERCISE** - 晨练安排
- **BREAKFAST_REMINDER** - 早餐提醒

### 意图处理流程
1. 接收用户意图
2. 验证意图有效性
3. 创建相应的待办事项
4. 更新宠物经验值
5. 返回处理结果

## 📝 使用示例

### 手动触发早安问候
```bash
curl -X POST "http://localhost:8080/api/morning/greeting/trigger?userId=1"
```

### 处理用户意图
```bash
curl -X POST "http://localhost:8080/api/morning/intent?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"intent": "SCHEDULE_PODCAST"}'
```

### 批量处理意图
```bash
curl -X POST "http://localhost:8080/api/morning/intents/batch?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"intents": ["REMIND_WALK", "MESSAGE_FAMILY"]}'
```

### 获取早晨建议
```bash
curl -X GET "http://localhost:8080/api/morning/suggestions?userId=1"
```

## 🔧 集成特性

### 1. 宠物情绪集成
- 早安问候自动增加宠物快乐度
- 意图处理增加宠物经验值
- 基于宠物情绪提供个性化建议

### 2. 智能建议系统
- 根据宠物情绪状态提供建议
- 心情不好时推荐轻音乐和散步
- 心情好时推荐新活动和社交

### 3. 自动日程创建
- 检查用户是否已有早晨活动
- 自动创建默认的晨间活动
- 避免重复创建相同提醒

## 🧪 测试

### 运行测试脚本

#### Linux/Mac
```bash
cd springboot
chmod +x test-morning-scheduler.sh
./test-morning-scheduler.sh
```

#### Windows
```cmd
cd springboot
test-morning-scheduler.bat
```

### 测试覆盖范围
- ✅ 早安问候管理
- ✅ 意图处理（单个/批量）
- ✅ 日程建议系统
- ✅ 错误处理
- ✅ 边界情况测试

## 🔄 配置说明

### 开发环境配置
```properties
# 测试模式：每10秒执行一次
pet.morning.cron=*/10 * * * * *

# 调试模式：每1分钟执行一次
pet.morning.cron=0 * * * * *
```

### 生产环境配置
```properties
# 每天8点执行
pet.morning.cron=0 0 8 * * *

# 每天8点和18点执行
pet.morning.cron=0 0 8,18 * * *
```

### 自定义时间配置
```properties
# 自定义早安时间
pet.wakeupHour=7

# 自定义播客时间
pet.defaultPodcastTime=14:00
```

## 🚨 注意事项

### 性能考虑
- 定时任务使用 Spring 的 @Scheduled 注解
- 支持集群环境下的任务调度
- 避免重复执行相同任务

### 错误处理
- 完善的异常捕获和日志记录
- 单个用户失败不影响其他用户
- 提供详细的错误信息

### 数据一致性
- 检查重复创建避免数据冗余
- 事务管理确保数据完整性
- 缓存机制提升响应速度

## 🔮 未来扩展

### 计划功能
- [ ] 语音播报集成
- [ ] 天气信息集成
- [ ] 个性化问候语
- [ ] 智能时间推荐
- [ ] 社交活动提醒

### 技术改进
- [ ] Redis 缓存支持
- [ ] 消息队列集成
- [ ] 机器学习推荐
- [ ] 多语言支持

## 📞 技术支持

如有问题或建议，请联系开发团队或查看项目文档。

---

**版本**: 1.0  
**最后更新**: 2025-01-XX  
**作者**: Lepeng Zhou  
**状态**: ✅ 生产就绪
