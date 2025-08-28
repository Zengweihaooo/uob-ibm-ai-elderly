package com.voicecommand.client;

import com.voicecommand.model.EmailRequest;
import com.voicecommand.model.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件服务客户端
 * 
 * 通过OpenFeign调用主项目的EmailComposeController
 * 使用主项目中的邮件发送API
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@FeignClient(name = "email-service", url = "${email.service.url}")
public interface EmailServiceClient {
    
    /**
     * 发送邮件 - 调用主项目EmailComposeController
     */
    @PostMapping("/email/api/send")
    EmailResponse sendEmail(@RequestParam("fromEmail") String fromEmail,
                           @RequestParam("toEmail") String toEmail,
                           @RequestParam("subject") String subject,
                           @RequestParam("content") String content,
                           @RequestParam(value = "senderName", required = false) String senderName);
    
    /**
     * 保存邮件草稿 - 调用主项目EmailComposeController
     */
    @PostMapping("/email/api/draft")
    EmailResponse saveDraft(@RequestParam("fromEmail") String fromEmail,
                           @RequestParam("toEmail") String toEmail,
                           @RequestParam("subject") String subject,
                           @RequestParam("content") String content);
    
    /**
     * 获取用户联系人 - 调用主项目EmailComposeController
     */
    @GetMapping("/email/api/contacts")
    EmailResponse getContacts(@RequestParam("userId") Long userId);
    
    /**
     * 添加新联系人 - 调用主项目EmailComposeController
     */
    @PostMapping("/email/api/contacts")
    EmailResponse addContact(@RequestParam("name") String name,
                            @RequestParam("email") String email,
                            @RequestParam("userId") Long userId);
}
