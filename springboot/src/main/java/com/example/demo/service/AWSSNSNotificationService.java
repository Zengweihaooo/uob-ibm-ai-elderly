package com.example.demo.service;

import com.example.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AWS SNS通知服务
 * 支持短信、推送通知等多种通知方式
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws") // 只在AWS环境下使用
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
     * 发送短信通知
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
     * 发送短信通知（带主题）
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
     * 发布到SNS主题
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
     * 发送提醒通知
     */
    public void sendReminderNotification(User user, String reminderType, String message) {
        if (remindersTopicArn == null || remindersTopicArn.isEmpty()) {
            logger.warning("Reminders topic ARN not configured");
            return;
        }
        
        try {
            // 构建通知消息
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
     * 发送紧急通知
     */
    public void sendEmergencyNotification(User user, String emergencyType, String message) {
        if (emergencyTopicArn == null || emergencyTopicArn.isEmpty()) {
            logger.warning("Emergency topic ARN not configured");
            return;
        }
        
        try {
            // 构建紧急通知消息
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
            
            // 如果用户有手机号，立即发送短信
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                sendSMS(user.getPhoneNumber(), "🚨 EMERGENCY: " + message, "EMERGENCY");
            }
            
        } catch (Exception e) {
            logger.severe("Failed to send emergency notification: " + e.getMessage());
        }
    }
    
    /**
     * 发送推送通知
     */
    public void sendPushNotification(String deviceToken, String message, String title) {
        if (pushTopicArn == null || pushTopicArn.isEmpty()) {
            logger.warning("Push topic ARN not configured");
            return;
        }
        
        try {
            // 构建推送通知消息
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
     * 创建SNS主题
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
     * 订阅SNS主题
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
     * 将Map转换为SNS消息属性
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
     * 将Map转换为JSON字符串
     */
    private String convertToJson(Map<String, Object> data) {
        try {
            // 简单的JSON转换，生产环境建议使用Jackson或Gson
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

