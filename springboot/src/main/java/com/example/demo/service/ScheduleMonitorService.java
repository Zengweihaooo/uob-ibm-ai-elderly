package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.demo.controller.EmotionCompanionController;
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
    private EmotionCompanionController emotionCompanionController;

    @Autowired
    private SmsService smsService;

    @Scheduled(cron = "0 0 9 * * ?") // Check the health data every morning at 9 am
    public void checkHealthSubmission() {
        List<User> users = healthService.getUsersWithoutTodayHealthLog();
        for (User user : users) {
            sendMultiChannelReminder(user, "health_check");
        }
    }

    @Scheduled(cron = "0 0 10 * * ?") // Check the schedule every morning at 10am
    public void checkScheduleSubmission() {
        List<User> users = scheduleService.getUsersWithoutTodaySchedule();
        for (User user : users) {
            sendMultiChannelReminder(user, "schedule_check");
        }
    }

    /**
     * Send reminders through multiple channels
     * 
     * @param user User to send reminder to
     * @param reminderType Type of reminder
     */
    public void sendMultiChannelReminder(User user, String reminderType) {
        String subject = "";
        String message = "";
        
        switch (reminderType) {
            case "health_check":
                subject = "Daily Health Check Reminder";
                message = "Hello " + user.getName() + ", you haven't submitted today's health data. Please do it soon.";
                break;
            case "schedule_check":
                subject = "Daily Plan Reminder";
                message = "Hello " + user.getName() + ", you haven't told your pet your plan for today. Please do it now.";
                break;
            case "medication":
                subject = "Medication Reminder";
                message = "Hello " + user.getName() + ", it's time to take your medication. Please don't forget!";
                break;
            case "appointment":
                subject = "Appointment Reminder";
                message = "Hello " + user.getName() + ", you have an appointment coming up. Please check your schedule.";
                break;
            default:
                subject = "General Reminder";
                message = "Hello " + user.getName() + ", you have a reminder from your AI companion.";
        }
        
        // Send email reminder
        try {
            emailService.sendHealthAlertEmail(user.getEmail(), subject, message);
        } catch (Exception e) {
            System.err.println("Failed to send email reminder to " + user.getEmail() + ": " + e.getMessage());
        }
        
        // Send SMS reminder (if phone number is available)
        try {
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                smsService.sendSMS(user.getPhoneNumber(), message, "REMINDER");
            }
        } catch (Exception e) {
            System.err.println("Failed to send SMS reminder to " + user.getPhoneNumber() + ": " + e.getMessage());
        }
        
        // Send pet message
        try {
            String petMessage = "";
            switch (reminderType) {
                case "health_check":
                    petMessage = "Beep... I noticed that you haven't checked in on your health information today. Is everything okay?";
                    break;
                case "schedule_check":
                    petMessage = "Chime...📅 You haven't told me your plan for today yet. Shall we arrange it together?";
                    break;
                case "medication":
                    petMessage = "Beep! 💊 It's time for your medication! Don't forget to take it!";
                    break;
                case "appointment":
                    petMessage = "Chime... 📅 You have an appointment coming up! Let me remind you!";
                    break;
                default:
                    petMessage = "Beep! I have a reminder for you!";
            }
            
            // TODO: Implement pet message creation
            // emotionCompanionController.createMessage("pet", petMessage, "reminder", user.getId());
        } catch (Exception e) {
            System.err.println("Failed to send pet message to user " + user.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Send urgent reminder for medication
     * 
     * @param user User to send reminder to
     * @param medicationName Name of medication
     * @param dosage Dosage information
     */
    public void sendMedicationReminder(User user, String medicationName, String dosage) {
        String message = "URGENT: It's time to take your " + medicationName + " (" + dosage + "). Please take it now!";
        
        // Send immediate SMS
        try {
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                smsService.sendSMS(user.getPhoneNumber(), message, "MEDICATION_URGENT");
            }
        } catch (Exception e) {
            System.err.println("Failed to send urgent medication SMS: " + e.getMessage());
        }
        
        // Send pet message
        try {
            String petMessage = "Meow! 🚨 URGENT: It's time for your " + medicationName + "! Please take it right now!";
            // TODO: Implement companion message creation
            // emotionCompanionController.createMessage("companion", petMessage, "medication_urgent", user.getId());
        } catch (Exception e) {
            System.err.println("Failed to send urgent pet message: " + e.getMessage());
        }
    }

    /**
     * Send appointment reminder
     * 
     * @param user User to send reminder to
     * @param appointmentTitle Appointment title
     * @param appointmentTime Appointment time
     * @param location Location of appointment
     */
    public void sendAppointmentReminder(User user, String appointmentTitle, String appointmentTime, String location) {
        String message = "REMINDER: You have " + appointmentTitle + " at " + appointmentTime + " at " + location + ".";
        
        // Send SMS reminder
        try {
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                smsService.sendSMS(user.getPhoneNumber(), message, "APPOINTMENT");
            }
        } catch (Exception e) {
            System.err.println("Failed to send appointment SMS: " + e.getMessage());
        }
        
        // Send pet message
        try {
            String petMessage = "Purr... 📅 Don't forget your " + appointmentTitle + " at " + appointmentTime + "!";
            // TODO: Implement companion message creation
            // emotionCompanionController.createMessage("companion", petMessage, "appointment", user.getId());
        } catch (Exception e) {
            System.err.println("Failed to send appointment pet message: " + e.getMessage());
        }
    }
}
