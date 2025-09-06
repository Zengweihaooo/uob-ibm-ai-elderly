package com.example.demo.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SNSService {
    
    @Autowired
    private AmazonSNS amazonSNS;
    
    @Value("${aws.sns.topic.reminders}")
    private String remindersTopicArn;
    
    @Value("${aws.sns.topic.emergency}")
    private String emergencyTopicArn;
    
    @Value("${aws.sns.topic.push}")
    private String pushTopicArn;
    
    public void sendHealthAlert(String message) {
        sendMessage(remindersTopicArn, "Health Alert", message);
    }
    
    public void sendEmergencyAlert(String message) {
        sendMessage(emergencyTopicArn, "Emergency Alert", message);
    }
    
    public void sendSystemNotification(String message) {
        sendMessage(pushTopicArn, "System Notification", message);
    }
    
    private void sendMessage(String topicArn, String subject, String message) {
        try {
            PublishRequest request = new PublishRequest()
                .withTopicArn(topicArn)
                .withSubject(subject)
                .withMessage(message);
            
            PublishResult result = amazonSNS.publish(request);
            System.out.println("SNS message sent successfully. MessageId: " + result.getMessageId());
        } catch (Exception e) {
            System.err.println("Error sending SNS message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}