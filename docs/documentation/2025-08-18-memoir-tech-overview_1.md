# AI Memoir — 技术实现概览（验收版）

更新时间：2025-08-18
分支：memoir

## 1. 功能总览
- 项目与分段管理：创建项目，按「章节/主题/正文」维护内容
- AI 助手：
  - 生成项目大纲（Gemini/Mock，严格 JSON 解析、异常回退）
  - 依据章节/主题/备注生成草稿
- 语音转文字（STT）：浏览器录音或文件上传，支持 en-GB 等语言；一键追加/替换正文
- 导出：
  - Markdown（带 YAML frontmatter: title/author/date/locale）
  - PDF（XHTML 严格，封面、目录含页码、锚点书签、页眉页脚、H2 强制分页、图片/链接转换、Base URI、可选字体、水印、元数据与可选加密）
- 分享与访问控制：
  - 生成分享链接（PIN/有效期/下载上限/scope）
  - 公开页 /s/{token}，受控下载 /s/{token}/download
- 前端页面：/pages/memoir.html 英文 UI，整合项目/分段、AI、STT、导出与分享

## 2. 架构与关键模块
- 后端：Spring Boot 3.x + MyBatis（SQLite）
  - 核心 Mapper：`MemoirMapper`（项目/分段查询、导出所需数据）
  - 导出服务：`MemoirExportService`
    - 生成 Markdown：拼装 frontmatter 与按章节/主题的正文
    - Markdown→XHTML：
      - 解析 frontmatter 设置 `<title>` 与 `<meta name="author">`
      - 自动生成 TOC（H1/H2/H3 可选）与锚点 slug
      - Markdown 图片与链接转换为合规 XHTML 元素
      - 封面/页眉页脚/页边距/水印/基础样式（均通过 CSS）
    - XHTML→PDF：基于 `openhtmltopdf` 反射调用（避免直接依赖冲突）
      - 注册可选字体；写入 PDF 元数据；可选加密（权限：打印/复制、密钥长度）
  - AI 服务：`AiMemoirService` + `GeminiClient`
    - Provider 开关（gemini/mock）；严格 JSON 解析；异常回退
    - Draft 与 Outline 两类生成接口
  - 分享服务：`MemoirShareService`
    - 创建分享：随机 token、可选 pinHash（SHA-256）、有效期、下载上限
    - 校验：`verifyPin`、`canDownload`、`markDownloaded`、`requiresPin`
  - 控制器：
    - `MemoirController`：项目/分段/导出/AI 端点
    - `MemoirShareController`：分享创建、公开页、受控下载
- 前端：静态页面 `/pages/memoir.html`
  - 项目/分段 CRUD；导出按钮；AI 助手；STT；创建分享
  - 简洁 UI（英文）、移动端友好

## 3. 数据与配置
- 数据库：SQLite（`springboot/data/elderly_companion.db`）
  - 新增表：`memoir_share_guard`（pin_hash、max_downloads、download_count）
  - 关联：`memoir_share_token`（token、project_id、expires_at、scope）
- 应用配置（`application.properties`）：
  - 导出：`app.export.pdf.*`（字体、页边距、TOC 级别、H2 分页、base-uri、水印、封面图、安全…）
  - AI：`app.ai.*`（provider、apiKey 等）

## 4. PDF 细节
- 严格 XHTML（声明 DTD；元素闭合）以确保渲染稳定
- CSS 特性：
  - `@page` 页眉页脚（running elements）、A4、边距；封面页独立版式
  - 目录页使用 `leader('.') + target-counter(attr(href), page)` 显示页码
  - `-fs-pdf-outline` 输出书签
  - `.page-break` 控制分页；H2 可开启 `page-break-before`
- 元数据与安全：反射走 PDFBox，对生成字节做二次处理

## 5. 分享与访问控制流程
1) 创建分享：`POST /api/memoir/projects/{id}/share`
   - 请求：`{ pin?, days?, maxDownloads?, scope='view' }`
   - 返回：`{ token, expiresAt, scope, maxDownloads }`
2) 公开页：`GET /s/{token}`
   - 若需要 PIN 显示输入框，可选选择下载格式
3) 下载：`GET /s/{token}/download?format=pdf|markdown[&pin=...]`
   - 校验过期/次数/PIN，成功后返回文件并计数

## 6. 测试与质量
- 已有：导出 HTML 结构、PDF 高级特性与导出接口测试
- 待补：分享端到端集成测试（创建→PIN→下载→上限/过期）

## 7. 已知边界与后续建议
- 分享的管理接口（列出/撤销）尚未提供
- 分享页错误提示与可用性可进一步打磨
- 建议补充：分享 E2E 测试、管理端点、README 使用说明与安全提示

## 8. 快速试用
- 打开 `/pages/memoir.html`：
  - 创建项目→添加分段→AI 生成大纲/草稿（可选）→导出 Markdown/PDF
  - 点击 Create Share→填写 PIN/有效期/上限→复制链接访问 `/s/{token}` 下载

以上覆盖了验收版所有功能与技术实现要点，可作为技术交接与验收依据。
