package com.voicecommand.service.impl;

import com.voicecommand.service.VoiceCommandService;
import com.voicecommand.service.AIIntentAnalysisService;
import com.voicecommand.service.FunctionRouterService;
import com.voicecommand.model.*;
import com.voicecommand.client.AIServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 语音命令服务实现类
 * 
 * 整合语音识别、AI意图分析、功能执行等所有功能
 * 提供完整的语音命令处理流程
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Service
@Slf4j
public class VoiceCommandServiceImpl implements VoiceCommandService {
    
    @Autowired
    private AIServiceClient aiServiceClient;
    
    @Autowired
    private AIIntentAnalysisService aiIntentAnalysisService;
    
    @Autowired
    private FunctionRouterService functionRouterService;
    
    // 存储执行状态
    private final Map<String, CommandExecutionStatus> executionStatusMap = new HashMap<>();
    
    @Override
    public VoiceCommandResponse processVoiceCommand(MultipartFile audioFile, String languageCode, 
                                                   String userId, String sessionId) {
        long startTime = System.currentTimeMillis();
        String executionId = generateExecutionId();
        
        log.info("开始处理语音命令: executionId={}, userId={}, languageCode={}", 
                executionId, userId, languageCode);
        
        try {
            // 1. 语音转文字
            String transcribedText = convertSpeechToText(audioFile, languageCode);
            if (transcribedText == null || transcribedText.trim().isEmpty()) {
                return buildErrorResponse(executionId, "语音识别失败，请重新尝试", startTime);
            }
            
            // 2. AI意图分析
            Map<String, Object> context = buildContext(userId, sessionId);
            IntentAnalysisResult intentResult = aiIntentAnalysisService.analyzeIntent(transcribedText, context);
            
            // 3. 执行功能
            FunctionExecutionResult executionResult = functionRouterService.executeFunction(intentResult);
            
            // 4. 生成反馈文本
            String feedbackText = generateFeedbackText(intentResult, executionResult);
            
            // 5. 文字转语音（可选）
            String audioResponse = convertTextToSpeech(feedbackText, languageCode);
            
            // 6. 构建响应
            VoiceCommandResponse response = VoiceCommandResponse.builder()
                .executionId(executionId)
                .transcribedText(transcribedText)
                .intent(intentResult)
                .execution(executionResult)
                .feedbackText(feedbackText)
                .audioResponse(audioResponse)
                .success(executionResult.isSuccess())
                .errorMessage(executionResult.getErrorMessage())
                .timestamp(System.currentTimeMillis())
                .processingTime(System.currentTimeMillis() - startTime)
                .statusCode(executionResult.isSuccess() ? 200 : 500)
                .build();
            
            // 7. 更新执行状态
            updateExecutionStatus(executionId, executionResult);
            
            log.info("语音命令处理完成: executionId={}, 成功={}, 耗时={}ms", 
                    executionId, executionResult.isSuccess(), response.getProcessingTime());
            
            return response;
            
        } catch (Exception e) {
            log.error("处理语音命令失败: executionId={}", executionId, e);
            return buildErrorResponse(executionId, "处理语音命令失败：" + e.getMessage(), startTime);
        }
    }
    
    @Override
    public VoiceCommandResponse processTextCommand(String textCommand, String languageCode, 
                                                  String userId, String sessionId) {
        long startTime = System.currentTimeMillis();
        String executionId = generateExecutionId();
        
        log.info("开始处理文本命令: executionId={}, userId={}, text={}", 
                executionId, userId, textCommand);
        
        try {
            // 1. AI意图分析
            Map<String, Object> context = buildContext(userId, sessionId);
            IntentAnalysisResult intentResult = aiIntentAnalysisService.analyzeIntent(textCommand, context);
            
            // 2. 执行功能
            FunctionExecutionResult executionResult = functionRouterService.executeFunction(intentResult);
            
            // 3. 生成反馈文本
            String feedbackText = generateFeedbackText(intentResult, executionResult);
            
            // 4. 文字转语音（可选）
            String audioResponse = convertTextToSpeech(feedbackText, languageCode);
            
            // 5. 构建响应
            VoiceCommandResponse response = VoiceCommandResponse.builder()
                .executionId(executionId)
                .transcribedText(textCommand)
                .intent(intentResult)
                .execution(executionResult)
                .feedbackText(feedbackText)
                .audioResponse(audioResponse)
                .success(executionResult.isSuccess())
                .errorMessage(executionResult.getErrorMessage())
                .timestamp(System.currentTimeMillis())
                .processingTime(System.currentTimeMillis() - startTime)
                .statusCode(executionResult.isSuccess() ? 200 : 500)
                .build();
            
            // 6. 更新执行状态
            updateExecutionStatus(executionId, executionResult);
            
            log.info("文本命令处理完成: executionId={}, 成功={}, 耗时={}ms", 
                    executionId, executionResult.isSuccess(), response.getProcessingTime());
            
            return response;
            
        } catch (Exception e) {
            log.error("处理文本命令失败: executionId={}", executionId, e);
            return buildErrorResponse(executionId, "处理文本命令失败：" + e.getMessage(), startTime);
        }
    }
    
