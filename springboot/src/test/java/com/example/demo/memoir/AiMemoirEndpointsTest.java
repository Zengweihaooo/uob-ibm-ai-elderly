package com.example.demo.memoir;

import com.example.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AI 端点集成测试（默认 mock=true）
 * 覆盖：/api/memoir/ai/outline 与 /api/memoir/ai/draft
 */
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class AiMemoirEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void outline_shouldReturnChaptersInMockMode() throws Exception {
        String body = "{\"locale\":\"zh-CN\",\"hint\":\"童年与故乡\"}";
        mockMvc.perform(post("/api/memoir/ai/outline")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.source", is("mock")))
            .andExpect(jsonPath("$.chapters", notNullValue()))
            .andExpect(jsonPath("$.chapters", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.chapters[0].chapter", not(blankOrNullString())))
            .andExpect(jsonPath("$.chapters[0].themes", notNullValue()));
    }

    @Test
    void draft_shouldReturnTextInMockMode() throws Exception {
        String body = "{\"chapter\":\"童年\",\"theme\":\"家庭\",\"notes\":\"与祖母在老屋\",\"locale\":\"zh-CN\"}";
        mockMvc.perform(post("/api/memoir/ai/draft")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.source", anyOf(is("mock"), is("gemini"))))
            .andExpect(jsonPath("$.text", not(blankOrNullString())));
    }
}
