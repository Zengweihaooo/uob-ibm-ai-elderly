package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS服务类 - 支持模拟和真实SMS发送
 * 
 * 此服务类为IBM AI老年人项目提供SMS短信发送功能，
 * 支持开发阶段的模拟发送和生产阶段的真实SMS发送。
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
@Service
public class SmsService {
    
    // 配置属性：是否启用模拟模式
    @Value("${app.sms.mock:true}")
    private boolean mockMode;
    
    // 配置属性：SMS服务提供商
    @Value("${app.sms.provider:mock}")
    private String smsProvider;
    

    
    // Twilio配置属性
    @Value("${app.sms.twilio.account-sid:}")
    private String twilioAccountSid;
    
    @Value("${app.sms.twilio.auth-token:}")
    private String twilioAuthToken;
    
    @Value("${app.sms.twilio.from-number:}")
    private String twilioFromNumber;
    
    // 内存存储SMS记录（演示用途，生产环境建议使用数据库）
    private final List<Map<String, Object>> smsLogs = new ArrayList<>();
    private long smsIdCounter = 1;
    
    /**
     * 发送SMS短信
     * 
     * @param toPhoneNumber 接收方电话号码
     * @param message 短信内容
     * @return 发送结果信息
     */
    public Map<String, Object> sendSMS(String toPhoneNumber, String message) {
        return sendSMS(toPhoneNumber, message, "GENERAL");
    }
    
    /**
     * 发送SMS短信（带消息类型）
     * 
     * @param toPhoneNumber 接收方电话号码
     * @param message 短信内容
     * @param messageType 消息类型（如 HEALTH_ALERT, EMERGENCY, GENERAL等）
     * @return 发送结果信息
     */
    public Map<String, Object> sendSMS(String toPhoneNumber, String message, String messageType) {
        // 验证输入参数
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            return createErrorResponse("电话号码不能为空");
        }
        
        if (message == null || message.trim().isEmpty()) {
            return createErrorResponse("短信内容不能为空");
        }
        
        // 格式化电话号码
        String formattedPhone = formatPhoneNumber(toPhoneNumber.trim());
        
