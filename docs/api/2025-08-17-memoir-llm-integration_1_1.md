# 回忆录模块 - LLM 集成与导出能力发布说明（2025-08-17）

本次提交聚焦“AI 回忆录”模块（独立于其他功能），在后端新增 AI 草拟与大纲接口、完善 Markdown/PDF 导出，并提供最小可用的前端页面，便于端到端试用与演示。

## 变更摘要

- 新增（后端）
  - AI 客户端：`service/ai/GeminiClient.java`（轻量级 REST 客户端，复用项目已有 Google 依赖）
  - AI 服务：`service/memoir/AiMemoirService.java`（原有 mock 扩展为可切换 Gemini，失败自动回退）
  - 回忆录控制器：`controller/MemoirController.java` 新增 AI 端点（`/api/memoir/ai/outline`、`/api/memoir/ai/draft`）
  - 导出服务：`service/memoir/MemoirExportService.java` 确保 HTML 严格 XHTML 以稳定 PDF 导出
  - 数据层：`resources/mapper/MemoirMapper.xml` 与相关实体/服务（CRUD、导出数据聚合）
  - 配置：`application.properties` 增补 `app.ai.*` 可配置项并修正历史属性注释导致的绑定问题
- 新增（前端）
  - `resources/static/pages/memoir.html`：最小 UI，支持新建项目、添加分段、查看分段、导出 Markdown/PDF
- 测试
  - 通过的集成测试：`MemoirControllerTest`（CRUD）、`MemoirExportTest`（Markdown/PDF）
  - 修复问题：属性绑定失败（内联注释）与 openhtmltopdf 对 XHTML 的严格要求

## 如何使用（应用功能）

- 访问页面：`/pages/memoir.html`
  - 新建项目：输入“标题/所有者”，点击“创建项目”
  - 进入项目：在“已有项目”列表中点击“进入”
  - 添加分段：填写“章节/主题/正文”，点击“添加分段”
  - 导出：
    - Markdown：点击“导出 Markdown”（下载 .md）
    - PDF：点击“导出 PDF”（下载 .pdf）

- 后端 API（回忆录）
  - 创建项目：`POST /api/memoir/projects` body: `{ "title": "...", "owner": "..." }`
  - 列出项目：`GET /api/memoir/projects`
  - 添加分段：`POST /api/memoir/projects/{id}/segments` body: `{ "chapter": "..", "theme": "..", "text": ".." }`
  - 列出分段：`GET /api/memoir/projects/{id}/segments`
  - 模板：`GET /api/memoir/templates`
  - 导出：`GET /api/memoir/projects/{id}/export?format=markdown|pdf`

- 后端 API（AI 草拟与大纲）
  - 大纲：`POST /api/memoir/ai/outline`
    - body: `{ "locale": "zh-CN", "hint": "童年与故乡" }`
    - 返回：在 mock 模式下返回结构化章节与主题；在 Gemini 模式下返回 `{"raw": "...", "source":"gemini"}`（原始 JSON 字符串由模型生成）
  - 草稿：`POST /api/memoir/ai/draft`
    - body: `{ "chapter":"童年", "theme":"家庭", "notes":"与祖母在老屋的回忆", "locale":"zh-CN" }`
    - 返回：`{"text":"...", "source":"mock|gemini", ...}`

## LLM（Gemini）启用指南（可选）

默认启用 mock（无需外部依赖）。如需启用 Gemini：

1) 配置凭据（2 选 1）
- 指定服务账号 JSON 文件路径（Windows PowerShell）：
  - `$env:GOOGLE_APPLICATION_CREDENTIALS = "D:\path\to\gcp-service-account.json"`
- 或直接提供 JSON 字符串：
  - `$env:GOOGLE_CLOUD_CREDENTIALS = '{"type":"service_account", ... }'`

2) 关闭 mock、指定提供方和模型（application.properties 或环境覆盖）
- `app.ai.mock=false`
- `app.ai.provider=gemini`
- `app.ai.gemini.model=gemini-1.5-flash`

3) 重启后端服务并调用上述 AI 接口。

失败回退策略：当凭据缺失或调用异常时，服务自动回退到 mock，保障功能可用性。

## 测试说明

- 已有集成测试
  - `MemoirControllerTest`：覆盖项目创建/查询、分段新增/查询，验证基本 CRUD 正常
  - `MemoirExportTest`：生成 Markdown 并导出 PDF，校验 XHTML 修正后的稳定性

- 运行方式（在 springboot 目录）
  - Windows PowerShell（示例）：
    - `./mvnw -q -Dtest=MemoirControllerTest test`
    - `./mvnw -q -Dtest=MemoirExportTest test`
  - 若遇到依赖下载慢，可尝试：`./mvnw -U -q -Dtest=... test`

- 建议新增（可选）
  - AI 端点测试：在 mock 与 gemini 两种配置下，验证 `/api/memoir/ai/outline` 与 `/api/memoir/ai/draft` 的响应形状与回退行为。

## 兼容性与注意事项

- 数据库存储仍为 SQLite，本次新增表通过初始化器确保在已有数据库下平滑创建
- PDF 导出依赖 openhtmltopdf，HTML 必须为严格 XHTML（本次已修正）
- 生产环境使用 Gemini 前需妥善管理服务账号密钥

---

如需我添加导航入口、将“语音转文本（/api/voice/stt）”串联到回忆录页面，或补齐 AI 端点的测试用例，请告诉我优先级。
