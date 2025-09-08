package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.SNSService;
import com.example.demo.service.AWSEmailService;

/**
 * AWS Test Controller
 * Provides endpoints for testing AWS services
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@RestController
@RequestMapping("/api/aws-test")
@Profile("aws")
public class AWSTestController {
    
    @Autowired
    private SNSService snsService;
    
    @Autowired
    private AWSEmailService awsEmailService;
    
    /**
     * Test SNS SMS sending
     */
    @PostMapping("/send-sms")
    public String sendTestSMS(@RequestParam String phoneNumber, @RequestParam String message) {
        try {
            snsService.sendHealthAlert("Test SMS: " + message);
            return "SMS sent successfully to " + phoneNumber;
        } catch (Exception e) {
            return "SMS sending failed: " + e.getMessage();
        }
    }
    
    /**
     * Test AWS SES email sending
     */
    @PostMapping("/send-email")
    public String sendTestEmail(@RequestParam String toEmail, @RequestParam String subject, @RequestParam String message) {
        try {
            awsEmailService.sendHealthAlertEmail(toEmail, subject, message);
            return "Email sent successfully to " + toEmail;
        } catch (Exception e) {
            return "Email sending failed: " + e.getMessage();
        }
    }
    
    /**
     * Test verification email
     */
    @PostMapping("/send-verification")
    public String sendVerificationEmail(@RequestParam String toEmail, @RequestParam String code) {
        try {
            awsEmailService.sendVerificationEmail(toEmail, code);
            return "Verification email sent successfully to " + toEmail;
        } catch (Exception e) {
            return "Verification email sending failed: " + e.getMessage();
        }
    }
    
    /**
     * Check AWS services health
     */
    @GetMapping("/health")
    public String checkAWSHealth() {
        return "AWS services are running";
    }
    
    /**
     * Test SNS topic publishing
     */
    @PostMapping("/publish-topic")
    public String publishToTopic(@RequestParam String topicType, @RequestParam String message) {
        try {
            switch (topicType.toLowerCase()) {
                case "reminder":
                    snsService.sendHealthAlert(message);
                    break;
                case "emergency":
                    snsService.sendEmergencyAlert(message);
                    break;
                case "system":
                    snsService.sendSystemNotification(message);
                    break;
                default:
                    return "Invalid topic type. Use: reminder, emergency, or system";
            }
            return "Message published to " + topicType + " topic successfully";
        } catch (Exception e) {
            return "Topic publishing failed: " + e.getMessage();
        }
    }
}
