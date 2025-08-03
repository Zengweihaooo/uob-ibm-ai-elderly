package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 播客自动播放服务
 * 负责管理播客的定时播放、播放状态跟踪等功能
 */
@Service
public class PodcastAutoPlayService {

    // 注入播客服务，用于获取播客信息
    @Autowired
    private PodcastService podcastService;

    // 存储用户的播客播放计划（用户ID -> 播放计划列表）
    private final Map<Long, List<Map<String, Object>>> userSchedules = new ConcurrentHashMap<>();
    
    // 存储当前播放状态（用户ID -> 播放状态）
    private final Map<Long, Map<String, Object>> playbackStatuses = new ConcurrentHashMap<>();

    /**
     * 为用户添加播客播放计划
     * @param userId 用户ID
     * @param scheduleData 播放计划数据
     * @return 播放计划ID
     */
    public String addSchedule(Long userId, Map<String, Object> scheduleData) {
        // 生成唯一的播放计划ID
        String scheduleId = "schedule_" + System.currentTimeMillis();
        
        // 设置播放计划的基本信息
        scheduleData.put("id", scheduleId);
        scheduleData.put("userId", userId);
        scheduleData.put("isActive", true);  // 默认激活状态
        scheduleData.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 将播放计划添加到用户的计划列表中
        userSchedules.computeIfAbsent(userId, k -> new ArrayList<>()).add(scheduleData);
        
        return scheduleId;
    }

