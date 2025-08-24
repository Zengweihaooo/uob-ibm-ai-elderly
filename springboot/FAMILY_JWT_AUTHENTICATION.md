# Family JWT Authentication System
# Family JWT认证系统

## 🎉 更新说明

Family功能已成功从硬编码userId改为JWT token认证系统！现在每个用户都能看到自己的数据，实现了真正的用户隔离。

## 🔧 **主要改动**

### 1. **新增JWT支持**
- ✅ `JwtUtil.java` - JWT工具类，处理token生成和解析
- ✅ 添加了JWT依赖到`pom.xml`
- ✅ 所有Family API现在都从JWT token中提取真实的userId

### 2. **FamilyController改造**
- ✅ 移除了所有硬编码的`Long userId = 1L`
- ✅ 添加了JWT token验证
- ✅ 实现了用户数据隔离

### 3. **数据迁移服务优化**
- ✅ 支持为多个用户创建示例数据
- ✅ 用户1、2、3都会获得独立的联系人数据

## 🚀 **使用方法**

### **1. 生成测试Token**

```bash
# 为用户1生成token
curl -X POST http://localhost:8080/api/test/jwt/generate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "userId": 1
  }'

# 为用户2生成token
curl -X POST http://localhost:8080/api/test/jwt/generate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2",
    "userId": 2
  }'
```

### **2. 使用Token访问Family API**

```bash
# 获取用户1的联系人
curl -X GET http://localhost:8080/api/family/contacts \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 添加新联系人
curl -X POST http://localhost:8080/api/family/contacts \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "新联系人",
    "phoneNumber": "+86 13800138000",
    "email": "new@example.com",
    "relationship": "朋友"
  }'
```

### **3. 验证Token**

```bash
# 验证token有效性
curl -X POST http://localhost:8080/api/test/jwt/verify \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_TOKEN_HERE",
    "username": "user1"
  }'

# 从Authorization header提取用户信息
curl -X GET http://localhost:8080/api/test/jwt/extract-user \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 📊 **用户数据隔离验证**

### **测试步骤**

1. **启动应用**
   ```bash
   cd springboot
   mvn spring-boot:run
   ```

2. **为不同用户生成token**
   - 用户1: `curl -X POST http://localhost:8080/api/test/jwt/generate -H "Content-Type: application/json" -d '{"username": "user1", "userId": 1}'`
   - 用户2: `curl -X POST http://localhost:8080/api/test/jwt/generate -H "Content-Type: application/json" -d '{"username": "user2", "userId": 2}'`

3. **使用不同token访问API**
   - 用户1的token只能看到用户1的联系人
   - 用户2的token只能看到用户2的联系人

## 🔐 **JWT Token结构**

```json
{
  "sub": "username",
  "userId": 123,
  "iat": 1640995200,
  "exp": 1641081600
}
```

- **sub**: 用户名
- **userId**: 用户ID
- **iat**: 签发时间
- **exp**: 过期时间（24小时后）

## ⚠️ **重要注意事项**

### **1. Token安全**
- Token有效期：24小时
- 请妥善保管token，不要泄露
- 生产环境应使用HTTPS

### **2. 错误处理**
- 无效token返回401状态码
- 过期token需要重新生成
- 所有API都需要有效的Authorization header

### **3. 用户ID验证**
- 每个API调用都会验证token中的userId
- 用户只能访问自己的数据
- 实现了真正的数据隔离

## 🧪 **测试场景**

### **场景1：用户隔离**
1. 用户1登录，添加联系人A
2. 用户2登录，添加联系人B
3. 用户1看不到联系人B
4. 用户2看不到联系人A

### **场景2：Token过期**
1. 使用过期token访问API
2. 返回401错误
3. 需要重新生成token

### **场景3：无效Token**
1. 使用无效token访问API
2. 返回401错误
3. 需要提供有效token

## 📝 **API端点列表**

| 方法 | 端点 | 说明 | 需要认证 |
|------|------|------|----------|
| POST | `/api/family/contacts` | 添加联系人 | ✅ |
| GET | `/api/family/contacts` | 获取联系人列表 | ✅ |
| GET | `/api/family/contacts/{id}` | 获取特定联系人 | ✅ |
| PUT | `/api/family/contacts/{id}` | 更新联系人 | ✅ |
| DELETE | `/api/family/contacts/{id}` | 删除联系人 | ✅ |
| GET | `/api/family/emergency-contacts` | 获取紧急联系人 | ✅ |
| GET | `/api/family/stats` | 获取统计信息 | ✅ |
| POST | `/api/family/emergency-notification` | 发送紧急通知 | ✅ |
| POST | `/api/family/health-alert` | 发送健康警报 | ✅ |

## 🔄 **下一步优化建议**

1. **用户注册/登录系统**
   - 实现完整的用户认证流程
   - 密码加密存储
   - 用户角色管理

2. **Token刷新机制**
   - 实现refresh token
   - 自动token续期

3. **权限控制**
   - 基于角色的访问控制
   - 细粒度权限管理

4. **安全增强**
   - 添加rate limiting
   - 实现token黑名单
   - 日志审计

## 📞 **技术支持**

如有问题，请检查：
1. JWT依赖是否正确添加
2. Token格式是否正确（Bearer + 空格 + token）
3. Token是否过期
4. 用户ID是否有效

---
*Last Updated: 2025*
*Version: 3.0 - JWT Authentication*
