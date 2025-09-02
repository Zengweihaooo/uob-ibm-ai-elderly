# Memoir Module - PDF Export Enhancements & Robustness Release Notes (2025-08-18)

This commit focuses on PDF export and user experience enhancements for the "AI Memoir" feature, covering cover page, TOC page numbers, pagination strategy, images/links, fonts, watermark, and security encryption. It also strengthens AI integration and English frontend adjustments, keeping the module isolated, ready-to-use, and configurable.

## Change Summary

- PDF Export (Backend)
  - Cover Page: Reads title/author/date from frontmatter; optional background image; no header/footer on the cover
  - Document Metadata: Writes PDF title and author
  - TOC: Auto-generated, clickable entries; TOC items include page numbers (leader + target-counter)
  - Bookmarks: H1/H2 produce PDF bookmarks by default; H3 bookmarks can be enabled
  - Pagination Strategy: New toggle to force new page before H2; configurable margins (separate for body and cover)
  - Resource Resolution: Added baseUri supporting relative paths for images/links
  - Markdown Extension: Images and links auto-converted to strict XHTML
  - Font Embedding: Configurable registration of TTF/OTF fonts for better mixed Chinese/English layout and offline consistency
  - Watermark: Text watermark via inline SVG (configurable text/opacity)
  - Security & Encryption: Optional PDF encryption (user/owner password, print/copy permissions, key length)
- AI Robustness (Backend)
  - Gemini API Key support (second channel besides service account)
  - Strict JSON parsing of outline; fallback to mock on error to ensure availability
- Frontend
  - `memoir.html` English UI for elderly users; STT default en-GB; keeps entry for "AI Assistant (Outline/Draft)"
- Testing
  - Added `MemoirExportHtmlAdvancedTest` covering: TOC page numbers, H2 pagination, H3 bookmarks, cover background, image/link conversion, watermark
  - Retained & passing: export endpoint tests, basic HTML build tests

## How to Use (Application)

- Page Entry: `/pages/memoir.html`
  - Create project → Add segments (chapter/topic/body) → Optionally invoke AI for outline/draft → Export as Markdown or PDF
- Backend Export API: `GET /api/memoir/projects/{id}/export?format=markdown|pdf`

## Key Configuration (application.properties)

- Font Embedding (Recommended):
  - `app.export.pdf.font.paths=C:\\Windows\\Fonts\\NotoSansSC-Regular.otf;D:\\fonts\\DejaVuSerif.ttf`
- Pagination & TOC:
  - `app.export.pdf.pagebreak.h2=true|false` (force new page before H2)
  - `app.export.pdf.toc.level=2|3` (TOC depth & bookmarks: 2=H1/H2, 3=H1/H2/H3)
  - `app.export.pdf.page.margin=20mm 16mm`
  - `app.export.pdf.page.margin.cover=25mm 20mm`
- Resource Resolution:
  - `app.export.pdf.base-uri=file:///D:/Desktop/uob-ibm-ai-elderly-1/`
- Watermark:
  - `app.export.pdf.watermark.text=DRAFT`
  - `app.export.pdf.watermark.opacity=0.08` (0..1)
- Cover Background Image:
  - `app.export.pdf.cover.image=assets/images/web/cover.jpg`
- PDF Security:
  - `app.export.pdf.security.enabled=true|false`
  - `app.export.pdf.security.user-password=`
  - `app.export.pdf.security.owner-password=`
  - `app.export.pdf.security.allow-print=true|false`
  - `app.export.pdf.security.allow-copy=true|false`
  - `app.export.pdf.security.key-length=256`
- AI (Optional Gemini):
  - `app.ai.mock=false`, `app.ai.provider=gemini`, `app.ai.gemini.model=gemini-1.5-flash`
  - API Key: `app.ai.gemini.api-key=` or `app.ai.gemini.api-key-env=GEMINI_API_KEY`

## Testing Notes

- Unit / Integration Tests
  - `MemoirExportHtmlTest`: TOC anchors, H1/H2 ids, bookmark CSS, header
  - `MemoirExportHtmlAdvancedTest`: TOC page numbers, H2 pagination, H3 bookmarks, cover image, image/link conversion, watermark
  - `MemoirExportTest`: Markdown/PDF export endpoint availability
- Windows PowerShell (Examples)
  - `./mvnw -q test` run all
  - `./mvnw -q test -Dtest="MemoirExportHtmlAdvancedTest"` run single test

## Compatibility & Notes

- openhtmltopdf requires strict XHTML; export service ensures compliant HTML
- Prefer TTF/OTF fonts (TTC varies by font); registration failures won't abort export
- Enabling PDF encryption affects later editing/printing/copying—verify in test first
- Offline packaging of remote images not enabled (optional future)

## Follow-up Recommendations (Optional)

- Remote image download & offline cache/package
- Font subsetting to reduce PDF size
- Large document render performance & timeout safeguards
- Frontend E2E (Mock AI) covering full chain: outline/draft → PDF export

---

If you want to enable "offline image packaging" or a "PDF security smoke test", please indicate priority.
