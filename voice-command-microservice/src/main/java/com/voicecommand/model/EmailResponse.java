package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * 邮件响应模型
 * 
 * 邮件服务返回的执行结果
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 邮件ID
     */
    private String emailId;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 发送时间
     */
    private long sentTime;
    
    /**
     * 响应时间戳
     */
    private long timestamp;
    
    /**
     * 邮件状态
     */
    private EmailStatus status;
    
    /**
     * 收件人邮箱
     */
    private String toEmail;
    
    /**
     * 邮件主题
     */
    private String subject;
    
    /**
     * 邮件状态枚举
     */
    public enum EmailStatus {
        SENT,       // 已发送
        DELIVERED,  // 已投递
        READ,       // 已读
        FAILED,     // 发送失败
        PENDING,    // 等待发送
        DRAFT       // 草稿
    }
}
