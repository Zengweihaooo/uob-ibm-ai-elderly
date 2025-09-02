package com.example.demo.repository;

import com.example.demo.pojo.User;
import java.util.List;
import java.util.Optional;

/**
 * User data access interface
 * Defines unified user data access methods with multiple database implementations
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
public interface UserRepository {
    
    /**
     * Find user by ID
     * @param id User ID
     * @return User object, or Optional.empty() if not found
     */
    Optional<User> findById(Long id);
    
    /**
     * Find user by email
     * @param email Email address
     * @return User object, or Optional.empty() if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Save user
     * @param user User object
     * @return Saved user object
     */
    User save(User user);
    
    /**
     * Update user
     * @param user User object
     * @return Updated user object
     */
    User update(User user);
    
    /**
     * Delete user by ID
     * @param id User ID
     */
    void deleteById(Long id);
    
    /**
     * Get all users
     * @return List of all users
     */
    List<User> findAll();
    
    /**
     * Find users by status
     * @param status User status
     * @return List of matching users
     */
    List<User> findByStatus(String status);
    
    /**
     * Find users by role
     * @param role User role
     * @return List of matching users
     */
    List<User> findByRole(String role);
    
    /**
     * Count total number of users
     * @return Total number of users
     */
    long count();
}
