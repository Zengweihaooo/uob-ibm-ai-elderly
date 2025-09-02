package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.User;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

/**
 * AWS SNS Notification Service
 * Supports multiple notification channels such as SMS and push notifications
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws") // Only used in AWS environment
public class AWSSNSNotificationService {
    
    private static final Logger logger = Logger.getLogger(AWSSNSNotificationService.class.getName());
    
    @Autowired
    private SnsClient snsClient;
    
    @Value("${aws.sns.topic.reminders:}")
    private String remindersTopicArn;
    
    @Value("${aws.sns.topic.emergency:}")
    private String emergencyTopicArn;
    
    @Value("${aws.sns.topic.push:}")
    private String pushTopicArn;
    
    /**
     * Send SMS notification
     */
    public void sendSMS(String phoneNumber, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumber)
                .build();
            
            PublishResponse response = snsClient.publish(request);
            logger.info("SMS sent successfully. Message ID: " + response.messageId());
            
        } catch (Exception e) {
            logger.severe("Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }
    
    /**
     * Send SMS notification (with subject)
     */
    public void sendSMS(String phoneNumber, String message, String subject) {
        try {
            Map<String, Object> messageAttributes = new HashMap<>();
            messageAttributes.put("AWS.SNS.SMS.SenderID", "ElderlyCompanion");
            messageAttributes.put("AWS.SNS.SMS.SMSType", "Transactional");
            
            PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumber)
                .subject(subject)
                .messageAttributes(convertToSNSAttributes(messageAttributes))
                .build();
            
            PublishResponse response = snsClient.publish(request);
            logger.info("SMS sent successfully. Message ID: " + response.messageId());
            
        } catch (Exception e) {
            logger.severe("Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }
    
    /**
     * Publish message to SNS topic
     */
    public void publishToTopic(String topicArn, String message, String subject) {
        try {
            PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .subject(subject)
                .build();
            
            PublishResponse response = snsClient.publish(request);
            logger.info("Message published to topic successfully. Message ID: " + response.messageId());
            
        } catch (Exception e) {
            logger.severe("Failed to publish to topic " + topicArn + ": " + e.getMessage());
            throw new RuntimeException("Topic publishing failed", e);
        }
    }
    
    /**
     * Send reminder notification
     */
    public void sendReminderNotification(User user, String reminderType, String message) {
        if (remindersTopicArn == null || remindersTopicArn.isEmpty()) {
            logger.warning("Reminders topic ARN not configured");
            return;
        }
        
        try {
            // Build notification payload
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", user.getId());
            notificationData.put("userName", user.getName());
            notificationData.put("userEmail", user.getEmail());
            notificationData.put("userPhone", user.getPhoneNumber());
            notificationData.put("reminderType", reminderType);
            notificationData.put("message", message);
            notificationData.put("timestamp", System.currentTimeMillis());
            
            String jsonMessage = convertToJson(notificationData);
            String subject = "Reminder: " + reminderType;
            
            publishToTopic(remindersTopicArn, jsonMessage, subject);
            
        } catch (Exception e) {
            logger.severe("Failed to send reminder notification: " + e.getMessage());
        }
    }
    
    /**
     * Send emergency notification
     */
    public void sendEmergencyNotification(User user, String emergencyType, String message) {
        if (emergencyTopicArn == null || emergencyTopicArn.isEmpty()) {
            logger.warning("Emergency topic ARN not configured");
            return;
        }
        
        try {
            // Build emergency notification payload
            Map<String, Object> emergencyData = new HashMap<>();
            emergencyData.put("userId", user.getId());
            emergencyData.put("userName", user.getName());
            emergencyData.put("userEmail", user.getEmail());
            emergencyData.put("userPhone", user.getPhoneNumber());
            emergencyData.put("emergencyType", emergencyType);
            emergencyData.put("message", message);
            emergencyData.put("timestamp", System.currentTimeMillis());
            emergencyData.put("priority", "HIGH");
            
            String jsonMessage = convertToJson(emergencyData);
            String subject = "EMERGENCY: " + emergencyType;
            
            publishToTopic(emergencyTopicArn, jsonMessage, subject);
            
            // If the user has a phone number, send SMS immediately
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                sendSMS(user.getPhoneNumber(), "🚨 EMERGENCY: " + message, "EMERGENCY");
            }
            
        } catch (Exception e) {
            logger.severe("Failed to send emergency notification: " + e.getMessage());
        }
    }
    
    /**
     * Send push notification
     */
    public void sendPushNotification(String deviceToken, String message, String title) {
        if (pushTopicArn == null || pushTopicArn.isEmpty()) {
            logger.warning("Push topic ARN not configured");
            return;
        }
        
        try {
            // Build push notification payload
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("deviceToken", deviceToken);
            pushData.put("title", title);
            pushData.put("message", message);
            pushData.put("timestamp", System.currentTimeMillis());
            
            String jsonMessage = convertToJson(pushData);
            String subject = "Push Notification: " + title;
            
            publishToTopic(pushTopicArn, jsonMessage, subject);
            
        } catch (Exception e) {
            logger.severe("Failed to send push notification: " + e.getMessage());
        }
    }
    
    /**
     * Create SNS topic
     */
    public String createTopic(String topicName) {
        try {
            CreateTopicRequest request = CreateTopicRequest.builder()
                .name(topicName)
                .build();
            
            CreateTopicResponse response = snsClient.createTopic(request);
            String topicArn = response.topicArn();
            
            logger.info("Topic created successfully: " + topicArn);
            return topicArn;
            
        } catch (Exception e) {
            logger.severe("Failed to create topic " + topicName + ": " + e.getMessage());
            throw new RuntimeException("Topic creation failed", e);
        }
    }
    
    /**
     * Subscribe to SNS topic
     */
    public void subscribeToTopic(String topicArn, String protocol, String endpoint) {
        try {
            SubscribeRequest request = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol(protocol)
                .endpoint(endpoint)
                .build();
            
            SubscribeResponse response = snsClient.subscribe(request);
            logger.info("Subscribed to topic successfully. Subscription ARN: " + response.subscriptionArn());
            
        } catch (Exception e) {
            logger.severe("Failed to subscribe to topic: " + e.getMessage());
            throw new RuntimeException("Topic subscription failed", e);
        }
    }
    
    /**
     * Convert Map to SNS message attributes
     */
    private Map<String, MessageAttributeValue> convertToSNSAttributes(Map<String, Object> attributes) {
        Map<String, MessageAttributeValue> snsAttributes = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            MessageAttributeValue value = MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(entry.getValue().toString())
                .build();
            
            snsAttributes.put(entry.getKey(), value);
        }
        
        return snsAttributes;
    }
    
    /**
     * Convert Map to JSON string
     */
    private String convertToJson(Map<String, Object> data) {
        try {
            // Simple JSON conversion; for production use Jackson or Gson
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                
                json.append("\"").append(entry.getKey()).append("\":");
                
                if (entry.getValue() instanceof String) {
                    json.append("\"").append(entry.getValue()).append("\"");
                } else {
                    json.append(entry.getValue());
                }
                
                first = false;
            }
            
            json.append("}");
            return json.toString();
            
        } catch (Exception e) {
            logger.warning("Failed to convert to JSON: " + e.getMessage());
            return "{}";
        }
    }
}

