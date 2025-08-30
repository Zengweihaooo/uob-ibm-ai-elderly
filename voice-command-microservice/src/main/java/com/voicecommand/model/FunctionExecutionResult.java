package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * 功能执行结果模型
 * 
 * 记录功能执行的详细结果
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FunctionExecutionResult {
    
    /**
     * 执行的功能名称
     */
    private String functionName;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 执行结果数据
     */
    private Object resultData;
    
    /**
     * 反馈文本
     */
    private String feedbackText;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 开始时间
     */
    private long startTime;
    
    /**
     * 结束时间
     */
    private long endTime;
    
    /**
     * 执行耗时
     */
    private long executionTime;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 执行状态
     */
    private ExecutionStatus status;
    
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
        CANCELLED   // 执行取消
    }
}
