package com.example.demo.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Twilio configuration class.
 * Similar to Google Cloud style config: loads Twilio credentials from a local JSON file.
 * 
 * Author: Yichen Zhang
 * Version: 1.0
 */
@Configuration
@ConditionalOnProperty(name = "app.sms.enabled", havingValue = "true", matchIfMissing = false)
public class TwilioConfig {

    @Value("${app.twilio.credentials-file:docs/keys/twilio-config.json}")
    private String twilioCredentialsFile;
    
    private final ObjectMapper objectMapper;
    
    public TwilioConfig() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Load Twilio configuration.
     * Reads sensitive information from a JSON file (or falls back to environment variables).
     * 
     * @return Map of Twilio credentials
     */
    @Bean(name = "twilioCredentials")
    public Map<String, String> loadTwilioCredentials() {
        Map<String, String> credentials = new HashMap<>();
        
        try {
            // 尝试多个可能的路径
            File configFile = findConfigFile();
            
            if (configFile != null && configFile.exists()) {
                System.out.println("📱 Loading Twilio credentials from: " + configFile.getAbsolutePath());
                
                JsonNode config = objectMapper.readTree(configFile);
                
                credentials.put("account_sid", config.get("account_sid").asText());
                credentials.put("auth_token", config.get("auth_token").asText());
                credentials.put("from_number", config.get("from_number").asText());
                
                System.out.println("✅ Twilio credentials loaded successfully");
                
            } else {
                System.out.println("⚠️  Twilio config file not found");
                useEnvironmentVariables(credentials);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Failed to load Twilio credentials: " + e.getMessage());
            useEnvironmentVariables(credentials);
        }
        
        return credentials;
    }

    private File findConfigFile() {
        System.out.println("🔍 Starting Twilio config file search...");
        System.out.println("   Current working directory: " + System.getProperty("user.dir"));
        System.out.println("   Original config path: " + twilioCredentialsFile);
        
        // 可能的配置文件路径 - 基于实际项目结构
        String[] possiblePaths = {
            twilioCredentialsFile,  // 原始路径
            "docs/keys/twilio-config.json",
            "uob-ibm-ai-elderly/docs/keys/twilio-config.json",  // 从当前目录到项目子目录
            "../uob-ibm-ai-elderly/docs/keys/twilio-config.json",  // 从springboot目录到项目根目录
            "../../uob-ibm-ai-elderly/docs/keys/twilio-config.json",  // 从springboot子目录到项目根目录
            "springboot/docs/keys/twilio-config.json",
            "../springboot/docs/keys/twilio-config.json",
            System.getProperty("user.dir") + "/docs/keys/twilio-config.json",
            System.getProperty("user.dir") + "/uob-ibm-ai-elderly/docs/keys/twilio-config.json",
            System.getProperty("user.dir") + "/springboot/docs/keys/twilio-config.json",
            System.getProperty("user.dir") + "/../uob-ibm-ai-elderly/docs/keys/twilio-config.json",
            System.getProperty("user.dir") + "/../../uob-ibm-ai-elderly/docs/keys/twilio-config.json"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            System.out.println("   Checking: " + file.getAbsolutePath());
            
            if (file.exists()) {
                System.out.println("   ✅ FOUND! File exists and is readable");
                System.out.println("   File size: " + file.length() + " bytes");
                System.out.println("   File last modified: " + new java.util.Date(file.lastModified()));
                return file;
            } else {
                System.out.println("   ❌ Not found");
            }
        }
        
        System.out.println("🔍 All paths checked - no config file found");
        return null;
    }

    private void useEnvironmentVariables(Map<String, String> credentials) {
        System.out.println("   🔄 Falling back to environment variables...");
        
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        String fromNumber = System.getenv("TWILIO_FROM_NUMBER");
        
        System.out.println("   Environment TWILIO_ACCOUNT_SID: " + (accountSid != null ? accountSid.substring(0, Math.min(10, accountSid.length())) + "..." : "NOT SET"));
        System.out.println("   Environment TWILIO_AUTH_TOKEN: " + (authToken != null ? authToken.substring(0, Math.min(10, authToken.length())) + "..." : "NOT SET"));
        System.out.println("   Environment TWILIO_FROM_NUMBER: " + (fromNumber != null ? fromNumber : "NOT SET"));
        
        credentials.put("account_sid", accountSid);
        credentials.put("auth_token", authToken);
        credentials.put("from_number", fromNumber);
        
        if (accountSid == null || authToken == null || fromNumber == null) {
            System.err.println("   ⚠️  WARNING: Some environment variables are missing!");
            System.err.println("   ⚠️  SMS functionality may not work properly");
        }
    }
    
    /**
     * Provide Twilio Account SID.
     */
    @Bean(name = "twilioAccountSid")
    public String getTwilioAccountSid() {
        Map<String, String> credentials = loadTwilioCredentials();
        return credentials.get("account_sid");
    }
    
    /**
     * Provide Twilio Auth Token.
     */
    @Bean(name = "twilioAuthToken")
    public String getTwilioAuthToken() {
        Map<String, String> credentials = loadTwilioCredentials();
        return credentials.get("auth_token");
    }
    
    /**
     * Provide Twilio From Number.
     */
    @Bean(name = "twilioFromNumber")
    public String getTwilioFromNumber() {
        Map<String, String> credentials = loadTwilioCredentials();
        return credentials.get("from_number");
    }
}
