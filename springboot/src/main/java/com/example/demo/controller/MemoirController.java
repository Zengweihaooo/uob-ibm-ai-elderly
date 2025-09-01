package com.example.demo.controller;

import com.example.demo.pojo.memoir.MemoirProject;
import com.example.demo.pojo.memoir.MemoirSegment;
import com.example.demo.service.memoir.MemoirService;
import com.example.demo.service.memoir.MemoirExportService;
import com.example.demo.service.memoir.AiMemoirService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Memoir controller.
 * Provides a minimal API: create project, list projects, add/list segments.
 */
@RestController
@RequestMapping("/api/memoir")
@CrossOrigin
public class MemoirController {
    private final MemoirService service;
    private final MemoirExportService exportService;
    private final AiMemoirService aiService;

    public MemoirController(MemoirService service, MemoirExportService exportService, AiMemoirService aiService) {
        this.service = service;
        this.exportService = exportService;
        this.aiService = aiService;
    }

    /** Create project */
    @PostMapping("/projects")
    public ResponseEntity<?> createProject(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.getOrDefault("title", "My Memoir");
        String owner = (String) payload.get("owner");
        String locale = (String) payload.getOrDefault("locale", "en-US");
        String pinHash = (String) payload.get("pin");
        MemoirProject p = service.createProject(title, owner, locale, pinHash);
        Map<String, Object> resp = new HashMap<>();
        resp.put("projectId", p.getId());
        resp.put("project", p);
        return ResponseEntity.ok(resp);
    }

    /** List projects (filter by owner; if owner is null returns all) */
    @GetMapping("/projects")
    public ResponseEntity<List<MemoirProject>> listProjects(@RequestParam(value = "owner", required = false) String owner) {
        return ResponseEntity.ok(service.listProjects(owner));
    }

    /** Add segment */
    @PostMapping("/projects/{id}/segments")
    public ResponseEntity<MemoirSegment> addSegment(@PathVariable("id") Integer projectId,
                                                    @RequestBody MemoirSegment seg) {
        seg.setProjectId(projectId);
        return ResponseEntity.ok(service.addSegment(seg));
    }

    /** List segments */
    @GetMapping("/projects/{id}/segments")
    public ResponseEntity<List<MemoirSegment>> listSegments(@PathVariable("id") Integer projectId) {
        return ResponseEntity.ok(service.listSegments(projectId));
    }

    /** Simple template (life stages + themes) used for frontend to initialize question catalog */
    @GetMapping("/templates")
    public ResponseEntity<?> templates() {
        Map<String, Object> t = new HashMap<>();
        t.put("locale", "en-US");
        t.put("chapters", List.of(
                Map.of("chapter", "Childhood", "themes", List.of("Hometown", "Family", "School")),
                Map.of("chapter", "Youth & Work", "themes", List.of("First job", "Mentors", "Choices")),
                Map.of("chapter", "Family & Relationships", "themes", List.of("Marriage", "Parenting", "Friendship")),
                Map.of("chapter", "Turning Points", "themes", List.of("Challenges", "Coping", "Proud moments")),
                Map.of("chapter", "Wisdom & Legacy", "themes", List.of("Advice", "What matters", "Hopes"))
        ));
        return ResponseEntity.ok(t);
    }

    /** Export: markdown or PDF */
    @GetMapping("/projects/{id}/export")
    public ResponseEntity<?> export(@PathVariable("id") Integer projectId,
                                    @RequestParam("format") String format) {
        String md = exportService.generateMarkdown(projectId);
        if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
            return ResponseEntity.ok()
                    .header("Content-Type", "text/markdown; charset=utf-8")
                    .body(md);
        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdf = exportService.generatePdfFromMarkdown(md);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=Memoir.pdf")
                    .body(pdf);
        }
        return ResponseEntity.badRequest().body(Map.of("error", "unsupported format"));
    }

    /**
     * AI: generate outline (mock enabled by default)
     */
    @PostMapping("/ai/outline")
    public ResponseEntity<?> aiOutline(@RequestBody Map<String, Object> payload) {
        String locale = (String) payload.getOrDefault("locale", "en-US");
        String hint = (String) payload.getOrDefault("hint", "");
        return ResponseEntity.ok(aiService.generateOutline(locale, hint));
    }

    /**
     * AI: generate segment draft (mock enabled by default)
     */
    @PostMapping("/ai/draft")
    public ResponseEntity<?> aiDraft(@RequestBody Map<String, Object> payload) {
        String chapter = (String) payload.get("chapter");
        String theme = (String) payload.get("theme");
        String notes = (String) payload.getOrDefault("notes", "");
        String locale = (String) payload.getOrDefault("locale", "en-US");
        return ResponseEntity.ok(aiService.draftSegment(chapter, theme, notes, locale));
    }
}
