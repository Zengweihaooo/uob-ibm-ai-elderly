# 🔑 双密钥配置策略说明

## 📋 概述

本项目采用**双密钥策略**来配置Google Cloud服务，确保AI服务和计算引擎服务能够正常工作。

## 🔑 密钥文件详情

### **密钥文件1 (主密钥)**
- **文件名**: `organic-totem-467918-a5-d17504cd5eba.json`
- **服务账号**: `university-of-bristol@organic-totem-467918-a5.iam.gserviceaccount.com`
- **用途**: 专门为布里斯托大学项目创建的服务账号
- **权限**: 包含AI服务所需的特定权限
- **服务**: Gemini AI、Text-to-Speech (TTS)、Speech-to-Text (STT)

### **密钥文件2 (备用密钥)**
- **文件名**: `organic-totem-467918-a5-6497b0d6925f.json`
- **服务账号**: `752873448452-compute@developer.gserviceaccount.com`
- **用途**: 计算引擎默认服务账号
- **权限**: 具有更广泛的Google Cloud权限
- **服务**: 计算引擎、存储、其他Google Cloud服务

## 🎯 配置策略

### **1. 主密钥优先策略**
- AI服务优先使用**密钥文件1**
- 提供更安全、权限明确的访问
- 专门为项目定制的服务账号

### **2. 备用密钥支持**
- 如果主密钥失败，自动切换到**密钥文件2**
- 提供更广泛的权限支持
- 确保服务的可用性

### **3. 服务分离**
- **AI服务**: 使用密钥文件1
- **计算服务**: 使用密钥文件2
- **故障转移**: 自动切换机制

## 📁 文件结构

```
docs/keys/
├── organic-totem-467918-a5-d17504cd5eba.json  # 主密钥文件
└── organic-totem-467918-a5-6497b0d6925f.json  # 备用密钥文件
```

## ⚙️ 环境变量配置

### **主密钥环境变量**
```bash
export GOOGLE_APPLICATION_CREDENTIALS="./docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
export GOOGLE_AI_CREDENTIALS="./docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
export GOOGLE_SPEECH_CREDENTIALS="./docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
export GOOGLE_TTS_CREDENTIALS="./docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
```

### **备用密钥环境变量**
```bash
export GOOGLE_BACKUP_CREDENTIALS="./docs/keys/organic-totem-467918-a5-6497b0d6925f.json"
export GOOGLE_COMPUTE_CREDENTIALS="./docs/keys/organic-totem-467918-a5-6497b0d6925f.json"
```

### **项目ID**
```bash
export GOOGLE_CLOUD_PROJECT_ID="organic-totem-467918-a5"
```

## 🚀 启动方式

### **使用启动脚本 (推荐)**
```bash
./start-with-credentials.sh
```

### **手动设置环境变量**
```bash
# 设置环境变量
export GOOGLE_APPLICATION_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-d17504cd5eba.json"
export GOOGLE_BACKUP_CREDENTIALS="$(pwd)/docs/keys/organic-totem-467918-a5-6497b0d6925f.json"
export GOOGLE_CLOUD_PROJECT_ID="organic-totem-467918-a5"

# 启动服务
./start_all.sh
```

## 🔧 配置文件

### **application.yml**
```yaml
google:
  cloud:
    project-id: ${GOOGLE_CLOUD_PROJECT_ID:organic-totem-467918-a5}
    # 主密钥文件 - 用于AI服务
    credentials-file: ${GOOGLE_APPLICATION_CREDENTIALS:./docs/keys/organic-totem-467918-a5-d17504cd5eba.json}
    # 备用密钥文件 - 用于计算引擎和其他服务
    backup-credentials-file: ${GOOGLE_BACKUP_CREDENTIALS:./docs/keys/organic-totem-467918-a5-6497b0d6925f.json}
    # AI服务配置
    ai:
      enabled: true
      credentials-file: ${GOOGLE_AI_CREDENTIALS:./docs/keys/organic-totem-467918-a5-d17504cd5eba.json}
    # 语音服务配置
    speech:
      enabled: true
      credentials-file: ${GOOGLE_SPEECH_CREDENTIALS:./docs/keys/organic-totem-467918-a5-d17504cd5eba.json}
    # TTS服务配置
    tts:
      enabled: true
      credentials-file: ${GOOGLE_TTS_CREDENTIALS:./docs/keys/organic-totem-467918-a5-d17504cd5eba.json}
    # 计算引擎配置
    compute:
      enabled: true
      credentials-file: ${GOOGLE_COMPUTE_CREDENTIALS:./docs/keys/organic-totem-467918-a5-6497b0d6925f.json}
```

## 🧪 测试验证

### **1. 检查密钥文件**
```bash
ls -la docs/keys/
```

### **2. 验证环境变量**
```bash
echo $GOOGLE_APPLICATION_CREDENTIALS
echo $GOOGLE_BACKUP_CREDENTIALS
echo $GOOGLE_CLOUD_PROJECT_ID
```

### **3. 测试服务**
```bash
# 测试微服务健康状态
curl http://localhost:8081/api/voice-command/health

# 测试AI服务
curl http://localhost:8080/api/gemini-new/chat
```

## 🚨 故障排除

### **常见问题**

1. **密钥文件不存在**
   - 确保两个密钥文件都在 `docs/keys/` 目录下
   - 检查文件名是否正确

2. **权限不足**
   - 检查密钥文件是否包含必要的权限
   - 验证服务账号是否已启用

3. **环境变量未设置**
   - 使用 `start-with-credentials.sh` 脚本
   - 手动设置所有必要的环境变量

### **日志检查**
```bash
# 查看微服务日志
tail -f microservice.log

# 查看后端日志
tail -f backend.log
```

## 🔒 安全注意事项

1. **不要提交密钥文件到Git**
   - 密钥文件已添加到 `.gitignore`
   - 确保密钥文件不会被意外提交

2. **定期轮换密钥**
   - 定期更新Google Cloud服务账号密钥
   - 监控API使用情况

3. **权限最小化**
   - 只授予必要的权限
   - 定期审查权限设置

## 📞 技术支持

如果遇到问题，请检查：
1. 密钥文件是否正确放置
2. 环境变量是否正确设置
3. 服务日志中的错误信息
4. Google Cloud Console中的权限设置

---

**注意**: 本配置确保了两个密钥文件都被正确使用，提供了更好的服务可用性和安全性。
