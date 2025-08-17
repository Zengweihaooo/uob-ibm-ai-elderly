package com.example.demo.service.memoir;

import com.example.demo.mapper.MemoirMapper;
import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

/**
 * 回忆录导出服务
 * 中文注释：生成 Markdown 源稿；PDF 通过反射调用 openhtmltopdf，避免编译期依赖冲突
 */
@Service
public class MemoirExportService {
    private final MemoirMapper mapper;

    public MemoirExportService(MemoirMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 生成 Markdown 文本
     */
    public String generateMarkdown(Integer projectId) {
        MemoirProject p = mapper.findProjectById(projectId);
        if (p == null) throw new IllegalArgumentException("项目不存在: " + projectId);
        List<MemoirSegment> segs = mapper.listSegmentsByProject(projectId);

        StringBuilder sb = new StringBuilder();
        // YAML frontmatter
        sb.append("---\n");
        sb.append("title: ").append(escapeYaml(p.getTitle())).append("\n");
        sb.append("author: ").append(escapeYaml(p.getOwner() == null ? "" : p.getOwner())).append("\n");
        sb.append("date: ").append(LocalDate.now()).append("\n");
        sb.append("locale: ").append(p.getLocale() == null ? "en-US" : p.getLocale()).append("\n");
        sb.append("---\n\n");

        // 正文结构
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
     * Markdown -> 简单 HTML（最小可用）
     */
    private String toSimpleHtml(String markdown) {
        String html = markdown;
        html = html.replaceAll("^# (.*)$", "<h1>$1</h1>");
        html = html.replaceAll("^## (.*)$", "<h2>$1</h2>");
        html = html.replaceAll("^### (.*)$", "<h3>$1</h3>");
        html = html.replaceAll("---\\n[\\s\\S]*?---\\n", ""); // 去掉frontmatter
        html = html.replace("\n\n", "<br/><br/>");
    String head = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
        "<style>body{font-family:Arial, sans-serif;line-height:1.6;}h1,h2,h3{color:#333}</style>" +
        "</head><body>";
    return head + html + "</body></html>";
    }

    /**
     * 通过 openhtmltopdf 生成 PDF（反射调用）
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
            withHtmlContent.invoke(builder, htmlDoc, "/");
            // builder.toStream(baos);
            Method toStream = builderClz.getMethod("toStream", java.io.OutputStream.class);
            toStream.invoke(builder, baos);
            // builder.run();
            Method run = builderClz.getMethod("run");
            run.invoke(builder);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成PDF失败", e);
        }
    }

    private static String escapeYaml(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace(":", "- ");
    }
}
