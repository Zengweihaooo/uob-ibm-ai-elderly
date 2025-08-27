# 分享模块 — 管理接口与集成测试方案

更新时间：2025-08-18
适用对象：开发与测试

## 1. 现有功能回顾
- 创建分享：`POST /api/memoir/projects/{id}/share`（PIN/有效期/下载上限/scope）
- 公开页：`GET /s/{token}`（按需显示 PIN、选择下载格式）
- 受控下载：`GET /s/{token}/download?format=pdf|markdown[&pin=...]`（校验过期/上限/PIN，计数）

## 2. 建议的管理接口（后端）
> 便于后台管理与自动化测试控制

- 列出分享（按项目）：
  - `GET /api/memoir/projects/{id}/shares`
  - 响应：`[{ id, token, expiresAt, scope, maxDownloads, downloadCount, requiresPin }]`
- 撤销分享：
  - `DELETE /api/memoir/shares/{id}` 或 `DELETE /api/memoir/projects/{projectId}/shares/{id}`
  - 行为：删除 token 记录与 guard（或设置为失效）
- 获取分享详情：
  - `GET /api/memoir/shares/{id}`（用于后台查看）

实现提示：
- Mapper 增加：按项目查询分享、按 id 删除分享 token/guard、汇总 downloadCount
- Service 增加：listShares(projectId)、revokeShare(shareId)、getShare(shareId)
- Controller 增加对应端点，返回统一 JSON

## 3. 集成测试方案（E2E）
使用 SpringBootTest + MockMvc，基于 SQLite 临时数据库或测试库。

场景用例：
1) 创建项目与分段 → 导出接口健康检查
2) 创建分享（带 PIN 与限制）→ 返回 token
3) 未带 PIN 下载 → 403 invalid pin
4) 带正确 PIN 下载 Markdown → 200 且 downloadCount=1
5) 重复下载至上限 → 达到上限后 429 download limit reached
6) 设置 expiresAt 为过去时间 → 访问下载返回 410 expired
7) 撤销分享 → 访问返回 404 not found

校验点：
- 响应状态码与消息体 `error` 字段
- 下载计数精确累加
- 过期/撤销后不可访问

## 4. 示例测试代码骨架
```java
@SpringBootTest
@AutoConfigureMockMvc
class MemoirShareE2ETest {
  @Autowired MockMvc mvc;

  @Test
  void share_flow_e2e() throws Exception {
    // 1) create project
    var p = mvc.perform(post("/api/memoir/projects").contentType(MediaType.APPLICATION_JSON)
        .content("{\"title\":\"Test\"}"))
      .andExpect(status().isOk()).andReturn();
    int projectId = JsonPath.read(p.getResponse().getContentAsString(), "$.projectId");

    // 2) create share
    var s = mvc.perform(post("/api/memoir/projects/"+projectId+"/share").contentType(MediaType.APPLICATION_JSON)
        .content("{\"pin\":\"1234\",\"days\":1,\"maxDownloads\":2}"))
      .andExpect(status().isOk()).andReturn();
    String token = JsonPath.read(s.getResponse().getContentAsString(), "$.token");

    // 3) invalid pin
    mvc.perform(get("/s/"+token+"/download").param("format","markdown"))
      .andExpect(status().isForbidden());

    // 4) valid pin
    mvc.perform(get("/s/"+token+"/download").param("format","markdown").param("pin","1234"))
      .andExpect(status().isOk());

    // 5) hit limit
    mvc.perform(get("/s/"+token+"/download").param("format","markdown").param("pin","1234"))
      .andExpect(status().isOk());
    mvc.perform(get("/s/"+token+"/download").param("format","markdown").param("pin","1234"))
      .andExpect(status().isTooManyRequests());
  }
}
```

> 说明：上述为骨架示例，实际项目已存在 `MemoirControllerTest` 等测试用例。可在 `springboot/src/test/java/.../memoir/` 下新建 `MemoirShareE2ETest.java` 并完善。

## 5. 验收建议
- 管理端点 + E2E 测试合入后，执行一轮：
  - Build、单元测试、分享 E2E、手工点击 /pages/memoir.html 完成一次分享下载
- 文档中加入安全提示（PIN 不等于强密码；敏感内容请谨慎分享）
