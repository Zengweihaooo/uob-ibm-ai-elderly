package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * 语音命令请求模型
 * 
 * 用于接收前端发送的语音命令请求
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
     * 音频数据（Base64编码）
     */
    private String audioData;
    
    /**
     * 语言代码 (zh-CN, en-US)
     */
    private String languageCode;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 上下文信息
     */
    private Map<String, Object> context;
    
    /**
     * 文本命令（如果直接发送文本而不是语音）
     */
    private String textCommand;
    
    /**
     * 请求时间戳
     */
    private long timestamp;
    
    /**
     * 请求来源（web, mobile, etc.）
     */
    private String source;
}
