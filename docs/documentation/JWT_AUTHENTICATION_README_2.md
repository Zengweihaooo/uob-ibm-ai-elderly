# 🔐 JWT认证系统实现说明

## 📋 **问题描述**

在之前的版本中，所有功能模块都硬编码返回用户ID `1L`，导致：
- 注册多个账号看到相同的数据
- 无法实现真正的用户数据隔离
- 安全性问题

## 🚀 **解决方案**

### **1. 实现完整的JWT认证系统**

#### **新增组件**
- `JwtUtil.java` - JWT工具类，负责token生成和解析
- `JwtConfig.java` - JWT配置类，管理JWT相关参数
- `UserContextUtil.java` - 用户上下文工具类，从Authorization header提取用户信息

#### **JWT依赖**
```xml
<!-- JWT 认证支持 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

### **2. 用户登录流程**

#### **登录成功后的响应**
```json
{
    "success": true,
    "message": "Login successful! Welcome back.",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
        "id": 123,
        "email": "user@example.com",
        "name": "John Doe",
        "role": "ELDERLY"
    }
}
```

#### **前端存储token**
```javascript
// 登录成功后保存token
localStorage.setItem('authToken', response.token);

// 后续请求携带token
const response = await fetch('/api/health/records', {
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        'Content-Type': 'application/json'
    }
});
```

### **3. 后端用户ID提取**

#### **替换硬编码**
```java
// ❌ 之前的问题代码
private Long getUserIdFromToken(String authHeader) {
    return 1L; // 硬编码，所有用户看到相同数据
}

// ✅ 修复后的代码
@Autowired
private UserContextUtil userContextUtil;

public ResponseEntity<Map<String, Object>> getHealthRecords(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    
    Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
    if (userId == null) {
        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "message", "Invalid or expired token"
        ));
    }
    
    // 使用真实的用户ID查询数据
    List<HealthRecord> records = healthService.getHealthRecordsByUser(userId);
    // ...
}
```

### **4. 已修复的Controller**

✅ **HealthController** - 健康记录管理
✅ **UserController** - 用户认证（新增JWT token生成）

### **5. 待修复的Controller**

❌ **ScheduleController** - 日程管理
❌ **PetController** - 宠物情绪
❌ **ImportantDateController** - 重要日期
❌ **MemoController** - 备忘录
❌ **ChatController** - 聊天记录
❌ **FamilyController** - 家庭联系人

## 🔧 **配置说明**

### **JWT配置参数**
```properties
# JWT认证配置
jwt.secret=yourSuperSecretKeyForJWTTokenGenerationChangeInProduction
jwt.expiration=86400000
jwt.header=Authorization
jwt.prefix=Bearer
```

### **安全建议**
1. **生产环境**：修改`jwt.secret`为强密码
2. **Token过期**：根据需要调整`jwt.expiration`
3. **HTTPS**：生产环境必须使用HTTPS

## 📱 **前端集成**

### **登录页面**
```html
<form id="loginForm">
    <input type="email" id="email" placeholder="邮箱" required>
    <input type="password" id="password" placeholder="密码" required>
    <button type="submit">登录</button>
</form>
```

### **登录逻辑**
```javascript
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData();
    formData.append('email', document.getElementById('email').value);
    formData.append('password', document.getElementById('password').value);
    
    try {
        const response = await fetch('/user/login', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        if (data.success) {
            // 保存token和用户信息
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('userInfo', JSON.stringify(data.user));
            
            // 跳转到主页面
            window.location.href = '/dashboard';
        } else {
            alert(data.message);
        }
    } catch (error) {
        console.error('Login failed:', error);
        alert('登录失败，请重试');
    }
});
```

### **API请求封装**
```javascript
// 通用API请求函数
async function apiRequest(url, options = {}) {
    const token = localStorage.getItem('authToken');
    
    const defaultOptions = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    };
    
    const finalOptions = { ...defaultOptions, ...options };
    
    try {
        const response = await fetch(url, finalOptions);
        
        if (response.status === 401) {
            // Token过期，跳转到登录页
            localStorage.removeItem('authToken');
            localStorage.removeItem('userInfo');
            window.location.href = '/login';
            return null;
        }
        
        return await response.json();
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// 使用示例
const healthRecords = await apiRequest('/api/health/records');
```

## 🧪 **测试步骤**

### **1. 启动应用**
```bash
cd springboot
mvn spring-boot:run
```

### **2. 注册新用户**
- 访问 `/user/register`
- 输入邮箱，获取验证码
- 完成注册

### **3. 登录获取token**
- 访问 `/user/login`
- 输入邮箱和密码
- 获取JWT token

### **4. 测试数据隔离**
- 使用不同账号登录
- 访问健康记录、日程等功能
- 验证是否看到不同的数据

## 🎯 **预期结果**

✅ **用户注册**：每个邮箱只能注册一个账号
✅ **用户登录**：登录成功后获得唯一的JWT token
✅ **数据隔离**：不同用户看到完全不同的数据
✅ **安全性**：token过期后自动跳转登录页
✅ **用户体验**：登录状态持久化，无需重复登录

## 🚨 **注意事项**

1. **首次部署**：需要重新注册用户，因为之前的用户没有密码
2. **Token管理**：前端需要妥善管理token的存储和刷新
3. **错误处理**：401错误时自动跳转登录页
4. **生产环境**：必须修改JWT secret和启用HTTPS

## 📚 **下一步计划**

1. **修复剩余Controller**：应用相同的用户ID提取模式
2. **添加权限控制**：基于用户角色的功能访问控制
3. **Token刷新机制**：实现无感知的token自动刷新
4. **会话管理**：支持多设备登录和会话控制
