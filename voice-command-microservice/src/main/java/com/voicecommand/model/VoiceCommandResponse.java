package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * 语音命令响应模型
 * 
 * 用于返回语音命令处理结果
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoiceCommandResponse {
    
    /**
     * 执行ID
     */
    private String executionId;
    
    /**
     * 转写的文本
     */
    private String transcribedText;
    
    /**
     * 意图分析结果
     */
    private IntentAnalysisResult intent;
    
    /**
     * 功能执行结果
     */
    private FunctionExecutionResult execution;
    
    /**
     * 反馈文本
     */
    private String feedbackText;
    
    /**
     * 语音响应（Base64编码）
     */
    private String audioResponse;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    /**
     * 处理耗时（毫秒）
     */
    private long processingTime;
    
    /**
     * 响应状态码
     */
    private int statusCode;
}
