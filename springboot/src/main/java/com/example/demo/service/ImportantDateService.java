package com.example.demo.service;

import com.example.demo.pojo.ImportantDate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for managing important dates (birthdays, anniversaries, holidays)
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Service
public class ImportantDateService {
    
    // In-memory storage for demo purposes
    private final Map<Long, ImportantDate> importantDates = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
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
        importantDate.setId(idGenerator.getAndIncrement());
        importantDate.setDescription(description);
        importantDate.setCreatedAt(LocalDateTime.now());
        importantDate.setUpdatedAt(LocalDateTime.now());
        
        importantDates.put(importantDate.getId(), importantDate);
        return importantDate;
    }
    
    /**
     * Get all important dates for a user
     * 
     * @param userId User ID
     * @return List of important dates
     */
    public List<ImportantDate> getImportantDatesByUser(Long userId) {
        return importantDates.values().stream()
                .filter(date -> date.getUserId().equals(userId))
                .filter(ImportantDate::isEnabled)
                .sorted(Comparator.comparing(ImportantDate::getDate))
                .toList();
    }
    
    /**
     * Get important dates by type
     * 
     * @param userId User ID
     * @param type Date type
     * @return List of important dates of specified type
     */
    public List<ImportantDate> getImportantDatesByType(Long userId, String type) {
        return importantDates.values().stream()
                .filter(date -> date.getUserId().equals(userId))
                .filter(date -> date.getType().equals(type))
                .filter(ImportantDate::isEnabled)
                .sorted(Comparator.comparing(ImportantDate::getDate))
                .toList();
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
        
        return importantDates.values().stream()
                .filter(date -> date.getUserId().equals(userId))
                .filter(ImportantDate::isEnabled)
                .filter(date -> {
                    LocalDate nextOccurrence = getNextOccurrence(date.getDate(), today);
                    return !nextOccurrence.isBefore(today) && !nextOccurrence.isAfter(thirtyDaysFromNow);
                })
                .sorted(Comparator.comparing(date -> getNextOccurrence(date.getDate(), today)))
                .toList();
    }
    
    /**
     * Get today's important dates
     * 
     * @param userId User ID
     * @return List of today's important dates
     */
    public List<ImportantDate> getTodayImportantDates(Long userId) {
        LocalDate today = LocalDate.now();
        
        return importantDates.values().stream()
                .filter(date -> date.getUserId().equals(userId))
                .filter(ImportantDate::isEnabled)
                .filter(date -> {
                    LocalDate nextOccurrence = getNextOccurrence(date.getDate(), today);
                    return nextOccurrence.equals(today);
                })
                .toList();
    }
    
    /**
     * Update an important date
     * 
     * @param id Important date ID
     * @param updatedDate Updated important date
     * @return Updated important date
     */
    public ImportantDate updateImportantDate(Long id, ImportantDate updatedDate) {
        ImportantDate existing = importantDates.get(id);
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
        
        return existing;
    }
    
    /**
     * Delete an important date
     * 
     * @param id Important date ID
     * @return true if deleted, false if not found
     */
    public boolean deleteImportantDate(Long id) {
        return importantDates.remove(id) != null;
    }
    
    /**
     * Toggle important date enabled status
     * 
     * @param id Important date ID
     * @return Updated important date
     */
    public ImportantDate toggleImportantDate(Long id) {
        ImportantDate date = importantDates.get(id);
        if (date == null) {
            throw new IllegalArgumentException("Important date not found with ID: " + id);
        }
        
        date.setEnabled(!date.isEnabled());
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
        List<ImportantDate> holidays = new ArrayList<>();
        
        // Add some common holidays
        holidays.add(new ImportantDate(userId, "New Year's Day", LocalDate.of(2025, 1, 1), "holiday"));
        holidays.add(new ImportantDate(userId, "Valentine's Day", LocalDate.of(2025, 2, 14), "holiday"));
        holidays.add(new ImportantDate(userId, "Easter Sunday", LocalDate.of(2025, 4, 20), "holiday"));
        holidays.add(new ImportantDate(userId, "Mother's Day", LocalDate.of(2025, 5, 11), "holiday"));
        holidays.add(new ImportantDate(userId, "Father's Day", LocalDate.of(2025, 6, 15), "holiday"));
        holidays.add(new ImportantDate(userId, "Independence Day", LocalDate.of(2025, 7, 4), "holiday"));
        holidays.add(new ImportantDate(userId, "Labor Day", LocalDate.of(2025, 9, 1), "holiday"));
        holidays.add(new ImportantDate(userId, "Thanksgiving", LocalDate.of(2025, 11, 27), "holiday"));
        holidays.add(new ImportantDate(userId, "Christmas", LocalDate.of(2025, 12, 25), "holiday"));
        
        // Set IDs and timestamps
        for (ImportantDate holiday : holidays) {
            holiday.setId(idGenerator.getAndIncrement());
            holiday.setCreatedAt(LocalDateTime.now());
            holiday.setUpdatedAt(LocalDateTime.now());
            importantDates.put(holiday.getId(), holiday);
        }
        
        return holidays;
    }
} 