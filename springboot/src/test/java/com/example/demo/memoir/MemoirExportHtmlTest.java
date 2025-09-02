package com.example.demo.memoir;

import com.example.demo.service.memoir.MemoirExportService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Memoir export HTML build tests (no DB dependency):
 * - TOC contains clickable anchors
 * - H1/H2 inject id attributes
 * - Styles include PDF outline CSS
 * - Header prefix contains "AI Memoir — Title · "
 */
public class MemoirExportHtmlTest {

    @Test
    void html_shouldContainClickableTocAnchorsAndOutlineCss() throws Exception {
        String md = "---\n" +
                "title: Sample\n" +
                "author: Tester\n" +
                "date: 2025-08-18\n" +
                "locale: en-GB\n" +
                "---\n\n" +
                "# Title\n\n" +
                "## Chapter One\n\n" +
                "Some text here.\n";

        // Do not access DB: call toSimpleHtml via reflection
        MemoirExportService svc = new MemoirExportService(null);
        Method meth = MemoirExportService.class.getDeclaredMethod("toSimpleHtml", String.class);
        meth.setAccessible(true);
        String html = (String) meth.invoke(svc, md);

        // TOC exists
        assertThat(html).contains("<div class=\"toc\">");
        // TOC items link to title anchors
        assertThat(html).contains("href=\"#title\"");
        assertThat(html).contains("href=\"#chapter-one\"");
        // H1/H2 inject id attributes
        assertThat(html).contains("<h1 id=\"title\">Title</h1>");
        assertThat(html).contains("<h2 id=\"chapter-one\">Chapter One</h2>");
        // Styles include PDF outline CSS
        assertThat(html).contains("-fs-pdf-outline-level: 1");
        assertThat(html).contains("-fs-pdf-outline-level: 2");
        // Header contains prefix (date not asserted precisely)
        assertThat(html).contains("<div class=\"header\">AI Memoir — Title · ");
    }
}
