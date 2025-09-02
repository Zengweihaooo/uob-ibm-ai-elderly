package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * Voice Command Request Model
 * 
 * Used to receive voice command requests sent from frontend
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoiceCommandRequest {
    
    /**
     * Audio data (Base64 encoded)
     */
    private String audioData;
    
    /**
     * Language code (en-US, zh-CN)
     */
    private String languageCode;
    
    /**
     * User ID
     */
    private String userId;
    
    /**
     * Session ID
     */
    private String sessionId;
    
    /**
     * Context information
     */
    private Map<String, Object> context;
    
    /**
     * Text command (if sending text directly instead of voice)
     */
    private String textCommand;
    
    /**
     * Request timestamp
     */
    private long timestamp;
    
    /**
     * Request source (web, mobile, etc.)
     */
    private String source;
}
