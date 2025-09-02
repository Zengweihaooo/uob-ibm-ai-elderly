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
 * Integration test: Minimal memoir interface
 * Description: Create project, add segments, query
 */
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class MemoirControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createProject_thenList_andAddSegment() throws Exception {
        // 1) Create project
        String createPayload = "{\"title\":\"Test Memoir\",\"owner\":\"elderly1\",\"locale\":\"en-US\"}";
        String createResp = mockMvc.perform(post("/api/memoir/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectId").exists())
            .andReturn().getResponse().getContentAsString();

        // Simple extraction of projectId (without introducing a JSON library)
        String pid = createResp.replaceAll(".*\\\"projectId\\\":(\\d+).*", "$1");

        // 2) List projects
        mockMvc.perform(get("/api/memoir/projects").param("owner", "elderly1"))
            .andExpect(status().isOk());

        // 3) Add segment
        String segJson = "{\"chapter\":\"Childhood\",\"theme\":\"Hometown\",\"text\":\"I grew up...\",\"orderIndex\":0}";
        mockMvc.perform(post("/api/memoir/projects/" + pid + "/segments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(segJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectId").value(Integer.parseInt(pid)));

        // 4) List segments
        mockMvc.perform(get("/api/memoir/projects/" + pid + "/segments"))
            .andExpect(status().isOk());
    }
}
