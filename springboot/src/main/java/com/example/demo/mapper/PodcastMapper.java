package com.example.demo.mapper;

import com.example.demo.pojo.Podcast;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper interface for Podcast entity
 * 
 * This interface provides database operations for podcasts
 * including CRUD operations and custom queries.
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Mapper
public interface PodcastMapper {
    
    /**
     * Insert a new podcast
     * 
     * @param podcast The podcast to insert
     * @return Number of affected rows
     */
    int insert(Podcast podcast);
    
    /**
     * Update an existing podcast
     * 
     * @param podcast The podcast to update
     * @return Number of affected rows
     */
    int update(Podcast podcast);
    
    /**
     * Delete a podcast by ID
     * 
     * @param id The ID of the podcast to delete
     * @return Number of affected rows
     */
    int deleteById(Long id);
    
    /**
     * Find a podcast by ID
     * 
     * @param id The ID of the podcast
     * @return The podcast, or null if not found
     */
    Podcast findById(Long id);
    
    /**
     * Find all podcasts for a specific user
     * 
     * @param userId The user ID
     * @return List of podcasts
     */
    List<Podcast> findByUserId(Long userId);
    
    /**
     * Find all podcasts
     * 
     * @return List of all podcasts
     */
    @Select("SELECT * FROM podcasts")
    List<Podcast> findAll();
    
    /**
     * Count total number of podcasts
     * 
     * @return Total number of podcasts
     */
    @Select("SELECT COUNT(*) FROM podcasts")
    long count();
}

