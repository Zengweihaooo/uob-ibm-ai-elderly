package com.example.demo.service;

import com.example.demo.mapper.ImportantDateMapper;
import com.example.demo.pojo.ImportantDate;
import com.example.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing important dates (birthdays, anniversaries, holidays)
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Service
public class ImportantDateService {

    @Autowired
    private ImportantDateMapper importantDateMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;
    
    /**
     * Add a new important date
     * 
     * @param userId User ID
     * @param title Date title
     * @param date Important date
     * @param type Date type (birthday, anniversary, holiday, custom)
     * @param description Optional description
     * @return Created important date
     */
    public ImportantDate addImportantDate(Long userId, String title, LocalDate date,
                                          String type, String description) {
        ImportantDate importantDate = new ImportantDate(userId, title, date, type);
        importantDate.setDescription(description);
        importantDate.setCreatedAt(LocalDateTime.now());
        importantDate.setUpdatedAt(LocalDateTime.now());

        importantDateMapper.insert(importantDate);
        return importantDate;
    }
    
    /**
     * Get all important dates for a user
     * 
     * @param userId User ID
     * @return List of important dates
     */
    public List<ImportantDate> getImportantDatesByUser(Long userId) {
        return importantDateMapper.findByUserId(userId);
    }
    
    /**
     * Get important dates by type
     * 
     * @param userId User ID
     * @param type Date type
     * @return List of important dates of specified type
     */
    public List<ImportantDate> getImportantDatesByType(Long userId, String type) {
        return importantDateMapper.findByUserIdAndType(userId, type);
    }
    
    /**
     * Get upcoming important dates (within next 30 days)
     * 
     * @param userId User ID
     * @return List of upcoming important dates
     */
    public List<ImportantDate> getUpcomingImportantDates(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        List<ImportantDate> all = importantDateMapper.findByUserId(userId);
        List<ImportantDate> filtered = new ArrayList<>();
        for (ImportantDate date : all) {
            if (!date.isEnabled()) continue;
            LocalDate nextOccurrence = getNextOccurrence(date.getDate(), today);
            if (!nextOccurrence.isBefore(today) && !nextOccurrence.isAfter(thirtyDaysFromNow)) {
                filtered.add(date);
            }
        }
        filtered.sort(Comparator.comparing(d -> getNextOccurrence(d.getDate(), today)));
        return filtered;
    }
    
    /**
     * Get today's important dates
     * 
     * @param userId User ID
     * @return List of today's important dates
     */
    public List<ImportantDate> getTodayImportantDates(Long userId) {
        LocalDate today = LocalDate.now();
        
        List<ImportantDate> all = importantDateMapper.findByUserId(userId);
        List<ImportantDate> todayList = new ArrayList<>();
        for (ImportantDate date : all) {
            if (!date.isEnabled()) continue;
            LocalDate nextOccurrence = getNextOccurrence(date.getDate(), today);
            if (nextOccurrence.equals(today)) {
                todayList.add(date);
            }
        }
        return todayList;
    }
    
    /**
     * Update an important date
     * 
     * @param id Important date ID
     * @param updatedDate Updated important date
     * @return Updated important date
     */
    public ImportantDate updateImportantDate(Long id, ImportantDate updatedDate) {
        ImportantDate existing = importantDateMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Important date not found with ID: " + id);
        }

        existing.setTitle(updatedDate.getTitle());
        existing.setDescription(updatedDate.getDescription());
        existing.setDate(updatedDate.getDate());
        existing.setType(updatedDate.getType());
        existing.setRepeatCycle(updatedDate.getRepeatCycle());
        existing.setEnabled(updatedDate.isEnabled());
        existing.setUpdatedAt(LocalDateTime.now());

