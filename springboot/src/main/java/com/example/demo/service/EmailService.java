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
            throw new RuntimeException("Failed to send verification email", e);
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
}