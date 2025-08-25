package com.example.demo.mapper;

import com.example.demo.pojo.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * MyBatis Mapper interface for Schedule operations
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Mapper
public interface ScheduleMapper {
    
    /**
     * Insert a new schedule
     * 
     * @param schedule Schedule to insert
     * @return Number of affected rows
     */
    int insert(Schedule schedule);
    
    /**
     * Update an existing schedule
     * 
     * @param schedule Schedule to update
     * @return Number of affected rows
     */
    int update(Schedule schedule);
    
    /**
     * Delete a schedule by ID
     * 
     * @param id Schedule ID
     * @return Number of affected rows
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * Find schedule by ID
     * 
     * @param id Schedule ID
     * @return Schedule or null if not found
     */
    Schedule findById(@Param("id") Long id);
    
    /**
     * Find all schedules for a user on a specific date
     * 
     * @param userId User ID
     * @param scheduleDate Target date
     * @return List of schedules
     */
    List<Schedule> findByUserIdAndDate(@Param("userId") Long userId, @Param("scheduleDate") LocalDate scheduleDate);
    
    /**
     * Find all schedules for a user
     * 
     * @param userId User ID
     * @return List of schedules
     */
    List<Schedule> findByUserId(@Param("userId") Long userId);
    
    /**
     * Update completion status of a schedule
     * 
     * @param id Schedule ID
     * @param completed Completion status
     * @return Number of affected rows
     */
    int updateCompletionStatus(@Param("id") Long id, @Param("completed") boolean completed);
    
    /**
     * Find schedules by user and date range
     * 
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of schedules
     */
    List<Schedule> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Find incomplete schedules for a user
     * 
     * @param userId User ID
     * @return List of incomplete schedules
     */
    List<Schedule> findIncompleteByUserId(@Param("userId") Long userId);
    
    /**
     * Count schedules for a user on a specific date
     * 
     * @param userId User ID
     * @param scheduleDate Target date
     * @return Number of schedules
     */
    int countByUserIdAndDate(@Param("userId") Long userId, @Param("scheduleDate") LocalDate scheduleDate);
    
    /**
     * Find all schedules
     * 
     * @return List of all schedules
     */
    @Select("SELECT * FROM schedules")
    List<Schedule> findAll();
    
    /**
     * Count total number of schedules
     * 
     * @return Total number of schedules
     */
    @Select("SELECT COUNT(*) FROM schedules")
    long count();
}
