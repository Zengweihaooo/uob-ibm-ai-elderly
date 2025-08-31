package com.example.demo.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Twilio配置类
 * 类似于Google Cloud配置，从本地JSON文件加载Twilio密钥
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
@Configuration
public class TwilioConfig {

    @Value("${app.twilio.credentials-file:docs/keys/twilio-config.json}")
    private String twilioCredentialsFile;
    
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    
    public TwilioConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 加载Twilio配置
     * 从JSON文件中读取敏感信息
     * 
     * @return Twilio配置Map
     */
    @Bean(name = "twilioCredentials")
    public Map<String, String> loadTwilioCredentials() {
        Map<String, String> credentials = new HashMap<>();
        
        try {
            // 尝试加载配置文件
            File configFile = new File(twilioCredentialsFile);
            
            if (configFile.exists()) {
                System.out.println("📱 Loading Twilio credentials from: " + configFile.getAbsolutePath());
                
                JsonNode config = objectMapper.readTree(configFile);
                
                credentials.put("account_sid", config.get("account_sid").asText());
                credentials.put("auth_token", config.get("auth_token").asText());
                credentials.put("from_number", config.get("from_number").asText());
                
                System.out.println("✅ Twilio credentials loaded successfully");
                System.out.println("   Account SID: " + config.get("account_sid").asText().substring(0, 10) + "...");
                System.out.println("   From Number: " + config.get("from_number").asText());
                
            } else {
                System.out.println("⚠️  Twilio config file not found: " + configFile.getAbsolutePath());
                System.out.println("   Using environment variables or default values");
                
                // 回退到环境变量
                credentials.put("account_sid", System.getenv("TWILIO_ACCOUNT_SID"));
                credentials.put("auth_token", System.getenv("TWILIO_AUTH_TOKEN"));
                credentials.put("from_number", System.getenv("TWILIO_FROM_NUMBER"));
            }
            
        } catch (IOException e) {
            System.err.println("❌ Failed to load Twilio credentials: " + e.getMessage());
            System.out.println("   Falling back to environment variables...");
            
            // 错误时回退到环境变量
            credentials.put("account_sid", System.getenv("TWILIO_ACCOUNT_SID"));
            credentials.put("auth_token", System.getenv("TWILIO_AUTH_TOKEN"));
            credentials.put("from_number", System.getenv("TWILIO_FROM_NUMBER"));
        }
        
        return credentials;
    }
    
    /**
     * 获取Twilio Account SID
     */
    @Bean(name = "twilioAccountSid")
    public String getTwilioAccountSid() {
        Map<String, String> credentials = loadTwilioCredentials();
        return credentials.get("account_sid");
    }
    
    /**
     * 获取Twilio Auth Token
     */
    @Bean(name = "twilioAuthToken")
    public String getTwilioAuthToken() {
        Map<String, String> credentials = loadTwilioCredentials();
        return credentials.get("auth_token");
    }
    
    /**
     * 获取Twilio From Number
     */
    @Bean(name = "twilioFromNumber")
    public String getTwilioFromNumber() {
        Map<String, String> credentials = loadTwilioCredentials();
        return credentials.get("from_number");
    }
}
