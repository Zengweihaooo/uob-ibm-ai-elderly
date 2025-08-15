package com.example.demo.service;

import com.example.demo.mapper.EmailMapper;
import com.example.demo.mapper.ContactMapper;
import com.example.demo.pojo.Email;
import com.example.demo.pojo.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for email composition and sending
 * 
 * @author AI Assistant
 * @version 1.0
 */
@Service
public class EmailComposeService {
    
    @Autowired
    private EmailMapper emailMapper;
    
    @Autowired
    private ContactMapper contactMapper;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Send an email
     */
    public Map<String, Object> sendEmail(String fromEmail, String toEmail, String subject, String content) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create email record
            Email email = new Email(fromEmail, toEmail, subject, content);
            emailMapper.insert(email);
            
            // Send the email
            emailService.sendCustomEmail(toEmail, subject, content, fromEmail);
            
            // Update email status to SENT
            email.markAsSent();
            emailMapper.updateStatus(email);
            
            result.put("success", true);
            result.put("message", "Email sent successfully to " + toEmail);
            result.put("emailId", email.getId());
            
        } catch (Exception e) {
            // Update email status to FAILED if record exists
            result.put("success", false);
            result.put("message", "Failed to send email: " + e.getMessage());
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Save email as draft
     */
    public Map<String, Object> saveDraft(String fromEmail, String toEmail, String subject, String content) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Email email = new Email(fromEmail, toEmail, subject, content);
            email.setStatus(Email.EmailStatus.DRAFT);
            emailMapper.insert(email);
            
            result.put("success", true);
            result.put("message", "Draft saved successfully");
            result.put("emailId", email.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to save draft: " + e.getMessage());
            System.err.println("Failed to save draft: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get contacts for a user
     */
    public List<Contact> getUserContacts(Long userId) {
        try {
            return contactMapper.findByUserId(userId);
        } catch (Exception e) {
            System.err.println("Failed to get contacts for user " + userId + ": " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Add a new contact
     */
    public Map<String, Object> addContact(Long userId, String name, String email, String phone, String relationship) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if contact already exists
            Contact existingContact = contactMapper.findByUserIdAndEmail(userId, email);
            if (existingContact != null) {
                result.put("success", false);
                result.put("message", "Contact with this email already exists");
                return result;
            }
            
            Contact contact = new Contact(userId, name, email, relationship);
            contact.setPhone(phone);
            contactMapper.insert(contact);
            
            result.put("success", true);
            result.put("message", "Contact added successfully");
            result.put("contact", contact);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to add contact: " + e.getMessage());
            System.err.println("Failed to add contact: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get email history for a user
     */
    public List<Email> getEmailHistory(String fromEmail, int limit) {
        try {
            return emailMapper.findRecentByFromEmail(fromEmail, limit);
        } catch (Exception e) {
            System.err.println("Failed to get email history: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get email statistics
     */
    public Map<String, Integer> getEmailStats() {
        Map<String, Integer> stats = new HashMap<>();
        
        try {
            stats.put("sent", emailMapper.countByStatus("SENT"));
            stats.put("failed", emailMapper.countByStatus("FAILED"));
            stats.put("draft", emailMapper.countByStatus("DRAFT"));
            
        } catch (Exception e) {
            System.err.println("Failed to get email statistics: " + e.getMessage());
            stats.put("sent", 0);
            stats.put("failed", 0);
            stats.put("draft", 0);
        }
        
        return stats;
    }
    
    /**
     * Search contacts by name
     */
    public List<Contact> searchContacts(Long userId, String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getUserContacts(userId);
            }
            return contactMapper.searchByUserIdAndName(userId, searchTerm.trim());
        } catch (Exception e) {
            System.err.println("Failed to search contacts: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Delete contact
     */
    public Map<String, Object> deleteContact(Long contactId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleted = contactMapper.deleteById(contactId);
            if (deleted > 0) {
                result.put("success", true);
                result.put("message", "Contact deleted successfully");
            } else {
                result.put("success", false);
                result.put("message", "Contact not found");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to delete contact: " + e.getMessage());
            System.err.println("Failed to delete contact: " + e.getMessage());
        }
        
        return result;
    }
}