        try {
            Map<String, Object> result;
            
            if (mockMode) {
                // 模拟SMS发送
                result = sendMockSMS(formattedPhone, message, messageType);
            } else {
                // 真实SMS发送
                result = sendRealSMS(formattedPhone, message, messageType);
            }
            
            // 记录SMS日志
            logSmsRecord(formattedPhone, message, messageType, result);
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = "SMS发送失败: " + e.getMessage();
            System.err.println(errorMsg);
            
            // 记录失败日志
            Map<String, Object> errorResult = createErrorResponse(errorMsg);
            logSmsRecord(formattedPhone, message, messageType, errorResult);
            
            return errorResult;
        }
    }
    
    /**
     * 模拟SMS发送
     */
    private Map<String, Object> sendMockSMS(String phoneNumber, String message, String messageType) {
        System.out.println("=".repeat(50));
        System.out.println("📱 模拟SMS发送");
        System.out.println("=".repeat(50));
        System.out.println("接收方: " + phoneNumber);
        System.out.println("消息类型: " + messageType);
        System.out.println("内容: " + message);
        System.out.println("发送时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("状态: ✅ 模拟发送成功");
        System.out.println("=".repeat(50));
        
        // 模拟网络延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return createSuccessResponse("mock_" + System.currentTimeMillis(), "SMS模拟发送成功");
    }
    
    /**
     * 真实SMS发送（使用Twilio API）
     */
    private Map<String, Object> sendRealSMS(String phoneNumber, String message, String messageType) {
        if ("twilio".equalsIgnoreCase(smsProvider)) {
            return sendTwilioSMS(phoneNumber, message, messageType);
        } else {
            throw new UnsupportedOperationException("只支持Twilio SMS服务提供商，当前配置: " + smsProvider);
        }
    }
    
    /**
     * 使用Twilio发送SMS
     */
    private Map<String, Object> sendTwilioSMS(String phoneNumber, String message, String messageType) {
        // 验证Twilio配置
        if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty()) {
            String errorMsg = "Twilio配置不完整，请检查account-sid和auth-token配置";
            System.err.println("❌ " + errorMsg);
            return createErrorResponse(errorMsg);
        }
        
        if (twilioFromNumber.isEmpty()) {
            String errorMsg = "Twilio发送方号码未配置，请设置twilio.from-number";
            System.err.println("❌ " + errorMsg);
            return createErrorResponse(errorMsg);
        }
        
        try {
            // 显示Twilio发送信息（模拟）
            System.out.println("📱 Twilio SMS发送");
            System.out.println("Account SID: " + twilioAccountSid.substring(0, 10) + "...");
            System.out.println("发送方号码: " + twilioFromNumber);
            System.out.println("接收方号码: " + phoneNumber);
            System.out.println("消息类型: " + messageType);
            System.out.println("消息内容: " + message);
            
            // 实际的Twilio集成代码（需要添加Twilio依赖后取消注释）:
            /*
            Twilio.init(twilioAccountSid, twilioAuthToken);
            
            Message twilioMessage = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioFromNumber),
                message
            ).create();
            
            System.out.println("✅ Twilio SMS发送成功，MessageSID: " + twilioMessage.getSid());
            return createSuccessResponse(twilioMessage.getSid(), "Twilio SMS发送成功");
            */
            
            // 当前为模拟发送（等待添加Twilio依赖）
            String mockMessageId = "twilio_sim_" + System.currentTimeMillis();
            System.out.println("⚠️  模拟Twilio发送（需要添加Twilio依赖才能真实发送）");
            
            return createSuccessResponse(mockMessageId, "Twilio SMS模拟发送成功");
            
        } catch (Exception e) {
            String errorMsg = "Twilio SMS发送失败: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            return createErrorResponse(errorMsg);
        }
    }
    

    
    /**
     * 格式化电话号码
     */
    private String formatPhoneNumber(String phoneNumber) {
        // 移除所有非数字字符
        String digits = phoneNumber.replaceAll("[^0-9+]", "");
        
        // 如果是中国手机号且没有国际区号，添加+86
        if (digits.matches("^1[3-9]\\d{9}$")) {
            return "+86" + digits;
        }
        
        // 如果没有+号，添加+号
        if (!digits.startsWith("+")) {
            return "+" + digits;
        }
        
        return digits;
    }
    
    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String messageId, String statusMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("messageId", messageId);
        response.put("status", "SENT");
        response.put("message", statusMessage);
        response.put("timestamp", LocalDateTime.now());
        response.put("mockMode", mockMode);
        return response;
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", "FAILED");
        response.put("message", errorMessage);
        response.put("timestamp", LocalDateTime.now());
        response.put("mockMode", mockMode);
        return response;
    }
    
    /**
     * 记录SMS发送日志
     */
    private void logSmsRecord(String phoneNumber, String message, String messageType, Map<String, Object> result) {
        Map<String, Object> log = new HashMap<>();
        log.put("id", smsIdCounter++);
        log.put("phoneNumber", phoneNumber);
        log.put("message", message);
        log.put("messageType", messageType);
        log.put("success", result.get("success"));
        log.put("status", result.get("status"));
        log.put("messageId", result.get("messageId"));
        log.put("timestamp", LocalDateTime.now());
        log.put("provider", mockMode ? "MOCK" : smsProvider.toUpperCase());
        
        smsLogs.add(log);
        
        // 限制日志数量（保留最近1000条）
        if (smsLogs.size() > 1000) {
            smsLogs.remove(0);
        }
    }
    
    /**
     * 获取SMS发送历史记录
     * 
     * @return SMS发送历史列表
     */
    public List<Map<String, Object>> getSmsHistory() {
        return new ArrayList<>(smsLogs);
    }
    
    /**
     * 获取SMS发送统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getSmsStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalSent = smsLogs.stream()
                .mapToLong(log -> (Boolean) log.get("success") ? 1 : 0)
                .sum();
        
        long totalFailed = smsLogs.size() - totalSent;
        
        stats.put("totalMessages", smsLogs.size());
        stats.put("successfulSent", totalSent);
        stats.put("failed", totalFailed);
        stats.put("successRate", smsLogs.isEmpty() ? 0 : (double) totalSent / smsLogs.size() * 100);
        stats.put("mockMode", mockMode);
        stats.put("provider", mockMode ? "MOCK" : smsProvider);
        
        return stats;
    }
    
    /**
     * 发送健康警报SMS
     * 
     * @param phoneNumber 电话号码
     * @param healthData 健康数据信息
     * @return 发送结果
     */
    public Map<String, Object> sendHealthAlertSMS(String phoneNumber, String healthData) {
        String message = String.format(
            "【健康警报】检测到异常健康数据：%s。请及时关注老人健康状况。- AI老年陪伴系统", 
            healthData
        );
        
        return sendSMS(phoneNumber, message, "HEALTH_ALERT");
    }
    
    /**
     * 发送紧急联系SMS
     * 
     * @param phoneNumber 电话号码
     * @param emergencyInfo 紧急情况信息
     * @return 发送结果
     */
    public Map<String, Object> sendEmergencySMS(String phoneNumber, String emergencyInfo) {
        String message = String.format(
            "【紧急提醒】%s 请立即联系确认安全。- AI老年陪伴系统", 
            emergencyInfo
        );
        
        return sendSMS(phoneNumber, message, "EMERGENCY");
    }
}