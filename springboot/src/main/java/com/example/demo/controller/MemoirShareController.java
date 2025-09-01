package com.example.demo.controller;

import com.example.demo.pojo.memoir.MemoirShareToken;
import com.example.demo.service.memoir.MemoirExportService;
import com.example.demo.service.memoir.MemoirShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Memoir sharing controller: create share links and controlled downloads.
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

    /** Simple share display page (HTML) allowing PIN input and format selection */
    @GetMapping("/s/{token}")
    public ResponseEntity<String> sharePage(@PathVariable("token") String token) {
        MemoirShareToken st = shareService.findByToken(token);
        if (st == null) return ResponseEntity.status(404).body("<h3>Share not found</h3>");
    boolean needPin = shareService.requiresPin(st.getId());
        String html = "<!doctype html><html><head><meta charset='utf-8'/><title>AI Memoir Share</title>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'/>" +
                "<style>body{font-family:Arial,'Microsoft YaHei',sans-serif;max-width:720px;margin:40px auto;padding:0 16px;color:#222}" +
                ".card{border:1px solid #eee;border-radius:8px;padding:16px;box-shadow:0 2px 6px rgba(0,0,0,.05)}" +
                "label{display:block;margin:8px 0 4px;color:#444}" +
                "input,select,button{font-size:14px;padding:8px;border:1px solid #ccc;border-radius:6px}" +
                "button{background:#1a73e8;color:#fff;border:none;cursor:pointer;margin-top:12px}" +
                "button:disabled{background:#9bbbf1;cursor:not-allowed}" +
                ".muted{color:#777;font-size:12px;margin-top:8px}" +
                "</style></head><body>" +
                "<h2>AI Memoir — Shared Download</h2>" +
                "<div class='card'><form method='GET' action='/s/" + token + "/download'>" +
                (needPin ? "<label>PIN (if required)</label><input type='password' name='pin' placeholder='Enter PIN'/>" : "") +
                "<label>Format</label><select name='format'><option value='pdf' selected>PDF</option><option value='markdown'>Markdown</option></select>" +
                "<div><button type='submit'>Download</button></div>" +
                "<div class='muted'>Keep this link safe. Downloads may be limited or expired.</div>" +
                "</form></div>" +
                "</body></html>";
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=utf-8").body(html);
    }

    /** Create share link */
    @PostMapping("/api/memoir/projects/{id}/share")
    public ResponseEntity<?> createShare(@PathVariable("id") Integer projectId,
                                         @RequestBody Map<String, Object> payload,
                                         jakarta.servlet.http.HttpServletRequest request) {
        String pin = (String) payload.get("pin");
        Integer days = payload.get("days") instanceof Number ? ((Number) payload.get("days")).intValue() : null;
        Integer max = payload.get("maxDownloads") instanceof Number ? ((Number) payload.get("maxDownloads")).intValue() : null;
        String scope = (String) payload.getOrDefault("scope", "view");
        Map<String, Object> res = shareService.createShare(projectId, pin, days, max, scope);
    // Dynamically build shareUrl (supports reverse proxy headers)
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) scheme = request.getScheme();
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) host = request.getHeader("Host");
        if (host == null || host.isBlank()) host = request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : (":" + request.getServerPort()));
        String base = scheme + "://" + host;
        Object token = res.get("token");
        if (token != null) {
            res.put("shareUrl", base + "/s/" + token);
        }
        return ResponseEntity.ok(res);
    }

    /** Share page download (controlled) */
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
