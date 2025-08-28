package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.pojo.FamilyContact;

/**
 * MyBatis Mapper interface for FamilyContact entity
 * 
 * This interface provides database operations for family contacts
 * including CRUD operations and custom queries.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Mapper
public interface FamilyContactMapper {
    
    /**
     * Insert a new family contact
     * 
     * @param familyContact The family contact to insert
     * @return Number of affected rows
     */
    int insert(FamilyContact familyContact);
    
    /**
     * Update an existing family contact
     * 
     * @param familyContact The family contact to update
     * @return Number of affected rows
     */
    int update(FamilyContact familyContact);
    
    /**
     * Delete a family contact by ID
     * 
     * @param id The ID of the family contact to delete
     * @return Number of affected rows
     */
    int deleteById(Long id);
    
    /**
     * Find a family contact by ID
     * 
     * @param id The ID of the family contact
     * @return The family contact, or null if not found
     */
    FamilyContact findById(Long id);
    
    /**
     * Find all family contacts for a specific user
     * 
     * @param userId The user ID
     * @return List of family contacts
     */
    List<FamilyContact> findByUserId(Long userId);
    
    /**
     * Find all active family contacts for a specific user
     * 
     * @param userId The user ID
     * @return List of active family contacts
     */
    List<FamilyContact> findActiveByUserId(Long userId);
    
    /**
     * Find all emergency contacts for a specific user
     * 
     * @param userId The user ID
     * @return List of emergency contacts
     */
    List<FamilyContact> findEmergencyContactsByUserId(Long userId);
    
    /**
     * Find family contacts by relationship type
     * 
     * @param userId The user ID
     * @param relationship The relationship type
     * @return List of family contacts with the specified relationship
     */
    List<FamilyContact> findByRelationship(@Param("userId") Long userId, @Param("relationship") String relationship);
    
    /**
     * Count family contacts for a specific user
     * 
     * @param userId The user ID
     * @return Number of family contacts
     */
    int countByUserId(Long userId);
    
    /**
     * Find family contacts by phone number
     * 
     * @param phone The phone number
     * @return List of family contacts with the specified phone number
     */
    List<FamilyContact> findByPhone(String phone);
    
    /**
     * Find family contacts by email
     * 
     * @param email The email address
     * @return List of family contacts with the specified email
     */
    List<FamilyContact> findByEmail(String email);
    
    /**
     * Find all family contacts
     * 
     * @return List of all family contacts
     */
    List<FamilyContact> findAll();
    
    /**
     * Count total number of family contacts
     * 
     * @return Total number of family contacts
     */
    long count();
} 