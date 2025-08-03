package com.example.demo.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; //新增 - 用于存储播客推荐和播放计划列表
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

import com.example.demo.service.PodcastAutoPlayService;
import com.example.demo.service.PodcastService;

@RestController
@RequestMapping("/api/pet")
@CrossOrigin(origins = "*")
public class PetController {

    // In-memory storage for demo purposes
    private Map<Long, Map<String, Object>> petData = new HashMap<>();
    private List<Map<String, Object>> conversationHistory = new ArrayList<>();
    private long messageIdCounter = 1;
    
    // ==================== 新增服务注入 ====================
    // 播客服务 - 用于获取播客推荐和搜索功能
    @Autowired
    private PodcastService podcastService;
    
    // 播客自动播放服务 - 用于管理播客的定时播放功能
    @Autowired
    private PodcastAutoPlayService podcastAutoPlayService;

    // ==================== 原有功能：宠物状态查询 ====================
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
            Map<String, Object> pet = getPetForUser(userId);
            
            // 检查是否被忽视（在这里调用）
            checkNeglect(userId);

            response.put("success", true);
            response.put("pet", pet);
            response.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving pet status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 原有功能：宠物互动 ====================
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
            
            Map<String, Object> pet = getPetForUser(userId);
            //Map<String, Object> interactionResult = processInteraction(interactionType, message, pet);
            Map<String, Object> interactionResult = processInteraction(interactionType, message, pet, userId);
            
            // Update pet data
            petData.put(userId, pet);
            
