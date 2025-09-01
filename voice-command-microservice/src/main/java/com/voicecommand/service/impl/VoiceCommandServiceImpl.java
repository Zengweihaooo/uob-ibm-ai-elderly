package com.voicecommand.service.impl;

import com.voicecommand.service.VoiceCommandService;
import com.voicecommand.service.AIIntentAnalysisService;
import com.voicecommand.service.FunctionRouterService;
import com.voicecommand.model.*;
import com.voicecommand.client.AIServiceClient;
import com.voicecommand.client.MainProjectAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    private static final Logger log = LoggerFactory.getLogger(VoiceCommandServiceImpl.class);
    
    @Autowired
    private AIServiceClient aiServiceClient;
    
    @Autowired
    private AIIntentAnalysisService aiIntentAnalysisService;
    
    @Autowired
    private FunctionRouterService functionRouterService;
    
    @Autowired
    private MainProjectAIClient mainProjectAIClient;
    
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
            
            // 2. 意图预判和双路径处理
            VoiceCommandResponse response = processWithIntentPrejudgment(transcribedText, languageCode, 
                                                                        userId, sessionId, executionId, startTime);
            
            log.info("语音命令处理完成: executionId={}, 成功={}, 耗时={}ms", 
                    executionId, response.isSuccess(), response.getProcessingTime());
            
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
            // 意图预判和双路径处理
            VoiceCommandResponse response = processWithIntentPrejudgment(textCommand, languageCode, 
                                                                        userId, sessionId, executionId, startTime);
            
            log.info("文本命令处理完成: executionId={}, 成功={}, 耗时={}ms", 
                    executionId, response.isSuccess(), response.getProcessingTime());
            
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

    private VoiceCommandResponse processWithIntentPrejudgment(String userText, String languageCode, 
                                                            String userId, String sessionId, 
                                                            String executionId, long startTime) {
        // 意图预判
        if (isFunctionCallIntent(userText)) {
            log.info("检测到功能调用意图，使用功能调用路径");
            return processAsFunctionCall(userText, languageCode, userId, sessionId, executionId, startTime);
        } else {
            log.info("检测到普通对话意图，使用AI对话路径");
            return processAsNormalChat(userText, languageCode, userId, sessionId, executionId, startTime);
        }
    }
    
    private boolean isFunctionCallIntent(String userText) {
        String lowerText = userText.toLowerCase();
        
        // 功能调用关键词
        List<String> functionKeywords = Arrays.asList(
            "发送邮件", "发邮件", "send email", "邮件", "email",
            "查看日程", "添加日程", "schedule", "日程", "calendar",
            "健康检查", "health check", "健康", "health",
            "联系人", "contact", "查找", "find",
            "重要日期", "important date", "生日", "birthday",
            "提醒", "reminder", "设置", "set"
        );
        
        // 如果包含功能关键词，认为是功能调用
        boolean isFunctionCall = functionKeywords.stream().anyMatch(lowerText::contains);
        log.info("意图预判结果: text={}, isFunctionCall={}", userText, isFunctionCall);
        
        return isFunctionCall;
    }
    
    private VoiceCommandResponse processAsFunctionCall(String userText, String languageCode, 
                                                     String userId, String sessionId, 
                                                     String executionId, long startTime) {
        try {
            // 1. AI意图分析
            Map<String, Object> context = buildContext(userId, sessionId);
            IntentAnalysisResult intentResult = aiIntentAnalysisService.analyzeIntent(userText, context);
            
            // 2. 检查置信度，如果太低则回退到普通对话
            if (intentResult.getConfidence() < 0.7) {
                log.info("功能调用置信度过低({})，回退到普通对话", intentResult.getConfidence());
                return processAsNormalChat(userText, languageCode, userId, sessionId, executionId, startTime);
            }
            
            // 3. 执行功能
            FunctionExecutionResult executionResult = functionRouterService.executeFunction(intentResult);
            
            // 4. 生成反馈文本
            String feedbackText = generateFeedbackText(intentResult, executionResult);
            
            // 5. 文字转语音（可选）
            String audioResponse = convertTextToSpeech(feedbackText, languageCode);
            
            // 6. 构建响应
            VoiceCommandResponse response = VoiceCommandResponse.builder()
                .executionId(executionId)
                .transcribedText(userText)
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
            
            return response;
            
        } catch (Exception e) {
            log.error("功能调用处理失败，回退到普通对话", e);
            return processAsNormalChat(userText, languageCode, userId, sessionId, executionId, startTime);
        }
    }
    
    private VoiceCommandResponse processAsNormalChat(String userText, String languageCode, 
                                                   String userId, String sessionId, 
                                                   String executionId, long startTime) {
        try {
            // 1. 调用主项目的AI对话服务
            Map<String, Object> request = new HashMap<>();
            request.put("message", userText);
            
            Map<String, Object> aiResponse = mainProjectAIClient.chatWithGemini(request);
            
            String feedbackText = (String) aiResponse.get("response");
            if (feedbackText == null) {
                feedbackText = "抱歉，我现在无法回答您的问题，请稍后再试。";
            }
            
            // 2. 文字转语音（可选）
            String audioResponse = convertTextToSpeech(feedbackText, languageCode);
            
            // 3. 构建响应
            VoiceCommandResponse response = VoiceCommandResponse.builder()
                .executionId(executionId)
                .transcribedText(userText)
                .feedbackText(feedbackText)
                .audioResponse(audioResponse)
                .success(true)
                .timestamp(System.currentTimeMillis())
                .processingTime(System.currentTimeMillis() - startTime)
                .statusCode(200)
                .build();
            
            return response;
            
        } catch (Exception e) {
            log.error("普通对话处理失败", e);
            return buildErrorResponse(executionId, "AI对话服务暂时不可用，请稍后再试", startTime);
        }
    }
}
