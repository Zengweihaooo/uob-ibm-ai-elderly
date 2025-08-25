package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.FamilyService;
import com.example.demo.util.UserContextUtil;

/**
 * REST Controller for family contacts (alternative path)
 * 
 * This controller provides an alternative API path for family contact operations
 * to maintain compatibility with frontend requests.
 * 
 * @author System Generated
 * @version 1.0
 */
@RestController
@RequestMapping("/api/family-contacts")
@CrossOrigin(origins = "*")
public class FamilyContactsController {

    @Autowired
    private FamilyService familyService;
    
    @Autowired
    private UserContextUtil userContextUtil;

    /**
     * Send message to family contact (alternative path)
     * 
     * @param contactId Contact ID
     * @param messageData Message content
     * @param authHeader Authorization header
     * @return Response with success status
     */
    @PostMapping("/{contactId}/message")
    public ResponseEntity<Map<String, Object>> sendMessageToFamilyContact(
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
            // Extract userId from JWT token
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

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
            response.put("contactId", contactId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
