package com.example.demo.service.memoir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.mapper.MemoirMapper;
import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;

/**
 * Memoir export service
 * Description: Generates Markdown source; PDF uses reflection to call openhtmltopdf, avoiding compile-time dependency conflicts
 */
@Service
public class MemoirExportService {
    private final MemoirMapper mapper;
    // Optional: Register PDF fonts through configuration, semicolon-separated multiple paths (e.g.: C:\\fonts\\NotoSansSC-Regular.otf;D:\\fonts\\DejaVuSerif.ttf)
    @Value("${app.export.pdf.font.paths:}")
    private String pdfFontPaths;
    // Optional: Whether H2 forces page break
    @Value("${app.export.pdf.pagebreak.h2:false}")
    private boolean pageBreakH2;
    // Optional: HTML baseUri (resource parsing base). If not set, will default to current working directory.
    @Value("${app.export.pdf.base-uri:}")
    private String baseUriConfig;
    // Optional: TOC level (2=H1/H2, 3=H1/H2/H3)
    @Value("${app.export.pdf.toc.level:2}")
    private int tocLevel;
    // Optional: Page margins (e.g.: "20mm 16mm") and cover page margins
    @Value("${app.export.pdf.page.margin:20mm 16mm}")
    private String pageMargin;
    @Value("${app.export.pdf.page.margin.cover:25mm 20mm}")
    private String coverMargin;
    // Optional: Watermark text and opacity (0-1)
    @Value("${app.export.pdf.watermark.text:}")
    private String watermarkText;
    @Value("${app.export.pdf.watermark.opacity:0.08}")
    private double watermarkOpacity;
    // Optional: Cover background image (URL or relative path)
    @Value("${app.export.pdf.cover.image:}")
    private String coverImage;
    // Optional: PDF encryption and permissions
    @Value("${app.export.pdf.security.enabled:false}")
    private boolean pdfSecEnabled;
    @Value("${app.export.pdf.security.user-password:}")
    private String pdfSecUserPassword;
    @Value("${app.export.pdf.security.owner-password:}")
    private String pdfSecOwnerPassword;
    @Value("${app.export.pdf.security.allow-print:true}")
    private boolean pdfSecAllowPrint;
    @Value("${app.export.pdf.security.allow-copy:false}")
    private boolean pdfSecAllowCopy;
    @Value("${app.export.pdf.security.key-length:256}")
    private int pdfSecKeyLength;

