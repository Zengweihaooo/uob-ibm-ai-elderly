package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.pojo.Memo;

/**
 * MyBatis Mapper interface for Memo entity
 * 
 * This interface provides database operations for memos
 * including CRUD operations and custom queries.
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Mapper
public interface MemoMapper {
    
    /**
     * Insert a new memo
     * 
     * @param memo The memo to insert
     * @return Number of affected rows
     */
    int insert(Memo memo);
    
    /**
     * Update an existing memo
     * 
     * @param memo The memo to update
     * @return Number of affected rows
     */
    int update(Memo memo);
    
    /**
     * Delete a memo by ID
     * 
     * @param id The ID of the memo to delete
     * @return Number of affected rows
     */
    int deleteById(Long id);
    
    /**
     * Find a memo by ID
     * 
     * @param id The ID of the memo
     * @return The memo, or null if not found
     */
    Memo findById(Long id);
    
    /**
     * Find all memos for a specific user
     * 
     * @param userId The user ID
     * @return List of memos
     */
    List<Memo> findByUserId(Long userId);
    
    /**
     * Find all memos
     * 
     * @return List of all memos
     */
    @Select("SELECT * FROM memos")
    List<Memo> findAll();
    
    /**
     * Count total number of memos
     * 
     * @return Total number of memos
     */
    @Select("SELECT COUNT(*) FROM memos")
    long count();
}

