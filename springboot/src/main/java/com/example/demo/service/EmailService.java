package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    private final List<String> emailList = new ArrayList<>();

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    // elderapp2025@163.com发送邮件
    @Value("${spring.mail.username}")
    private String fromAddress;

    public void addEmail(String email) {
        if (!emailList.contains(email)) {
            emailList.add(email);
        }
    }

    public List<String> getAllEmails() {
        return emailList;
    }

    public void sendEmailsToAll() {
        for (String to : emailList) {
            try {
                sendHtmlEmail(to);
                System.out.println("已发送邮件给: " + to);
            } catch (MessagingException e) {
                System.err.println("发送失败: " + to + " => " + e.getMessage());
            }
        }
    }

    private void sendHtmlEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        // true 表示 multipart (支持附件), 指定 UTF-8 编码
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // 发件人  收件人  主题
        helper.setFrom(fromAddress);
        helper.setTo(toEmail);
        helper.setSubject("宠物提醒 App 测试邮件");

        // Thymeleaf 模板
        Context ctx = new Context();
        ctx.setVariable("toEmail", toEmail);
        String htmlContent = templateEngine.process("mailTemplate", ctx);
        helper.setText(htmlContent, true);  // true = HTML 模式

        mailSender.send(message);
    }
}