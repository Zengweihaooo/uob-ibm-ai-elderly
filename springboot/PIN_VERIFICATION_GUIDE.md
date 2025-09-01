# 🔐 PIN码验证和修改流程测试指南

## 📋 问题描述
用户点击"Set PIN"按钮时，需要：
1. 首先验证当前的PIN码
2. 验证成功后，允许输入新PIN码
3. 更新所有重要memo的PIN码为新设置的PIN码

## ✅ 已修复的问题
1. **数据库PIN码一致性**：已将所有用户ID 1的重要memo的PIN码统一为 `1234`
2. **添加调试日志**：在PIN码验证和设置方法中添加了详细的调试日志
3. **验证逻辑**：确保PIN码验证逻辑正确

## 🧪 测试步骤

### 步骤1：检查数据库状态
```bash
# 检查用户ID 1的PIN码状态
sqlite3 data/elderly_companion.db "SELECT id, user_id, title, pin_code FROM memos WHERE user_id = 1 AND is_important = 1 AND is_deleted = 0;"
```

### 步骤2：重启Spring Boot应用
```bash
# 重启应用以加载新的调试日志
cd springboot
./mvnw spring-boot:run
```

### 步骤3：在浏览器中测试

#### 3.1 获取JWT Token
打开浏览器开发者工具（F12），在Console中运行：
```javascript
// 获取token
const token = localStorage.getItem('authToken');
console.log('Token:', token);

// 解码token查看用户ID
if (token && token !== 'null') {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('用户ID:', payload.userId);
        console.log('用户邮箱:', payload.email);
    } catch (e) {
        console.log('Token解码失败');
    }
}
```

#### 3.2 测试PIN码验证
```javascript
// 测试PIN码验证
async function testPinVerification(pinCode) {
    const token = localStorage.getItem('authToken');
    
    const response = await fetch('http://localhost:8080/api/memo/verify-current-pin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            currentPin: pinCode
        })
    });
    
    const data = await response.json();
    console.log('PIN验证结果:', data);
    return data;
}

// 测试PIN码1234
testPinVerification('1234');
```

#### 3.3 测试设置新PIN码
```javascript
// 测试设置新PIN码
async function testSetNewPin(newPinCode) {
    const token = localStorage.getItem('authToken');
    
    const response = await fetch('http://localhost:8080/api/memo/set-new-pin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            newPinCode: newPinCode
        })
    });
    
    const data = await response.json();
    console.log('设置新PIN码结果:', data);
    return data;
}

// 测试设置新PIN码为5678
testSetNewPin('5678');
```

### 步骤4：检查服务器日志
在Spring Boot控制台中查看调试日志：
```
DEBUG: 验证PIN码 - 用户ID: 1, 输入PIN: 1234
DEBUG: 找到 8 个重要memo
DEBUG: 检查memo ID: 1, 存储的PIN: 1234
DEBUG: PIN码匹配成功!
DEBUG: PIN码验证成功
```

## 🔧 故障排除

### 问题1：PIN码验证失败
**可能原因：**
- 用户ID不匹配
- JWT token无效
- 数据库中的PIN码不一致

**解决方案：**
1. 检查用户ID是否正确
2. 重新登录获取新的JWT token
3. 运行以下命令修复数据库：
```bash
sqlite3 data/elderly_companion.db "UPDATE memos SET pin_code = '1234' WHERE user_id = YOUR_USER_ID AND is_important = 1 AND is_deleted = 0;"
```

### 问题2：设置新PIN码失败
**可能原因：**
- 当前PIN码验证未通过
- 新PIN码格式不正确
- 数据库更新失败

**解决方案：**
1. 确保当前PIN码验证成功
2. 确保新PIN码是4位数字
3. 检查服务器日志中的调试信息

### 问题3：前端显示错误
**可能原因：**
- API调用错误
- 网络连接问题
- 前端逻辑错误

**解决方案：**
1. 检查浏览器控制台错误
2. 确认API端点正确
3. 检查网络请求状态

## 📊 预期结果

### 成功的PIN码验证
```json
{
    "success": true,
    "message": "当前PIN码验证成功"
}
```

### 成功的PIN码设置
```json
{
    "success": true,
    "message": "PIN码更新成功，已更新 8 个重要备忘录"
}
```

### 数据库更新后
```sql
SELECT id, user_id, title, pin_code FROM memos WHERE user_id = 1 AND is_important = 1;
-- 所有重要memo的pin_code都应该更新为新设置的PIN码
```

## 🎯 完整流程测试

1. **登录系统** → 获取JWT token
2. **验证当前PIN** → 输入 `1234`，应该验证成功
3. **设置新PIN** → 输入新PIN码（如 `5678`），应该设置成功
4. **验证新PIN** → 输入 `5678`，应该验证成功
5. **检查数据库** → 确认所有重要memo的PIN码已更新

## 📝 注意事项

1. **用户隔离**：每个用户的PIN码是独立的
2. **数据一致性**：所有重要memo使用相同的PIN码
3. **安全性**：PIN码验证通过JWT token进行用户身份验证
4. **调试日志**：生产环境中应移除调试日志

## 🚀 下一步

如果测试成功，可以：
1. 移除调试日志
2. 优化用户体验
3. 添加PIN码强度验证
4. 实现PIN码重置功能
