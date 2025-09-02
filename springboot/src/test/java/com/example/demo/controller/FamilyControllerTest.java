package com.example.demo.controller;

import com.example.demo.pojo.FamilyContact;
import com.example.demo.service.FamilyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FamilyController
 * 
 * Tests all API endpoints for family contact management
 * 
 * @author Weihao Zeng
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
        // Create test contact data
        testContact = new FamilyContact();
        testContact.setId(1L);
        testContact.setUserId(1L);
        testContact.setName("Zhang San");
        testContact.setPhone("13800138000");
        testContact.setEmail("zhangsan@example.com");
        testContact.setRelationship("son");
        testContact.setIsEmergencyContact(true);
        testContact.setIsActive(true);
        testContact.setCreatedAt(LocalDateTime.now());
        testContact.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Test adding a family contact - success case
     */
    @Test
    void testAddFamilyContact_Success() throws Exception {
        // Prepare test data
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("name", "Zhang San");
        contactData.put("phone", "13800138000");
        contactData.put("email", "zhangsan@example.com");
        contactData.put("relationship", "son");
        contactData.put("isEmergencyContact", true);

        // Mock service method
        when(familyService.addFamilyContact(
            anyLong(), anyString(), anyString(), anyString(), 
            anyString(), any(Boolean.class)
        )).thenReturn(testContact);

        // Execute test
        mockMvc.perform(post("/api/family/contacts")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Family contact added successfully"))
                .andExpect(jsonPath("$.contact.id").value(1))
                .andExpect(jsonPath("$.contact.name").value("Zhang San"));
    }

    /**
     * Test adding a family contact - missing authentication
     */
    @Test
    void testAddFamilyContact_NoAuth() throws Exception {
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("name", "Zhang San");
        contactData.put("phone", "13800138000");

        mockMvc.perform(post("/api/family/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactData)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    /**
     * Test adding a family contact - missing required fields
     */
    @Test
    void testAddFamilyContact_MissingRequiredFields() throws Exception {
        Map<String, Object> contactData = new HashMap<>();
        // Intentionally omit name field

        mockMvc.perform(post("/api/family/contacts")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contact name is required"));
    }

    /**
     * Test getting all family contacts - success case
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
     * Test getting a single family contact - success case
     */
    @Test
    void testGetFamilyContact_Success() throws Exception {
        when(familyService.getFamilyContact(anyLong(), anyLong())).thenReturn(testContact);

        mockMvc.perform(get("/api/family/contacts/1")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.contact.id").value(1))
                .andExpect(jsonPath("$.contact.name").value("Zhang San"));
    }

    /**
     * Test getting a single family contact - not found
     */
    @Test
    void testGetFamilyContact_NotFound() throws Exception {
        when(familyService.getFamilyContact(anyLong(), anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/family/contacts/999")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contact not found"));
    }

    /**
     * Test updating a family contact - success case
     */
    @Test
    void testUpdateFamilyContact_Success() throws Exception {
        // Prepare update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Zhang San (updated)");
        updateData.put("phone", "13900139000");
        updateData.put("email", "zhangsan_updated@example.com");
        updateData.put("relationship", "son");
        updateData.put("isEmergencyContact", true);

        // Create updated contact object
        FamilyContact updatedContact = new FamilyContact();
        updatedContact.setId(1L);
        updatedContact.setUserId(1L);
        updatedContact.setName("Zhang San (updated)");
        updatedContact.setPhone("13900139000");
        updatedContact.setEmail("zhangsan_updated@example.com");
        updatedContact.setRelationship("son");
        updatedContact.setIsEmergencyContact(true);
        updatedContact.setIsActive(true);
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
                .andExpect(jsonPath("$.message").value("Family contact updated successfully"))
                .andExpect(jsonPath("$.contact.name").value("Zhang San (updated)"));
    }

    /**
     * Test deleting a family contact - success case
     */
    @Test
    void testDeleteFamilyContact_Success() throws Exception {
        when(familyService.deleteFamilyContact(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/family/contacts/1")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Family contact deleted successfully"));
    }

    /**
     * Test deleting a family contact - not found
     */
    @Test
    void testDeleteFamilyContact_NotFound() throws Exception {
        when(familyService.deleteFamilyContact(anyLong(), anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/family/contacts/999")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Contact not found"));
    }

    /**
     * Test sending a message to a family contact - success case
     */
    @Test
    void testSendMessageToFamily_Success() throws Exception {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", "test message");
        messageData.put("type", "general");

        when(familyService.sendMessageToFamily(anyLong(), anyLong(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/family/contacts/1/message")
                .header("Authorization", validAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Message sent successfully"));
    }

    /**
     * Test sending a message to a family contact - empty message
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
     * Test getting emergency contacts - success case
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
                .andExpect(jsonPath("$.contacts").isArray())
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    /**
     * Test getting family statistics - success case
     */
    @Test
    void testGetFamilyStats_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", 3);
        stats.put("emergencyContacts", 2);
        stats.put("activeContacts", 3);

        when(familyService.getFamilyStats(anyLong())).thenReturn(stats);

        mockMvc.perform(get("/api/family/stats")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats.totalContacts").value(3))
                .andExpect(jsonPath("$.stats.emergencyContacts").value(2))
                .andExpect(jsonPath("$.stats.activeContacts").value(3));
    }

    /**
     * Test all endpoints - missing authentication
     */
    @Test
    void testAllEndpoints_NoAuth() throws Exception {
        // Verify all main endpoints return 401 without authentication
        mockMvc.perform(get("/api/family/contacts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/family/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/family/contacts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/family/contacts/1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test service exception handling
     */
    @Test
    void testServiceExceptionHandling() throws Exception {
        when(familyService.getFamilyContacts(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/family/contacts")
                .header("Authorization", validAuthHeader))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
} 