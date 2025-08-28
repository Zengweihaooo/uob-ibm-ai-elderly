# FamilyController 测试说明

## 概述

本项目为FamilyController创建了完整的单元测试套件。

## 测试文件结构

```
src/test/java/com/example/demo/controller/
└── FamilyControllerTest.java              # 单元测试（使用Mock）
```

## 测试类型

### 单元测试 (FamilyControllerTest.java)

**特点：**
- 使用`@WebMvcTest`注解，只加载Web层
- 使用`@MockBean`模拟FamilyService
- 测试Controller层的逻辑和HTTP响应
- 运行速度快，隔离性好
- 不依赖数据库或外部服务

**测试内容：**
- ✅ 添加家庭联系人（成功/失败场景）
- ✅ 获取所有联系人
- ✅ 获取特定联系人
- ✅ 更新联系人
- ✅ 删除联系人
- ✅ 发送消息
- ✅ 获取紧急联系人
- ✅ 获取统计信息
- ✅ 认证验证
- ✅ 数据验证
- ✅ 错误处理

## 如何运行测试

### 方法1：使用Maven Wrapper

```bash
# 进入项目目录
cd uob-ibm-ai-elderly/springboot

# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=FamilyControllerTest

# 运行特定测试方法
./mvnw test -Dtest=FamilyControllerTest#testAddFamilyContact_Success
```

### 方法2：使用批处理脚本（Windows）

```bash
# 双击运行
run-familytests.bat
```

### 方法3：使用IDE

在IDE中右键点击测试类或测试方法，选择"Run Test"。

## 测试数据

### 测试联系人数据

```json
{
  "name": "张三",
  "phoneNumber": "13800138000",
  "email": "zhangsan@example.com",
  "relationship": "CHILD",
  "notificationPreference": "ALL",
  "isEmergencyContact": true,
  "notes": "我的儿子"
}
```

### 认证头

所有需要认证的API都使用以下认证头：
```
Authorization: Bearer test-token
```

## 测试覆盖的API端点

| HTTP方法 | 端点 | 描述 | 测试状态 |
|---------|------|------|----------|
| POST | `/api/family/contacts` | 添加联系人 | ✅ |
| GET | `/api/family/contacts` | 获取所有联系人 | ✅ |
| GET | `/api/family/contacts/{id}` | 获取特定联系人 | ✅ |
| PUT | `/api/family/contacts/{id}` | 更新联系人 | ✅ |
| DELETE | `/api/family/contacts/{id}` | 删除联系人 | ✅ |
| POST | `/api/family/contacts/{id}/message` | 发送消息 | ✅ |
| GET | `/api/family/emergency-contacts` | 获取紧急联系人 | ✅ |
| GET | `/api/family/stats` | 获取统计信息 | ✅ |

## 测试场景

### 成功场景
- ✅ 正常添加联系人
- ✅ 正常获取联系人列表
- ✅ 正常更新联系人信息
- ✅ 正常删除联系人
- ✅ 正常发送消息

### 错误场景
- ✅ 缺少认证头
- ✅ 无效的认证头
- ✅ 缺少必填字段
- ✅ 联系人不存在
- ✅ 服务异常

### 边界场景
- ✅ 空联系人列表
- ✅ 多个联系人管理
- ✅ 紧急联系人筛选

## 测试结果解读

### 成功测试
- HTTP状态码：200 OK
- 响应格式：`{"success": true, "message": "...", "data": {...}}`

### 失败测试
- HTTP状态码：400 Bad Request / 401 Unauthorized / 404 Not Found / 500 Internal Server Error
- 响应格式：`{"success": false, "message": "错误描述"}`

## 注意事项

1. **内存存储**：测试使用内存存储，每次测试前会自动清理数据
2. **认证模拟**：目前使用简单的Bearer token验证，实际项目中需要实现JWT验证
3. **邮件服务**：测试环境禁用了真实的邮件发送，避免发送测试邮件
4. **数据隔离**：每个测试方法都是独立的，不会相互影响
5. **Mock使用**：使用Mockito模拟FamilyService，确保测试的隔离性

## 扩展测试

如果需要添加更多测试，可以：

1. **添加更多边界条件测试**
2. **测试不同的关系类型**
3. **测试通知偏好设置**
4. **测试并发操作**
5. **测试性能指标**

## 故障排除

### 常见问题

1. **测试失败**：检查FamilyService是否正确实现
2. **编译错误**：确保所有依赖都已正确导入
3. **认证失败**：检查认证头的格式是否正确

### 调试技巧

1. 在测试中添加`System.out.println()`输出调试信息
2. 使用IDE的调试功能逐步执行测试
3. 查看测试日志了解详细错误信息

## 未来规划

### 当项目接入数据库后
1. **添加Service层单元测试**
2. **添加Repository层测试**
3. **添加数据库集成测试**
4. **添加事务测试**

### 当项目有外部服务时
1. **添加外部服务集成测试**
2. **添加端到端测试**
3. **添加性能测试**
4. **添加负载测试** 