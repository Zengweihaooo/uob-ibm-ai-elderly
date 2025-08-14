package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.Schedule;
import com.example.demo.pojo.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Morning Scheduler Service
 * 负责每天早上为活跃用户创建早安问候和日程安排
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
public class MorningScheduler {

    private static final Logger logger = Logger.getLogger(MorningScheduler.class.getName());
    
    private final ScheduleService scheduleService;
    private final UserService userService;
    private final PetMoodService petMoodService;

    @Autowired
    public MorningScheduler(ScheduleService scheduleService, 
                           UserService userService,
                           PetMoodService petMoodService) {
        this.scheduleService = scheduleService;
        this.userService = userService;
        this.petMoodService = petMoodService;
    }

    @Value("${pet.wakeupHour:8}")
    private int wakeupHour;
    
    @Value("${pet.defaultPodcastTime:15:00}")
    private String defaultPodcastTime;

    /**
     * 每天早上定时任务
     * 调试时用 pet.morning.cron，生产可改回 0 0 8 * * *
     */
    @Scheduled(cron = "${pet.morning.cron:0 0 8 * * *}")
    public void morningPing() {
        logger.info("🌅 开始执行早安问候任务...");
        
        try {
            List<Long> activeUserIds = scheduleService.activeUserIds();
            logger.info("找到 " + activeUserIds.size() + " 个活跃用户");
            
            for (Long userId : activeUserIds) {
                try {
                    // 检查是否已经有早安问候
                    if (!scheduleService.hasMorningGreeting(userId)) {
                        // 创建早安问候
                        Schedule morningGreeting = scheduleService.createTodo(
                            userId, 
                            "早安问候", 
                            "今天做什么？我可以安排播客/提醒散步/联系家人。", 
                            "08:05"
                        );
                        
                        // 更新宠物情绪（早晨互动）
                        petMoodService.adjustMood(userId, 10);
                        
                        logger.info("为用户 " + userId + " 创建早安问候: " + morningGreeting.getTitle());
                    }
                    
                    // 创建默认的早晨活动
                    createDefaultMorningActivities(userId);
                    
                } catch (Exception e) {
                    logger.warning("为用户 " + userId + " 创建早安问候失败: " + e.getMessage());
                }
            }
            
            logger.info("✅ 早安问候任务完成");
            
        } catch (Exception e) {
            logger.severe("早安问候任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户意图，创建相应的待办事项
     * 
     * @param userId 用户ID
     * @param intent 用户意图
     * @return 创建的待办事项
     */
    public Schedule handleIntent(Long userId, String intent) {
        logger.info("处理用户 " + userId + " 的意图: " + intent);
        
        Schedule createdTodo = null;
        
        try {
            switch (intent) {
                case "SCHEDULE_PODCAST" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "播放播客", 
                        "自动安排的播客时间，放松心情", 
                        defaultPodcastTime
                    );
                    logger.info("为用户 " + userId + " 安排播客时间");
                }
                
                case "REMIND_WALK" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "散步20分钟", 
                        "健康提醒：户外散步，呼吸新鲜空气", 
                        "16:00"
                    );
                    logger.info("为用户 " + userId + " 安排散步提醒");
                }
                
                case "MESSAGE_FAMILY" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "给家人发消息", 
                        "关怀沟通：联系家人，分享今日心情", 
                        "10:00"
                    );
                    logger.info("为用户 " + userId + " 安排家人联系提醒");
                }
                
                case "MORNING_EXERCISE" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "晨练", 
                        "轻度晨练：伸展运动，唤醒身体", 
                        "07:30"
                    );
                    logger.info("为用户 " + userId + " 安排晨练");
                }
                
                case "BREAKFAST_REMINDER" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "早餐提醒", 
                        "营养早餐：记得吃早餐，补充能量", 
                        "08:00"
                    );
                    logger.info("为用户 " + userId + " 安排早餐提醒");
                }
                
                default -> {
                    logger.warning("未知的用户意图: " + intent);
                    return null;
                }
            }
            
            // 如果成功创建待办事项，增加宠物经验值
            if (createdTodo != null) {
                petMoodService.addExperience(userId, 5);
            }
            
        } catch (Exception e) {
            logger.severe("处理用户意图失败: " + e.getMessage());
        }
        
        return createdTodo;
    }

    /**
     * 创建默认的早晨活动
     * 
     * @param userId 用户ID
     */
    private void createDefaultMorningActivities(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            
            // 检查是否已有早晨活动
            List<Schedule> todaySchedule = scheduleService.getTodaySchedule(userId);
            boolean hasMorningActivities = todaySchedule.stream()
                .anyMatch(schedule -> schedule.getCategory().equals("morning") && 
                                   schedule.getActivityTime().isBefore(LocalTime.of(12, 0)));
            
            if (!hasMorningActivities) {
                // 创建默认的早晨活动
                scheduleService.createTodo(
                    userId, 
                    "晨间伸展", 
                    "轻柔的伸展运动，唤醒身体", 
                    "07:30"
                );
                
                scheduleService.createTodo(
                    userId, 
                    "早餐时间", 
                    "营养均衡的早餐", 
                    "08:00"
                );
                
                scheduleService.createTodo(
                    userId, 
                    "晨间散步", 
                    "在公园散步15分钟，呼吸新鲜空气", 
                    "08:30"
                );
                
                logger.info("为用户 " + userId + " 创建默认早晨活动");
            }
            
        } catch (Exception e) {
            logger.warning("为用户 " + userId + " 创建默认早晨活动失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发早安问候（用于测试）
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean triggerMorningGreeting(Long userId) {
        try {
            if (!scheduleService.hasMorningGreeting(userId)) {
                Schedule morningGreeting = scheduleService.createTodo(
                    userId, 
                    "早安问候", 
                    "今天做什么？我可以安排播客/提醒散步/联系家人。", 
                    "08:05"
                );
                
                // 更新宠物情绪
                petMoodService.adjustMood(userId, 10);
                
                logger.info("手动触发用户 " + userId + " 的早安问候");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.severe("手动触发早安问候失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户的早晨日程建议
     * 
     * @param userId 用户ID
     * @return 早晨日程建议列表
     */
    public List<String> getMorningSuggestions(Long userId) {
        List<String> suggestions = new java.util.ArrayList<>();
        
        try {
            // 基于用户状态提供个性化建议
            var petStatus = petMoodService.getFullPetStatus(userId);
            int moodScore = (Integer) petStatus.get("moodScore");
            
            if (moodScore < 0) {
                suggestions.add("今天心情不太好，建议听些轻音乐");
                suggestions.add("可以安排一次户外散步，改善心情");
            } else if (moodScore > 20) {
                suggestions.add("今天心情不错，可以尝试新的活动");
                suggestions.add("建议联系朋友分享好心情");
            } else {
                suggestions.add("今天心情平稳，适合规律作息");
                suggestions.add("可以安排一些轻松的活动");
            }
            
            // 添加通用建议
            suggestions.add("记得按时吃药");
            suggestions.add("保持适度运动");
            suggestions.add("多与家人朋友联系");
            
        } catch (Exception e) {
            logger.warning("获取早晨建议失败: " + e.getMessage());
            // 返回默认建议
            suggestions.add("保持规律作息");
            suggestions.add("适度运动");
            suggestions.add("保持社交联系");
        }
        
        return suggestions;
    }
}