    public MemoirExportService(MemoirMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Generate Markdown text
     */
    public String generateMarkdown(Integer projectId) {
        MemoirProject p = mapper.findProjectById(projectId);
        if (p == null) throw new IllegalArgumentException("Project not found: " + projectId);
        List<MemoirSegment> segs = mapper.listSegmentsByProject(projectId);

        StringBuilder sb = new StringBuilder();
        // YAML frontmatter
        sb.append("---\n");
        sb.append("title: ").append(escapeYaml(p.getTitle())).append("\n");
        sb.append("author: ").append(escapeYaml(p.getOwner() == null ? "" : p.getOwner())).append("\n");
        sb.append("date: ").append(LocalDate.now()).append("\n");
        sb.append("locale: ").append(p.getLocale() == null ? "en-US" : p.getLocale()).append("\n");
        sb.append("---\n\n");

        // Body structure
        sb.append("# ").append(p.getTitle()).append("\n\n");

        String currentChapter = null;
        for (MemoirSegment s : segs) {
            if (s.getChapter() != null && !s.getChapter().equals(currentChapter)) {
                currentChapter = s.getChapter();
                sb.append("## ").append(currentChapter).append("\n\n");
            }
            if (s.getTheme() != null && !s.getTheme().isEmpty()) {
                sb.append("### ").append(s.getTheme()).append("\n\n");
            }
            if (s.getText() != null) {
                sb.append(s.getText()).append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * Markdown -> minimal HTML (MVP)
     */
    private String toSimpleHtml(String markdown) {
        // Parse frontmatter to set <title>/author
        String[] lines = markdown.split("\r?\n");
        String fmTitle = null, fmAuthor = null;
        if (lines.length > 0 && lines[0].trim().equals("---")) {
            for (int i = 1; i < lines.length; i++) {
                String ln = lines[i];
                if (ln.trim().equals("---")) break;
                int idx = ln.indexOf(":");
                if (idx > 0) {
                    String k = ln.substring(0, idx).trim().toLowerCase();
                    String v = ln.substring(idx + 1).trim();
                    if ("title".equals(k)) fmTitle = v;
                    if ("author".equals(k)) fmAuthor = v;
                }
            }
        }
    // First roughly extract TOC based on markdown (only recognize # and ## titles), while preparing slug for anchors
        StringBuilder toc = new StringBuilder();
        boolean hasToc = false;
        java.util.List<String[]> headingList = new java.util.ArrayList<>(); // [level, title, slug]
        java.util.Set<String> used = new java.util.HashSet<>();
        toc.append("<div class=\"toc\"><h2>Table of Contents</h2><ol>");
        for (String line : lines) {
            if (line.startsWith("# ")) {
                hasToc = true;
                String title = line.substring(2).trim();
                String slug = makeSlug(title, used);
                headingList.add(new String[]{"1", title, slug});
                toc.append("<li><a href=\"#").append(slug).append("\">" )
                   .append(escapeHtml(title)).append("</a></li>");
            } else if (line.startsWith("## ")) {
                hasToc = true;
                String title = line.substring(3).trim();
                String slug = makeSlug(title, used);
                headingList.add(new String[]{"2", title, slug});
                toc.append("<li style=\"margin-left:14pt\"><a href=\"#").append(slug).append("\">")
                   .append(escapeHtml(title)).append("</a></li>");
            }
        }
        // H3 in TOC (optional)
        if (tocLevel >= 3) {
            for (String line : lines) {
                if (line.startsWith("### ")) {
                    hasToc = true;
                    String title = line.substring(4).trim();
                    String slug = makeSlug(title, used);
                    headingList.add(new String[]{"3", title, slug});
                    toc.append("<li style=\"margin-left:28pt\"><a href=\"#").append(slug).append("\">")
                       .append(escapeHtml(title)).append("</a></li>");
                }
            }
        }
        toc.append("</ol></div>");

        String html = markdown;
        // Multi-line replacements: support ^ and $ anchors
        html = html.replaceAll("(?m)^# (.*)$", "<h1>$1</h1>");
        html = html.replaceAll("(?m)^## (.*)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^### (.*)$", "<h3>$1</h3>");
        html = html.replaceAll("---\n[\\s\\S]*?---\n", ""); // remove frontmatter
        // Convert Markdown images and links (images first, then links)
        html = convertMarkdownImages(html);
        html = convertMarkdownLinks(html);
        html = html.replace("\n\n", "<br/><br/>");
        String head = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
            "<title>" + escapeHtml((fmTitle == null || fmTitle.isBlank()) ? (headingList.isEmpty() ? "Memoir" : headingList.get(0)[1]) : fmTitle) + "</title>" +
            (fmAuthor == null ? "" : ("<meta name=\"author\" content=\"" + escapeHtml(fmAuthor) + "\" />")) +
            // Basic styles: page margins, heading levels, paragraph line-height, page breaks
            "<style>" +
            "@page { size: A4; margin: " + escapeCss(pageMargin) + "; }" +
            "@page cover { size: A4; margin: " + escapeCss(coverMargin) + "; }" +
            // Header/Footer (openhtmltopdf supports running elements + margin boxes)
            "@page {" +
            "  @top-center { content: element(doc-header) }" +
            "  @bottom-center { content: element(doc-footer) }" +
            "}" +
            "@page cover {" +
            "  @top-center { content: none }" +
            "  @bottom-center { content: none }" +
            "}" +
            "body{ font-family: Arial, 'Microsoft YaHei', 'Noto Sans SC', sans-serif; line-height:1.6; color:#222; font-size:12pt; " + buildWatermarkCss() + " }" +
            "h1{ font-size:22pt; margin:0 0 12pt; page-break-before: always; -fs-pdf-outline: true; -fs-pdf-outline-level: 1; }" +
            "h1:first-of-type{ page-break-before: auto; }" +
            "h2{ font-size:16pt; margin:18pt 0 8pt; -fs-pdf-outline: true; -fs-pdf-outline-level: 2; " + (pageBreakH2 ? "page-break-before: always;" : "") + " }" +
            "h3{ font-size:13pt; margin:12pt 0 6pt; " + (tocLevel >= 3 ? "-fs-pdf-outline: true; -fs-pdf-outline-level: 3;" : "") + " }" +
            "p{ margin:0 0 10pt; }" +
            "hr{ border:0; border-top:1px solid #ccc; margin:12pt 0; }" +
            ".page-break{ page-break-after: always; }" +
            "img{ max-width:100%; height:auto; page-break-inside: avoid; }" +
            "a{ color:#1a73e8; text-decoration: underline; }" +
            ".cover{ page: cover; text-align:center; margin-top:30mm; }" +
            ".cover .title{ font-size:28pt; margin:0 0 12pt; }" +
            ".cover .author-date{ font-size:14pt; color:#555; }" +
            ".toc{ border:1px solid #eee; background:#fafafa; padding:12pt; margin:0 0 14pt; }" +
            ".toc h2{ margin:0 0 8pt; font-size:14pt; }" +
            ".toc ol{ margin:0; padding-left:14pt; }" +
            ".toc li{ list-style:decimal; margin:4pt 0; }" +
            ".toc a{ text-decoration:none; color:#333; }" +
            ".toc a::after{ content: leader('.') target-counter(attr(href), page); }" +
            ".header{ position: running(doc-header); color:#777; font-size:10pt; border-bottom:1px solid #ddd; padding-bottom:4pt; margin-bottom:6pt; }" +
            ".footer{ position: running(doc-footer); color:#777; font-size:10pt; border-top:1px solid #ddd; padding-top:4pt; margin-top:6pt; }" +
            ".footer .page-num:after{ content: 'Page ' counter(page) ' of ' counter(pages); }" +
            "</style>" +
            "</head><body>";
        // Inject id to headings for TOC anchors
        for (String[] h : headingList) {
            String level = h[0];
            String title = h[1];
            String slug = h[2];
            String find = "<h" + level + ">" + java.util.regex.Pattern.quote(title) + "</h" + level + ">";
            String repl = "<h" + level + " id=\\\"" + slug + "\\\">" + title + "</h" + level + ">";
            html = html.replaceFirst(find, repl);
        }

    // Compose header: project name (approx from first title) + generate date (UK format)
    String headerTitle = headingList.isEmpty() ? "Memoir" : escapeHtml(headingList.get(0)[1]);
    String dateUk = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.UK));
    String header = "<div class=\"header\">AI Memoir — " + headerTitle + " · " + dateUk + "</div>";
        // Cover page (if frontmatter title exists or inferred)
        String coverTitle = (fmTitle == null || fmTitle.isBlank()) ? headerTitle : escapeHtml(fmTitle);
        String coverMeta = (fmAuthor == null || fmAuthor.isBlank()) ? dateUk : (escapeHtml(fmAuthor) + " — " + dateUk);
    String coverStyle = (coverImage != null && !coverImage.isBlank())
        ? (" style=\"background:url('" + escapeHtml(coverImage) + "') no-repeat center/cover; color:#fff; text-shadow:0 1px 2px rgba(0,0,0,.6);\"")
        : "";
    String cover = "<div class=\"cover\"" + coverStyle + "><div class=\"title\">" + coverTitle + "</div>" +
        "<div class=\"author-date\">" + coverMeta + "</div></div><div class=\"page-break\"></div>";
        String footer = "<div class=\"footer\"><span class=\"page-num\"></span></div>";
        String tocHtml = hasToc ? (toc.toString() + "<div class=\"page-break\"></div>") : "";
        return head + header + cover + tocHtml + html + footer + "</body></html>";
    }

    // Simple HTML escaping to ensure safety (minimal)
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

     // Generate a unique slug from the title
     private String makeSlug(String title, java.util.Set<String> used) {
         String base = title == null ? "" : title.toLowerCase()
                 .replaceAll("[^a-z0-9]+", "-")
                 .replaceAll("(^-|-$)", "");
         if (base.isEmpty()) base = "section";
         String slug = base;
         int i = 2;
         while (used.contains(slug)) {
             slug = base + "-" + i++;
         }
         used.add(slug);
         return slug;
     }

    /**
     * Generate PDF via openhtmltopdf (reflection)
     */
    public byte[] generatePdfFromMarkdown(String markdown) {
        String htmlDoc = toSimpleHtml(markdown);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Class<?> builderClz = Class.forName("com.openhtmltopdf.pdfboxout.PdfRendererBuilder");
            Object builder = builderClz.getDeclaredConstructor().newInstance();
            // builder.useFastMode();
            Method useFastMode = builderClz.getMethod("useFastMode");
            useFastMode.invoke(builder);
            // builder.withHtmlContent(htmlDoc, "/");
            Method withHtmlContent = builderClz.getMethod("withHtmlContent", String.class, String.class);
            String base = resolveBaseUri();
            withHtmlContent.invoke(builder, htmlDoc, base);
            // Optional: register font files to improve mixed CJK/Latin rendering and offline consistency
            if (pdfFontPaths != null && !pdfFontPaths.isBlank()) {
                String[] paths = pdfFontPaths.split(";");
                for (String p : paths) {
                    String path = (p == null) ? null : p.trim();
                    if (path == null || path.isEmpty()) continue;
                    try {
                        File f = new File(path);
                        if (f.exists() && f.isFile()) {
                            String family = deriveFamilyFromFilename(f.getName());
                            try {
                                Method useFont = builderClz.getMethod("useFont", File.class, String.class);
                                useFont.invoke(builder, f, family);
                            } catch (NoSuchMethodException ignore) {
                                // Ignore: different versions may have different signatures
                            }
                        }
                    } catch (Exception ignore) {
                        // Font registration failure should not block export
                    }
                }
            }
            // builder.toStream(baos);
            Method toStream = builderClz.getMethod("toStream", java.io.OutputStream.class);
            toStream.invoke(builder, baos);
            // builder.run();
            Method run = builderClz.getMethod("run");
            run.invoke(builder);
            byte[] pdf = baos.toByteArray();
            // Optional: apply PDF metadata and encryption (via PDFBox reflection), does not block export
            if (pdfSecEnabled || true) { // Even if not encrypted, try to set metadata
                try {
                    String title = parseFrontmatterValue(markdown, "title");
                    String author = parseFrontmatterValue(markdown, "author");
                    pdf = applyPdfMetadataAndSecurity(pdf, title, author);
                } catch (Throwable ignore) {
                    // Metadata or encryption failure should not block
                }
            }
            return pdf;
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private String deriveFamilyFromFilename(String name) {
        if (name == null) return "CustomFont";
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        return base.replaceAll("(?i)-(regular|bold|italic|medium|light|semibold)", "");
    }

    // Build watermark CSS (based on inline SVG background)
    private String buildWatermarkCss() {
        if (watermarkText == null || watermarkText.isBlank()) return "";
        try {
            String safe = watermarkText.replace("\n", " ");
            double op = Math.max(0.0, Math.min(1.0, watermarkOpacity));
            String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='400' height='300' viewBox='0 0 400 300'>" +
                    "<g fill='#000000' opacity='" + op + "' font-family='Arial' font-size='28' transform='rotate(-30 200 150)'>" +
                    "<text x='50' y='160'>" + escapeXml(safe) + "</text>" +
                    "</g></svg>";
            String data = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
            return " background-image:url('data:image/svg+xml;base64," + data + "'); background-repeat:repeat; background-position:center; background-size:400px 300px;";
        } catch (Exception ignore) {
            return "";
        }
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    // Simple filtering for newlines and invalid characters to avoid breaking CSS
    private String escapeCss(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ").trim();
    }

    // Parse simple key-value pairs from frontmatter
    private String parseFrontmatterValue(String markdown, String key) {
        if (markdown == null) return null;
        String[] lines = markdown.split("\r?\n");
        if (lines.length == 0 || !"---".equals(lines[0].trim())) return null;
        for (int i = 1; i < lines.length; i++) {
            String ln = lines[i];
            if (ln.trim().equals("---")) break;
            int idx = ln.indexOf(":");
            if (idx > 0) {
                String k = ln.substring(0, idx).trim().toLowerCase();
                if (key.equalsIgnoreCase(k)) {
                    return ln.substring(idx + 1).trim();
                }
            }
        }
        return null;
    }

    // Apply PDF metadata and optional encryption using PDFBox (reflection)
    private byte[] applyPdfMetadataAndSecurity(byte[] pdf, String title, String author) throws Exception {
        Class<?> pdDocumentClz = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
        Class<?> apClz = Class.forName("org.apache.pdfbox.pdmodel.encryption.AccessPermission");
        Class<?> sppClz = Class.forName("org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy");
        Class<?> infoClz = Class.forName("org.apache.pdfbox.pdmodel.PDDocumentInformation");

        // PDDocument.load(byte[])
        java.lang.reflect.Method load = pdDocumentClz.getMethod("load", byte[].class);
        Object doc = load.invoke(null, (Object) pdf);
        try {
            // Set document info
            java.lang.reflect.Method getInfo = pdDocumentClz.getMethod("getDocumentInformation");
            Object info = getInfo.invoke(doc);
            if (info == null) {
                info = infoClz.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method setInfo = pdDocumentClz.getMethod("setDocumentInformation", infoClz);
                setInfo.invoke(doc, info);
            }
            if (title != null && !title.isBlank()) {
                infoClz.getMethod("setTitle", String.class).invoke(info, title);
            }
            if (author != null && !author.isBlank()) {
                infoClz.getMethod("setAuthor", String.class).invoke(info, author);
            }

            // Optional encryption
            if (pdfSecEnabled) {
                Object ap = apClz.getDeclaredConstructor().newInstance();
                // Set allowed permissions
                try { apClz.getMethod("setCanPrint", boolean.class).invoke(ap, pdfSecAllowPrint); } catch (Throwable ignored) {}
                try { apClz.getMethod("setCanExtractContent", boolean.class).invoke(ap, pdfSecAllowCopy); } catch (Throwable ignored) {}

                String owner = (pdfSecOwnerPassword != null && !pdfSecOwnerPassword.isBlank()) ? pdfSecOwnerPassword : pdfSecUserPassword;
                String user = (pdfSecUserPassword != null && !pdfSecUserPassword.isBlank()) ? pdfSecUserPassword : pdfSecOwnerPassword;
                if (owner == null || owner.isBlank()) owner = "owner"; // fallback
                if (user == null) user = "";

                Object spp = sppClz.getConstructor(String.class, String.class, apClz).newInstance(owner, user, ap);
                // key length
                try { sppClz.getMethod("setEncryptionKeyLength", int.class).invoke(spp, pdfSecKeyLength); } catch (Throwable ignored) {}
                // Apply protection
                pdDocumentClz.getMethod("protect", Class.forName("org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy")).invoke(doc, spp);
            }

            // Save to byte array
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            pdDocumentClz.getMethod("save", java.io.OutputStream.class).invoke(doc, out);
            return out.toByteArray();
        } finally {
            try { pdDocumentClz.getMethod("close").invoke(doc); } catch (Throwable ignored) {}
        }
    }

    // Convert Markdown image syntax to XHTML <img/>
    private String convertMarkdownImages(String text) {
        // ![alt](url "title") or ![alt](url)
        Pattern p = Pattern.compile("!\\[(.*?)\\]\\((\\S+?)(?:\\s+\\\"(.*?)\\\")?\\)");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String alt = escapeHtml(m.group(1));
            String url = m.group(2);
            String title = m.groupCount() >= 3 ? m.group(3) : null;
            String tag = "<img src=\"" + escapeHtml(url) + "\" alt=\"" + alt + "\"" +
                    (title != null ? (" title=\"" + escapeHtml(title) + "\"") : "") + " />";
            m.appendReplacement(sb, Matcher.quoteReplacement(tag));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // Convert Markdown link syntax to <a>
    private String convertMarkdownLinks(String text) {
        // [text](url "title") or [text](url)
        Pattern p = Pattern.compile("(?<!!)\\[(.+?)\\]\\((\\S+?)(?:\\s+\\\"(.*?)\\\")?\\)");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String label = escapeHtml(m.group(1));
            String url = m.group(2);
            String title = m.groupCount() >= 3 ? m.group(3) : null;
            String tag = "<a href=\"" + escapeHtml(url) + "\"" +
                    (title != null ? (" title=\"" + escapeHtml(title) + "\"") : "") + ">" + label + "</a>";
            m.appendReplacement(sb, Matcher.quoteReplacement(tag));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String resolveBaseUri() {
        if (baseUriConfig != null && !baseUriConfig.isBlank()) {
            return baseUriConfig;
        }
        try {
            File cwd = new File(".").getCanonicalFile();
            String uri = cwd.toURI().toString();
            if (!uri.endsWith("/")) uri = uri + "/";
            return uri; // file:///.../
        } catch (Exception e) {
            return "/"; // fallback
        }
    }

    private static String escapeYaml(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace(":", "- ");
    }
}
