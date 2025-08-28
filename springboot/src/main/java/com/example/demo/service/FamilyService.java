package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.mapper.FamilyContactMapper;
import com.example.demo.pojo.FamilyContact;

/**
 * Service class for managing family contacts and family-related operations
 * 
 * This service handles family contact management, emergency notifications,
 * and family member communication for the IBM AI Elderly Project.
 * 
 * @author Weihao Zeng
 * @version 2.0 - 已实现数据持久化和用户数据隔离
 */
@Service
public class FamilyService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private FamilyContactMapper familyContactMapper;

    // 调试信息开关
    private static final boolean DEBUG_ENABLED = true;

    /**
     * Add a new family contact
     * 
     * @param userId User ID
     * @param name Contact name
     * @param phone Phone number
     * @param email Email address
     * @param relationship Relationship type
     * @param isEmergencyContact Whether this is an emergency contact
     * @return Created family contact
     */
    public FamilyContact addFamilyContact(Long userId, String name, String phone, String email,
                                       String relationship, Boolean isEmergencyContact) {
        
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.addFamilyContact DEBUG ====");
            System.out.println("用户ID: " + userId);
            System.out.println("联系人姓名: " + name);
            System.out.println("电话号码: " + phone);
            System.out.println("邮箱地址: " + email);
            System.out.println("关系: " + relationship);
            System.out.println("是否紧急联系人: " + isEmergencyContact);
            System.out.println("该用户现有联系人数量: " + getFamilyContacts(userId).size());
        }
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 验证失败 - 联系人姓名为空");
            }
            throw new IllegalArgumentException("Contact name is required");
        }

        if ((phone == null || phone.trim().isEmpty()) && 
            (email == null || email.trim().isEmpty())) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 验证失败 - 电话和邮箱都为空");
            }
            throw new IllegalArgumentException("Either phone number or email is required");
        }

        // Create new contact
        FamilyContact contact = new FamilyContact();
        contact.setUserId(userId);
        contact.setName(name.trim());
        contact.setPhone(phone != null ? phone.trim() : null);
        contact.setEmail(email != null ? email.trim() : null);
        contact.setRelationship(relationship != null ? relationship.trim() : "其他");
        contact.setIsEmergencyContact(isEmergencyContact != null ? isEmergencyContact : false);
        contact.setIsActive(true);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());

        // Save to database
        try {
            int result = familyContactMapper.insert(contact);
            if (result > 0) {
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 联系人保存到数据库成功");
                    System.out.println("数据库返回结果: " + result);
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: 数据库插入失败");
                }
                throw new RuntimeException("Failed to save contact to database");
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 数据库操作异常");
                System.err.println("异常信息: " + e.getMessage());
                e.printStackTrace();
            }
            throw new RuntimeException("Database operation failed: " + e.getMessage());
        }

        if (DEBUG_ENABLED) {
            System.out.println("DEBUG: 联系人创建成功");
            System.out.println("分配的联系人ID: " + contact.getId());
            System.out.println("联系人创建时间: " + contact.getCreatedAt());
            System.out.println("该用户当前联系人总数: " + getFamilyContacts(userId).size());
            System.out.println("============================================");
        }

        System.out.println("Family contact added: " + contact.getName() + " for user " + userId);
        return contact;
    }

    /**
     * Get all family contacts for a user
     * 
     * @param userId User ID
     * @return List of family contacts
     */
    public List<FamilyContact> getFamilyContacts(Long userId) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.getFamilyContacts DEBUG ====");
            System.out.println("查询用户ID: " + userId);
        }
        
        try {
            List<FamilyContact> userContacts = familyContactMapper.findActiveByUserId(userId);
            
            if (DEBUG_ENABLED) {
                System.out.println("该用户的联系人数量: " + userContacts.size());
                System.out.println("联系人详情:");
                for (FamilyContact contact : userContacts) {
                    System.out.println("  - ID: " + contact.getId() + ", 姓名: " + contact.getName() + 
                                     ", 关系: " + contact.getRelationship() + 
                                     ", 紧急联系人: " + contact.getIsEmergencyContact());
                }
                System.out.println("==============================================");
            }
            
            return userContacts;
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 数据库查询异常");
                System.err.println("异常信息: " + e.getMessage());
                e.printStackTrace();
            }
            // 返回空列表而不是抛出异常，保证系统稳定性
            return new ArrayList<>();
        }
    }

    /**
     * Get a specific family contact by ID
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @return Family contact or null if not found
     */
    public FamilyContact getFamilyContact(Long userId, Long contactId) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.getFamilyContact DEBUG ====");
            System.out.println("查询用户ID: " + userId);
            System.out.println("查询联系人ID: " + contactId);
        }
        
        try {
            FamilyContact contact = familyContactMapper.findById(contactId);
            
            // 验证数据隔离：确保用户只能访问自己的联系人
            if (contact != null && contact.getUserId().equals(userId) && contact.getIsActive()) {
                if (DEBUG_ENABLED) {
                    System.out.println("找到联系人: " + contact.getName() + ", 关系: " + contact.getRelationship());
                }
                return contact;
            } else {
                if (DEBUG_ENABLED) {
                    if (contact == null) {
                        System.out.println("未找到联系人");
                    } else if (!contact.getUserId().equals(userId)) {
                        System.out.println("数据隔离验证失败：用户ID不匹配");
                    } else if (!contact.getIsActive()) {
                        System.out.println("联系人已被删除");
                    }
                }
                return null;
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 数据库查询异常");
                System.err.println("异常信息: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Update a family contact
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @param contactData Updated contact data
     * @return Updated family contact or null if not found
     */
    public FamilyContact updateFamilyContact(Long userId, Long contactId, Map<String, Object> contactData) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.updateFamilyContact DEBUG ====");
            System.out.println("更新用户ID: " + userId);
            System.out.println("更新联系人ID: " + contactId);
            System.out.println("更新数据: " + contactData);
        }
        
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 未找到要更新的联系人");
                System.out.println("===========================================");
            }
            return null;
        }

        if (DEBUG_ENABLED) {
            System.out.println("更新前联系人信息: " + contact.getName());
        }

        // Update fields if provided
        if (contactData.containsKey("name")) {
            String name = (String) contactData.get("name");
            if (name != null && !name.trim().isEmpty()) {
                contact.setName(name.trim());
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 更新姓名为: " + name.trim());
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: 姓名验证失败 - 姓名不能为空");
                }
                throw new IllegalArgumentException("Contact name cannot be empty");
            }
        }

        if (contactData.containsKey("phone")) {
            String newPhone = (String) contactData.get("phone");
            contact.setPhone(newPhone);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: 更新电话为: " + newPhone);
            }
        }

        if (contactData.containsKey("email")) {
            String newEmail = (String) contactData.get("email");
            contact.setEmail(newEmail);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: 更新邮箱为: " + newEmail);
            }
        }

        if (contactData.containsKey("relationship")) {
            String newRelationship = (String) contactData.get("relationship");
            contact.setRelationship(newRelationship);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: 更新关系为: " + newRelationship);
            }
        }

        if (contactData.containsKey("isEmergencyContact")) {
            Boolean isEmergency = (Boolean) contactData.get("isEmergencyContact");
            contact.setIsEmergencyContact(isEmergency);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: 更新紧急联系人状态为: " + isEmergency);
            }
        }

        if (contactData.containsKey("address")) {
            String newAddress = (String) contactData.get("address");
            contact.setAddress(newAddress);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: 更新地址为: " + newAddress);
            }
        }

        // Validate contact has at least phone or email
        if ((contact.getPhone() == null || contact.getPhone().trim().isEmpty()) &&
            (contact.getEmail() == null || contact.getEmail().trim().isEmpty())) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 验证失败 - 电话和邮箱都为空");
            }
            throw new IllegalArgumentException("Either phone number or email is required");
        }

        contact.setUpdatedAt(LocalDateTime.now());
        
        // Save updates to database
        try {
            int result = familyContactMapper.update(contact);
            if (result > 0) {
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 联系人更新成功");
                    System.out.println("数据库更新结果: " + result);
                    System.out.println("更新时间: " + contact.getUpdatedAt());
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: 数据库更新失败");
                }
                throw new RuntimeException("Failed to update contact in database");
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 数据库更新异常");
                System.err.println("异常信息: " + e.getMessage());
                e.printStackTrace();
            }
            throw new RuntimeException("Database update failed: " + e.getMessage());
        }
        
        if (DEBUG_ENABLED) {
            System.out.println("==========================================");
        }
        
        return contact;
    }

    /**
     * Delete a family contact (soft delete)
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @return true if deleted, false if not found
     */
    public boolean deleteFamilyContact(Long userId, Long contactId) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.deleteFamilyContact DEBUG ====");
            System.out.println("删除用户ID: " + userId);
            System.out.println("删除联系人ID: " + contactId);
        }
        
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 未找到要删除的联系人");
                System.out.println("============================================");
            }
            return false;
        }

        if (DEBUG_ENABLED) {
            System.out.println("删除联系人: " + contact.getName());
        }

        contact.setIsActive(false);
        contact.setUpdatedAt(LocalDateTime.now());
        
        // Save deletion to database
        try {
            int result = familyContactMapper.update(contact);
            if (result > 0) {
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 联系人软删除成功");
                    System.out.println("数据库更新结果: " + result);
                    System.out.println("删除时间: " + contact.getUpdatedAt());
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: 数据库删除失败");
                }
                return false;
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 数据库删除异常");
                System.err.println("异常信息: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
        
        if (DEBUG_ENABLED) {
            System.out.println("=========================================");
        }
        
        return true;
    }

    /**
     * Send message to a family contact
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @param message Message content
     * @param messageType Message type (email/sms)
     * @return true if sent successfully, false otherwise
     */
    public boolean sendMessageToFamily(Long userId, Long contactId, String message, String messageType) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.sendMessageToFamily DEBUG ====");
            System.out.println("发送消息用户ID: " + userId);
            System.out.println("接收联系人ID: " + contactId);
            System.out.println("消息内容: " + message);
            System.out.println("消息类型: " + messageType);
        }
        
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 未找到联系人，无法发送消息");
                System.out.println("==========================================");
            }
            return false;
        }

        if (DEBUG_ENABLED) {
            System.out.println("目标联系人: " + contact.getName());
            System.out.println("联系人电话: " + contact.getPhone());
            System.out.println("联系人邮箱: " + contact.getEmail());
        }

        try {
            if ("email".equalsIgnoreCase(messageType) && contact.getEmail() != null) {
                String subject = buildMessageSubject(messageType, contact.getName());
                String content = buildMessageContent(message, messageType, contact.getName());
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 发送邮件");
                    System.out.println("邮件主题: " + subject);
                    System.out.println("邮件内容长度: " + content.length());
                }
                
                emailService.sendHealthAlertEmail(contact.getEmail(), subject, content);
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 邮件发送成功");
                    System.out.println("=========================================");
                }
                return true;
            } else if ("sms".equalsIgnoreCase(messageType) && contact.getPhone() != null) {
                String content = buildMessageContent(message, messageType, contact.getName());
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 发送短信");
                    System.out.println("短信内容长度: " + content.length());
                }
                
                smsService.sendSMS(contact.getPhone(), content);
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: 短信发送成功");
                    System.out.println("========================================");
                }
                return true;
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: 无法发送消息 - 联系方式不匹配或不存在");
                    System.out.println("消息类型: " + messageType);
                    System.out.println("联系人电话: " + contact.getPhone());
                    System.out.println("联系人邮箱: " + contact.getEmail());
                }
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 发送消息异常");
                System.err.println("异常信息: " + e.getMessage());
                e.printStackTrace();
            }
            System.err.println("Failed to send message to family contact: " + e.getMessage());
        }

        if (DEBUG_ENABLED) {
            System.out.println("==========================================");
        }
        
        return false;
    }

    /**
     * Get emergency contacts for a user
     * 
     * @param userId User ID
     * @return List of emergency contacts
     */
    public List<FamilyContact> getEmergencyContacts(Long userId) {
        return familyContactMapper.findActiveByUserId(userId).stream()
                .filter(contact -> contact.getIsEmergencyContact())
                .collect(Collectors.toList());
    }

    /**
     * Get family statistics for a user
     * 
     * @param userId User ID
     * @return Map containing family statistics
     */
    public Map<String, Object> getFamilyStats(Long userId) {
        List<FamilyContact> userContacts = getFamilyContacts(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", userContacts.size());
        stats.put("emergencyContacts", userContacts.stream()
                .filter(contact -> contact.getIsEmergencyContact())
                .count());
        stats.put("activeContacts", userContacts.size());
        
        return stats;
    }

    /**
     * Send emergency notification to all emergency contacts
     * 
     * @param userId User ID
     * @param emergencyType Type of emergency
     * @param description Emergency description
     * @return Number of contacts notified
     */
    public int sendEmergencyNotification(Long userId, String emergencyType, String description) {
        List<FamilyContact> emergencyContacts = getEmergencyContacts(userId);
        int notifiedCount = 0;

        for (FamilyContact contact : emergencyContacts) {
            try {
                String message = buildEmergencyMessage(emergencyType, description, contact.getName());
                
                if (contact.getPhone() != null) {
                    smsService.sendSMS(contact.getPhone(), message);
                    notifiedCount++;
                }
                
                if (contact.getEmail() != null) {
                    String subject = "紧急情况通知 - " + emergencyType;
                    emailService.sendHealthAlertEmail(contact.getEmail(), subject, message);
                    notifiedCount++;
                }
            } catch (Exception e) {
                System.err.println("Failed to notify emergency contact " + contact.getName() + ": " + e.getMessage());
            }
        }

        return notifiedCount;
    }

    /**
     * Build message subject
     */
    private String buildMessageSubject(String messageType, String contactName) {
        switch (messageType.toLowerCase()) {
            case "health":
                return "健康状态更新 - " + contactName;
            case "schedule":
                return "日程提醒 - " + contactName;
            case "emergency":
                return "紧急情况通知 - " + contactName;
            default:
                return "消息通知 - " + contactName;
        }
    }

    /**
     * Build message content
     */
    private String buildMessageContent(String message, String messageType, String contactName) {
        StringBuilder content = new StringBuilder();
        content.append("亲爱的 ").append(contactName).append("，\n\n");
        content.append(message).append("\n\n");
        content.append("此消息由AI老年人陪伴系统自动发送。\n");
        content.append("发送时间：").append(LocalDateTime.now().toString());
        
        return content.toString();
    }

    /**
     * Build emergency message
     */
    private String buildEmergencyMessage(String emergencyType, String description, String contactName) {
        return "紧急情况通知：\n" + emergencyType + "\n" + description + "\n\n联系人：" + contactName;
    }

    /**
     * Get all contacts (for testing)
     */
    public List<FamilyContact> getAllContacts() {
        return familyContactMapper.findAll();
    }

    /**
     * Clear all contacts (for testing)
     */
    public void clearAllContacts() {
        // 注意：这个方法仅用于测试，生产环境应该谨慎使用
        // 这里我们通过软删除所有联系人的方式来实现
        try {
            List<FamilyContact> allContacts = familyContactMapper.findAll();
            for (FamilyContact contact : allContacts) {
                contact.setIsActive(false);
                contact.setUpdatedAt(LocalDateTime.now());
                familyContactMapper.update(contact);
            }
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: 已软删除所有联系人，数量: " + allContacts.size());
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 清空联系人失败: " + e.getMessage());
            }
        }
    }

    /**
     * 验证多用户数据隔离功能
     * 此方法用于测试JWT集成后不同用户的数据是否正确隔离
     * 
     * @return 测试结果信息
     */
    public Map<String, Object> testUserDataIsolation() {
        Map<String, Object> testResult = new HashMap<>();
        
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.testUserDataIsolation DEBUG ====");
            System.out.println("开始测试多用户数据隔离功能");
        }
        
        try {
            // 清理现有数据
            clearAllContacts();
            
            // 创建测试用户1的联系人
            Long user1Id = 100L;
            FamilyContact user1Contact1 = addFamilyContact(user1Id, "张三", "13800000001", "zhangsan@example.com", "儿子", true);
            addFamilyContact(user1Id, "李四", "13800000002", "lisi@example.com", "女儿", false);
            
            // 创建测试用户2的联系人
            Long user2Id = 200L;
            FamilyContact user2Contact1 = addFamilyContact(user2Id, "王五", "13800000003", "wangwu@example.com", "配偶", true);
            addFamilyContact(user2Id, "赵六", "13800000004", "zhaoliu@example.com", "朋友", false);
            
            // 验证用户1只能看到自己的联系人
            List<FamilyContact> user1Contacts = getFamilyContacts(user1Id);
            List<FamilyContact> user2Contacts = getFamilyContacts(user2Id);
            
            // 验证数据隔离
            boolean isolationTest1 = user1Contacts.size() == 2;
            boolean isolationTest2 = user2Contacts.size() == 2;
            boolean isolationTest3 = user1Contacts.stream().allMatch(c -> c.getUserId().equals(user1Id));
            boolean isolationTest4 = user2Contacts.stream().allMatch(c -> c.getUserId().equals(user2Id));
            
            // 验证跨用户访问
            FamilyContact crossUserAccess1 = getFamilyContact(user1Id, user2Contact1.getId());
            FamilyContact crossUserAccess2 = getFamilyContact(user2Id, user1Contact1.getId());
            boolean isolationTest5 = (crossUserAccess1 == null);
            boolean isolationTest6 = (crossUserAccess2 == null);
            
            // 汇总测试结果
            boolean allTestsPassed = isolationTest1 && isolationTest2 && isolationTest3 && 
                                    isolationTest4 && isolationTest5 && isolationTest6;
            
            testResult.put("success", allTestsPassed);
            testResult.put("user1ContactCount", user1Contacts.size());
            testResult.put("user2ContactCount", user2Contacts.size());
            testResult.put("user1CanAccessOwnData", isolationTest1 && isolationTest3);
            testResult.put("user2CanAccessOwnData", isolationTest2 && isolationTest4);
            testResult.put("crossUserAccessBlocked", isolationTest5 && isolationTest6);
            testResult.put("totalContactsInSystem", familyContactMapper.findAll().size());
            
            if (DEBUG_ENABLED) {
                System.out.println("测试结果:");
                System.out.println("用户1联系人数量: " + user1Contacts.size());
                System.out.println("用户2联系人数量: " + user2Contacts.size());
                System.out.println("数据隔离是否成功: " + allTestsPassed);
                System.out.println("跨用户访问是否被阻止: " + (isolationTest5 && isolationTest6));
                System.out.println("系统中总联系人数量: " + familyContactMapper.findAll().size());
                System.out.println("===============================================");
            }
            
            return testResult;
            
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: 数据隔离测试异常");
                System.err.println("异常信息: " + e.getMessage());
                e.printStackTrace();
            }
            
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
            return testResult;
        }
    }
}
