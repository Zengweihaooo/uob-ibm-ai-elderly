package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.pojo.Schedule;
import com.example.demo.service.MorningScheduler;
import com.example.demo.service.ScheduleService;

/**
 * Morning Scheduler Controller
 * 提供早晨调度相关的REST API接口
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
//@RestController
//@RequestMapping("/api/morning")
@CrossOrigin(origins = "*")
public class MorningSchedulerController {

    private final MorningScheduler morningScheduler;
    private final ScheduleService scheduleService;

    @Autowired
    public MorningSchedulerController(MorningScheduler morningScheduler, 
                                    ScheduleService scheduleService) {
        this.morningScheduler = morningScheduler;
        this.scheduleService = scheduleService;
    }

    /**
     * 手动触发早安问候（用于测试）
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/greeting/trigger")
    public ResponseEntity<Map<String, Object>> triggerMorningGreeting(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = morningScheduler.triggerMorningGreeting(userId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "早安问候已触发");
                response.put("userId", userId);
            } else {
                response.put("success", false);
                response.put("message", "用户今天已有早安问候");
                response.put("userId", userId);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "触发早安问候失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 处理用户意图，创建相应的待办事项
     * 
     * @param userId 用户ID
     * @param requestBody 包含intent的请求体
     * @return 创建的待办事项
     */
    @PostMapping("/intent")
    public ResponseEntity<Map<String, Object>> handleIntent(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String intent = (String) requestBody.get("intent");
            
            if (intent == null || intent.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "意图不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            Schedule createdTodo = morningScheduler.handleIntent(userId, intent);
            
            if (createdTodo != null) {
                response.put("success", true);
                response.put("message", "成功处理用户意图");
                response.put("todo", createdTodo);
                response.put("intent", intent);
            } else {
                response.put("success", false);
                response.put("message", "处理用户意图失败或意图未知");
                response.put("intent", intent);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理用户意图失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户的早晨日程建议
     * 
     * @param userId 用户ID
     * @return 早晨日程建议
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getMorningSuggestions(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> suggestions = morningScheduler.getMorningSuggestions(userId);
            
            response.put("success", true);
            response.put("suggestions", suggestions);
            response.put("userId", userId);
            response.put("count", suggestions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取早晨建议失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户今天的早晨日程
     * 
     * @param userId 用户ID
     * @return 今天的早晨日程
     */
    @GetMapping("/schedule")
    public ResponseEntity<Map<String, Object>> getTodayMorningSchedule(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Schedule> todaySchedule = scheduleService.getTodaySchedule(userId);
            
            // 过滤早晨活动（12点之前）
            List<Schedule> morningSchedule = todaySchedule.stream()
                .filter(schedule -> schedule.getActivityTime().getHour() < 12)
                .sorted((a, b) -> a.getActivityTime().compareTo(b.getActivityTime()))
                .toList();
            
            response.put("success", true);
            response.put("morningSchedule", morningSchedule);
            response.put("userId", userId);
            response.put("count", morningSchedule.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取早晨日程失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查用户是否有早安问候
     * 
     * @param userId 用户ID
     * @return 检查结果
     */
    @GetMapping("/greeting/check")
    public ResponseEntity<Map<String, Object>> checkMorningGreeting(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean hasGreeting = scheduleService.hasMorningGreeting(userId);
            
            response.put("success", true);
            response.put("hasMorningGreeting", hasGreeting);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查早安问候失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取可用的用户意图列表
     * 
     * @return 可用的意图列表
     */
    @GetMapping("/intents")
    public ResponseEntity<Map<String, Object>> getAvailableIntents() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, String> intents = new HashMap<>();
            intents.put("SCHEDULE_PODCAST", "安排播客时间");
            intents.put("REMIND_WALK", "提醒散步");
            intents.put("MESSAGE_FAMILY", "联系家人");
            intents.put("MORNING_EXERCISE", "晨练安排");
            intents.put("BREAKFAST_REMINDER", "早餐提醒");
            
            response.put("success", true);
            response.put("intents", intents);
            response.put("count", intents.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取意图列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 批量处理多个意图
     * 
     * @param userId 用户ID
     * @param requestBody 包含intents数组的请求体
     * @return 处理结果
     */
    @PostMapping("/intents/batch")
    public ResponseEntity<Map<String, Object>> handleMultipleIntents(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> intents = (List<String>) requestBody.get("intents");
            
            if (intents == null || intents.isEmpty()) {
                response.put("success", false);
                response.put("message", "意图列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Schedule> createdTodos = new java.util.ArrayList<>();
            List<String> failedIntents = new java.util.ArrayList<>();
            
            for (String intent : intents) {
                try {
                    Schedule todo = morningScheduler.handleIntent(userId, intent);
                    if (todo != null) {
                        createdTodos.add(todo);
                    } else {
                        failedIntents.add(intent);
                    }
                } catch (Exception e) {
                    failedIntents.add(intent + " (错误: " + e.getMessage() + ")");
                }
            }
            
            response.put("success", true);
            response.put("createdTodos", createdTodos);
            response.put("failedIntents", failedIntents);
            response.put("userId", userId);
            response.put("totalIntents", intents.size());
            response.put("successCount", createdTodos.size());
            response.put("failedCount", failedIntents.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量处理意图失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
