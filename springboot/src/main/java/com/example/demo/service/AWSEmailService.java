package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.logging.Logger;

/**
 * AWS SES Email Service
 * Handles email sending using AWS Simple Email Service
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws")
public class AWSEmailService {
    
    private static final Logger logger = Logger.getLogger(AWSEmailService.class.getName());
    
    @Autowired
    private SesClient sesClient;
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    /**
     * Send health alert email using AWS SES
     */
    public void sendHealthAlertEmail(String toEmail, String subject, String message) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(toEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder()
                        .data(subject)
                        .charset("UTF-8")
                        .build())
                    .body(Body.builder()
                        .html(Content.builder()
                            .data(message)
                            .charset("UTF-8")
                            .build())
                        .build())
                    .build())
                .build();
            
            SendEmailResponse response = sesClient.sendEmail(request);
            logger.info("AWS SES email sent successfully. MessageId: " + response.messageId());
            
        } catch (Exception e) {
            logger.severe("Failed to send AWS SES email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("AWS SES email sending failed", e);
        }
    }
    
    /**
     * Send verification email using AWS SES
     */
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            String subject = "Pet Reminder App - Email Verification";
            String message = String.format(
                "<html><body>" +
                "<h2>Email Verification</h2>" +
                "<p>Your verification code is: <strong>%s</strong></p>" +
                "<p>Please use this code to complete your registration.</p>" +
                "</body></html>", 
                verificationCode
            );
            
            sendHealthAlertEmail(toEmail, subject, message);
            
        } catch (Exception e) {
            logger.severe("Failed to send verification email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Verification email sending failed", e);
        }
    }
    
    /**
     * Send important date reminder email using AWS SES
     */
    public void sendImportantDateReminderEmail(String toEmail, String subject, String message) {
        try {
            sendHealthAlertEmail(toEmail, subject, message);
            
        } catch (Exception e) {
            logger.severe("Failed to send important date reminder email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Important date reminder email sending failed", e);
        }
    }
    
    /**
     * Send custom email using AWS SES
     */
    public void sendCustomEmail(String toEmail, String subject, String content, String senderName) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(toEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder()
                        .data(subject)
                        .charset("UTF-8")
                        .build())
                    .body(Body.builder()
                        .html(Content.builder()
                            .data(content)
                            .charset("UTF-8")
                            .build())
                        .build())
                    .build())
                .build();
            
            SendEmailResponse response = sesClient.sendEmail(request);
            logger.info("AWS SES custom email sent successfully. MessageId: " + response.messageId());
            
        } catch (Exception e) {
            logger.severe("Failed to send AWS SES custom email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("AWS SES custom email sending failed", e);
        }
    }
}
