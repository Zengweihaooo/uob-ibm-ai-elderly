package com.example.demo.controller;

import com.example.demo.pojo.memoir.MemoirShareToken;
import com.example.demo.service.memoir.MemoirExportService;
import com.example.demo.service.memoir.MemoirShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 回忆录分享控制器：创建分享、受控下载
 */
@RestController
@RequestMapping
@CrossOrigin
public class MemoirShareController {
    private final MemoirShareService shareService;
    private final MemoirExportService exportService;

    public MemoirShareController(MemoirShareService shareService, MemoirExportService exportService) {
        this.shareService = shareService;
        this.exportService = exportService;
    }

    /** 创建分享链接 */
    @PostMapping("/api/memoir/projects/{id}/share")
    public ResponseEntity<?> createShare(@PathVariable("id") Integer projectId,
                                         @RequestBody Map<String, Object> payload) {
        String pin = (String) payload.get("pin");
        Integer days = payload.get("days") instanceof Number ? ((Number) payload.get("days")).intValue() : null;
        Integer max = payload.get("maxDownloads") instanceof Number ? ((Number) payload.get("maxDownloads")).intValue() : null;
        String scope = (String) payload.getOrDefault("scope", "view");
        Map<String, Object> res = shareService.createShare(projectId, pin, days, max, scope);
        return ResponseEntity.ok(res);
    }

    /** 分享页下载（受控） */
    @GetMapping("/s/{token}/download")
    public ResponseEntity<?> download(@PathVariable("token") String token,
                                      @RequestParam(value = "pin", required = false) String pin,
                                      @RequestParam(value = "format", defaultValue = "pdf") String format) {
        MemoirShareToken st = shareService.findByToken(token);
        if (st == null) return ResponseEntity.status(404).body(Map.of("error", "not found"));
        if (st.getExpiresAt() != null && st.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(410).body(Map.of("error", "expired"));
        }
        if (!shareService.verifyPin(st.getId(), pin)) {
            return ResponseEntity.status(403).body(Map.of("error", "invalid pin"));
        }
        if (!shareService.canDownload(st.getId())) {
            return ResponseEntity.status(429).body(Map.of("error", "download limit reached"));
        }
        String md = exportService.generateMarkdown(st.getProjectId());
        if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
            shareService.markDownloaded(st.getId());
            return ResponseEntity.ok().header("Content-Type", "text/markdown; charset=utf-8").body(md);
        }
        byte[] pdf = exportService.generatePdfFromMarkdown(md);
        shareService.markDownloaded(st.getId());
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=Memoir.pdf")
                .body(pdf);
    }
}
