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
 * AI endpoints integration tests (default mock=true)
 * Covers: /api/memoir/ai/outline and /api/memoir/ai/draft
 */
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class AiMemoirEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void outline_shouldReturnChaptersInMockMode() throws Exception {
        String body = "{\"locale\":\"zh-CN\",\"hint\":\"Childhood and hometown\"}";
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
        String body = "{\"chapter\":\"Childhood\",\"theme\":\"Family\",\"notes\":\"With grandmother in the old house\",\"locale\":\"zh-CN\"}";
        mockMvc.perform(post("/api/memoir/ai/draft")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.source", anyOf(is("mock"), is("gemini"))))
            .andExpect(jsonPath("$.text", not(blankOrNullString())));
    }
}
