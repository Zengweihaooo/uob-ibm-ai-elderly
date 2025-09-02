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
 * Export feature integration tests (Markdown and PDF)
 */
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class MemoirExportTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exportMarkdownAndPdf() throws Exception {
        // Create project
        String createPayload = "{\"title\":\"Export Memoir\",\"owner\":\"elderly1\"}";
        String resp = mockMvc.perform(post("/api/memoir/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String pid = resp.replaceAll(".*\\\"projectId\\\":(\\d+).*", "$1");

        // Add segment
        mockMvc.perform(post("/api/memoir/projects/" + pid + "/segments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"chapter\":\"Childhood\",\"theme\":\"Family\",\"text\":\"We lived...\"}"))
            .andExpect(status().isOk());

        // Export MD
        mockMvc.perform(get("/api/memoir/projects/" + pid + "/export").param("format", "markdown"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/markdown")));

        // Export PDF
        mockMvc.perform(get("/api/memoir/projects/" + pid + "/export").param("format", "pdf"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/pdf")));
    }
}