        importantDateMapper.update(existing);
        return existing;
    }
    
    /**
     * Delete an important date
     * 
     * @param id Important date ID
     * @return true if deleted, false if not found
     */
    public boolean deleteImportantDate(Long id) {
        return importantDateMapper.deleteById(id) > 0;
    }
    
    /**
     * Toggle important date enabled status
     * 
     * @param id Important date ID
     * @return Updated important date
     */
    public ImportantDate toggleImportantDate(Long id) {
        ImportantDate date = importantDateMapper.findById(id);
        if (date == null) {
            throw new IllegalArgumentException("Important date not found with ID: " + id);
        }
        boolean newEnabled = !date.isEnabled();
        importantDateMapper.setEnabled(id, newEnabled, LocalDateTime.now());
        date.setEnabled(newEnabled);
        date.setUpdatedAt(LocalDateTime.now());
        return date;
    }

    /**
     * Explicitly set enabled status for an important date
     * 
     * @param id Important date ID
     * @param enabled Target enabled status
     * @return Updated important date
     */
    public ImportantDate setImportantDateEnabled(Long id, boolean enabled) {
        ImportantDate date = importantDateMapper.findById(id);
        if (date == null) {
            throw new IllegalArgumentException("Important date not found with ID: " + id);
        }
        importantDateMapper.setEnabled(id, enabled, LocalDateTime.now());
        date.setEnabled(enabled);
        date.setUpdatedAt(LocalDateTime.now());
        return date;
    }
    
    /**
     * Get next occurrence of a date (for yearly repeating dates)
     * 
     * @param originalDate Original date
     * @param fromDate Starting date to search from
     * @return Next occurrence date
     */
    private LocalDate getNextOccurrence(LocalDate originalDate, LocalDate fromDate) {
        int currentYear = fromDate.getYear();
        LocalDate thisYearOccurrence = originalDate.withYear(currentYear);
        
        if (thisYearOccurrence.isBefore(fromDate)) {
            return originalDate.withYear(currentYear + 1);
        } else {
            return thisYearOccurrence;
        }
    }
    
    /**
     * Get important date statistics
     * 
     * @param userId User ID
     * @return Statistics map
     */
    public Map<String, Object> getImportantDateStats(Long userId) {
        List<ImportantDate> userDates = getImportantDatesByUser(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", userDates.size());
        stats.put("birthdays", (int) userDates.stream().filter(d -> d.getType().equals("birthday")).count());
        stats.put("anniversaries", (int) userDates.stream().filter(d -> d.getType().equals("anniversary")).count());
        stats.put("holidays", (int) userDates.stream().filter(d -> d.getType().equals("holiday")).count());
        stats.put("custom", (int) userDates.stream().filter(d -> d.getType().equals("custom")).count());
        stats.put("upcoming", getUpcomingImportantDates(userId).size());
        stats.put("today", getTodayImportantDates(userId).size());
        
        return stats;
    }
    
    /**
     * Get default holidays for a user
     * 
     * @param userId User ID
     * @return List of default holidays
     */
    public List<ImportantDate> getDefaultHolidays(Long userId) {
        // Define default holidays
        List<ImportantDate> defaults = new ArrayList<>();
        defaults.add(new ImportantDate(userId, "New Year's Day", LocalDate.of(2025, 1, 1), "holiday"));
        defaults.add(new ImportantDate(userId, "Valentine's Day", LocalDate.of(2025, 2, 14), "holiday"));
        defaults.add(new ImportantDate(userId, "Easter Sunday", LocalDate.of(2025, 4, 20), "holiday"));
        defaults.add(new ImportantDate(userId, "Mother's Day", LocalDate.of(2025, 5, 11), "holiday"));
        defaults.add(new ImportantDate(userId, "Father's Day", LocalDate.of(2025, 6, 15), "holiday"));
        defaults.add(new ImportantDate(userId, "Independence Day", LocalDate.of(2025, 7, 4), "holiday"));
        defaults.add(new ImportantDate(userId, "Labor Day", LocalDate.of(2025, 9, 1), "holiday"));
        defaults.add(new ImportantDate(userId, "Thanksgiving", LocalDate.of(2025, 11, 27), "holiday"));
        defaults.add(new ImportantDate(userId, "Christmas", LocalDate.of(2025, 12, 25), "holiday"));

        // Insert those not existing yet
        for (ImportantDate d : defaults) {
            int exists = importantDateMapper.existsByUserTitleDateType(userId, d.getTitle(), d.getDate(), d.getType());
            if (exists == 0) {
                d.setEnabled(true);
                d.setRepeatCycle("yearly");
                d.setCreatedAt(LocalDateTime.now());
                d.setUpdatedAt(LocalDateTime.now());
                importantDateMapper.insert(d);
            }
        }

        // Return the persisted default holiday rows (with IDs)
        List<ImportantDate> all = importantDateMapper.findByUserId(userId);
        List<ImportantDate> result = new ArrayList<>();
        for (ImportantDate persisted : all) {
            if (!"holiday".equals(persisted.getType())) continue;
            for (ImportantDate def : defaults) {
                if (persisted.getTitle().equals(def.getTitle()) &&
                    persisted.getDate().equals(def.getDate())) {
                    result.add(persisted);
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Send email reminder for important dates
     * 
     * @param importantDate The important date
     * @param reminderType The type of reminder (week/day)
     */
    public void sendImportantDateReminder(ImportantDate importantDate, String reminderType) {
        try {
            // Get user information
            User user = userService.getUserById(importantDate.getUserId());
            if (user == null || user.getEmail() == null) {
                System.err.println("User not found or email not available for user ID: " + importantDate.getUserId());
                return;
            }
            
            // Send email reminder
            emailService.sendImportantDateReminderEmail(
                user.getEmail(), 
                user.getName(), 
                importantDate, 
                reminderType
            );
            
            // Update reminder sent timestamp
            if ("week".equals(reminderType)) {
                importantDate.setWeekReminderSent(LocalDateTime.now());
            } else if ("day".equals(reminderType)) {
                importantDate.setDayReminderSent(LocalDateTime.now());
            }
            
            System.out.println("Important date reminder sent successfully for: " + importantDate.getTitle() + 
                             " (reminder type: " + reminderType + ")");
        } catch (Exception e) {
            System.err.println("Failed to send important date reminder: " + e.getMessage());
        }
    }
    
    /**
     * Get important dates that need week reminders (7 days before)
     * 
     * @return List of important dates needing week reminders
     */
    public List<ImportantDate> getImportantDatesNeedingWeekReminders() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        List<ImportantDate> list = new ArrayList<>();
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            List<ImportantDate> all = importantDateMapper.findByUserId(user.getId());
            for (ImportantDate date : all) {
                if (!date.isEnabled()) continue;
                LocalDate nextOccurrence = getNextOccurrence(date.getDate(), today);
                if (nextOccurrence.equals(weekFromNow) && date.getWeekReminderSent() == null) {
                    list.add(date);
                }
            }
        }
        return list;
    }
    
    /**
     * Get important dates that need day reminders (1 day before)
     * 
     * @return List of important dates needing day reminders
     */
    public List<ImportantDate> getImportantDatesNeedingDayReminders() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        List<ImportantDate> list = new ArrayList<>();
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            List<ImportantDate> all = importantDateMapper.findByUserId(user.getId());
            for (ImportantDate date : all) {
                if (!date.isEnabled()) continue;
                LocalDate nextOccurrence = getNextOccurrence(date.getDate(), today);
                if (nextOccurrence.equals(tomorrow) && date.getDayReminderSent() == null) {
                    list.add(date);
                }
            }
        }
        return list;
    }
    
    /**
     * Send reminders for all important dates that need them
     */
    public void sendAllPendingReminders() {
        // Send week reminders
        List<ImportantDate> weekReminders = getImportantDatesNeedingWeekReminders();
        for (ImportantDate date : weekReminders) {
            sendImportantDateReminder(date, "week");
            importantDateMapper.updateWeekReminderSent(date.getId(), LocalDateTime.now());
        }
        
        // Send day reminders
        List<ImportantDate> dayReminders = getImportantDatesNeedingDayReminders();
        for (ImportantDate date : dayReminders) {
            sendImportantDateReminder(date, "day");
            importantDateMapper.updateDayReminderSent(date.getId(), LocalDateTime.now());
        }
        
        System.out.println("Sent " + weekReminders.size() + " week reminders and " + 
                         dayReminders.size() + " day reminders");
    }
} 