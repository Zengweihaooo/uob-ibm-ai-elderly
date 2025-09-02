package com.example.demo.repository;

import com.example.demo.pojo.PetMood;
import java.util.List;
import java.util.Optional;

/**
 * Pet mood data access interface
 * Defines unified data access methods with multiple database implementations
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
public interface PetMoodRepository {
    
    /**
     * Find pet mood by user ID
     * @param userId User ID
     * @return Pet mood object, or Optional.empty() if not found
     */
    Optional<PetMood> findByUserId(Long userId);
    
    /**
     * Save pet mood record
     * @param petMood Pet mood object
     * @return Saved pet mood object
     */
    PetMood save(PetMood petMood);
    
    /**
     * Update pet mood record
     * @param petMood Pet mood object
     * @return Updated pet mood object
     */
    PetMood update(PetMood petMood);
    
    /**
     * Delete pet mood record by user ID
     * @param userId User ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * Check if user has a pet mood record
     * @param userId User ID
     * @return Whether a record exists
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Get all pet mood records
     * @return List of all records
     */
    List<PetMood> findAll();
    
    /**
     * Find records by mood score range
     * @param minScore Minimum score
     * @param maxScore Maximum score
     * @return List of matching records
     */
    List<PetMood> findByMoodScoreBetween(int minScore, int maxScore);
    
    /**
     * Count total number of records
     * @return Total count
     */
    long count();
}


