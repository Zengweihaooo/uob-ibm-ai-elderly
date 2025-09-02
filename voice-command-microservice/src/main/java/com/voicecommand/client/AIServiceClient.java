package com.voicecommand.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI Service Client
 * 
 * Calls the main project's GeminiController through OpenFeign
 * Uses Google Gemini AI for intent analysis
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@FeignClient(name = "ai-service", url = "${email.service.url}")
public interface AIServiceClient {
    
    /**
     * Call Gemini AI for chat - calls the main project's GeminiController
     */
    @PostMapping("/api/gemini/chat")
    Map<String, Object> chatWithGemini(@RequestBody Map<String, Object> request);
    
    /**
     * Get Gemini AI status - calls the main project's GeminiController
     */
    @GetMapping("/api/gemini/status")
    Map<String, Object> getGeminiStatus();
    
    /**
     * Speech to Text - calls the main project's VoiceController
     */
    @PostMapping("/api/voice/stt")
    Map<String, Object> speechToText(@RequestParam("audio") String audioData,
                                     @RequestParam(value = "languageCode", defaultValue = "zh-CN") String languageCode);
    
    /**
     * Text to Speech - calls the main project's VoiceController
     */
    @PostMapping("/api/voice/tts")
    Map<String, Object> textToSpeech(@RequestBody Map<String, Object> request);
}
