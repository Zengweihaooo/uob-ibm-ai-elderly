package com.voicecommand.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * 联系人查询服务
 * 用于根据姓名查找联系人的邮箱地址等信息
 */
@Service
@Slf4j
public class ContactLookupService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    // 主后端服务的联系人查询端点
    private static final String CONTACT_LOOKUP_URL = "http://localhost:8080/api/family/contacts/search";
    
    /**
     * 根据姓名查找联系人信息
     * 
     * @param name 联系人姓名
     * @param userId 用户ID（可选）
     * @return 联系人信息，如果未找到则返回null
     */
    public ContactInfo lookupContactByName(String name, String userId) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("联系人姓名为空，无法查询");
            return null;
        }
        
        log.info("开始查询联系人: name={}, userId={}", name, userId);
        
        try {
            // 构建查询参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("name", name.trim());
            if (userId != null && !userId.trim().isEmpty()) {
                requestParams.put("userId", userId);
            }
            
            // 调用主后端服务查询联系人
            ResponseEntity<Map> response = restTemplate.postForEntity(
                CONTACT_LOOKUP_URL, 
                requestParams, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (Boolean.TRUE.equals(responseBody.get("success"))) {
                    List<Map<String, Object>> contacts = (List<Map<String, Object>>) responseBody.get("contacts");
                    
                    if (contacts != null && !contacts.isEmpty()) {
                        // 返回第一个匹配的联系人
                        Map<String, Object> contact = contacts.get(0);
                        ContactInfo contactInfo = new ContactInfo();
                        contactInfo.setName((String) contact.get("name"));
                        contactInfo.setEmail((String) contact.get("email"));
                        contactInfo.setPhone((String) contact.get("phone"));
                        contactInfo.setRelationship((String) contact.get("relationship"));
                        
                        log.info("找到联系人: {}", contactInfo);
                        return contactInfo;
                    }
                } else {
                    log.warn("查询联系人失败: {}", responseBody.get("message"));
                }
            }
            
        } catch (Exception e) {
            log.error("查询联系人时发生错误: name={}", name, e);
        }
        
        log.info("未找到联系人: name={}", name);
        return null;
    }
    
    /**
     * 根据姓名查找邮箱地址
     * 
     * @param name 联系人姓名
     * @param userId 用户ID（可选）
     * @return 邮箱地址，如果未找到则返回null
     */
    public String lookupEmailByName(String name, String userId) {
        ContactInfo contact = lookupContactByName(name, userId);
        return contact != null ? contact.getEmail() : null;
    }
    
    /**
     * 联系人信息类
     */
    public static class ContactInfo {
        private String name;
        private String email;
        private String phone;
        private String relationship;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        
        @Override
        public String toString() {
            return "ContactInfo{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
        }
    }
}
