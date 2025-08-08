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

/**
 * Service class for managing email operations
 * 
 * This service handles email subscription management and sending verification emails
 * for the IBM AI Elderly Project.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Service
public class EmailService {

    // In-memory storage for email addresses (consider using database in production)
    private final List<String> emailList = new ArrayList<>();

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    // Sender email address configured in application.properties
    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * Add a new email address to the subscription list
     * 
     * @param email The email address to add
     */
    public void addEmail(String email) {
        if (!emailList.contains(email)) {
            emailList.add(email);
        }
    }

    /**
     * Get all subscribed email addresses
     * 
     * @return List of all email addresses
     */
    public List<String> getAllEmails() {
        return emailList;
    }

    /**
     * Send HTML emails to all subscribed users
     * 
     * This method iterates through all email addresses and sends
     * a test email using the mail template.
     */
    public void sendEmailsToAll() {
        for (String to : emailList) {
            try {
                sendHtmlEmail(to);
                System.out.println("Email sent successfully to: " + to);
            } catch (MessagingException e) {
                System.err.println("Failed to send email to: " + to + " => " + e.getMessage());
            }
        }
    }

    /**
     * Send verification email with 6-digit code
     * 
     * @param toEmail The recipient's email address
     * @param verificationCode The 6-digit verification code
     * @throws RuntimeException if email sending fails
     */
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (supports attachments), UTF-8 encoding
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set sender, recipient, and subject
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Pet Reminder App - Email Verification");

            // Process Thymeleaf template for verification email
            Context ctx = new Context();
            ctx.setVariable("toEmail", toEmail);
            ctx.setVariable("verificationCode", verificationCode);
            String htmlContent = templateEngine.process("verificationTemplate", ctx);
            helper.setText(htmlContent, true);  // true = HTML mode

            mailSender.send(message);
            System.out.println("Verification email sent successfully to: " + toEmail + " with code: " + verificationCode);
        } catch (MessagingException e) {
            System.err.println("Failed to send verification email to: " + toEmail + " => " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            e.printStackTrace(); // 打印完整堆栈跟踪
            throw new RuntimeException("Failed to send verification email", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending email to: " + toEmail + " => " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error sending verification email", e);
        }
    }

    /**
     * Send HTML email to a specific recipient
     * 
     * @param toEmail The recipient's email address
     * @throws MessagingException If email sending fails
     */
    private void sendHtmlEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        // true = multipart (supports attachments), UTF-8 encoding
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Set sender, recipient, and subject
        helper.setFrom(fromAddress);
        helper.setTo(toEmail);
        helper.setSubject("Pet Reminder App - Welcome Message");

        // Process Thymeleaf template
        Context ctx = new Context();
        ctx.setVariable("toEmail", toEmail);
        String htmlContent = templateEngine.process("mailTemplate", ctx);
        helper.setText(htmlContent, true);  // true = HTML mode

        mailSender.send(message);
    }

    /**
     * Send health alert email to emergency contact
     * 
     * @param toEmail The recipient's email address
     * @param subject The email subject
     * @param message The email message content
     * @throws RuntimeException if email sending fails
     */
    public void sendHealthAlertEmail(String toEmail, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set sender, recipient, and subject
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Create HTML content for health alert
            Context ctx = new Context();
            ctx.setVariable("toEmail", toEmail);
            ctx.setVariable("subject", subject);
            ctx.setVariable("message", message);
            ctx.setVariable("timestamp", java.time.LocalDateTime.now());
            
            String htmlContent = templateEngine.process("healthAlertTemplate", ctx);
            helper.setText(htmlContent, true);  // true = HTML mode

            mailSender.send(mimeMessage);
            System.out.println("Health alert email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send health alert email to: " + toEmail + " => " + e.getMessage());
            throw new RuntimeException("Failed to send health alert email", e);
        }
    }

    public void sendDailyHealthCheckReminderEmail(String toEmail, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set sender, recipient, and subject
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Create HTML content for health check reminder
            Context ctx = new Context();
            ctx.setVariable("toEmail", toEmail);
            ctx.setVariable("subject", subject);
            ctx.setVariable("message", message);
            ctx.setVariable("timestamp", java.time.LocalDateTime.now());

            String htmlContent = templateEngine.process("dailyHealthCheckReminderTemplate", ctx);
            helper.setText(htmlContent, true);  // true = HTML mode

            mailSender.send(mimeMessage);
            System.out.println("Daily health check reminder email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send daily health check reminder email to: " + toEmail + " => " + e.getMessage());
            throw new RuntimeException("Failed to send daily health check reminder email", e);
        }
    }

    public void sendDailyPlanReminderEmail(String toEmail, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set sender, recipient, and subject
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Create HTML content for plan reminder
            Context ctx = new Context();
            ctx.setVariable("toEmail", toEmail);
            ctx.setVariable("subject", subject);
            ctx.setVariable("message", message);
            ctx.setVariable("timestamp", java.time.LocalDateTime.now());

            String htmlContent = templateEngine.process("dailyPlanReminderTemplate", ctx);
            helper.setText(htmlContent, true);  // true = HTML mode

            mailSender.send(mimeMessage);
            System.out.println("Daily plan reminder email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send daily plan reminder email to: " + toEmail + " => " + e.getMessage());
            throw new RuntimeException("Failed to send daily plan reminder email", e);
        }
    }

    /**
     * Send important date reminder email
     * 
     * @param toEmail The recipient's email address
     * @param userName The user's name
     * @param importantDate The important date object
     * @param reminderType The type of reminder (week/day)
     * @throws RuntimeException if email sending fails
     */
    public void sendImportantDateReminderEmail(String toEmail, String userName, 
                                             com.example.demo.pojo.ImportantDate importantDate, 
                                             String reminderType) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set sender, recipient, and subject
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            
            String subject = "";
            if ("week".equals(reminderType)) {
                subject = "重要日期提醒 - 一周前通知";
            } else if ("day".equals(reminderType)) {
                subject = "重要日期提醒 - 一天前通知";
            } else {
                subject = "重要日期提醒";
            }

            helper.setSubject(subject);

            // Create HTML content for important date reminder
            Context ctx = new Context();
            ctx.setVariable("toEmail", toEmail);
            ctx.setVariable("userName", userName);
            ctx.setVariable("importantDate", importantDate);
            ctx.setVariable("reminderType", reminderType);
            ctx.setVariable("timestamp", java.time.LocalDateTime.now());

            String htmlContent = templateEngine.process("importantDateReminderTemplate", ctx);
            helper.setText(htmlContent, true);  // true = HTML mode

            mailSender.send(mimeMessage);
            System.out.println("Important date reminder email sent successfully to: " + toEmail + 
                             " for date: " + importantDate.getTitle() + " (reminder type: " + reminderType + ")");
        } catch (MessagingException e) {
            System.err.println("Failed to send important date reminder email to: " + toEmail + " => " + e.getMessage());
            throw new RuntimeException("Failed to send important date reminder email", e);
        }
    }
}