package com.example.demo.memoir;

import com.example.demo.service.memoir.MemoirExportService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 回忆录导出 HTML 构建测试（不依赖数据库）：
 * - 目录（TOC）包含可点击锚点
 * - H1/H2 注入 id
 * - 样式包含 PDF 书签（outline）CSS
 * - 页眉前缀包含“AI Memoir — 标题 · ”
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

        // 不访问数据库：仅用反射调用 toSimpleHtml
        MemoirExportService svc = new MemoirExportService(null);
        Method meth = MemoirExportService.class.getDeclaredMethod("toSimpleHtml", String.class);
        meth.setAccessible(true);
        String html = (String) meth.invoke(svc, md);

        // 目录存在
        assertThat(html).contains("<div class=\"toc\">");
        // 目录项链接到标题锚点
        assertThat(html).contains("href=\"#title\"");
        assertThat(html).contains("href=\"#chapter-one\"");
        // H1/H2 注入 id
        assertThat(html).contains("<h1 id=\"title\">Title</h1>");
        assertThat(html).contains("<h2 id=\"chapter-one\">Chapter One</h2>");
        // 样式包含 PDF 书签（outline）CSS
        assertThat(html).contains("-fs-pdf-outline-level: 1");
        assertThat(html).contains("-fs-pdf-outline-level: 2");
        // 页眉包含前缀（日期不做精确断言）
        assertThat(html).contains("<div class=\"header\">AI Memoir — Title · ");
    }
}