    @Override
    public CommandExecutionStatus getExecutionStatus(String executionId) {
        return executionStatusMap.getOrDefault(executionId, 
            CommandExecutionStatus.builder()
                .executionId(executionId)
                .status(CommandExecutionStatus.ExecutionStatus.FAILED)
                .errorMessage("执行ID不存在")
                .build());
    }
    
    @Override
    public boolean cancelExecution(String executionId) {
        CommandExecutionStatus status = executionStatusMap.get(executionId);
        if (status != null && status.getStatus() == CommandExecutionStatus.ExecutionStatus.RUNNING) {
            status.setStatus(CommandExecutionStatus.ExecutionStatus.CANCELLED);
            status.setCompletionTime(System.currentTimeMillis());
            executionStatusMap.put(executionId, status);
            log.info("取消执行: executionId={}", executionId);
            return true;
        }
        return false;
    }
    
    /**
     * 语音转文字
     */
    private String convertSpeechToText(MultipartFile audioFile, String languageCode) {
        try {
            // 调用主项目的语音识别服务
            Map<String, Object> request = new HashMap<>();
            request.put("audio", audioFile);
            request.put("languageCode", languageCode);
            
            Map<String, Object> response = aiServiceClient.speechToText(
                audioFile.getBytes().toString(), languageCode);
            
            if (response != null && (Boolean) response.get("success")) {
                return (String) response.get("text");
            } else {
                log.error("语音识别失败: {}", response);
                return null;
            }
            
        } catch (Exception e) {
            log.error("语音转文字失败", e);
            return null;
        }
    }
    
    /**
     * 文字转语音
     */
    private String convertTextToSpeech(String text, String languageCode) {
        try {
            // 调用主项目的文字转语音服务
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            request.put("languageCode", languageCode);
            
            Map<String, Object> response = aiServiceClient.textToSpeech(request);
            
            if (response != null && (Boolean) response.get("success")) {
                return (String) response.get("audio");
            } else {
                log.warn("文字转语音失败: {}", response);
                return null;
            }
            
        } catch (Exception e) {
            log.warn("文字转语音失败", e);
            return null;
        }
    }
    
    /**
     * 构建上下文信息
     */
    private Map<String, Object> buildContext(String userId, String sessionId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("sessionId", sessionId);
        context.put("timestamp", System.currentTimeMillis());
        context.put("source", "voice-command-microservice");
        return context;
    }
    
    /**
     * 生成反馈文本
     */
    private String generateFeedbackText(IntentAnalysisResult intent, FunctionExecutionResult execution) {
        if (execution.isSuccess()) {
            return execution.getFeedbackText();
        } else {
            return String.format("抱歉，%s功能执行失败：%s", 
                intent.getFunctionName(), execution.getErrorMessage());
        }
    }
    
    /**
     * 生成执行ID
     */
    private String generateExecutionId() {
        return "exec_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 构建错误响应
     */
    private VoiceCommandResponse buildErrorResponse(String executionId, String errorMessage, long startTime) {
        return VoiceCommandResponse.builder()
            .executionId(executionId)
            .success(false)
            .errorMessage(errorMessage)
            .timestamp(System.currentTimeMillis())
            .processingTime(System.currentTimeMillis() - startTime)
            .statusCode(500)
            .build();
    }
    
    /**
     * 更新执行状态
     */
    private void updateExecutionStatus(String executionId, FunctionExecutionResult executionResult) {
        CommandExecutionStatus status = CommandExecutionStatus.builder()
            .executionId(executionId)
            .status(convertStatus(executionResult.getStatus()))
            .progress(executionResult.isSuccess() ? 100 : 0)
            .currentStep(executionResult.getFunctionName())
            .totalSteps(1)
            .startTime(executionResult.getStartTime())
            .completionTime(executionResult.getEndTime())
            .errorMessage(executionResult.getErrorMessage())
            .build();
        
        executionStatusMap.put(executionId, status);
    }
    
    /**
     * 转换执行状态
     */
    private CommandExecutionStatus.ExecutionStatus convertStatus(FunctionExecutionResult.ExecutionStatus status) {
        switch (status) {
            case COMPLETED:
                return CommandExecutionStatus.ExecutionStatus.COMPLETED;
            case FAILED:
                return CommandExecutionStatus.ExecutionStatus.FAILED;
            case TIMEOUT:
                return CommandExecutionStatus.ExecutionStatus.TIMEOUT;
            case CANCELLED:
                return CommandExecutionStatus.ExecutionStatus.CANCELLED;
            default:
                return CommandExecutionStatus.ExecutionStatus.RUNNING;
        }
    }
}
