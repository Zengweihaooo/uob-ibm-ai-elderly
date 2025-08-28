# AI 回忆录 — 用户使用指南（验收版）

更新时间：2025-08-18
适用对象：长者与照护者、普通用户
访问入口：/pages/memoir.html

## 1. 主要能力
- 管理回忆录项目与分段（章节/主题/正文）
- AI 助手：生成大纲、生成草稿
- 语音转文字（STT）：录音或上传音频转写
- 导出：Markdown、PDF（成品排版）
- 分享：生成链接（可设置 PIN、有效期、下载上限）

## 2. 快速开始
1) 打开页面 `/pages/memoir.html`
2) 新建项目：填写 Title（可选 Owner），点击 Create
3) 添加分段：
   - 填写 Chapter、Theme、Text
   - 点击 Add 加入当前项目
4) 导出：
   - Export Markdown → 下载 .md
   - Export PDF → 下载 .pdf（含封面、目录、页眉页脚等）
5) 分享给家人朋友：
   - 点击 Create Share，按提示设置 PIN（可选）、有效期（天）、下载上限（次）
   - 复制弹窗中的链接（形如 /s/{token}）发给对方
   - 如设置了 PIN，对方打开链接需输入 PIN 才能下载

## 3. AI 助手
- 生成大纲：
  - 在 AI Assistant 区域输入提示（可选，如“童年在伦敦”）
  - 点击 Generate Outline → 列表展示章节与主题建议
- 生成草稿：
  - 在 Add Segment 填好 Chapter/Theme，AI Notes 可写备注
  - 点击 Generate Draft → 结果自动填充到 Text（可再编辑）
- 可靠性：
  - 优先调用 Gemini；若网络或配额异常自动回退到内置 Mock，保证功能可用

## 4. 语音转文字（STT）
- 录音方式：点击 Start Recording 开始录音，再点击 Stop & Transcribe 结束并转写
- 上传方式：选择本地音频（webm/ogg），点击 Upload & Transcribe
- 语言：默认 English (UK)，可切换 en-US/中文普通话/粤语等
- 转写结果：
  - Append 追加到 Text
  - Replace 直接替换 Text

## 5. PDF 导出说明
- 自动生成封面（标题/作者/UK 日期）与目录（含页码与跳转）
- H1/H2/H3 生成 PDF 书签（可配置 TOC 等级）
- 页眉显示“AI Memoir — 标题 · 日期”，页脚显示页码
- H2 可配置强制分页；支持 Markdown 图片/链接；可选水印与字体
- 若打开加密，PDF 可限制打印/复制等权限（由运维配置）

## 6. 分享与访问控制
- 创建分享：点击 Create Share → 设置 PIN/有效期（天）/下载上限（次）
- 访问链接：/s/{token}
  - 若需要 PIN，会显示输入框
  - 可选下载 PDF 或 Markdown
- 常见提示：
  - Invalid pin → PIN 错误
  - Download limit reached → 达到下载上限
  - Expired → 链接已过期
  - Not found → 链接不存在或已撤销

## 7. 常见问题
- 下载 PDF 打不开？
  - 请确认下载完整；如开启了 PDF 安全策略，可能需要输入打开密码（若配置了）
- AI 生成失败？
  - 网络问题或 API 配额限制时会自动回退到 Mock；稍后重试或使用 Mock 结果继续编辑
- STT 录音不可用？
  - 某些浏览器不支持，建议使用 Upload & Transcribe 上传本地音频

## 8. 建议使用方式
- 先用手机录音或边回忆边录音 → STT 转为文字
- 用 AI 生成大纲和草稿，快速起稿后再人工润色
- 按章节/主题拆分成段，导出 PDF 与家人分享
- 对于隐私内容，务必设置 PIN 且限制下载次数
