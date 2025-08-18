package com.example.demo.memoir;

import com.example.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 集成测试：最小回忆录接口
 * 中文注释：创建项目、添加分段、查询
 */
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class MemoirControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createProject_thenList_andAddSegment() throws Exception {
        // 1) 创建项目
        String createPayload = "{\"title\":\"Test Memoir\",\"owner\":\"elderly1\",\"locale\":\"en-US\"}";
        String createResp = mockMvc.perform(post("/api/memoir/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectId").exists())
            .andReturn().getResponse().getContentAsString();

        // 简单提取 projectId（不引入JSON库，做个粗略提取）
    String pid = createResp.replaceAll(".*\\\"projectId\\\":(\\d+).*", "$1");

        // 2) 列出项目
        mockMvc.perform(get("/api/memoir/projects").param("owner", "elderly1"))
            .andExpect(status().isOk());

        // 3) 添加分段
        String segJson = "{\"chapter\":\"Childhood\",\"theme\":\"Hometown\",\"text\":\"I grew up...\",\"orderIndex\":0}";
        mockMvc.perform(post("/api/memoir/projects/" + pid + "/segments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(segJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectId").value(Integer.parseInt(pid)));

        // 4) 列出分段
        mockMvc.perform(get("/api/memoir/projects/" + pid + "/segments"))
            .andExpect(status().isOk());
    }
}
