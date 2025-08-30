package com.voicecommand.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 邮件请求模型
 * 
 * 用于向邮件服务发送邮件请求
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
    
    /**
     * 发件人邮箱
     */
    private String fromEmail;
    
    /**
     * 发件人姓名
     */
    private String fromName;
    
    /**
     * 收件人邮箱
     */
    private String toEmail;
    
    /**
     * 收件人姓名
     */
    private String toName;
    
    /**
     * 邮件主题
     */
    private String subject;
    
    /**
     * 邮件内容
     */
    private String content;
    
    /**
     * 邮件类型 (text, html)
     */
    private String contentType;
    
    /**
     * 附件列表
     */
    private List<String> attachments;
    
    /**
     * 抄送邮箱列表
     */
    private List<String> ccEmails;
    
    /**
     * 密送邮箱列表
     */
    private List<String> bccEmails;
    
    /**
     * 优先级 (high, normal, low)
     */
    private String priority;
    
    /**
     * 是否保存为草稿
     */
    private boolean saveAsDraft;
    
    /**
     * 请求时间戳
     */
    private long timestamp;
    
    /**
     * 用户ID
     */
    private String userId;
}
