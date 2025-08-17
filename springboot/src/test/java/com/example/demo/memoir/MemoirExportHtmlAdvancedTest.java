package com.example.demo.memoir;

import com.example.demo.service.memoir.MemoirExportService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 回忆录导出 HTML 高级功能测试（不依赖数据库）：
 * - 目录样式包含页码（target-counter）
 * - H2 分页开关生效
 * - H3 纳入目录与书签（toc.level=3）
 * - 封面图样式
 * - Markdown 图片/链接转换
 * - 水印（内联 SVG 背景）
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
        // 反射注入配置：H2 分页、H3 入目录、封面图、水印
        setPrivate(svc, "pageBreakH2", true);
        setPrivate(svc, "tocLevel", 3);
        setPrivate(svc, "coverImage", "assets/images/web/cover.jpg");
        setPrivate(svc, "watermarkText", "DRAFT");

        Method meth = MemoirExportService.class.getDeclaredMethod("toSimpleHtml", String.class);
        meth.setAccessible(true);
        String html = (String) meth.invoke(svc, md);

        // 目录项页码（样式包含 target-counter）
        assertThat(html).contains("target-counter(attr(href), page)");

        // H2 强制分页样式
        assertThat(html).contains("page-break-before: always");

        // H3 作为目录与书签
        assertThat(html).contains("-fs-pdf-outline-level: 3");
        assertThat(html).contains("href=\"#subtopic\"");
        assertThat(html).contains("<h3 id=\"subtopic\">Subtopic</h3>");

        // 封面背景图样式
        assertThat(html).contains("<div class=\"cover\" style=\"background:url('assets/images/web/cover.jpg')");

        // 图片与链接转换
        assertThat(html).contains("<img src=\"assets/images/web/pic.png\" alt=\"alt\" title=\"An Image\" />");
        assertThat(html).contains("<a href=\"https://example.com\" title=\"Ex\">link</a>");

        // 水印（内联 SVG 背景）
        assertThat(html).contains("data:image/svg+xml;base64,");
    }

    private static void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
