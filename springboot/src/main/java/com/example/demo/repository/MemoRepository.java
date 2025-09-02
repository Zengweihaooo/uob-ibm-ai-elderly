package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.pojo.Memo;

/**
 * Memo data access layer - MyBatis implementation
 * Provides database operations for memos
 */
@Repository("sqliteMemoRepository")
@Mapper
public interface MemoRepository {
    
    /**
     * Insert new memo
     * @param memo memo object
     * @return number of affected rows
     */
    int insert(Memo memo);
    
    /**
     * Save memo (insert or update)
     * @param memo memo object
     * @return number of affected rows
     */
    int save(Memo memo);
    
    /**
     * Update memo
     * @param memo memo object
     * @return number of affected rows
     */
    int update(Memo memo);
    
    /**
     * Find memo by ID
     * @param id memo ID
     * @return memo object, returns Optional.empty() if not found
     */
    Optional<Memo> findById(Long id);
    
    /**
     * Find undeleted memo by user ID and memo ID
     * @param userId user ID
     * @param memoId memo ID
     * @return memo object, returns Optional.empty() if not found
     */
    Optional<Memo> findByUserIdAndId(@Param("userId") Long userId, @Param("memoId") Long memoId);
    
    /**
     * Find all undeleted memos by user ID
     * @param userId user ID
     * @return memo list
     */
    List<Memo> findByUserId(Long userId);
    
    /**
     * Find undeleted memos by user ID and type
     * @param userId user ID
     * @param type memo type
     * @return memo list
     */
    List<Memo> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    /**
     * Find important memos by user ID
     * @param userId user ID
     * @return important memo list
     */
    List<Memo> findImportantByUserId(Long userId);
    
    /**
     * Find memos by user ID and PIN code
     * @param userId user ID
     * @param pinCode PIN code
     * @return memo list
     */
    List<Memo> findByUserIdAndPinCode(@Param("userId") Long userId, @Param("pinCode") String pinCode);
    
    /**
     * Search memos (title or content contains keyword)
     * @param userId user ID
     * @param keyword search keyword
     * @return search results
     */
    List<Memo> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    /**
     * Soft delete memo
     * @param userId user ID
     * @param memoId memo ID
     * @return number of affected rows
     */
    int softDelete(@Param("userId") Long userId, @Param("memoId") Long memoId);
    
    /**
     * Count memos by user ID
     * @param userId user ID
     * @return number of memos
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
     * @param userId user ID
     * @param type memo type
     * @return number of memos of this type
     */
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    /**
     * Check if user has memos with the specified PIN code
     * @param userId user ID
     * @param pinCode PIN code
     * @return true if exists, false otherwise
     */
    boolean existsByUserIdAndPinCode(@Param("userId") Long userId, @Param("pinCode") String pinCode);
    
    /**
     * Get all memos (for data migration)
     * @return all memo list
     */
    List<Memo> findAll();
    
    /**
     * Count total records (for data migration)
     * @return total records
     */
    long count();
}
