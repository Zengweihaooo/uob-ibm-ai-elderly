package com.example.demo.memoir;

import com.example.demo.service.memoir.MemoirExportService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Memoir export HTML advanced features tests (no DB dependency):
 * - TOC style contains page numbers (target-counter)
 * - H2 page break switch works
 * - H3 included in TOC and bookmarks (toc.level=3)
 * - Cover image style
 * - Markdown image/link conversion
 * - Watermark (inline SVG background)
 */
public class MemoirExportHtmlAdvancedTest {

    @Test
    void html_shouldReflectAdvancedPdfFeatures() throws Exception {
        String md = "---\n" +
                "title: Sample\n" +
                "author: Tester\n" +
                "date: 2025-08-18\n" +
                "locale: en-GB\n" +
                "---\n\n" +
                "# Title\n\n" +
                "## Chapter One\n\n" +
                "### Subtopic\n\n" +
                "Some [link](https://example.com \"Ex\") and an image ![alt](assets/images/web/pic.png \"An Image\").\n";

        MemoirExportService svc = new MemoirExportService(null);
        // Inject configuration via reflection: H2 page break, H3 in TOC, cover image, watermark
        setPrivate(svc, "pageBreakH2", true);
        setPrivate(svc, "tocLevel", 3);
        setPrivate(svc, "coverImage", "assets/images/web/cover.jpg");
        setPrivate(svc, "watermarkText", "DRAFT");

        Method meth = MemoirExportService.class.getDeclaredMethod("toSimpleHtml", String.class);
        meth.setAccessible(true);
        String html = (String) meth.invoke(svc, md);

        // TOC item page numbers (style contains target-counter)
        assertThat(html).contains("target-counter(attr(href), page)");

        // H2 forced page break style
        assertThat(html).contains("page-break-before: always");

        // H3 as TOC and bookmarks
        assertThat(html).contains("-fs-pdf-outline-level: 3");
        assertThat(html).contains("href=\"#subtopic\"");
        assertThat(html).contains("<h3 id=\"subtopic\">Subtopic</h3>");

        // Cover background image style
        assertThat(html).contains("<div class=\"cover\" style=\"background:url('assets/images/web/cover.jpg')");

        // Image and link conversion
        assertThat(html).contains("<img src=\"assets/images/web/pic.png\" alt=\"alt\" title=\"An Image\" />");
        assertThat(html).contains("<a href=\"https://example.com\" title=\"Ex\">link</a>");

        // Watermark (inline SVG background)
        assertThat(html).contains("data:image/svg+xml;base64,");
    }

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
