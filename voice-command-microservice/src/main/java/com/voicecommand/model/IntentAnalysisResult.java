package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 意图分析结果模型
 * 
 * AI分析用户语音命令后得出的意图和参数
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntentAnalysisResult {
    
    /**
     * 要调用的功能名称
     */
    private String functionName;
    
    /**
     * 置信度 (0.0-1.0)
     */
    private double confidence;
    
    /**
     * 功能参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 分析原因
     */
    private String reasoning;
    
    /**
     * 使用的知识库信息
     */
    private String knowledgeUsed;
    
    /**
     * 可能的替代功能
     */
    private List<String> alternativeFunctions;
    
    /**
     * 是否需要澄清
     */
    private boolean needsClarification;
    
    /**
     * 澄清问题
     */
    private String clarificationQuestion;
    
    /**
     * 原始用户输入
     */
    private String originalText;
    
    /**
     * 分析时间戳
     */
    private long analysisTimestamp;
    
    /**
     * 使用的AI模型
     */
    private String aiModel;
}
