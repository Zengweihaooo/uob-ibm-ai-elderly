package com.example.demo.controller;

import com.example.demo.pojo.Pet;
import com.example.demo.service.PetEmotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pet-emotion")
@CrossOrigin(origins = "*")
public class PetEmotionController {

    @Autowired
    private PetEmotionService petEmotionService;

    // In-memory storage for demo purposes (in production, use database)
    private Map<Long, Pet> pets = new HashMap<>();

    /**
     * Get pet's current emotional state
     * @param authHeader Authorization token
     * @return Pet's emotional state and expressions
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getPetEmotionalState(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            Pet pet = getPetForUser(userId);
            
            // Update pet's emotional state (no interaction)
            petEmotionService.updatePetEmotion(pet, false);
            
            // Get emotional state summary
            Map<String, Object> emotionalState = petEmotionService.getEmotionalState(pet);
            
            response.put("success", true);
            response.put("pet", emotionalState);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving pet emotional state: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Interact with pet and get emotional response
     * @param interactionData Interaction details
     * @param authHeader Authorization token
     * @return Pet's response and updated emotional state
     */
    @PostMapping("/interact")
    public ResponseEntity<Map<String, Object>> interactWithPet(
            @RequestBody Map<String, Object> interactionData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String interactionType = (String) interactionData.get("type");
            String message = (String) interactionData.get("message");
            
            if (interactionType == null) {
                response.put("success", false);
                response.put("message", "Interaction type is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Pet pet = getPetForUser(userId);
            
            // Process interaction and get response
            Map<String, Object> interactionResponse = petEmotionService.processInteraction(pet, interactionType);
            
            // Update pet's emotional state (with interaction)
            petEmotionService.updatePetEmotion(pet, true);
            
            response.put("success", true);
            response.put("interaction", interactionResponse);
            response.put("pet", petEmotionService.getEmotionalState(pet));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing interaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get pet's current expressions (sound and visual)
     * @param authHeader Authorization token
     * @return Current expressions
     */
    @GetMapping("/expressions")
    public ResponseEntity<Map<String, Object>> getPetExpressions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            Pet pet = getPetForUser(userId);
            
            // Update pet's emotional state
            petEmotionService.updatePetEmotion(pet, false);
            
            Map<String, Object> expressions = new HashMap<>();
            expressions.put("sound", pet.getCurrentSound());
            expressions.put("isMakingSound", pet.isMakingSound());
            expressions.put("visualExpression", pet.getVisualExpression());
            expressions.put("isExpressingEmotion", pet.isExpressingEmotion());
            expressions.put("movement", pet.getMovementType());
            expressions.put("isMoving", pet.isMoving());
            expressions.put("location", pet.getCurrentLocation());
            
            response.put("success", true);
            response.put("expressions", expressions);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving pet expressions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Check if pet needs attention
     * @param authHeader Authorization token
     * @return Attention status
     */
    @GetMapping("/attention-check")
    public ResponseEntity<Map<String, Object>> checkPetAttention(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            Pet pet = getPetForUser(userId);
            
            // Update pet's emotional state
            petEmotionService.updatePetEmotion(pet, false);
            
            Map<String, Object> attentionStatus = new HashMap<>();
            attentionStatus.put("needsAttention", pet.isNeedsAttention());
            attentionStatus.put("neglectLevel", pet.getNeglectLevel());
            attentionStatus.put("lastAttentionTime", pet.getLastAttentionTime());
            attentionStatus.put("emotion", pet.getEmotion());
            attentionStatus.put("happiness", pet.getHappiness());
            
            // Generate attention message
            String attentionMessage = generateAttentionMessage(pet);
            attentionStatus.put("message", attentionMessage);
            
            response.put("success", true);
            response.put("attentionStatus", attentionStatus);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking pet attention: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get pet's movement and location status
     * @param authHeader Authorization token
     * @return Movement and location information
     */
    @GetMapping("/movement")
    public ResponseEntity<Map<String, Object>> getPetMovement(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            Pet pet = getPetForUser(userId);
            
            // Update pet's emotional state
            petEmotionService.updatePetEmotion(pet, false);
            
            Map<String, Object> movementInfo = new HashMap<>();
            movementInfo.put("isMoving", pet.isMoving());
            movementInfo.put("movementType", pet.getMovementType());
            movementInfo.put("currentLocation", pet.getCurrentLocation());
            movementInfo.put("energy", pet.getEnergy());
            movementInfo.put("emotion", pet.getEmotion());
            
            // Generate movement description
            String movementDescription = generateMovementDescription(pet);
            movementInfo.put("description", movementDescription);
            
            response.put("success", true);
            response.put("movement", movementInfo);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving pet movement: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Force pet to stay in place (prevent random movement)
     * @param authHeader Authorization token
     * @return Updated movement status
     */
    @PostMapping("/stay")
    public ResponseEntity<Map<String, Object>> makePetStay(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            Pet pet = getPetForUser(userId);
            
            // Force pet to stay in place
            pet.setMoving(false);
            pet.setMovementType("sitting");
            
            // Update emotional state
            petEmotionService.updatePetEmotion(pet, false);
            
            response.put("success", true);
            response.put("message", pet.getName() + " is now staying in place.");
            response.put("movement", Map.of(
                "isMoving", pet.isMoving(),
                "movementType", pet.getMovementType(),
                "currentLocation", pet.getCurrentLocation()
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error making pet stay: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Helper methods
    private Pet getPetForUser(Long userId) {
        return pets.computeIfAbsent(userId, id -> createDefaultPet(id));
    }

    private Pet createDefaultPet(Long userId) {
        Pet pet = new Pet(userId, "Buddy", "dog", "Golden Retriever", 3);
        pet.setId(userId);
        pet.setLastAttentionTime(LocalDateTime.now());
        return pet;
    }

    private Long getUserIdFromToken(String authHeader) {
        // TODO: Extract real userId from JWT token
        // For demo purposes, return a default userId
        return 1L;
    }

    private String generateAttentionMessage(Pet pet) {
        if (pet.isNeedsAttention()) {
            return pet.getName() + " needs attention! The pet is feeling " + 
                   pet.getEmotion() + " and has a neglect level of " + pet.getNeglectLevel() + 
                   ". Please interact with " + pet.getName() + " soon.";
        } else {
            return pet.getName() + " is content and doesn't need immediate attention.";
        }
    }

    private String generateMovementDescription(Pet pet) {
        String name = pet.getName();
        String movementType = pet.getMovementType();
        String location = pet.getCurrentLocation();
        
        if (pet.isMoving()) {
            return name + " is " + movementType + " in the " + location + ".";
        } else {
            return name + " is staying in place in the " + location + ".";
        }
    }
} 