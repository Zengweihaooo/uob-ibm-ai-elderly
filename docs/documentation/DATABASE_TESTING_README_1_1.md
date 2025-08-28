# SQLite Database Testing Guide
# SQLite数据库测试指南

## 概述 / Overview

本指南说明如何测试SQLite数据库集成，验证所有数据库功能是否正常工作。
This guide explains how to test SQLite database integration and verify all database functions work correctly.

## 测试结构 / Test Structure

### 1. 单元测试 / Unit Tests
- **文件**: `DatabaseManagementServiceTest.java`
- **目的**: 测试数据库管理服务的各个方法
- **测试内容**:
  - 数据库路径获取
  - 备份路径获取
  - 数据库状态检查
  - 数据库验证
  - 备份创建
  - 备份恢复
  - 旧备份清理

### 2. 集成测试 / Integration Tests
- **文件**: `DatabaseIntegrationTestTypeSafe.java`
- **目的**: 测试完整的数据库API功能
- **测试内容**:
  - 数据库文件自动创建
  - REST API端点
  - 数据库状态API
  - 数据库信息API
  - 备份功能API
  - 错误处理

## 运行测试 / Running Tests

### 方法1: 使用批处理脚本 / Method 1: Using Batch Script
```bash
# Windows系统
cd springboot
run-database-tests.bat
```

### 方法2: 使用Maven命令 / Method 2: Using Maven Commands
```bash
# 运行所有数据库测试
mvn test -Dtest="*Database*"

# 运行单元测试
mvn test -Dtest=DatabaseManagementServiceTest

# 运行集成测试
mvn test -Dtest=DatabaseIntegrationTestTypeSafe
```

### 方法3: 在IDE中运行 / Method 3: Running in IDE
1. 在VS Code中打开测试文件
2. 点击测试方法旁的运行按钮
3. 或使用测试面板运行所有测试

## 测试配置 / Test Configuration

### 测试数据库路径 / Test Database Paths
- **数据库文件**: `test_data/test_elderly_companion.db`
- **备份目录**: `test_data/test_backups/`
- **单元测试数据库**: `test_data/test_unit_elderly_companion.db`

### 测试属性 / Test Properties
```properties
# 集成测试配置
app.database.path=test_data/test_elderly_companion.db
app.database.backup.path=test_data/test_backups/
spring.datasource.url=jdbc:sqlite:test_data/test_elderly_companion.db
```

## 预期结果 / Expected Results

### 成功的测试应该显示 / Successful tests should show:
- ✅ 数据库文件自动创建
- ✅ 所有API端点返回正确响应
- ✅ 备份功能正常工作
- ✅ 数据库验证通过
- ✅ 错误处理正确

### 测试覆盖的功能 / Functions covered by tests:
1. **数据库初始化** / Database initialization
2. **连接测试** / Connection testing
3. **API端点测试** / API endpoint testing
4. **备份和恢复** / Backup and restore
5. **错误处理** / Error handling
6. **数据完整性验证** / Data integrity validation

## 故障排除 / Troubleshooting

### 常见问题 / Common Issues

#### 1. 测试数据库文件锁定 / Test database file locked
```
错误: Database is locked
解决方案: 关闭所有使用数据库的应用程序，删除测试文件重新运行
```

#### 2. 权限问题 / Permission issues
```
错误: Access denied to test_data directory
解决方案: 确保有读写权限，或以管理员身份运行
```

#### 3. 端口冲突 / Port conflicts
```
错误: Port already in use
解决方案: 停止其他Spring Boot应用程序
```

### 清理测试文件 / Cleaning test files
```bash
# 手动清理测试文件
rmdir /s test_data
# 或在PowerShell中
Remove-Item -Recurse -Force test_data
```

## 测试数据 / Test Data

### 测试过程中创建的文件 / Files created during testing:
- `test_data/test_elderly_companion.db` - 主测试数据库
- `test_data/test_unit_elderly_companion.db` - 单元测试数据库
- `test_data/test_backups/*.db` - 备份文件
- `test_data/test_backups/before_restore_*.db` - 恢复前备份

### 自动清理 / Automatic cleanup:
测试完成后会自动清理测试文件，无需手动干预。
Test files are automatically cleaned up after test completion.

## 与主应用的集成 / Integration with Main Application

### 测试不会影响生产数据 / Tests don't affect production data:
- 使用独立的测试数据库路径
- 测试配置与生产配置隔离
- 自动清理测试文件

### 验证生产配置 / Verifying production configuration:
```bash
# 检查生产数据库路径
application.properties中的配置:
app.database.path=data/elderly_companion.db
app.database.backup.path=data/backups/
```

## 性能测试注意事项 / Performance Testing Notes

### 测试执行时间 / Test execution time:
- 单元测试: ~10-30秒
- 集成测试: ~30-60秒  
- 完整测试套件: ~1-2分钟

### 并行测试 / Parallel testing:
测试设计为顺序执行，避免文件冲突。
Tests are designed to run sequentially to avoid file conflicts.

## 下一步 / Next Steps

测试通过后，可以进行以下操作:
After tests pass, you can proceed with:

1. **部署到生产环境** / Deploy to production
2. **添加更多数据模型测试** / Add more data model tests
3. **集成CrewAI功能测试** / Integrate CrewAI functionality tests
4. **性能和负载测试** / Performance and load testing
