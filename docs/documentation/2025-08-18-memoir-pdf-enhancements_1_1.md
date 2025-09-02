# Memoir Module - PDF Export Enhancements & Robustness Release Notes (2025-08-18)

This commit focuses on PDF export and user experience enhancements for the "AI Memoir" feature, covering cover page, table of contents pagination, page-breaking strategy, images/links, fonts, watermark, and security encryption. It also strengthens AI integration and English UI refinements, keeping the module isolated, plug-and-play, and configurable.

## Change Summary

- PDF Export (Backend)
  - Cover Page: Reads title/author/date from frontmatter; optional background image; no header/footer on the cover
  - Document Metadata: Writes PDF title and author
  - TOC (Table of Contents): Auto-generated, clickable entries; TOC entries include page numbers (leader + target-counter)
  - Bookmarks: H1/H2 produce PDF bookmarks by default; H3 bookmarks can be enabled
  - Pagination Strategy: New toggle to force page break before H2; configurable page margins (separate for body and cover)
  - Resource Resolution: Added baseUri to support relative image/link paths
  - Markdown Extension: Images and links auto-converted to strict XHTML
  - Font Embedding: Configurable registration of TTF/OTF fonts to improve mixed-language layout and offline fidelity
  - Watermark: Text watermark via inline SVG (configurable text/opacity)
  - Security & Encryption: Optional PDF encryption (user/owner password, print/copy permissions, key length)
- AI Robustness (Backend)
  - Gemini API Key support (a second channel in addition to service account)
  - Strict JSON parsing of outline structure with fallback to mock on error to guarantee availability
- Frontend
  - `memoir.html` English UI tailored for elderly users; STT defaults to en-GB; retains entry points for "AI Assistant (Outline/Draft)"
- Testing
  - Added `MemoirExportHtmlAdvancedTest` covering: TOC page numbers, H2 pagination, H3 bookmarks, cover background image, image/link conversion, watermark
  - Retained & passing: export endpoint tests, basic HTML build tests

## How to Use (Application Functionality)

- Page Entry: `/pages/memoir.html`
  - Create project → Add segments (chapter/topic/body) → Optionally invoke AI for outline/draft generation → Export as Markdown or PDF
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
- AI (Optionally enable Gemini):
  - `app.ai.mock=false`, `app.ai.provider=gemini`, `app.ai.gemini.model=gemini-1.5-flash`
  - API Key: `app.ai.gemini.api-key=` or `app.ai.gemini.api-key-env=GEMINI_API_KEY`

## Testing Notes

- Unit / Integration Tests
  - `MemoirExportHtmlTest`: TOC anchors, H1/H2 ids, bookmark CSS, header
  - `MemoirExportHtmlAdvancedTest`: TOC page numbers, H2 pagination, H3 bookmarks, cover image, image/link conversion, watermark
  - `MemoirExportTest`: Export endpoint availability for Markdown/PDF
- Windows PowerShell (Examples)
  - `./mvnw -q test` run all
  - `./mvnw -q test -Dtest="MemoirExportHtmlAdvancedTest"` run single test

## Compatibility & Notes

- openhtmltopdf requires strict XHTML; the export service guarantees compliant HTML generation
- Prefer TTF/OTF font files (TTC compatibility varies by font); registration failures won't abort export
- Enabling PDF encryption affects subsequent editing/printing/copying—validate in a test environment first
- Offline packaging of remote images is not enabled (optional future item)

## Follow-up Recommendations (Optional)

- Remote image download with offline caching/packaging
- Font subsetting to reduce PDF size
- Large document render performance & timeout safeguards
- Frontend E2E (Mock AI) covering the full chain: generate outline/draft → export PDF

---

If you would like me to enable "offline image packaging" or run a "PDF security smoke test", please specify the priority.
