package com.voicecommand.controller;

import com.voicecommand.service.VoiceCommandService;
import com.voicecommand.service.FunctionKnowledgeService;
import com.voicecommand.model.VoiceCommandResponse;
import com.voicecommand.model.VoiceCommandRequest;
import com.voicecommand.model.CommandExecutionStatus;
import com.voicecommand.model.FunctionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语音命令控制器
 * 
 * 提供语音命令处理的REST API接口
 * 支持语音输入和文本输入两种方式
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/voice-command")
@CrossOrigin(origins = "*")
@Slf4j
public class VoiceCommandController {
    
    @Autowired
    private VoiceCommandService voiceCommandService;
    
    @Autowired
    private FunctionKnowledgeService functionKnowledgeService;
    
    /**
     * 处理语音命令
     * 
     * @param audioFile 音频文件
     * @param languageCode 语言代码
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 处理结果
     */
    @PostMapping("/process")
    public ResponseEntity<VoiceCommandResponse> processVoiceCommand(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "languageCode", defaultValue = "zh-CN") String languageCode,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        
        log.info("收到语音命令请求: languageCode={}, userId={}, sessionId={}", 
                languageCode, userId, sessionId);
        
        try {
            // 设置默认值
            if (userId == null) userId = "guest";
            if (sessionId == null) sessionId = "session_" + System.currentTimeMillis();
            
            VoiceCommandResponse response = voiceCommandService.processVoiceCommand(
                audioFile, languageCode, userId, sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理语音命令失败", e);
            
            VoiceCommandResponse errorResponse = VoiceCommandResponse.builder()
                .success(false)
                .errorMessage("处理语音命令失败：" + e.getMessage())
                .timestamp(System.currentTimeMillis())
                .statusCode(500)
                .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 处理文本命令
     * 
     * @param request 请求体
     * @return 处理结果
     */
    @PostMapping("/text")
    public ResponseEntity<VoiceCommandResponse> processTextCommand(
            @RequestBody VoiceCommandRequest request) {
        
        log.info("收到文本命令请求: text={}, userId={}, sessionId={}", 
                request.getTextCommand(), request.getUserId(), request.getSessionId());
        
        // 检查textCommand是否为空，如果为空则尝试从text字段获取
        String textCommand = request.getTextCommand();
        if (textCommand == null || textCommand.trim().isEmpty()) {
            // 尝试从context中获取text字段
            if (request.getContext() != null && request.getContext().containsKey("text")) {
                textCommand = (String) request.getContext().get("text");
                log.info("从context中获取到text字段: {}", textCommand);
            }
        }
        
        if (textCommand == null || textCommand.trim().isEmpty()) {
            log.error("文本命令为空，无法处理");
            VoiceCommandResponse errorResponse = VoiceCommandResponse.builder()
                .success(false)
                .errorMessage("文本命令不能为空")
                .timestamp(System.currentTimeMillis())
                .statusCode(400)
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // 设置默认值
            if (request.getLanguageCode() == null) request.setLanguageCode("zh-CN");
            if (request.getUserId() == null) request.setUserId("guest");
            if (request.getSessionId() == null) request.setSessionId("session_" + System.currentTimeMillis());
            
            VoiceCommandResponse response = voiceCommandService.processTextCommand(
                textCommand, request.getLanguageCode(), 
                request.getUserId(), request.getSessionId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理文本命令失败", e);
            
            VoiceCommandResponse errorResponse = VoiceCommandResponse.builder()
                .success(false)
                .errorMessage("处理文本命令失败：" + e.getMessage())
                .timestamp(System.currentTimeMillis())
                .statusCode(500)
                .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 获取命令执行状态
     * 
     * @param executionId 执行ID
     * @return 执行状态
     */
    @GetMapping("/status/{executionId}")
    public ResponseEntity<CommandExecutionStatus> getExecutionStatus(
            @PathVariable String executionId) {
        
        log.info("查询执行状态: executionId={}", executionId);
        
        try {
            CommandExecutionStatus status = voiceCommandService.getExecutionStatus(executionId);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("查询执行状态失败: executionId={}", executionId, e);
            
            CommandExecutionStatus errorStatus = CommandExecutionStatus.builder()
                .executionId(executionId)
                .status(CommandExecutionStatus.ExecutionStatus.FAILED)
                .errorMessage("查询执行状态失败：" + e.getMessage())
                .build();
            
            return ResponseEntity.status(500).body(errorStatus);
        }
    }
    
    /**
     * 取消命令执行
     * 
     * @param executionId 执行ID
     * @return 取消结果
     */
    @PostMapping("/cancel/{executionId}")
    public ResponseEntity<Map<String, Object>> cancelExecution(
            @PathVariable String executionId) {
        
        log.info("取消执行: executionId={}", executionId);
        
        try {
            boolean cancelled = voiceCommandService.cancelExecution(executionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", cancelled);
            response.put("message", cancelled ? "执行已取消" : "无法取消执行");
            response.put("executionId", executionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("取消执行失败: executionId={}", executionId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "取消执行失败：" + e.getMessage());
            errorResponse.put("executionId", executionId);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 获取功能知识库
     * 
     * @return 功能知识库信息
     */
    @GetMapping("/knowledge-base")
    public ResponseEntity<Map<String, Object>> getKnowledgeBase() {
        log.info("获取功能知识库");
        
        try {
            if (functionKnowledgeService.isKnowledgeBaseLoaded()) {
                List<FunctionInfo> functions = functionKnowledgeService.getAllFunctions();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("totalFunctions", functions.size());
                response.put("functions", functions);
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("errorMessage", "功能知识库未加载");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.status(500).body(errorResponse);
            }
            
        } catch (Exception e) {
            log.error("获取功能知识库失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("errorMessage", "获取功能知识库失败：" + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 健康检查
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Voice Command Microservice");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * 获取服务信息
     * 
     * @return 服务信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "Voice Command Microservice");
        info.put("description", "AI语音命令处理微服务");
        info.put("version", "1.0.0");
        info.put("features", Arrays.asList(
            "语音转文字",
            "AI意图分析", 
            "功能自动执行",
            "邮件发送",
            "状态跟踪"
        ));
        info.put("supportedLanguages", Arrays.asList("zh-CN", "en-US"));
        info.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(info);
    }
}
