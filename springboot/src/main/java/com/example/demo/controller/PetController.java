package com.example.demo.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.PetMoodService;
import com.example.demo.pojo.PetMood;

@RestController
@RequestMapping("/api/pet")
@CrossOrigin(origins = "*")
public class PetController {

    @Autowired
    private PetMoodService petMoodService;

    // In-memory storage for demo purposes (conversation history only)
    private List<Map<String, Object>> conversationHistory = new ArrayList<>();
    private long messageIdCounter = 1;

    /**
     * Get pet status and information
     * @param authHeader Authorization token
     * @return Pet status including happiness, health, energy
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPetStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // Retrieve pet status using PetMoodService
            Map<String, Object> petStatus = petMoodService.getFullPetStatus(userId);
            
            // Check if the pet has been neglected
            checkNeglect(userId);

            response.put("success", true);
            response.put("pet", petStatus);
            response.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving pet status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Interact with pet (feed, play, care, talk)
     * @param interactionData Interaction type and parameters
     * @param authHeader Authorization token
     * @return Pet response and updated status
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
            
            // Process interaction via PetMoodService helper
            Map<String, Object> interactionResult = processInteraction(interactionType, message, userId);
            
            // Get updated pet status after interaction
            Map<String, Object> petStatus = petMoodService.getFullPetStatus(userId);
            
            response.put("success", true);
            response.put("pet", petStatus);
            response.put("interaction", interactionResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing interaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Send text message to pet
     * @param messageData Message content
     * @param authHeader Authorization token
     * @return Pet response
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessageToPet(
            @RequestBody Map<String, Object> messageData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String message = (String) messageData.get("message");
            String messageType = (String) messageData.getOrDefault("type", "text");
            
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message content is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Store user message
            Map<String, Object> userMessage = createMessage("user", message, messageType, userId);
            conversationHistory.add(userMessage);
            
            // Generate pet response
            Map<String, Object> petResponse = generatePetResponse(message, userId);
            conversationHistory.add(petResponse);
            
            response.put("success", true);
            response.put("userMessage", userMessage);
            response.put("petResponse", petResponse);
            response.put("conversationId", userMessage.get("id"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Voice interaction with pet
     * @param voiceData Voice message data
     * @param authHeader Authorization token
     * @return Pet voice response
     */
    @PostMapping("/voice")
    public ResponseEntity<Map<String, Object>> voiceInteraction(
            @RequestBody Map<String, Object> voiceData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String voiceContent = (String) voiceData.get("content");
            Integer duration = (Integer) voiceData.getOrDefault("duration", 5);
            String transcription = (String) voiceData.get("transcription"); // Future: real transcription
            
            // For now, simulate voice processing
            String processedText = transcription != null ? transcription : "Voice message received";
            
            // Store voice message
            Map<String, Object> voiceMessage = createMessage("user", processedText, "voice", userId);
            voiceMessage.put("duration", duration);
            conversationHistory.add(voiceMessage);
            
            // Generate pet voice response
            Map<String, Object> petVoiceResponse = generatePetVoiceResponse(processedText, userId);
            conversationHistory.add(petVoiceResponse);
            
            response.put("success", true);
            response.put("voiceMessage", voiceMessage);
            response.put("petResponse", petVoiceResponse);
            response.put("transcription", processedText);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing voice interaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check user's daily schedule and provide reminders
     * @param authHeader Authorization token
     * @return Schedule-based reminders and suggestions
     */
    @GetMapping("/schedule-check")
    public ResponseEntity<Map<String, Object>> checkScheduleReminders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // TODO: Integrate with ScheduleService to get real schedule data
            List<Map<String, Object>> reminders = generateScheduleReminders(userId);
            Map<String, Object> petSuggestion = generatePetScheduleSuggestion(reminders);
            
            response.put("success", true);
            response.put("reminders", reminders);
            response.put("petSuggestion", petSuggestion);
            response.put("checkTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Emergency detection and response
     * @param emergencyData Emergency situation data
     * @param authHeader Authorization token
     * @return Emergency response and actions
     */
    @PostMapping("/emergency")
    public ResponseEntity<Map<String, Object>> handleEmergency(
            @RequestBody Map<String, Object> emergencyData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String emergencyType = (String) emergencyData.get("type");
            String description = (String) emergencyData.get("description");
            String severity = (String) emergencyData.getOrDefault("severity", "medium");
            
            Map<String, Object> emergencyResponse = processEmergency(emergencyType, description, severity, userId);
            
            // Store emergency interaction
            Map<String, Object> emergencyMessage = createMessage("system", "Emergency detected: " + description, "emergency", userId);
            emergencyMessage.put("emergencyType", emergencyType);
            emergencyMessage.put("severity", severity);
            conversationHistory.add(emergencyMessage);
            
            response.put("success", true);
            response.put("emergency", emergencyResponse);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error handling emergency: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get conversation history
     * @param limit Number of messages to retrieve
     * @param authHeader Authorization token
     * @return Conversation history
     */
    @GetMapping("/conversation")
    public ResponseEntity<Map<String, Object>> getConversationHistory(
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            List<Map<String, Object>> userConversation = conversationHistory.stream()
                .filter(msg -> userId.equals(msg.get("userId")))
                .sorted((a, b) -> ((String) b.get("timestamp")).compareTo((String) a.get("timestamp")))
                .limit(limit)
                .toList();
            
            response.put("success", true);
            response.put("conversation", userConversation);
            response.put("totalMessages", userConversation.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update pet settings and preferences
     * @param settingsData Pet settings
     * @param authHeader Authorization token
     * @return Updated pet settings
     */
    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updatePetSettings(
            @RequestBody Map<String, Object> settingsData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // Get current pet status
            Map<String, Object> petStatus = petMoodService.getFullPetStatus(userId);
            
            // Update settings (can extend with more configurable fields)
            Map<String, Object> settings = new HashMap<>();
            if (settingsData.containsKey("name")) {
                settings.put("name", settingsData.get("name"));
            }
            if (settingsData.containsKey("personality")) {
                settings.put("personality", settingsData.get("personality"));
            }
            if (settingsData.containsKey("reminderFrequency")) {
                settings.put("reminderFrequency", settingsData.get("reminderFrequency"));
            }
            if (settingsData.containsKey("voiceEnabled")) {
                settings.put("voiceEnabled", settingsData.get("voiceEnabled"));
            }
            
            response.put("success", true);
            response.put("settings", settings);
            response.put("petStatus", petStatus);
            response.put("message", "Pet settings updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper Methods

    private Map<String, Object> processInteraction(String type, String message, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
    // Get current pet status
        PetMood currentPet = petMoodService.getOrInitPetMood(userId);
        int currentHappiness = currentPet.getHappiness();
        int currentHealth = currentPet.getHealth();
        int currentEnergy = currentPet.getEnergy();
        
        switch (type.toLowerCase()) {
            case "feed":
                currentHappiness = Math.min(100, currentHappiness + 10);
                currentHealth = Math.min(100, currentHealth + 5);
                result.put("response", "Meow! 🍖 Thank you for the delicious food! I feel much better now! *purrs happily*");
                result.put("animation", "eating");
                break;
                
            case "play":
                currentHappiness = Math.min(100, currentHappiness + 15);
                currentEnergy = Math.max(0, currentEnergy - 10);
                result.put("response", "Yay! 🎾 That was so much fun! I love playing with you! *bounces excitedly*");
                result.put("animation", "playing");
                break;
                
            case "care":
                currentHealth = Math.min(100, currentHealth + 15);
                currentHappiness = Math.min(100, currentHappiness + 5);
                result.put("response", "Purr... 💊 Thank you for taking care of me! I feel so much healthier now!");
                result.put("animation", "caring");
                break;
                
            case "talk":
                String[] talkResponses = {
                    "Meow! I'm so happy to see you! How are you feeling today? 😸",
                    "Purr... I love spending time with you! You're the best! 🐾",
                    "Meow meow! Did you remember to take your medicine today? 💊",
                    "I noticed you have some activities scheduled. Would you like me to remind you? 📅",
                    "Purr... You seem a bit tired. Maybe we should take a little break? 😴",
                    "Meow! I'm here if you need any help with your daily routine! 🤗"
                };
                result.put("response", talkResponses[new Random().nextInt(talkResponses.length)]);
                result.put("animation", "talking");
                break;
                
            default:
                result.put("response", "Meow? I'm not sure what you want me to do. Try feeding, playing, caring, or talking to me!");
                result.put("animation", "confused");
        }
        
    // Persist updated pet attributes via service
        petMoodService.updatePetAttributes(userId, currentHappiness, currentHealth, currentEnergy);
        
    // Increase experience points
        petMoodService.addExperience(userId, 5);
        
        result.put("type", type);
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return result;
    }

    private void checkNeglect(Long userId) {
        try {
            PetMood pet = petMoodService.getOrInitPetMood(userId);
            LocalDateTime lastInteraction = pet.getLastInteraction();
            
            if (lastInteraction != null) {
                long hoursSinceLastInteraction = Duration.between(lastInteraction, LocalDateTime.now()).toHours();
                
                if (hoursSinceLastInteraction >= 3) {
                    // Decrease happiness when neglected
                    int currentHappiness = pet.getHappiness();
                    int newHappiness = Math.max(0, currentHappiness - 10);
                    
                    // Update pet attributes after penalty
                    petMoodService.updatePetAttributes(userId, newHappiness, pet.getHealth(), pet.getEnergy());
                    
                    // Send a sad message to the conversation history
                    Map<String, Object> sadMessage = createMessage(
                        "pet",
                        "Meow... I feel lonely, can you be with me? 😢",
                        "text",
                        userId
                    );
                    conversationHistory.add(sadMessage);
                }
            }
        } catch (Exception e) {
            // Swallow errors to avoid impacting main flow
            System.err.println("Error checking neglect: " + e.getMessage());
        }
    }

    private Map<String, Object> createMessage(String sender, String content, String type, Long userId) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", messageIdCounter++);
        message.put("sender", sender);
        message.put("content", content);
        message.put("type", type);
        message.put("userId", userId);
        message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return message;
    }

    private Map<String, Object> generatePetResponse(String userMessage, Long userId) {
        String lowerMessage = userMessage.toLowerCase();
        String response;
        
    // Improved sentiment detection logic - check negatives first
        boolean isNegative = lowerMessage.contains("not ") || lowerMessage.contains("no ") || 
                           lowerMessage.contains("don't ") || lowerMessage.contains("doesn't ") ||
                           lowerMessage.contains("isn't ") || lowerMessage.contains("aren't ") ||
                           lowerMessage.contains("can't ") || lowerMessage.contains("won't ") ||
                           lowerMessage.contains("bad") || lowerMessage.contains("terrible") ||
                           lowerMessage.contains("awful") || lowerMessage.contains("horrible");
        
        // Health-related responses
        if (lowerMessage.contains("health") || lowerMessage.contains("medicine") || lowerMessage.contains("doctor")) {
            response = "Meow! 🏥 Health is very important! I can help remind you about your medications and appointments. Have you taken your medicine today?";
        }
        // Schedule-related responses
        else if (lowerMessage.contains("schedule") || lowerMessage.contains("appointment") || lowerMessage.contains("reminder")) {
            response = "Purr! 📅 I love helping with schedules! I can see you have some activities planned. Would you like me to remind you about them?";
        }
    // Negative emotional response (priority)
        else if (isNegative || lowerMessage.contains("sad") || lowerMessage.contains("lonely") || 
                lowerMessage.contains("tired") || lowerMessage.contains("depressed") || 
                lowerMessage.contains("anxious") || lowerMessage.contains("worried") ||
                lowerMessage.contains("angry") || lowerMessage.contains("frustrated") ||
                lowerMessage.contains("upset") || lowerMessage.contains("disappointed")) {
            response = "Meow... 😢 I'm sorry you're feeling this way! I'm here for you and I care about you very much. Would you like to talk about what's bothering you? I'm a good listener! 🤗";
        }
    // Positive emotional response only when no negation detected
        else if (!isNegative && (lowerMessage.contains("happy") || lowerMessage.contains("good") || 
                lowerMessage.contains("great") || lowerMessage.contains("wonderful") || 
                lowerMessage.contains("excellent") || lowerMessage.contains("fantastic"))) {
            response = "Purr! 😸 I'm so happy to hear that! Your happiness makes me happy too! Let's keep this positive energy going!";
        }
        // Greeting responses
        else if (lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey")) {
            response = "Meow! 👋 Hello there! I'm so excited to chat with you! How are you feeling today?";
        }
        // Default responses
        else {
            String[] defaultResponses = {
                "Meow! That's interesting! Tell me more! 😸",
                "Purr... I love chatting with you! You always have such nice things to say! 🐾",
                "Meow meow! I may be a virtual pet, but my care for you is very real! 💕",
                "That sounds important! I'm always here to listen and help however I can! 🤗",
                "Purr... You know, talking with you always brightens my day! 🌟"
            };
            response = defaultResponses[new Random().nextInt(defaultResponses.length)];
        }
        
        return createMessage("pet", response, "text", userId);
    }

    private Map<String, Object> generatePetVoiceResponse(String transcription, Long userId) {
        String[] voiceResponses = {
            "Meow! I heard you loud and clear! Your voice always makes me happy! 🐱",
            "Purr... I love hearing your voice! You sound wonderful today! 😻",
            "Meow meow! I wish I could understand every word, but I can feel your love! 💕",
            "Your voice is so soothing! It reminds me to tell you - don't forget your daily activities! 📋",
            "Purr... I heard concern in your voice. Is everything okay? I'm here for you! 🤗"
        };
        
        String response = voiceResponses[new Random().nextInt(voiceResponses.length)];
        Map<String, Object> voiceResponse = createMessage("pet", response, "voice", userId);
        voiceResponse.put("audioUrl", "/api/pet/tts/" + voiceResponse.get("id")); // Future: real TTS
        voiceResponse.put("duration", 3);
        
        return voiceResponse;
    }

    private List<Map<String, Object>> generateScheduleReminders(Long userId) {
        // TODO: Integrate with real schedule data
        List<Map<String, Object>> reminders = new ArrayList<>();
        
        Map<String, Object> reminder1 = new HashMap<>();
        reminder1.put("type", "medication");
        reminder1.put("title", "Morning Medication");
        reminder1.put("time", "08:00");
        reminder1.put("priority", "high");
        reminder1.put("message", "Don't forget your morning medication!");
        reminders.add(reminder1);
        
        Map<String, Object> reminder2 = new HashMap<>();
        reminder2.put("type", "activity");
        reminder2.put("title", "Morning Walk");
        reminder2.put("time", "08:30");
        reminder2.put("priority", "medium");
        reminder2.put("message", "Time for your morning walk in the park!");
        reminders.add(reminder2);
        
        return reminders;
    }

    private Map<String, Object> generatePetScheduleSuggestion(List<Map<String, Object>> reminders) {
        Map<String, Object> suggestion = new HashMap<>();
        
        if (!reminders.isEmpty()) {
            suggestion.put("message", "Meow! 📋 I see you have some important activities coming up! Would you like me to remind you about them?");
            suggestion.put("type", "schedule_reminder");
            suggestion.put("urgency", "normal");
            suggestion.put("reminderCount", reminders.size());
        } else {
            suggestion.put("message", "Purr... 😌 Your schedule looks clear right now! How about we spend some quality time together?");
            suggestion.put("type", "free_time");
            suggestion.put("urgency", "low");
            suggestion.put("reminderCount", 0);
        }
        
        suggestion.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return suggestion;
    }

    private Map<String, Object> processEmergency(String type, String description, String severity, Long userId) {
        Map<String, Object> emergency = new HashMap<>();
        
        emergency.put("type", type);
        emergency.put("description", description);
        emergency.put("severity", severity);
        emergency.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Generate appropriate response based on emergency type
        String response;
        List<String> actions = new ArrayList<>();
        
        switch (type.toLowerCase()) {
            case "health":
                response = "Meow! 🚨 I'm concerned about your health! Let me help you right away!";
                actions.add("Contact emergency services (911)");
                actions.add("Call your emergency contact");
                actions.add("Provide first aid guidance");
                break;
                
            case "fall":
                response = "Oh no! 😿 I detected you might have fallen! Are you okay? I'm getting help!";
                actions.add("Emergency services contacted");
                actions.add("Family members notified");
                actions.add("Location shared with responders");
                break;
                
            case "medication":
                response = "Meow! 💊 There seems to be an issue with your medication! Let me help you sort this out!";
                actions.add("Review medication schedule");
                actions.add("Contact healthcare provider");
                actions.add("Check for drug interactions");
                break;
                
            default:
                response = "Meow! 🆘 I sense something might be wrong! I'm here to help you through this!";
                actions.add("Assess the situation");
                actions.add("Contact appropriate help");
                actions.add("Stay with you for support");
        }
        
        emergency.put("petResponse", response);
        emergency.put("recommendedActions", actions);
        emergency.put("status", "processing");
        
        return emergency;
    }

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 0L; // Guest user
        }
        
        String token = authHeader.substring(7);
        if (token.startsWith("demo-token-")) {
            return 1L; // Demo user
        }
        
        return 0L; // Default to guest
    }
} 