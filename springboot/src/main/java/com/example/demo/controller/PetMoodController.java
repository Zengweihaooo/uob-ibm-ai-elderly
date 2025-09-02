package com.example.demo.controller;

import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PetMoodService;
import com.example.demo.pojo.PetMood;

/**
 * Pet Mood Management Controller
 * Provides REST API endpoints related to virtual pet mood and attributes.
 */
@RestController
@RequestMapping("/api/pet/mood")
@CrossOrigin(origins = "*")
public class PetMoodController {
    
    private final PetMoodService moodService;
    
    @Autowired
    public PetMoodController(PetMoodService moodService) {
        this.moodService = moodService;
    }

    /**
     * Get pet mood state.
     * @param userId user ID
     * @return mood score and state string
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getMoodState(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int score = moodService.getMood(userId);
            String state = moodService.moodState(score);
            
            response.put("success", true);
            response.put("score", score);
            response.put("state", state);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving mood state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Adjust pet mood score.
     * @param userId user ID
     * @param requestBody request body containing delta
     * @return adjusted score and derived state
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, Object>> adjustMood(
            @RequestParam Long userId, 
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object deltaObj = requestBody.getOrDefault("delta", 0);
            int delta;
            
            if (deltaObj instanceof Number) {
                delta = ((Number) deltaObj).intValue();
            } else {
                delta = Integer.parseInt(deltaObj.toString());
            }
            
            int score = moodService.adjustMood(userId, delta);
            String state = moodService.moodState(score);
            
            response.put("success", true);
            response.put("score", score);
            response.put("state", state);
            response.put("delta", delta);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adjusting mood: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get full pet status (happiness, health, energy, exp, level, etc.).
     * @param userId user ID
     * @return full pet status map
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFullPetStatus(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> status = moodService.getFullPetStatus(userId);
            
            response.put("success", true);
            response.put("petStatus", status);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving pet status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update pet attributes.
     * @param userId user ID
     * @param requestBody request body with happiness, health, energy
     * @return updated pet status
     */
    @PostMapping("/attributes")
    public ResponseEntity<Map<String, Object>> updatePetAttributes(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int happiness = getIntValue(requestBody, "happiness", 85);
            int health = getIntValue(requestBody, "health", 92);
            int energy = getIntValue(requestBody, "energy", 78);
            
            PetMood updatedPet = moodService.updatePetAttributes(userId, happiness, health, energy);
            
            response.put("success", true);
            response.put("pet", updatedPet);
            response.put("message", "Pet attributes updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating pet attributes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Add experience points.
     * @param userId user ID
     * @param requestBody request body containing exp value
     * @return updated total experience
     */
    @PostMapping("/experience")
    public ResponseEntity<Map<String, Object>> addExperience(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int exp = getIntValue(requestBody, "exp", 10);
            int newExp = moodService.addExperience(userId, exp);
            
            response.put("success", true);
            response.put("experience", newExp);
            response.put("addedExp", exp);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding experience: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Reset pet mood (testing / manual reset helper).
     * @param userId user ID
     * @return reset pet state
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPetMood(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Reset to default values
            PetMood resetPet = moodService.updatePetAttributes(userId, 85, 92, 78);
            
            response.put("success", true);
            response.put("pet", resetPet);
            response.put("message", "Pet mood reset to default values");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resetting pet mood: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Clear cache (maintenance endpoint).
     * @param userId optional user ID; if absent clears all caches
     * @return operation result
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, Object>> clearCache(
            @RequestParam(required = false) Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userId != null) {
                moodService.clearCache(userId);
                response.put("message", "Cache cleared for user: " + userId);
            } else {
                moodService.clearAllCache();
                response.put("message", "All caches cleared");
            }
            
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error clearing cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Safely extract an int value from request body.
     * @param requestBody request body map
     * @param key key name
     * @param defaultValue fallback default value
     * @return parsed int value or default
     */
    private int getIntValue(Map<String, Object> requestBody, String key, int defaultValue) {
        Object value = requestBody.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
