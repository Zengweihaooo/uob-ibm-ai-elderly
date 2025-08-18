package com.example.demo.service.memoir;

import com.example.demo.mapper.MemoirMapper;
import com.example.demo.pojo.memoir.MemoirShareGuard;
import com.example.demo.pojo.memoir.MemoirShareToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * 分享服务：创建分享、校验 PIN、计数与过期检查
 */
@Service
public class MemoirShareService {
    private final MemoirMapper mapper;
    private final SecureRandom random = new SecureRandom();

    public MemoirShareService(MemoirMapper mapper) { this.mapper = mapper; }

    public Map<String, Object> createShare(Integer projectId, String pin, Integer days, Integer maxDownloads, String scope) {
        if (projectId == null) throw new IllegalArgumentException("projectId required");
        String token = genToken();
        MemoirShareToken st = new MemoirShareToken();
        st.setProjectId(projectId);
        st.setToken(token);
        st.setScope(StringUtils.hasText(scope) ? scope : "view");
        if (days != null && days > 0) st.setExpiresAt(LocalDateTime.now().plusDays(days));
        mapper.insertShareToken(st);

        MemoirShareGuard gd = new MemoirShareGuard();
        gd.setShareId(st.getId());
        gd.setDownloadCount(0);
        gd.setMaxDownloads(maxDownloads);
        if (StringUtils.hasText(pin)) gd.setPinHash(sha256(pin));
        mapper.insertShareGuard(gd);

        return Map.of(
                "token", token,
                "expiresAt", st.getExpiresAt(),
                "scope", st.getScope(),
                "maxDownloads", maxDownloads
        );
    }

    public boolean verifyPin(Integer shareId, String pin) {
        MemoirShareGuard gd = mapper.findShareGuard(shareId);
        if (gd == null || !StringUtils.hasText(gd.getPinHash())) return true; // 无 PIN 视为通过
        return sha256(pin).equals(gd.getPinHash());
    }

    public boolean canDownload(Integer shareId) {
        MemoirShareGuard gd = mapper.findShareGuard(shareId);
        if (gd == null) return true;
        if (gd.getMaxDownloads() == null) return true;
        return (gd.getDownloadCount() == null ? 0 : gd.getDownloadCount()) < gd.getMaxDownloads();
    }

    public void markDownloaded(Integer shareId) {
        mapper.incrementDownloadCount(shareId);
    }

    public MemoirShareToken findByToken(String token) {
        return mapper.findShareByToken(token);
    }

    /** 是否需要 PIN（存在并已设置 pinHash） */
    public boolean requiresPin(Integer shareId) {
        MemoirShareGuard gd = mapper.findShareGuard(shareId);
        return gd != null && org.springframework.util.StringUtils.hasText(gd.getPinHash());
    }

    private String genToken() {
        byte[] buf = new byte[18];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private String sha256(String text) {
        if (!StringUtils.hasText(text)) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
