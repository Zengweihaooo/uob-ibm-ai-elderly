package com.voicecommand.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI服务客户端
 * 
 * 通过OpenFeign调用主项目的GeminiController
 * 使用Google Gemini AI进行意图分析
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@FeignClient(name = "ai-service", url = "${email.service.url}")
public interface AIServiceClient {
    
    /**
     * 调用Gemini AI进行聊天 - 调用主项目GeminiController
     */
    @PostMapping("/api/gemini/chat")
    Map<String, Object> chatWithGemini(@RequestBody Map<String, Object> request);
    
    /**
     * 获取Gemini AI状态 - 调用主项目GeminiController
     */
    @GetMapping("/api/gemini/status")
    Map<String, Object> getGeminiStatus();
    
    /**
     * 语音转文字 - 调用主项目VoiceController
     */
    @PostMapping("/api/voice/stt")
    Map<String, Object> speechToText(@RequestParam("audio") String audioData,
                                     @RequestParam(value = "languageCode", defaultValue = "zh-CN") String languageCode);
    
    /**
     * 文字转语音 - 调用主项目VoiceController
     */
    @PostMapping("/api/voice/tts")
    Map<String, Object> textToSpeech(@RequestBody Map<String, Object> request);
}
