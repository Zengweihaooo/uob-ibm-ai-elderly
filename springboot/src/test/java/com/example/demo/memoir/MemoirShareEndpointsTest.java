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
 * Share end-to-end: create share -> public page -> download (PIN/limit/expiry)
 */
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class MemoirShareEndpointsTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createShare_thenPublicPage_thenDownload_flow() throws Exception {
        // 1) Create project
        String createPayload = "{\"title\":\"Share Memoir\",\"owner\":\"elderly1\"}";
        String createResp = mockMvc.perform(post("/api/memoir/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String pid = createResp.replaceAll(".*\\\"projectId\\\":(\\d+).*", "$1");

        // 2) Add segment
        mockMvc.perform(post("/api/memoir/projects/" + pid + "/segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapter\":\"Childhood\",\"theme\":\"Family\",\"text\":\"We lived...\"}"))
                .andExpect(status().isOk());

        // 3) Create share (PIN=1234, max downloads=1, valid for 1 day)
        String shareReq = "{\"pin\":\"1234\",\"days\":1,\"maxDownloads\":1,\"scope\":\"view\"}";
        String shareResp = mockMvc.perform(post("/api/memoir/projects/" + pid + "/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shareReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();
        String token = shareResp.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");

        // 4) Open public page
        mockMvc.perform(get("/s/" + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/html")));

        // 5) Download without PIN should be 403
        mockMvc.perform(get("/s/" + token + "/download").param("format", "markdown"))
                .andExpect(status().isForbidden());

        // 6) Correct PIN download once OK
        mockMvc.perform(get("/s/" + token + "/download").param("format", "markdown").param("pin", "1234"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/markdown")));

        // 7) Exceeding limit should return 429
        mockMvc.perform(get("/s/" + token + "/download").param("format", "markdown").param("pin", "1234"))
                .andExpect(status().isTooManyRequests());
    }
}
