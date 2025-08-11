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
            e.printStackTrace(); // Print full stack trace
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
     * Send password reset email with verification code
     * 
     * @param toEmail The recipient's email address
     * @param resetCode The password reset verification code
     * @throws RuntimeException if email sending fails
     */
    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Pet Reminder App - Password Reset Code");

            // Create HTML content for password reset
            String htmlContent = String.format(
                "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #4CAF50; text-align: center;'>🔒 Password Reset Request</h2>" +
                "<p>Dear User,</p>" +
                "<p>You have requested to reset your password for your Pet Reminder App account.</p>" +
                "<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;'>" +
                "<h3 style='color: #333; margin-bottom: 10px;'>Your Password Reset Code:</h3>" +
                "<div style='font-size: 32px; font-weight: bold; color: #4CAF50; letter-spacing: 3px; font-family: monospace;'>%s</div>" +
                "<p style='color: #666; font-size: 14px; margin-top: 15px;'>This code will expire in 30 minutes</p>" +
                "</div>" +
                "<p><strong>How to reset your password:</strong></p>" +
                "<ol>" +
                "<li>Go to the login page</li>" +
                "<li>Click on 'Forgot Password'</li>" +
                "<li>Enter this verification code</li>" +
                "<li>Set your new password</li>" +
                "</ol>" +
                "<p style='color: #666; font-size: 14px; margin-top: 30px;'>" +
                "If you did not request this password reset, please ignore this email. Your account remains secure." +
                "</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
                "<p style='color: #888; font-size: 12px; text-align: center;'>" +
                "This email was sent by IBM AI Elderly Project - Pet Reminder App<br>" +
                "Please do not reply to this email." +
                "</p>" +
                "</div></body></html>",
                resetCode
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            System.out.println("Password reset email sent successfully to: " + toEmail + " with code: " + resetCode);
            
        } catch (MessagingException e) {
            System.err.println("Failed to send password reset email to: " + toEmail + " => " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending password reset email to: " + toEmail + " => " + e.getMessage());
            throw new RuntimeException("Unexpected error sending password reset email", e);
        }
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
                subject = "Important Date Reminder - One Week Notice";
            } else if ("day".equals(reminderType)) {
                subject = "Important Date Reminder - One Day Notice";
            } else {
                subject = "Important Date Reminder";
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