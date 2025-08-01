package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.FamilyContact;
import com.example.demo.service.FamilyService;

/**
 * REST Controller for family contact management
 * 
 * This controller handles family contact operations including adding, updating,
 * deleting contacts, and sending messages to family members.
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
@RestController
@RequestMapping("/api/family")
@CrossOrigin(origins = "*")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    /**
     * Add a new family contact
     * 
     * @param contactData Contact information from request body
     * @param authHeader Authorization header
     * @return Created contact or error message
     */
    @PostMapping("/contacts")
    public ResponseEntity<Map<String, Object>> addFamilyContact(
            @RequestBody Map<String, Object> contactData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            // Extract contact data
            String name = (String) contactData.get("name");
            String phoneNumber = (String) contactData.get("phoneNumber");
            String email = (String) contactData.get("email");
            String relationship = (String) contactData.get("relationship");
            String notificationPreference = (String) contactData.getOrDefault("notificationPreference", "ALL");
            Boolean isEmergencyContact = (Boolean) contactData.getOrDefault("isEmergencyContact", false);
            String notes = (String) contactData.get("notes");

            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Contact name is required");
                return ResponseEntity.badRequest().body(response);
            }

            if ((phoneNumber == null || phoneNumber.trim().isEmpty()) && 
                (email == null || email.trim().isEmpty())) {
                response.put("success", false);
                response.put("message", "Either phone number or email is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Create and save contact
            FamilyContact contact = familyService.addFamilyContact(
                userId, name, phoneNumber, email, relationship, 
                notificationPreference, isEmergencyContact, notes
            );

            response.put("success", true);
            response.put("message", "Family contact added successfully");
            response.put("contact", contact);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add family contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all family contacts for a user
     * 
     * @param authHeader Authorization header
     * @return List of family contacts
     */
    @GetMapping("/contacts")
    public ResponseEntity<Map<String, Object>> getFamilyContacts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            List<FamilyContact> contacts = familyService.getFamilyContacts(userId);
            
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("totalCount", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching family contacts: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get a specific family contact by ID
     * 
     * @param contactId Contact ID
     * @param authHeader Authorization header
     * @return Contact details
     */
    @GetMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> getFamilyContact(
            @PathVariable Long contactId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            FamilyContact contact = familyService.getFamilyContact(userId, contactId);
            
            if (contact == null) {
                response.put("success", false);
                response.put("message", "Contact not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("contact", contact);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update a family contact
     * 
     * @param contactId Contact ID
     * @param contactData Updated contact data
     * @param authHeader Authorization header
     * @return Updated contact
     */
    @PutMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> updateFamilyContact(
            @PathVariable Long contactId,
            @RequestBody Map<String, Object> contactData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            FamilyContact updatedContact = familyService.updateFamilyContact(
                userId, contactId, contactData
            );

            if (updatedContact == null) {
                response.put("success", false);
                response.put("message", "Contact not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("message", "Contact updated successfully");
            response.put("contact", updatedContact);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete a family contact
     * 
     * @param contactId Contact ID
     * @param authHeader Authorization header
     * @return Success message
     */
    @DeleteMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> deleteFamilyContact(
            @PathVariable Long contactId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            boolean deleted = familyService.deleteFamilyContact(userId, contactId);

            if (!deleted) {
                response.put("success", false);
                response.put("message", "Contact not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("message", "Contact deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Send message to family contact
     * 
     * @param contactId Contact ID
     * @param messageData Message content
     * @param authHeader Authorization header
     * @return Message sending result
     */
    @PostMapping("/contacts/{contactId}/message")
    public ResponseEntity<Map<String, Object>> sendMessageToFamily(
            @PathVariable Long contactId,
            @RequestBody Map<String, Object> messageData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            String message = (String) messageData.get("message");
            String messageType = (String) messageData.getOrDefault("type", "general");

            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message content is required");
                return ResponseEntity.badRequest().body(response);
            }

            boolean sent = familyService.sendMessageToFamily(userId, contactId, message, messageType);

            if (!sent) {
                response.put("success", false);
                response.put("message", "Contact not found or message could not be sent");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("message", "Message sent successfully");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get emergency contacts
     * 
     * @param authHeader Authorization header
     * @return List of emergency contacts
     */
    @GetMapping("/emergency-contacts")
    public ResponseEntity<Map<String, Object>> getEmergencyContacts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            List<FamilyContact> emergencyContacts = familyService.getEmergencyContacts(userId);
            
            response.put("success", true);
            response.put("emergencyContacts", emergencyContacts);
            response.put("count", emergencyContacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching emergency contacts: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get family contact statistics
     * 
     * @param authHeader Authorization header
     * @return Contact statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFamilyStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            Map<String, Object> stats = familyService.getFamilyStats(userId);
            
            response.put("success", true);
            response.put("stats", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching family statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}