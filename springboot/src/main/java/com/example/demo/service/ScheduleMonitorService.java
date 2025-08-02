package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import com.example.demo.pojo.User;

@Service
public class ScheduleMonitorService {
    @Autowired
    private HealthService healthService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 9 * * ?") // 每天早上9点检查健康数据
    public void checkHealthSubmission() {
        List<User> users = healthService.getUsersWithoutTodayHealthLog();
        for (User user : users) {
            emailService.sendHealthAlertEmail(
                user.getEmail(),
                " Daily Health Check Reminder",
                "Hello " + user.getName() + ", you haven’t submitted today’s health data. Please do it soon."
            );
        }
    }

    @Scheduled(cron = "0 0 10 * * ?") // 每天早上10点检查日程
    public void checkScheduleSubmission() {
        List<User> users = scheduleService.getUsersWithoutTodaySchedule();
        for (User user : users) {
            emailService.sendHealthAlertEmail(
                user.getEmail(),
                "Daily Plan Reminder",
                "Hello " + user.getName() + ", you haven’t told your pet your plan for today. Please do it now."
            );
        }
    }
}
