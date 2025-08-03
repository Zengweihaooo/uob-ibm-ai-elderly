package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.demo.controller.PetController;
import com.example.demo.pojo.User;


/**
 * This service is used to monitor the health and schedule of the users
 * It will send reminder emails to the users who haven't submitted the health and schedule data today
 * @author Lepeng Zhou
 * @version 1.0
*/

@Service
public class ScheduleMonitorService {
    @Autowired
    private HealthService healthService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PetController petController;

    @Scheduled(cron = "0 0 9 * * ?") // Check the health data every morning at 9 am
    public void checkHealthSubmission() {
        List<User> users = healthService.getUsersWithoutTodayHealthLog();
        for (User user : users) {
            emailService.sendHealthAlertEmail(
                user.getEmail(),
                "Daily Health Check Reminder",
                "Hello " + user.getName() + ", you haven’t submitted today’s health data. Please do it soon."
            );

            petController.createMessage(
                "pet",
                "Meow... I noticed that you haven't checked in on your health information today. Is everything okay?",
                "text",
                user.getId()  
            );
        }
    }

    @Scheduled(cron = "0 0 10 * * ?") // Check the schedule every morning at 10am
    public void checkScheduleSubmission() {
        List<User> users = scheduleService.getUsersWithoutTodaySchedule();
        for (User user : users) {
            emailService.sendHealthAlertEmail(
                user.getEmail(),
                "Daily Plan Reminder",
                "Hello " + user.getName() + ", you haven’t told your pet your plan for today. Please do it now."
            );

             // 添加宠物消息
            petController.createMessage(
                "pet",
                "Purr...📅你还没告诉我今天的计划呢，要一起安排一下吗？",
                "text",
                user.getId()
            );
        }
    }
}
