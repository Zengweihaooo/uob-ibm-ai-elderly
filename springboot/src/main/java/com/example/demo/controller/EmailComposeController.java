package com.example.demo.controller;

import com.example.demo.service.EmailComposeService;
import com.example.demo.pojo.Email;
import com.example.demo.pojo.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for email composition and sending
 * 
 * @author AI Assistant
 * @version 1.0
 */
@Controller
@RequestMapping("/email")
public class EmailComposeController {
    
    @Autowired
    private EmailComposeService emailComposeService;
    
    /**
     * Show email compose page
     */
    @GetMapping("/compose")
    public String showComposePage(Model model) {
        // Add some initial data for demo purposes
        model.addAttribute("stats", emailComposeService.getEmailStats());
        return "email-compose";
    }
    
    /**
     * Send email API endpoint
     */
    @PostMapping("/api/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEmail(
            @RequestParam("fromEmail") String fromEmail,
            @RequestParam("toEmail") String toEmail,
            @RequestParam("subject") String subject,
            @RequestParam("content") String content,
            @RequestParam(value = "senderName", required = false) String senderName) {
        
        Map<String, Object> result = emailComposeService.sendEmail(fromEmail, toEmail, subject, content);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Save draft API endpoint
     */
    @PostMapping("/api/draft")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveDraft(
            @RequestParam("fromEmail") String fromEmail,
            @RequestParam("toEmail") String toEmail,
            @RequestParam("subject") String subject,
            @RequestParam("content") String content) {
        
        Map<String, Object> result = emailComposeService.saveDraft(fromEmail, toEmail, subject, content);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get contacts for a user
     */
    @GetMapping("/api/contacts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getContacts(@RequestParam("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Contact> contacts = emailComposeService.getUserContacts(userId);
            response.put("success", true);
            response.put("contacts", contacts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get contacts: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Add new contact
     */
    @PostMapping("/api/contacts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addContact(
            @RequestParam("userId") Long userId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "relationship", required = false) String relationship) {
        
        Map<String, Object> result = emailComposeService.addContact(userId, name, email, phone, relationship);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Search contacts
     */
    @GetMapping("/api/contacts/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchContacts(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "q", required = false) String searchTerm) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Contact> contacts = emailComposeService.searchContacts(userId, searchTerm);
            response.put("success", true);
            response.put("contacts", contacts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to search contacts: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete contact
     */
    @DeleteMapping("/api/contacts/{contactId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long contactId) {
        Map<String, Object> result = emailComposeService.deleteContact(contactId);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Get email history
     */
    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmailHistory(
            @RequestParam("fromEmail") String fromEmail,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Email> emails = emailComposeService.getEmailHistory(fromEmail, limit);
            response.put("success", true);
            response.put("emails", emails);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get email history: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get email statistics
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmailStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Integer> stats = emailComposeService.getEmailStats();
            response.put("success", true);
            response.put("stats", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
