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
 * 分享端到端：创建分享 -> 公开页 -> 下载(PIN/上限/过期)
 */
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class MemoirShareEndpointsTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createShare_thenPublicPage_thenDownload_flow() throws Exception {
        // 1) 创建项目
        String createPayload = "{\"title\":\"Share Memoir\",\"owner\":\"elderly1\"}";
        String createResp = mockMvc.perform(post("/api/memoir/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String pid = createResp.replaceAll(".*\\\"projectId\\\":(\\d+).*", "$1");

        // 2) 添加分段
        mockMvc.perform(post("/api/memoir/projects/" + pid + "/segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapter\":\"Childhood\",\"theme\":\"Family\",\"text\":\"We lived...\"}"))
                .andExpect(status().isOk());

        // 3) 创建分享（设置 PIN=1234，下载上限=1，有效期=1 天）
        String shareReq = "{\"pin\":\"1234\",\"days\":1,\"maxDownloads\":1,\"scope\":\"view\"}";
        String shareResp = mockMvc.perform(post("/api/memoir/projects/" + pid + "/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shareReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();
        String token = shareResp.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");

        // 4) 打开公开页
        mockMvc.perform(get("/s/" + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/html")));

        // 5) 无 PIN 下载应 403
        mockMvc.perform(get("/s/" + token + "/download").param("format", "markdown"))
                .andExpect(status().isForbidden());

        // 6) 正确 PIN 下载一次 OK
        mockMvc.perform(get("/s/" + token + "/download").param("format", "markdown").param("pin", "1234"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/markdown")));

        // 7) 超出上限应 429
        mockMvc.perform(get("/s/" + token + "/download").param("format", "markdown").param("pin", "1234"))
                .andExpect(status().isTooManyRequests());
    }
}
