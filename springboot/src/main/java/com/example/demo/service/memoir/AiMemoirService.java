package com.example.demo.service.memoir;

import com.example.demo.service.ai.GeminiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 回忆录AI服务（可Mock/可用Gemini）。
 * 说明：默认使用本地规则生成（mock），避免外部云依赖；当 app.ai.mock=false 时尝试调用 Gemini。
 */
@Service
public class AiMemoirService {
    @Value("${app.ai.mock:true}")
    private boolean mock;

    @Value("${app.ai.provider:gemini}")
    private String provider;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private final GeminiClient gemini;

    public AiMemoirService(GeminiClient gemini) {
        this.gemini = gemini;
    }

    /**
     * 生成项目大纲（章节+主题）。
     */
    public Map<String, Object> generateOutline(String locale, String hint) {
        if (!mock && "gemini".equalsIgnoreCase(provider)) {
            try {
                String sys = "You are a memoir assistant. Return a JSON with chapters and themes suitable for reminiscence therapy.";
                String prompt = String.format("Locale: %s\nHint: %s\nOutput schema: {chapters:[{chapter:string,themes:string[]}]}\nReturn only JSON.",
                        locale == null ? "en-US" : locale, hint == null ? "" : hint);
                String text = gemini.generateText(geminiModel, sys, prompt, 0.3, 0.9, 800);
                // 简单兜底：若未返回或非JSON，回退mock
                if (text != null && text.trim().startsWith("{")) {
                    // 为避免引入JSON库，这里直接以字符串返回给前端；前端/调用方可解析。
                    Map<String, Object> m = new HashMap<>();
                    m.put("raw", text);
                    m.put("source", "gemini");
                    return m;
                }
            } catch (Exception ignored) { /* 回退到mock */ }
        }
        return mockOutline(locale, hint);
    }

    /**
     * 根据提示与要点生成分段草稿。
     */
    public Map<String, Object> draftSegment(String chapter, String theme, String notes, String locale) {
        if (!mock && "gemini".equalsIgnoreCase(provider)) {
            try {
                String sys = "You are a memoir ghostwriter. Write a warm, clear, first-person paragraph suitable for elderly reminiscence.";
                String prompt = String.format("Chapter: %s\nTheme: %s\nNotes: %s\nLocale: %s\nWrite 1-3 paragraphs.",
                        nvl(chapter, "Unknown"), nvl(theme, "General"), nvl(notes, ""), nvl(locale, "en-US"));
                String text = gemini.generateText(geminiModel, sys, prompt, 0.7, 0.95, 700);
                if (text != null && !text.isBlank()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("chapter", chapter);
                    m.put("theme", theme);
                    m.put("text", text);
                    m.put("locale", locale == null ? "en-US" : locale);
                    m.put("source", "gemini");
                    return m;
                }
            } catch (Exception ignored) { /* 回退到mock */ }
        }
        return mockDraft(chapter, theme, notes, locale);
    }

    private String nvl(String s, String d) { return (s == null || s.isBlank()) ? d : s; }

    private Map<String, Object> mockOutline(String locale, String hint) {
        List<Map<String, Object>> chapters = List.of(
                Map.of("chapter", "Childhood", "themes", List.of("Family", "School", "Hometown")),
                Map.of("chapter", "Youth & Work", "themes", List.of("First job", "Mentors", "Choices")),
                Map.of("chapter", "Family & Relationships", "themes", List.of("Marriage", "Parenting", "Friendship")),
                Map.of("chapter", "Turning Points", "themes", List.of("Challenges", "Coping", "Proud moments")),
                Map.of("chapter", "Wisdom & Legacy", "themes", List.of("Advice", "What matters", "Hopes"))
        );
        Map<String, Object> r = new HashMap<>();
        r.put("locale", locale == null ? "en-US" : locale);
        r.put("hint", hint);
        r.put("chapters", chapters);
        r.put("source", "mock");
        return r;
    }

    private Map<String, Object> mockDraft(String chapter, String theme, String notes, String locale) {
        StringBuilder sb = new StringBuilder();
        sb.append("This is a draft for ")
          .append(chapter == null ? "Unknown Chapter" : chapter)
          .append(" - ")
          .append(theme == null ? "General" : theme)
          .append(".\n\n");
        if (notes != null && !notes.isBlank()) {
            sb.append("Key points mentioned: ").append(notes).append("\n\n");
        }
        sb.append("In those days, life was simple yet meaningful. The memories remind me of warmth, challenges, and growth. ")
          .append("We learned from our family and community, and we carried those values forward.\n");
        Map<String, Object> r = new HashMap<>();
        r.put("chapter", chapter);
        r.put("theme", theme);
        r.put("text", sb.toString());
        r.put("locale", locale == null ? "en-US" : locale);
        r.put("source", "mock");
        return r;
    }
}
