package com.example.demo.service.ai;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 轻量级 Gemini GenAI REST 客户端（无外部 SDK 依赖）。
 * 中文说明：从环境变量读取凭据，调用 v1beta generateContent 接口，返回首个候选文本。
 */
@Service
public class GeminiClient {
    // 缺省模型，可被上层覆盖
    private static final String DEFAULT_MODEL = "gemini-1.5-flash";
    private static final String API_URL_FMT = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    /**
     * 生成文本（可选 systemInstruction，用于设定写作风格/输出格式）。
     */
    public String generateText(String model,
                               String systemInstruction,
                               String userText,
                               Double temperature,
                               Double topP,
                               Integer maxOutputTokens) throws IOException {
        GoogleCredentials credentials = getGoogleCredentials();
        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();

        String url = String.format(API_URL_FMT, (model == null || model.isBlank()) ? DEFAULT_MODEL : model);

        // 请求体
        Map<String, Object> requestBody = new HashMap<>();

        if (systemInstruction != null && !systemInstruction.isBlank()) {
            Map<String, Object> systemContent = new HashMap<>();
            Map<String, Object> systemPart = new HashMap<>();
            systemPart.put("text", systemInstruction);
            systemContent.put("parts", List.of(systemPart));
            requestBody.put("systemInstruction", systemContent);
        }

        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", userText == null ? "" : userText);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));

        Map<String, Object> generationConfig = new HashMap<>();
        if (temperature != null) generationConfig.put("temperature", temperature);
        if (topP != null) generationConfig.put("topP", topP);
        if (maxOutputTokens != null) generationConfig.put("maxOutputTokens", maxOutputTokens);
        if (!generationConfig.isEmpty()) requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = restTemplate.postForObject(url, entity, Map.class);
            if (responseBody != null && responseBody.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content2 = (Map<String, Object>) candidate.get("content");
                    if (content2 != null && content2.containsKey("parts")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content2.get("parts");
                        if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
                            Object text = parts.get(0).get("text");
                            return text == null ? "" : String.valueOf(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to call Gemini GenAI REST API: " + e.getMessage(), e);
        }

        return ""; // 无结果返回空串
    }

    /**
     * 从环境变量加载凭据：
     * - GOOGLE_CLOUD_CREDENTIALS（内联JSON）优先
     * - GOOGLE_APPLICATION_CREDENTIALS（文件路径）其次
     */
    private GoogleCredentials getGoogleCredentials() throws IOException {
        GoogleCredentials credentials;
        String credentialsJson = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
            credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
        } else {
            String credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credsPath != null && Files.exists(Paths.get(credsPath))) {
                credentials = GoogleCredentials.fromStream(Files.newInputStream(Paths.get(credsPath)));
            } else {
                throw new IOException("No Google Cloud credentials found. Set GOOGLE_CLOUD_CREDENTIALS or GOOGLE_APPLICATION_CREDENTIALS.");
            }
        }
        return credentials.createScoped(Arrays.asList(
                "https://www.googleapis.com/auth/cloud-platform",
                "https://www.googleapis.com/auth/generative-language"
        ));
    }
}
