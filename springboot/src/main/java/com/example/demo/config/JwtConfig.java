package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration class.
 * Holds JWT related parameters.
 * 
 * Author: Weihao Zeng
 * Version: 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    private String secret = "defaultSecretKeyForDevelopmentOnlyChangeInProduction";
    private long expiration = 86400000; // 24 hours in milliseconds
    private String header = "Authorization";
    private String prefix = "Bearer ";
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public String getHeader() {
        return header;
    }
    
    public void setHeader(String header) {
        this.header = header;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
