# 🚀 UOB-IBM AI Elderly 项目启动指南

## 📋 快速启动 (推荐方式)

### 方法一：使用启动脚本 ⚡
```bash
cd /Users/zengweihao/Documents/IBM/uob-ibm-ai-elderly
./start_backend.sh
```

### 方法二：手动启动 🔧
```bash
# 1. 进入springboot目录
cd /Users/zengweihao/Documents/IBM/uob-ibm-ai-elderly/springboot

# 2. 启动后端
mvn spring-boot:run
```

---

## 🌐 网页访问地址

### 🏠 主页面 (推荐入口)
```
http://localhost:5500/index.html
```
**或者直接打开文件:**
```
/Users/zengweihao/Documents/IBM/uob-ibm-ai-elderly/index.html
```

### 🎯 功能页面直达

#### 📅 日程管理 + AI助手 + 虚拟宠物
```
http://localhost:5500/src/pages/schedule.html
```

#### 👤 用户注册/登录
```
http://localhost:5500/src/pages/register.html
```

---

## 🎮 功能使用指南

### 🔄 底部导航三合一系统

打开 `schedule.html` 后，你会看到底部有三个按钮：

#### 1. 📅 **Schedule (日程安排)**
- ✅ 添加、编辑、删除日程活动
- ✅ 设置优先级和紧急联系人
- ✅ 重复周期和提醒功能
- ✅ 地理栅栏位置提醒

#### 2. 🤖 **AI Assistant (AI对话助手)**  
- ✅ 健康咨询和建议
- ✅ 文件和图片上传
- ✅ 位置分享功能
- ✅ 语音消息支持

#### 3. 🐱 **Virtual Pet (虚拟宠物)**
- ✅ 与Whiskers互动 (喂食🍖、玩耍🎾、护理💊、对话💬)
- ✅ 宠物状态管理 (快乐度、健康度、精力值)
- ✅ 语音交互 (按住🎤说话)
- ✅ 智能文字聊天
- ✅ 日程提醒和健康关怀

---

## 🎭 用户模式

### 🎪 游客模式 (Guest Mode)
- 在注册页面选择 "Guest Mode"
- 预装示例日程和活动
- 所有功能都可使用
- 数据不会永久保存

### 👤 注册用户模式
- 完整的注册流程
- 数据持久化存储
- 个性化设置
- 历史记录保存

---

## 🔍 常见问题解决

### ❌ **后端启动失败**
```bash
# 检查是否在正确目录
pwd
# 应该显示: /Users/zengweihao/Documents/IBM/uob-ibm-ai-elderly/springboot

# 检查pom.xml是否存在
ls pom.xml

# 如果端口被占用，停止现有进程
pkill -f "spring-boot:run"
```

### 🌐 **网页无法访问**
1. **确保后端已启动** - 看到 "Started DemoApplication" 消息
2. **使用正确的URL** - `http://localhost:5500/` 而不是 `http://localhost:8080/`
3. **检查文件路径** - 确保HTML文件存在

### 🔌 **API调用失败**
```bash
# 测试后端是否正常
curl http://localhost:8080/api/pet/status

# 应该返回JSON格式的宠物状态
```

---

## 📊 API测试命令

### 🐱 测试虚拟宠物功能
```bash
# 获取宠物状态
curl http://localhost:8080/api/pet/status

# 喂食宠物
curl -X POST http://localhost:8080/api/pet/interact \
  -H "Content-Type: application/json" \
  -d '{"type":"feed"}'

# 发送消息
curl -X POST http://localhost:8080/api/pet/message \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello Whiskers!","type":"text"}'
```

### 📅 测试日程功能
```bash
# 获取今天的日程
curl http://localhost:8080/api/schedule/$(date +%Y-%m-%d)

# 添加新活动
curl -X POST http://localhost:8080/api/schedule/activity \
  -H "Content-Type: application/json" \
  -d '{"date":"2024-07-28","time":"10:00","title":"测试活动","description":"这是一个测试","category":"morning"}'
```

---

## 🎯 完整使用流程

### 1. **启动系统**
```bash
cd /Users/zengweihao/Documents/IBM/uob-ibm-ai-elderly
./start_backend.sh
```

### 2. **打开网页**
浏览器访问: `http://localhost:5500/index.html`

### 3. **选择模式**
- 点击 "Register or Guest Mode" → 选择 "Guest Mode" 或完整注册

### 4. **体验功能**
- 点击 "Try Daily Schedule with AI Assistant"
- 使用底部导航切换功能：📅日程、🤖AI助手、🐱虚拟宠物

### 5. **停止系统**
在终端按 `Ctrl+C` 停止后端

---

## 📱 系统架构

```
前端 (HTML/CSS/JS)     后端 (Spring Boot)
├── index.html         ├── ScheduleController
├── schedule.html      ├── ChatController  
├── register.html      ├── PetController
└── ...               └── UserController
     ↕                        ↕
  浏览器访问             API接口 (端口8080)
(端口5500/文件系统)
```

---

## 🎊 恭喜！

现在你已经有了一个完整的**老年人友好的AI陪伴系统**，包含：
- 📅 智能日程管理
- 🤖 AI健康助手  
- 🐱 虚拟宠物陪伴
- 🎭 游客模式和用户注册
- 🔊 语音交互支持
- 📱 适老化界面设计

享受你的AI陪伴系统吧！🚀✨ 