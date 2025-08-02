package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.pojo.FamilyContact;
import com.example.demo.service.FamilyService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 测试类：FamilyController
 * 
 * 这个测试类测试FamilyController的所有API端点，包括：
 * - 添加家庭联系人
 * - 获取所有联系人
 * - 获取特定联系人
 * - 更新联系人
 * - 删除联系人
 * - 发送消息
 * - 获取紧急联系人
 * - 获取统计信息
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
@WebMvcTest(FamilyController.class)
public class FamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FamilyService familyService;

    @Autowired
    private ObjectMapper objectMapper;

    private FamilyContact testContact;
    private String validAuthHeader = "Bearer test-token";

    @BeforeEach
    void setUp() {
        // 创建测试用的联系人数据
        testContact = new FamilyContact();
        testContact.setId(1L);
        testContact.setUserId(1L);
        testContact.setName("张三");
        testContact.setPhoneNumber("13800138000");
        testContact.setEmail("zhangsan@example.com");
        testContact.setRelationship(FamilyContact.RelationshipType.CHILD);
        testContact.setNotificationPreference(FamilyContact.NotificationPreference.ALL);
        testContact.setEmergencyContact(true);
        testContact.setActive(true);
        testContact.setNotes("我的儿子");
        testContact.setCreatedAt(LocalDateTime.now());
        testContact.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 测试添加家庭联系人 - 成功情况
     */
    @Test
    void testAddFamilyContact_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("name", "张三");
        contactData.put("phoneNumber", "13800138000");
        contactData.put("email", "zhangsan@example.com");
        contactData.put("relationship", "CHILD");
        contactData.put("notificationPreference", "ALL");
        contactData.put("isEmergencyContact", true);
        contactData.put("notes", "我的儿子");

        // Mock service方法
        when(familyService.addFamilyContact(
            anyLong(), anyString(), anyString(), anyString(), 
            anyString(), anyString(), any(Boolean.class), anyString()
        )).thenReturn(testContact);

        // 执行测试
        mockMvc.perform(post("/api/family/contacts")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Family contact added successfully"))
                .andExpect(jsonPath("$.contact.id").value(1))
                .andExpect(jsonPath("$.contact.name").value("张三"));
    }

    /**
     * 测试添加家庭联系人 - 缺少认证
     */
    @Test
    void testAddFamilyContact_NoAuth() throws Exception {
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("name", "张三");
        contactData.put("phoneNumber", "13800138000");

        mockMvc.perform(post("/api/family/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactData)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    /**
     * 测试添加家庭联系人 - 缺少必填字段
     */
    @Test
    void testAddFamilyContact_MissingRequiredFields() throws Exception {
        Map<String, Object> contactData = new HashMap<>();
        // 故意不设置name字段

        mockMvc.perform(post("/api/family/contacts")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contact name is required"));
    }

    /**
     * 测试获取所有家庭联系人 - 成功情况
     */
    @Test
    void testGetFamilyContacts_Success() throws Exception {
        List<FamilyContact> contacts = new ArrayList<>();
        contacts.add(testContact);

        when(familyService.getFamilyContacts(anyLong())).thenReturn(contacts);

        mockMvc.perform(get("/api/family/contacts")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.contacts").isArray())
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    /**
     * 测试获取特定联系人 - 成功情况
     */
    @Test
    void testGetFamilyContact_Success() throws Exception {
        when(familyService.getFamilyContact(anyLong(), anyLong())).thenReturn(testContact);

        mockMvc.perform(get("/api/family/contacts/1")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.contact.id").value(1))
                .andExpect(jsonPath("$.contact.name").value("张三"));
    }

    /**
     * 测试获取特定联系人 - 联系人不存在
     */
    @Test
    void testGetFamilyContact_NotFound() throws Exception {
        when(familyService.getFamilyContact(anyLong(), anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/family/contacts/999")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试更新联系人 - 成功情况
     */
    @Test
    void testUpdateFamilyContact_Success() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "张三（更新）");
        updateData.put("phoneNumber", "13900139000");

        FamilyContact updatedContact = new FamilyContact();
        updatedContact.setId(1L);
        updatedContact.setUserId(1L);
        updatedContact.setName("张三（更新）");
        updatedContact.setPhoneNumber("13900139000");
        updatedContact.setRelationship(FamilyContact.RelationshipType.CHILD);
        updatedContact.setNotificationPreference(FamilyContact.NotificationPreference.ALL);
        updatedContact.setEmergencyContact(true);
        updatedContact.setActive(true);
        updatedContact.setCreatedAt(LocalDateTime.now());
        updatedContact.setUpdatedAt(LocalDateTime.now());

        when(familyService.updateFamilyContact(anyLong(), anyLong(), any(Map.class)))
                .thenReturn(updatedContact);

        mockMvc.perform(put("/api/family/contacts/1")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Contact updated successfully"))
                .andExpect(jsonPath("$.contact.name").value("张三（更新）"));
    }

    /**
     * 测试删除联系人 - 成功情况
     */
    @Test
    void testDeleteFamilyContact_Success() throws Exception {
        when(familyService.deleteFamilyContact(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/family/contacts/1")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Contact deleted successfully"));
    }

    /**
     * 测试删除联系人 - 联系人不存在
     */
    @Test
    void testDeleteFamilyContact_NotFound() throws Exception {
        when(familyService.deleteFamilyContact(anyLong(), anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/family/contacts/999")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试发送消息 - 成功情况
     */
    @Test
    void testSendMessageToFamily_Success() throws Exception {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", "你好，我很好！");
        messageData.put("type", "general");

        when(familyService.sendMessageToFamily(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(true);

        mockMvc.perform(post("/api/family/contacts/1/message")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Message sent successfully"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 测试发送消息 - 消息内容为空
     */
    @Test
    void testSendMessageToFamily_EmptyMessage() throws Exception {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", "");
        messageData.put("type", "general");

        mockMvc.perform(post("/api/family/contacts/1/message")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Message content is required"));
    }

    /**
     * 测试获取紧急联系人 - 成功情况
     */
    @Test
    void testGetEmergencyContacts_Success() throws Exception {
        List<FamilyContact> emergencyContacts = new ArrayList<>();
        emergencyContacts.add(testContact);

        when(familyService.getEmergencyContacts(anyLong())).thenReturn(emergencyContacts);

        mockMvc.perform(get("/api/family/emergency-contacts")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.emergencyContacts").isArray())
                .andExpect(jsonPath("$.count").value(1));
    }

    /**
     * 测试获取家庭统计信息 - 成功情况
     */
    @Test
    void testGetFamilyStats_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", 5);
        stats.put("emergencyContacts", 2);
        stats.put("activeContacts", 4);

        when(familyService.getFamilyStats(anyLong())).thenReturn(stats);

        mockMvc.perform(get("/api/family/stats")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats.totalContacts").value(5))
                .andExpect(jsonPath("$.stats.emergencyContacts").value(2))
                .andExpect(jsonPath("$.stats.activeContacts").value(4));
    }

    /**
     * 测试所有端点 - 缺少认证
     */
    @Test
    void testAllEndpoints_NoAuth() throws Exception {
        // 测试所有需要认证的端点
        String[] endpoints = {
            "/api/family/contacts",
            "/api/family/contacts/1",
            "/api/family/emergency-contacts",
            "/api/family/stats"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Authentication required"));
        }

        // 测试DELETE端点
        mockMvc.perform(delete("/api/family/contacts/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    /**
     * 测试服务异常处理
     */
    @Test
    void testServiceExceptionHandling() throws Exception {
        when(familyService.getFamilyContacts(anyLong()))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/family/contacts")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error fetching family contacts: Database connection failed"));
    }
} 