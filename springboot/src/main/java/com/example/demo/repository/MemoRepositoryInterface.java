package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.pojo.Memo;

/**
 * Memo data access interface
 * Defines unified data access methods with multiple database implementations
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
public interface MemoRepositoryInterface {
    
    /**
     * Save a memo
     * @param memo Memo object
     * @return Saved memo object
     */
    Memo save(Memo memo);
    
    /**
     * Find memo by ID
     * @param id Memo ID
     * @return Memo object, or Optional.empty() if not found
     */
    Optional<Memo> findById(Long id);
    
    /**
     * Find non-deleted memo by user ID and memo ID
     * @param userId User ID
     * @param memoId Memo ID
     * @return Memo object, or Optional.empty() if not found
     */
    Optional<Memo> findByUserIdAndId(Long userId, Long memoId);
    
    /**
     * Find all non-deleted memos by user ID
     * @param userId User ID
     * @return List of memos
     */
    List<Memo> findByUserId(Long userId);
    
    /**
     * Find non-deleted memos by user ID and type
     * @param userId User ID
     * @param type Memo type
     * @return List of memos
     */
    List<Memo> findByUserIdAndType(Long userId, String type);
    
    /**
     * Find important memos by user ID
     * @param userId user ID
     * @return important memo list
     */
    List<Memo> findImportantByUserId(Long userId);
    
    /**
     * Find memos by user ID and PIN code
     * @param userId User ID
     * @param pinCode PIN code
     * @return List of memos
     */
    List<Memo> findByUserIdAndPinCode(Long userId, String pinCode);
    
    /**
     * Search memos (title or content contains keyword)
     * @param userId User ID
     * @param keyword Search keyword
     * @return Search results
     */
    List<Memo> searchByKeyword(Long userId, String keyword);
    
    /**
     * Soft delete memo
     * @param userId User ID
     * @param memoId Memo ID
     * @return Number of affected rows
     */
    int softDelete(Long userId, Long memoId);
    
    /**
     * Count memos by user ID
     * @param userId User ID
     * @return Number of memos
     */
    long countByUserId(Long userId);
    
    /**
     * Count important memos by user ID
     * @param userId user ID
     * @return number of important memos
     */
    long countImportantByUserId(Long userId);
    
    /**
     * Count memos by user ID and type
     * @param userId User ID
     * @param type Memo type
     * @return Number of memos of the type
     */
    long countByUserIdAndType(Long userId, String type);
    
    /**
     * Check if user has memos with specified PIN code
     * @param userId User ID
     * @param pinCode PIN code
     * @return Whether exists
     */
    boolean existsByUserIdAndPinCode(Long userId, String pinCode);
    
    /**
     * Get all memos (for data migration)
     * @return List of all memos
     */
    List<Memo> findAll();
    
    /**
     * Count total records (for data migration)
     * @return Total count
     */
    long count();
} 
 
 