    /**
     * 获取用户的所有播客播放计划
     * @param userId 用户ID
     * @return 播放计划列表
     */
    public List<Map<String, Object>> getUserSchedules(Long userId) {
        return userSchedules.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * 删除指定的播放计划
     * @param userId 用户ID
     * @param scheduleId 播放计划ID
     * @return 是否删除成功
     */
    public boolean removeSchedule(Long userId, String scheduleId) {
        List<Map<String, Object>> schedules = userSchedules.get(userId);
        if (schedules != null) {
            // 根据播放计划ID删除对应的计划
            return schedules.removeIf(schedule -> scheduleId.equals(schedule.get("id")));
        }
        return false;
    }

    /**
     * 获取用户当前的播放状态
     * @param userId 用户ID
     * @return 播放状态信息
     */
    public Map<String, Object> getPlaybackStatus(Long userId) {
        return playbackStatuses.get(userId);
    }

    /**
     * 开始播放指定的播客
     * @param userId 用户ID
     * @param scheduleId 播放计划ID
     * @return 是否成功开始播放
     */
    public boolean startPlayback(Long userId, String scheduleId) {
        // 获取用户的所有播放计划
        List<Map<String, Object>> schedules = getUserSchedules(userId);
        
        // 查找对应的播放计划
        for (Map<String, Object> schedule : schedules) {
            if (scheduleId.equals(schedule.get("id"))) {
                // 创建播放状态对象
                Map<String, Object> playbackStatus = new HashMap<>();
                playbackStatus.put("userId", userId);
                playbackStatus.put("scheduleId", scheduleId);
                playbackStatus.put("podcastId", schedule.get("podcastId"));
                playbackStatus.put("podcastTitle", schedule.get("podcastTitle"));
                playbackStatus.put("isPlaying", true);  // 设置为播放状态
                playbackStatus.put("startedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                playbackStatus.put("currentPosition", 0);  // 播放位置从0开始
                
                // 保存播放状态
                playbackStatuses.put(userId, playbackStatus);
                return true;
            }
        }
        
        return false;  // 未找到对应的播放计划
    }

    /**
     * 停止用户的播客播放
     * @param userId 用户ID
     * @return 是否成功停止播放
     */
    public boolean stopPlayback(Long userId) {
        // 移除用户的播放状态
        Map<String, Object> status = playbackStatuses.remove(userId);
        return status != null;  // 返回是否之前有播放状态
    }

    /**
     * 更新播放位置
     * @param userId 用户ID
     * @param position 播放位置（秒）
     * @return 是否成功更新
     */
    public boolean updatePlaybackPosition(Long userId, int position) {
        Map<String, Object> status = playbackStatuses.get(userId);
        if (status != null) {
            // 更新播放位置和最后更新时间
            status.put("currentPosition", position);
            status.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return true;
        }
        return false;
    }

    /**
     * 定时任务：检查并执行预定的播客播放
     * 每分钟执行一次，检查是否有需要播放的播客
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndExecuteScheduledPodcasts() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // 遍历所有用户的播放计划
        for (Map.Entry<Long, List<Map<String, Object>>> entry : userSchedules.entrySet()) {
            Long userId = entry.getKey();
            List<Map<String, Object>> schedules = entry.getValue();

            // 检查用户的每个播放计划
            for (Map<String, Object> schedule : schedules) {
                // 只处理激活状态的播放计划
                if (Boolean.TRUE.equals(schedule.get("isActive"))) {
                    String playDateStr = (String) schedule.get("playDate");
                    String playTimeStr = (String) schedule.get("playTime");

                    if (playDateStr != null && playTimeStr != null) {
                        try {
                            // 解析播放日期和时间
                            LocalDate playDate = LocalDate.parse(playDateStr);
                            LocalTime playTime = LocalTime.parse(playTimeStr);

                            // 检查是否到了播放时间
                            if (playDate.equals(today) && playTime.equals(currentTime)) {
                                // 开始播放播客
                                String scheduleId = (String) schedule.get("id");
                                startPlayback(userId, scheduleId);
                                
                                // 输出播放日志
                                System.out.println("🎵 自动播放开始: " + schedule.get("podcastTitle") + 
                                                 " (用户: " + userId + ") 时间: " + currentTime);
                            }
                        } catch (Exception e) {
                            // 处理日期时间解析错误
                            System.err.println("解析播放计划时间失败: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取用户即将播放的播客列表
     * @param userId 用户ID
     * @return 即将播放的播客列表
     */
    public List<Map<String, Object>> getUpcomingPodcasts(Long userId) {
        List<Map<String, Object>> schedules = getUserSchedules(userId);
        List<Map<String, Object>> upcoming = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 遍历所有播放计划
        for (Map<String, Object> schedule : schedules) {
            // 只处理激活状态的播放计划
            if (Boolean.TRUE.equals(schedule.get("isActive"))) {
                String playDateStr = (String) schedule.get("playDate");
                String playTimeStr = (String) schedule.get("playTime");

                if (playDateStr != null && playTimeStr != null) {
                    try {
                        // 解析播放日期和时间
                        LocalDate playDate = LocalDate.parse(playDateStr);
                        LocalTime playTime = LocalTime.parse(playTimeStr);
                        LocalDateTime scheduledTime = LocalDateTime.of(playDate, playTime);

                        // 只添加未来的播放计划
                        if (scheduledTime.isAfter(now)) {
                            Map<String, Object> upcomingPodcast = new HashMap<>(schedule);
                            upcomingPodcast.put("scheduledTime", scheduledTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            upcoming.add(upcomingPodcast);
                        }
                    } catch (Exception e) {
                        // 处理日期时间解析错误
                        System.err.println("解析即将播放计划时间失败: " + e.getMessage());
                    }
                }
            }
        }

        // 按计划时间排序
        upcoming.sort((a, b) -> {
            String timeA = (String) a.get("scheduledTime");
            String timeB = (String) b.get("scheduledTime");
            return timeA.compareTo(timeB);
        });

        return upcoming;
    }

    /**
     * 获取用户最近播放的播客列表
     * @param userId 用户ID
     * @return 最近播放的播客列表
     */
    public List<Map<String, Object>> getRecentlyPlayedPodcasts(Long userId) {
        List<Map<String, Object>> schedules = getUserSchedules(userId);
        List<Map<String, Object>> recent = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusDays(7);  // 一周前的时间

        // 遍历所有播放计划
        for (Map<String, Object> schedule : schedules) {
            String createdAtStr = (String) schedule.get("createdAt");
            if (createdAtStr != null) {
                try {
                    // 解析创建时间
                    LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
                    // 只添加一周内创建的播放计划
                    if (createdAt.isAfter(oneWeekAgo)) {
                        recent.add(schedule);
                    }
                } catch (Exception e) {
                    // 处理时间解析错误
                    System.err.println("解析创建时间失败: " + e.getMessage());
                }
            }
        }

        return recent;
    }
} 