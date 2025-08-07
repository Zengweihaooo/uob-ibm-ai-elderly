package com.example.demo.mapper;

import com.example.demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MyBatis Mapper interface for User entity
 * 
 * This interface provides database operations for users
 * including CRUD operations and custom queries.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Mapper
public interface UserMapper {
    
    /**
     * Insert a new user
     * 
     * @param user The user to insert
     * @return Number of affected rows
     */
    int insert(User user);
    
    /**
     * Update an existing user
     * 
     * @param user The user to update
     * @return Number of affected rows
     */
    int update(User user);
    
    /**
     * Delete a user by ID
     * 
     * @param id The ID of the user to delete
     * @return Number of affected rows
     */
    int deleteById(Long id);
    
    /**
     * Find a user by ID
     * 
     * @param id The ID of the user
     * @return The user, or null if not found
     */
    User findById(Long id);
    
    /**
     * Find a user by email
     * 
     * @param email The email address
     * @return The user, or null if not found
     */
    User findByEmail(String email);
    
    /**
     * Find all users
     * 
     * @return List of all users
     */
    List<User> findAll();
    
    /**
     * Find users by status
     * 
     * @param status The user status
     * @return List of users with the specified status
     */
    List<User> findByStatus(String status);
    
    /**
     * Find users by role
     * 
     * @param role The user role
     * @return List of users with the specified role
     */
    List<User> findByRole(String role);
    
    /**
     * Find verified users
     * 
     * @return List of verified users
     */
    List<User> findVerifiedUsers();
    
    /**
     * Find users with expired verification codes
     * 
     * @return List of users with expired codes
     */
    List<User> findUsersWithExpiredCodes();
    
    /**
     * Update user verification status
     * 
     * @param userId The user ID
     * @param status The new status
     * @param verifiedAt The verification time
     * @return Number of affected rows
     */
    int updateVerificationStatus(@Param("userId") Long userId, 
                                @Param("status") String status,
                                @Param("verifiedAt") LocalDateTime verifiedAt);
    
    /**
     * Update verification code
     * 
     * @param userId The user ID
     * @param verificationCode The verification code
     * @param codeExpiresAt The code expiry time
     * @return Number of affected rows
     */
    int updateVerificationCode(@Param("userId") Long userId,
                              @Param("verificationCode") String verificationCode,
                              @Param("codeExpiresAt") LocalDateTime codeExpiresAt);
    
    /**
     * Count users by status
     * 
     * @param status The user status
     * @return Number of users with the specified status
     */
    int countByStatus(String status);
    
    /**
     * Count users by role
     * 
     * @param role The user role
     * @return Number of users with the specified role
     */
    int countByRole(String role);
    
    /**
     * Find users created within a date range
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of users created within the date range
     */
    List<User> findByCreatedDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
} 