            response.put("success", true);
            response.put("pet", pet);
            response.put("interaction", interactionResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing interaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 原有功能：文本消息交互 ====================
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

    // ==================== 原有功能：语音交互 ====================
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

    // ==================== 原有功能：日程检查 ====================
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

    // ==================== 原有功能：紧急情况处理 ====================
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

    // ==================== 原有功能：对话历史 ====================
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

    // ==================== 原有功能：宠物设置 ====================
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
            Map<String, Object> pet = getPetForUser(userId);
            Map<String, Object> settings = (Map<String, Object>) pet.getOrDefault("settings", new HashMap<>());
            
            // Update settings
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
            
            pet.put("settings", settings);
            pet.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            petData.put(userId, pet);
            
            response.put("success", true);
            response.put("settings", settings);
            response.put("message", "Pet settings updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 原有辅助方法 ====================
    // Helper Methods

    private Map<String, Object> getPetForUser(Long userId) {
        return petData.computeIfAbsent(userId, k -> createDefaultPet());
    }

    private Map<String, Object> createDefaultPet() {
        Map<String, Object> pet = new HashMap<>();
        pet.put("name", "Whiskers");
        pet.put("type", "cat");
        pet.put("happiness", 85);
        pet.put("health", 92);
        pet.put("energy", 78);
        pet.put("mood", "😊");
        pet.put("status", "Happy & Healthy");
        pet.put("level", 1);
        pet.put("experience", 0);
        pet.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        pet.put("lastInteraction", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("name", "Whiskers");
        settings.put("personality", "caring");
        settings.put("reminderFrequency", "normal");
        settings.put("voiceEnabled", true);
        pet.put("settings", settings);
        
        return pet;
    }

    // ==================== 修改的方法：宠物互动处理 ====================
    //private Map<String, Object> processInteraction(String type, String message, Map<String, Object> pet) {
    private Map<String, Object> processInteraction(String type, String message, Map<String, Object> pet, Long userId){
        Map<String, Object> result = new HashMap<>();
        
        switch (type.toLowerCase()) {
            case "feed":
                pet.put("happiness", Math.min(100, (Integer) pet.get("happiness") + 10));
                pet.put("health", Math.min(100, (Integer) pet.get("health") + 5));
                result.put("response", "Meow! 🍖 Thank you for the delicious food! I feel much better now! *purrs happily*");
                result.put("animation", "eating");
                break;
                
            case "play":
                pet.put("happiness", Math.min(100, (Integer) pet.get("happiness") + 15));
                pet.put("energy", Math.max(0, (Integer) pet.get("energy") - 10));
                result.put("response", "Yay! 🎾 That was so much fun! I love playing with you! *bounces excitedly*");
                result.put("animation", "playing");
                break;
                
            case "care":
                pet.put("health", Math.min(100, (Integer) pet.get("health") + 15));
                pet.put("happiness", Math.min(100, (Integer) pet.get("happiness") + 5));
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
        
        // Update mood based on stats
        updatePetMood(pet, userId);
        pet.put("lastInteraction", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        result.put("type", type);
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return result;
    }
    // ==================== 修改的方法：宠物情绪更新 ====================
/* 
    private void updatePetMood(Map<String, Object> pet) {
        int happiness = (Integer) pet.get("happiness");
        int health = (Integer) pet.get("health");
        int energy = (Integer) pet.get("energy");
        int avgStats = (happiness + health + energy) / 3;
        
        String mood, status;
        
        if (avgStats > 80) {
            mood = "😊";
            status = "Very Happy!";
        } else if (avgStats > 60) {
            mood = "🙂";
            status = "Content";
        } else if (avgStats > 40) {
            mood = "😐";
            status = "Okay";
        } else {
            mood = "😢";
            status = "Needs Care";
        }
        
        pet.put("mood", mood);
        pet.put("status", status);
    }
*/

private void updatePetMood(Map<String, Object> pet, Long userId) {
    int happiness = (Integer) pet.get("happiness");
    int health = (Integer) pet.get("health");
    int energy = (Integer) pet.get("energy");
    int avgStats = (happiness + health + energy) / 3;

    String previousMood = (String) pet.getOrDefault("mood", "😊");
    String newMood;
    String status;

    if (avgStats > 80) {
        newMood = "😊";
        status = "Very Happy!";
    } else if (avgStats > 60) {
        newMood = "🙂";
        status = "Content";
    } else if (avgStats > 40) {
        newMood = "😐";
        status = "Okay";
    } else {
        newMood = "😢";
        status = "Needs Care";
    }

    pet.put("mood", newMood);
    pet.put("status", status);

    // 如果情绪从非悲伤状态变成“难过”，发送主动信息
    if (!previousMood.equals("😢") && newMood.equals("😢")) {
        Map<String, Object> sadMessage = createMessage(
            "pet",
            "Meow... I feel lonely, can you be with me? " + newMood,
            "text",
            userId
        );
        conversationHistory.add(sadMessage);
    }
}
    // ==================== 新增方法：忽视检查 ====================
    private void checkNeglect(Long userId) {
    Map<String, Object> pet = getPetForUser(userId);
    LocalDateTime lastInteraction = LocalDateTime.parse((String) pet.get("lastInteraction"));
    long hoursSinceLastInteraction = Duration.between(lastInteraction, LocalDateTime.now()).toHours();

    if (hoursSinceLastInteraction >= 3) { 
        pet.put("happiness", Math.max(0, (Integer) pet.get("happiness") - 10));
        updatePetMood(pet, userId); // 触发悲伤表情与消息
    }
}

    public Map<String, Object> createMessage(String sender, String content, String type, Long userId) {
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
        
        // Health-related responses
        if (lowerMessage.contains("health") || lowerMessage.contains("medicine") || lowerMessage.contains("doctor")) {
            response = "Meow! 🏥 Health is very important! I can help remind you about your medications and appointments. Have you taken your medicine today?";
        }
        // Schedule-related responses
        else if (lowerMessage.contains("schedule") || lowerMessage.contains("appointment") || lowerMessage.contains("reminder")) {
            response = "Purr! 📅 I love helping with schedules! I can see you have some activities planned. Would you like me to remind you about them?";
        }
        // Emotional responses
        else if (lowerMessage.contains("sad") || lowerMessage.contains("lonely") || lowerMessage.contains("tired")) {
            response = "Meow... 🤗 I'm here for you! You're never alone when I'm around. Let's do something fun together to cheer you up!";
        }
        // Happy responses
        else if (lowerMessage.contains("happy") || lowerMessage.contains("good") || lowerMessage.contains("great")) {
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

    // ==================== 新增播客推荐功能 ====================
    /**
     * 根据用户兴趣获取播客推荐（包含宠物响应）
     * 结合播客推荐和虚拟宠物的友好回应
     * 新增功能：为用户提供个性化的播客推荐，同时结合虚拟宠物的互动体验
     * 
     * @param interestsData 用户兴趣数据
     * @param authHeader 授权令牌
     * @return 播客推荐和宠物响应
     */
    @PostMapping("/podcast/recommendations")
    public ResponseEntity<Map<String, Object>> getPodcastRecommendations(
            @RequestBody Map<String, Object> interestsData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // 从请求体中提取用户兴趣列表
            @SuppressWarnings("unchecked")
            List<String> interests = (List<String>) interestsData.get("interests");
            
            // 验证兴趣列表是否为空
            if (interests == null || interests.isEmpty()) {
                response.put("success", false);
                response.put("message", "Interests list is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 调用播客服务获取推荐
            Map<String, Object> recommendationsResult = podcastService.getPodcastRecommendations(interests);
            
            if ((Boolean) recommendationsResult.get("success")) {
                // 获取用户宠物信息并生成宠物响应
                Map<String, Object> pet = getPetForUser(userId);
                String petResponse = generatePodcastRecommendationResponse(interests, recommendationsResult);
                
                // 构建成功响应
                response.put("success", true);
                response.put("recommendations", recommendationsResult.get("recommendations"));
                response.put("totalRecommendations", recommendationsResult.get("totalRecommendations"));
                response.put("interests", interests);
                response.put("petResponse", petResponse);
                response.put("pet", pet);
                
                return ResponseEntity.ok(response);
            } else {
                // 推荐失败，返回错误信息
                response.put("success", false);
                response.put("message", recommendationsResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting podcast recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 新增播客自动播放功能 ====================
    /**
     * 安排播客自动播放
     * 允许用户设置播客在指定时间自动播放
     * 新增功能：用户可以设置播客在特定时间自动播放，提供定时娱乐功能
     * 
     * @param scheduleData 播放计划数据
     * @param authHeader 授权令牌
     * @return 播放计划确认信息
     */
    @PostMapping("/podcast/schedule")
    public ResponseEntity<Map<String, Object>> schedulePodcastAutoPlay(
            @RequestBody Map<String, Object> scheduleData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // 从请求体中提取播放计划信息
            String podcastId = (String) scheduleData.get("podcastId");
            String podcastTitle = (String) scheduleData.get("podcastTitle");
            String playTime = (String) scheduleData.get("playTime"); // 格式: "HH:mm"
            String playDate = (String) scheduleData.get("playDate"); // 格式: "yyyy-MM-dd"
            
            // 验证必需参数
            if (podcastId == null || podcastTitle == null || playTime == null || playDate == null) {
                response.put("success", false);
                response.put("message", "podcastId, podcastTitle, playTime, and playDate are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建播放计划条目
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("id", "schedule_" + System.currentTimeMillis());
            schedule.put("podcastId", podcastId);
            schedule.put("podcastTitle", podcastTitle);
            schedule.put("playTime", playTime);
            schedule.put("playDate", playDate);
            schedule.put("isActive", true);
            schedule.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 使用自动播放服务添加播放计划
            podcastAutoPlayService.addSchedule(userId, schedule);
            
            // 生成宠物响应
            Map<String, Object> pet = getPetForUser(userId);
            String petResponse = generateScheduleConfirmationResponse(podcastTitle, playTime, playDate);
            
            // 构建成功响应
            response.put("success", true);
            response.put("schedule", schedule);
            response.put("message", "Podcast scheduled for auto-play successfully");
            response.put("petResponse", petResponse);
            response.put("pet", pet);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error scheduling podcast: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 新增播放计划管理功能 ====================
    /**
     * 获取用户的播客播放计划
     * 显示用户所有已安排的播客播放计划
     * 新增功能：用户可以查看和管理所有已安排的播客播放计划
     * 
     * @param authHeader 授权令牌
     * @return 用户的播客播放计划列表
     */
    @GetMapping("/podcast/schedules")
    public ResponseEntity<Map<String, Object>> getUserPodcastSchedules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // 获取用户的所有播放计划
            List<Map<String, Object>> schedules = podcastAutoPlayService.getUserSchedules(userId);
            
            // 生成宠物响应
            Map<String, Object> pet = getPetForUser(userId);
            String petResponse = generateSchedulesOverviewResponse(schedules);
            
            // 构建成功响应
            response.put("success", true);
            response.put("schedules", schedules);
            response.put("totalSchedules", schedules.size());
            response.put("petResponse", petResponse);
            response.put("pet", pet);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 新增老年用户专用推荐功能 ====================
    /**
     * 获取专为老年用户设计的播客推荐（包含宠物响应）
     * 使用预定义的老年用户兴趣，结合虚拟宠物的友好回应
     * 新增功能：专门为老年用户设计的播客推荐，包含健康、冥想、古典音乐等适合老年人的内容
     * 
     * @param authHeader 授权令牌
     * @return 适合老年用户的播客推荐
     */
    @GetMapping("/podcast/elderly-recommendations")
    public ResponseEntity<Map<String, Object>> getElderlyPodcastRecommendations(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // ==================== 新增老年用户兴趣定义 ====================
            // 定义老年用户的常见兴趣 - 新增：专门为老年用户设计的播客兴趣标签
            List<String> elderlyInterests = List.of(
                "health and wellness",      // 健康与保健 - 适合老年人的健康内容
                "meditation",              // 冥想 - 帮助老年人放松和减压
                "classical music",         // 古典音乐 - 温和的音乐选择
                "history",                 // 历史 - 老年人感兴趣的历史故事
                "gardening",               // 园艺 - 适合老年人的休闲活动
                "cooking",                 // 烹饪 - 实用的生活技能
                "travel stories",          // 旅行故事 - 激发想象力的内容
                "inspirational stories",   // 励志故事 - 积极正面的内容
                "memory exercises",        // 记忆练习 - 帮助保持认知能力
                "relaxation"               // 放松 - 舒缓身心的内容
            );
            
            // 调用播客服务获取推荐
            Map<String, Object> recommendationsResult = podcastService.getPodcastRecommendations(elderlyInterests);
            
            if ((Boolean) recommendationsResult.get("success")) {
                // 生成宠物响应
                Map<String, Object> pet = getPetForUser(userId);
                String petResponse = generateElderlyRecommendationsResponse(recommendationsResult);
                
                // 构建成功响应
                response.put("success", true);
                response.put("recommendations", recommendationsResult.get("recommendations"));
                response.put("totalRecommendations", recommendationsResult.get("totalRecommendations"));
                response.put("targetAudience", "elderly");
                response.put("interests", elderlyInterests);
                response.put("petResponse", petResponse);
                response.put("pet", pet);
                
                return ResponseEntity.ok(response);
            } else {
                // 推荐失败，返回错误信息
                response.put("success", false);
                response.put("message", recommendationsResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting elderly recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 新增播放控制功能 ====================
    /**
     * 获取当前播放状态
     * 检查用户当前是否有播客在播放
     * 新增功能：用户可以查看当前播客的播放状态，包括是否正在播放、播放进度等信息
     * 
     * @param authHeader 授权令牌
     * @return 当前播放状态信息
     */
    @GetMapping("/podcast/playback/status")
    public ResponseEntity<Map<String, Object>> getPlaybackStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // 获取播放状态和宠物信息
            Map<String, Object> playbackStatus = podcastAutoPlayService.getPlaybackStatus(userId);
            Map<String, Object> pet = getPetForUser(userId);
            
            if (playbackStatus != null) {
                // 有播客在播放，生成相应的宠物响应
                String petResponse = generatePlaybackStatusResponse(playbackStatus);
                response.put("success", true);
                response.put("playbackStatus", playbackStatus);
                response.put("petResponse", petResponse);
                response.put("pet", pet);
            } else {
                // 没有播客在播放
                response.put("success", true);
                response.put("playbackStatus", null);
                response.put("petResponse", "Meow! 🎵 No podcast is currently playing. Would you like me to start one for you?");
                response.put("pet", pet);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting playback status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 开始播放指定的播客
     * 根据播放计划ID开始播放播客
     * 新增功能：用户可以手动开始播放已安排的播客，提供即时播放控制
     * 
     * @param playbackData 播放数据
     * @param authHeader 授权令牌
     * @return 播放状态
     */
    @PostMapping("/podcast/playback/start")
    public ResponseEntity<Map<String, Object>> startPlayback(
            @RequestBody Map<String, Object> playbackData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // 从请求体中提取播放计划ID
            String scheduleId = (String) playbackData.get("scheduleId");
            
            // 验证必需参数
            if (scheduleId == null) {
                response.put("success", false);
                response.put("message", "scheduleId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 尝试开始播放
            boolean started = podcastAutoPlayService.startPlayback(userId, scheduleId);
            
            if (started) {
                // 播放成功，生成宠物响应
                Map<String, Object> pet = getPetForUser(userId);
                String petResponse = generateStartPlaybackResponse(scheduleId);
                
                response.put("success", true);
                response.put("message", "Playback started successfully");
                response.put("petResponse", petResponse);
                response.put("pet", pet);
            } else {
                // 播放失败，播放计划未找到
                response.put("success", false);
                response.put("message", "Failed to start playback - schedule not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error starting playback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 停止当前播放
     * 停止用户当前正在播放的播客
     * 新增功能：用户可以停止当前正在播放的播客，提供播放控制功能
     * 
     * @param authHeader 授权令牌
     * @return 停止确认信息
     */
    @PostMapping("/podcast/playback/stop")
    public ResponseEntity<Map<String, Object>> stopPlayback(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // 尝试停止播放
            boolean stopped = podcastAutoPlayService.stopPlayback(userId);
            Map<String, Object> pet = getPetForUser(userId);
            
            // 根据是否成功停止生成不同的宠物响应
            String petResponse = stopped ? 
                "Meow! ⏹️ I've stopped the podcast for you. Let me know when you'd like to listen to something else!" :
                "Purr... 🎵 There wasn't anything playing, but I'm here if you want to start a podcast!";
            
            // 构建成功响应
            response.put("success", true);
            response.put("stopped", stopped);
            response.put("petResponse", petResponse);
            response.put("pet", pet);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error stopping playback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取即将播放的播客列表
     * 显示用户未来将要播放的播客计划
     * 新增功能：用户可以查看未来将要播放的播客列表，便于提前了解播放安排
     * 
     * @param authHeader 授权令牌
     * @return 即将播放的播客列表
     */
    @GetMapping("/podcast/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingPodcasts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // 获取即将播放的播客列表
            List<Map<String, Object>> upcoming = podcastAutoPlayService.getUpcomingPodcasts(userId);
            Map<String, Object> pet = getPetForUser(userId);
            String petResponse = generateUpcomingPodcastsResponse(upcoming);
            
            // 构建成功响应
            response.put("success", true);
            response.put("upcomingPodcasts", upcoming);
            response.put("totalUpcoming", upcoming.size());
            response.put("petResponse", petResponse);
            response.put("pet", pet);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting upcoming podcasts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 新增播客功能辅助方法 ====================
    // 新增：这些方法用于生成虚拟宠物对播客功能的友好回应，增强用户体验

    /**
     * 生成播客推荐响应
     * 根据用户兴趣和推荐结果生成宠物的友好回应
     * 新增功能：为播客推荐功能添加虚拟宠物的互动体验
     * 
     * @param interests 用户兴趣列表
     * @param recommendationsResult 推荐结果
     * @return 宠物的推荐响应
     */
    private String generatePodcastRecommendationResponse(List<String> interests, Map<String, Object> recommendationsResult) {
        int totalRecommendations = (Integer) recommendationsResult.get("totalRecommendations");
        
        // ==================== 新增播客推荐响应数组 ====================
        // 预定义的宠物响应数组 - 新增：为播客推荐功能设计的友好回应
        String[] responses = {
            "Meow! 🎧 I found some amazing podcasts for you based on your interests! I think you'll love these " + totalRecommendations + " recommendations!",
            "Purr... 🎵 I've discovered some wonderful podcasts that match your interests perfectly! There are " + totalRecommendations + " great options for you!",
            "Meow meow! 🎶 I'm so excited to share these podcast recommendations with you! I found " + totalRecommendations + " that I think you'll enjoy!",
            "Purr... 🎧 Your interests are so interesting! I've found " + totalRecommendations + " podcasts that I think will be perfect for you!"
        };
        
        // 随机选择一个响应
        return responses[new Random().nextInt(responses.length)];
    }

    /**
     * 生成播放计划确认响应
     * 当用户成功安排播客播放时，生成宠物的确认回应
     * 新增功能：为用户安排播客播放时提供宠物的确认和鼓励回应
     * 
     * @param podcastTitle 播客标题
     * @param playTime 播放时间
     * @param playDate 播放日期
     * @return 宠物的确认响应
     */
    private String generateScheduleConfirmationResponse(String podcastTitle, String playTime, String playDate) {
        // ==================== 新增播放计划确认响应数组 ====================
        // 预定义的确认响应数组 - 新增：为播客播放计划确认功能设计的回应
        String[] responses = {
            "Meow! ⏰ I've scheduled '" + podcastTitle + "' to play at " + playTime + " on " + playDate + ". I'll make sure you don't miss it!",
            "Purr... 🎵 Perfect! I've set up '" + podcastTitle + "' to automatically play at " + playTime + " on " + playDate + ". I'll remind you when it's time!",
            "Meow meow! ⏰ Great choice! I've scheduled '" + podcastTitle + "' for " + playTime + " on " + playDate + ". I'll make sure it plays right on time!",
            "Purr... 🎧 Wonderful! I've arranged for '" + podcastTitle + "' to play at " + playTime + " on " + playDate + ". I'll take care of everything!"
        };
        
        // 随机选择一个响应
        return responses[new Random().nextInt(responses.length)];
    }

    /**
     * 生成播放计划概览响应
     * 当用户查看播放计划时，生成宠物的概览回应
     * 新增功能：当用户查看播放计划时，提供宠物的友好概览和提醒
     * 
     * @param schedules 播放计划列表
     * @return 宠物的概览响应
     */
    private String generateSchedulesOverviewResponse(List<Map<String, Object>> schedules) {
        if (schedules.isEmpty()) {
            // 没有播放计划时的响应
            return "Meow! 📅 You don't have any podcasts scheduled yet. Would you like me to help you find some great podcasts to schedule?";
        }
        
        // ==================== 新增播放计划概览响应数组 ====================
        // 有播放计划时的响应数组
        String[] responses = {
            "Purr... 📋 I can see you have " + schedules.size() + " podcast(s) scheduled! I'll make sure they all play at the right time!",
            "Meow! 📅 You have " + schedules.size() + " podcast(s) in your schedule. I'm looking forward to playing them for you!",
            "Purr... 🎵 I found " + schedules.size() + " podcast(s) in your schedule. I'll make sure each one plays perfectly on time!",
            "Meow meow! 📋 Great! You have " + schedules.size() + " podcast(s) scheduled. I'll take care of playing them all for you!"
        };
        
        // 随机选择一个响应
        return responses[new Random().nextInt(responses.length)];
    }

    /**
     * 生成老年用户推荐响应
     * 为老年用户生成专门的播客推荐回应
     * 新增功能：专门为老年用户设计的播客推荐回应，更加温和和关怀
     * 
     * @param recommendationsResult 推荐结果
     * @return 宠物的老年用户推荐响应
     */
    private String generateElderlyRecommendationsResponse(Map<String, Object> recommendationsResult) {
        int totalRecommendations = (Integer) recommendationsResult.get("totalRecommendations");
        
        // ==================== 新增老年用户推荐响应数组 ====================
        // 专门为老年用户设计的响应数组 - 新增：为老年用户播客推荐功能设计的温和回应
        String[] responses = {
            "Meow! 🧘‍♀️ I've found some wonderful podcasts perfect for you! There are " + totalRecommendations + " relaxing and inspiring options!",
            "Purr... 🎵 I've discovered some amazing podcasts that I think you'll love! There are " + totalRecommendations + " great choices for you!",
            "Meow meow! 🌟 I've selected some special podcasts just for you! I found " + totalRecommendations + " that I think will bring you joy!",
            "Purr... 🎧 I've found some wonderful podcasts that are perfect for you! There are " + totalRecommendations + " great options to choose from!"
        };
        
        // 随机选择一个响应
        return responses[new Random().nextInt(responses.length)];
    }

    /**
     * 生成播放状态响应
     * 根据当前播放状态生成宠物的回应
     * 新增功能：根据播客播放状态生成相应的宠物回应，增强互动体验
     * 
     * @param playbackStatus 播放状态信息
     * @return 宠物的播放状态响应
     */
    private String generatePlaybackStatusResponse(Map<String, Object> playbackStatus) {
        String podcastTitle = (String) playbackStatus.get("podcastTitle");
        Boolean isPlaying = (Boolean) playbackStatus.get("isPlaying");
        
        if (Boolean.TRUE.equals(isPlaying)) {
                    // ==================== 新增播放状态响应数组 ====================
        // 正在播放时的响应数组 - 新增：为播客播放状态功能设计的回应
        String[] responses = {
            "Meow! 🎵 '" + podcastTitle + "' is currently playing for you! I hope you're enjoying it!",
            "Purr... 🎧 I can see that '" + podcastTitle + "' is playing right now. How do you like it?",
            "Meow meow! 🎶 '" + podcastTitle + "' is on! I'm so glad you're listening to something wonderful!",
            "Purr... 🎵 Great choice! '" + podcastTitle + "' is playing beautifully for you!"
        };
            return responses[new Random().nextInt(responses.length)];
        } else {
            // 暂停时的响应
            return "Meow! 🎵 '" + podcastTitle + "' is paused. Would you like me to resume it for you?";
        }
    }

    /**
     * 生成开始播放响应
     * 当播客开始播放时生成宠物的回应
     * 新增功能：当用户开始播放播客时，提供宠物的鼓励和确认回应
     * 
     * @param scheduleId 播放计划ID
     * @return 宠物的开始播放响应
     */
    private String generateStartPlaybackResponse(String scheduleId) {
        // ==================== 新增开始播放响应数组 ====================
        // 开始播放时的响应数组 - 新增：为播客开始播放功能设计的鼓励回应
        String[] responses = {
            "Meow! 🎵 I've started the podcast for you! I hope you enjoy listening to it!",
            "Purr... 🎧 Perfect! The podcast is now playing. I hope it brings you joy!",
            "Meow meow! 🎶 Wonderful! I've started the podcast. I hope you love it!",
            "Purr... 🎵 Excellent! The podcast is now playing for you. Enjoy!"
        };
        return responses[new Random().nextInt(responses.length)];
    }

    /**
     * 生成即将播放播客响应
     * 当用户查看即将播放的播客时生成宠物的回应
     * 新增功能：当用户查看即将播放的播客时，提供宠物的友好提醒和期待回应
     * 
     * @param upcoming 即将播放的播客列表
     * @return 宠物的即将播放响应
     */
    private String generateUpcomingPodcastsResponse(List<Map<String, Object>> upcoming) {
        if (upcoming.isEmpty()) {
            // 没有即将播放的播客时的响应
            return "Meow! 📅 You don't have any upcoming podcasts scheduled. Would you like me to help you find some great podcasts to schedule?";
        }
        
        // ==================== 新增即将播放响应数组 ====================
        // 有即将播放的播客时的响应数组 - 新增：为即将播放播客功能设计的期待回应
        String[] responses = {
            "Purr... 📋 I can see you have " + upcoming.size() + " upcoming podcast(s)! I'm looking forward to playing them for you!",
            "Meow! 📅 You have " + upcoming.size() + " podcast(s) coming up! I'll make sure they all play at the perfect time!",
            "Purr... 🎵 Great! You have " + upcoming.size() + " podcast(s) scheduled for the future. I'll take care of everything!",
            "Meow meow! 📋 Wonderful! You have " + upcoming.size() + " upcoming podcast(s). I'm excited to play them for you!"
        };
        
        // 随机选择一个响应
        return responses[new Random().nextInt(responses.length)];
    }

    // ==================== 原有方法：用户身份验证 ====================
    /**
     * 从授权令牌中解析用户ID
     * 用于用户身份验证和识别
     * 
     * @param authHeader 授权头信息
     * @return 用户ID，0表示游客用户，1表示演示用户
     */
    private Long getUserIdFromToken(String authHeader) {
        // 检查授权头是否为空或格式不正确
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 0L; // 游客用户
        }
        
        // 提取令牌部分
        String token = authHeader.substring(7);
        if (token.startsWith("demo-token-")) {
            return 1L; // 演示用户
        }
        
        return 0L; // 默认为游客用户
    }
} 