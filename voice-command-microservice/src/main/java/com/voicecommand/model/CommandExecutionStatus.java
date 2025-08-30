package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * 命令执行状态模型
 * 
 * 用于跟踪语音命令的执行状态
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandExecutionStatus {
    
    /**
     * 执行ID
     */
    private String executionId;
    
    /**
     * 执行状态
     */
    private ExecutionStatus status;
    
    /**
     * 进度百分比 (0-100)
     */
    private int progress;
    
    /**
     * 当前步骤
     */
    private String currentStep;
    
    /**
     * 总步骤数
     */
    private int totalSteps;
    
    /**
     * 开始时间
     */
    private long startTime;
    
    /**
     * 预计完成时间
     */
    private long estimatedCompletionTime;
    
    /**
     * 实际完成时间
     */
    private long completionTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 重试次数
     */
    private int retryCount;
    
    /**
     * 最大重试次数
     */
    private int maxRetryCount;
    
    /**
     * 执行环境信息
     */
    private String environment;
    
    /**
     * 执行版本
     */
    private String version;
    
    /**
     * 执行状态枚举
     */
    public enum ExecutionStatus {
        PENDING,    // 等待执行
        RUNNING,    // 正在执行
        COMPLETED,  // 执行完成
        FAILED,     // 执行失败
        TIMEOUT,    // 执行超时
        CANCELLED,  // 执行取消
        RETRYING    // 重试中
    }
}
