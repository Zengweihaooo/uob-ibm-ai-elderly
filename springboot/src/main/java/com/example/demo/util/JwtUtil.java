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
 * JWT Utility Class
 * Used for generating and parsing JWT tokens, implementing user authentication
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // Default 24 hours
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate JWT token
     * 
     * @param userId User ID
     * @param email User email
     * @return JWT token string
     */
    public String generateToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        return createToken(claims, email);
    }

    /**
     * Create JWT token
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
     * Extract user ID from token
     * 
     * @param token JWT token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("userId", Long.class);
    }

    /**
     * Extract email from token
     * 
     * @param token JWT token
     * @return User email
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     * 
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract specified claim from token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            // Use correct API for JJWT 0.12.x
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // If JWT parsing fails, return null
            System.err.println("JWT parsing error: " + e.getMessage());
            
            // Add more detailed error information
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
     * Check if token is expired
     * 
     * @param token JWT token
     * @return Whether expired
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return true; // If unable to get expiration time, consider it expired
        }
        return expiration.before(new Date());
    }

    /**
     * Validate if token is valid
     * 
     * @param token JWT token
     * @param email User email
     * @return Whether valid
     */
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = getEmailFromToken(token);
        return (email.equals(tokenEmail) && !isTokenExpired(token));
    }

    /**
     * Validate token format and validity
     * 
     * @param token JWT token
     * @return Whether valid
     */
    public Boolean isValidToken(String token) {
        try {
            // Check if token is null or empty
            if (token == null || token.trim().isEmpty()) {
                System.err.println("DEBUG: JWT token is null or empty");
                return false;
            }
            
            // Check if token is "null" string
            if ("null".equalsIgnoreCase(token.trim())) {
                System.err.println("DEBUG: JWT token is 'null' string");
                return false;
            }
            
            // Check token length (JWT typically at least 50 characters)
            if (token.length() < 50) {
                System.err.println("DEBUG: JWT token too short: " + token.length() + " characters");
                return false;
            }
            
            // Check token format (should contain two dots)
            long dotCount = token.chars().filter(ch -> ch == '.').count();
            if (dotCount != 2) {
                System.err.println("DEBUG: JWT format invalid - expected 2 dots, found " + dotCount);
                return false;
            }
            
            // Try to parse token
            Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                System.err.println("DEBUG: Failed to parse JWT claims");
                return false;
            }
            
            // Check if necessary claims are included
            if (claims.get("userId") == null) {
                System.err.println("DEBUG: JWT token missing userId claim");
                return false;
            }
            
            // Check if token is expired
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
