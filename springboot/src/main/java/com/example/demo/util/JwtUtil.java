package com.example.demo.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT工具类
 * 用于生成和解析JWT token，实现用户身份验证
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成JWT token
     * 
     * @param userId 用户ID
     * @param email 用户邮箱
     * @return JWT token字符串
     */
    public String generateToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        return createToken(claims, email);
    }

    /**
     * 创建JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从token中提取用户ID
     * 
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中提取邮箱
     * 
     * @param token JWT token
     * @return 用户邮箱
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中提取过期时间
     * 
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中提取所有claims
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            // 使用JJWT 0.12.x的正确API
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // 如果JWT解析失败，返回null
            System.err.println("JWT parsing error: " + e.getMessage());
            
            // 添加更详细的错误信息
            if (e.getMessage().contains("Invalid compact JWT string")) {
                System.err.println("DEBUG: JWT format is invalid - token structure is malformed");
            } else if (e.getMessage().contains("JWT signature does not match")) {
                System.err.println("DEBUG: JWT signature verification failed");
            } else if (e.getMessage().contains("JWT expired")) {
                System.err.println("DEBUG: JWT token has expired");
            } else if (e.getMessage().contains("JWT not yet valid")) {
                System.err.println("DEBUG: JWT token is not yet valid");
            } else {
                System.err.println("DEBUG: Unknown JWT parsing error");
            }
            
            return null;
        }
    }

    /**
     * 检查token是否过期
     * 
     * @param token JWT token
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return true; // 如果无法获取过期时间，认为已过期
        }
        return expiration.before(new Date());
    }

    /**
     * 验证token是否有效
     * 
     * @param token JWT token
     * @param email 用户邮箱
     * @return 是否有效
     */
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = getEmailFromToken(token);
        return (email.equals(tokenEmail) && !isTokenExpired(token));
    }

    /**
     * 验证token格式和有效性
     * 
     * @param token JWT token
     * @return 是否有效
     */
    public Boolean isValidToken(String token) {
        try {
            // 检查token是否为空
            if (token == null || token.trim().isEmpty()) {
                System.err.println("DEBUG: JWT token is null or empty");
                return false;
            }
            
            // 检查token是否为"null"字符串
            if ("null".equalsIgnoreCase(token.trim())) {
                System.err.println("DEBUG: JWT token is 'null' string");
                return false;
            }
            
            // 检查token长度（JWT通常至少50个字符）
            if (token.length() < 50) {
                System.err.println("DEBUG: JWT token too short: " + token.length() + " characters");
                return false;
            }
            
            // 检查token格式（应该包含两个点）
            long dotCount = token.chars().filter(ch -> ch == '.').count();
            if (dotCount != 2) {
                System.err.println("DEBUG: JWT format invalid - expected 2 dots, found " + dotCount);
                return false;
            }
            
            // 尝试解析token
            Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                System.err.println("DEBUG: Failed to parse JWT claims");
                return false;
            }
            
            // 检查是否包含必要的claims
            if (claims.get("userId") == null) {
                System.err.println("DEBUG: JWT token missing userId claim");
                return false;
            }
            
            // 检查token是否过期
            if (isTokenExpired(token)) {
                System.err.println("DEBUG: JWT token is expired");
                return false;
            }
            
            System.err.println("DEBUG: JWT token validation successful");
            return true;
            
        } catch (Exception e) {
            System.err.println("DEBUG: Exception during JWT validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
