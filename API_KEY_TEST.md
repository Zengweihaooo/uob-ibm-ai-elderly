# Listen Notes API 密钥测试

## 当前API密钥
```
a3432f0d55d940e3bbe3d18f7acdeea6
```

## 测试步骤

### 1. 验证API密钥是否有效
```bash
curl -H "X-ListenAPI-Key: a3432f0d55d940e3bbe3d18f7acdeea6" \
     "https://listen-api.listennotes.com/api/v2/search?q=health&type=podcast&limit=1"
```

### 2. 检查API响应
- **成功响应**: 返回JSON格式的播客数据
- **401错误**: API密钥无效或已过期
- **429错误**: 请求频率超限
- **404错误**: 端点不存在

### 3. 获取新的API密钥
如果当前密钥无效，需要：
1. 访问 https://www.listennotes.com/api/
2. 注册/登录账户
3. 获取新的API密钥

## Listen Notes API 限制
- **免费计划**: 每月1,000次请求
- **付费计划**: 更多请求次数
- **请求频率**: 每分钟最多10次请求

## 正确的API端点

### 搜索播客
```
GET https://listen-api.listennotes.com/api/v2/search
参数:
- q: 搜索关键词
- type: podcast
- language: 语言代码 (en, zh, etc.)
- region: 地区代码
- sort_by: relevance, rating, latest
- offset: 偏移量
- limit: 结果数量 (最大10)
```

### 获取播客详情
```
GET https://listen-api.listennotes.com/api/v2/podcasts/{id}
```

### 获取播客剧集
```
GET https://listen-api.listennotes.com/api/v2/podcasts/{id}/episodes
```

## 问题排查

### 如果API密钥无效
1. 检查密钥是否正确复制
2. 确认密钥是否已过期
3. 检查账户状态

### 如果请求被限制
1. 检查请求频率
2. 考虑升级到付费计划
3. 实现请求缓存机制

### 如果端点不存在
1. 查看官方API文档
2. 使用替代端点
3. 实现模拟数据作为备用 