package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.demo.service.ScheduleMonitorService;
import com.example.demo.service.HealthService;
import com.example.demo.service.ScheduleService;
import com.example.demo.service.EmailService;
import com.example.demo.pojo.User;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;


public class ScheduleMonitorServiceTest {
    @InjectMocks
    private ScheduleMonitorService scheduleMonitorService;

    @Mock
    private HealthService healthService;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

// Test the checkHealthSubmission method
@Test
public void testCheckHealthSubmission() {
    // Mock testing data
    User user1 = new User("a@example.com", "A");
    User user2 = new User("b@example.com", "B");
    User user3 = new User("c@example.com", "C");
    // Define users who have not submitted health data today
    List<User> usersWithoutTodayHealthLog = Arrays.asList(user1, user2, user3);
    // If haven't received any health data today, return the users without today's health data
    when(healthService.getUsersWithoutTodayHealthLog()).thenReturn(usersWithoutTodayHealthLog);
    // Call the method to be tested
    scheduleMonitorService.checkHealthSubmission();

    
    // Verify that the emailService.sendHealthAlertEmail method was called for each user
    verify(emailService, times(1)).sendHealthAlertEmail(
        user1.getEmail(),
        "Daily Health Check Reminder",
        "Hello " + user1.getName() + ", you haven’t submitted today’s health data. Please do it soon."
    );
    verify(emailService, times(1)).sendHealthAlertEmail(
        user2.getEmail(),
        "Daily Health Check Reminder",
        "Hello " + user2.getName() + ", you haven’t submitted today’s health data. Please do it soon."
    );
    verify(emailService, times(1)).sendHealthAlertEmail(
        user3.getEmail(),
        "Daily Health Check Reminder",
        "Hello " + user3.getName() + ", you haven’t submitted today’s health data. Please do it soon."
    );
}


// Test the two methods when there are no users without today's health data
// i.e. all users have submitted the health data / schedule today
@Test
public void testHealthAllGood(){
    List<User> usersWithoutTodayHealthLog = Arrays.asList();
    when(healthService.getUsersWithoutTodayHealthLog()).thenReturn(usersWithoutTodayHealthLog);
    scheduleMonitorService.checkHealthSubmission();

    verify(emailService, times(0)).sendHealthAlertEmail(
        org.mockito.ArgumentMatchers.anyString(), 
        org.mockito.ArgumentMatchers.anyString(), 
        org.mockito.ArgumentMatchers.anyString()
    );
}



// Test the checkScheduleSubmission method
@Test
public void testCheckScheduleSubmission() {
    // Mock testing data
    User user1 = new User("a@example.com", "A");
    User user2 = new User("b@example.com", "B");
    User user3 = new User("c@example.com", "C");
    // Define users who have not submitted schedule data today
    List<User> usersWithoutTodaySchedule = Arrays.asList(user1, user2, user3);
    // If haven't received any schedule data today, return the users without today's schedule data
    when(scheduleService.getUsersWithoutTodaySchedule()).thenReturn(usersWithoutTodaySchedule);
    // Call the method to be tested
    scheduleMonitorService.checkScheduleSubmission();

    // Verify that the emailService.sendHealthAlertEmail method was called for each user
    verify(emailService, times(1)).sendHealthAlertEmail(
        user1.getEmail(),
        "Daily Plan Reminder",
        "Hello " + user1.getName() + ", you haven’t told your pet your plan for today. Please do it now."
    );
    verify(emailService, times(1)).sendHealthAlertEmail(
        user2.getEmail(),
        "Daily Plan Reminder",
        "Hello " + user2.getName() + ", you haven’t told your pet your plan for today. Please do it now."
    );
    verify(emailService, times(1)).sendHealthAlertEmail(
        user3.getEmail(),
        "Daily Plan Reminder",
        "Hello " + user3.getName() + ", you haven’t told your pet your plan for today. Please do it now."
    );
}

@Test
public void testScheduleAllGood(){
    List<User> usersWithoutTodaySchedule = Arrays.asList();
    when(scheduleService.getUsersWithoutTodaySchedule()).thenReturn(usersWithoutTodaySchedule);
    scheduleMonitorService.checkScheduleSubmission();

    verify(emailService, times(0)).sendHealthAlertEmail(
        org.mockito.ArgumentMatchers.anyString(), 
        org.mockito.ArgumentMatchers.anyString(), 
        org.mockito.ArgumentMatchers.anyString()
    );
}



}
