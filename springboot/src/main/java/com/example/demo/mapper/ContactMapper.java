package com.example.demo.mapper;

import com.example.demo.pojo.Contact;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Mapper interface for Contact operations
 * 
 * @author AI Assistant
 * @version 1.0
 */
@Mapper
public interface ContactMapper {
    
    /**
     * Insert a new contact
     */
    @Insert("INSERT INTO contacts (user_id, name, email, phone, relationship, created_at, updated_at) " +
            "VALUES (#{userId}, #{name}, #{email}, #{phone}, #{relationship}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Contact contact);
    
    /**
     * Update contact information
     */
    @Update("UPDATE contacts SET name = #{name}, email = #{email}, phone = #{phone}, " +
            "relationship = #{relationship}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Contact contact);
    
    /**
     * Find contact by ID
     */
    @Select("SELECT * FROM contacts WHERE id = #{id}")
    Contact findById(Long id);
    
    /**
     * Find all contacts for a specific user
     */
    @Select("SELECT * FROM contacts WHERE user_id = #{userId} ORDER BY name ASC")
    List<Contact> findByUserId(Long userId);
    
    /**
     * Find contacts by relationship type for a user
     */
    @Select("SELECT * FROM contacts WHERE user_id = #{userId} AND relationship = #{relationship} ORDER BY name ASC")
    List<Contact> findByUserIdAndRelationship(@Param("userId") Long userId, @Param("relationship") String relationship);
    
    /**
     * Find contact by email for a specific user
     */
    @Select("SELECT * FROM contacts WHERE user_id = #{userId} AND email = #{email}")
    Contact findByUserIdAndEmail(@Param("userId") Long userId, @Param("email") String email);
    
    /**
     * Search contacts by name for a user
     */
    @Select("SELECT * FROM contacts WHERE user_id = #{userId} AND name LIKE CONCAT('%', #{name}, '%') ORDER BY name ASC")
    List<Contact> searchByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);
    
    /**
     * Delete contact by ID
     */
    @Delete("DELETE FROM contacts WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * Delete all contacts for a user
     */
    @Delete("DELETE FROM contacts WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
    
    /**
     * Count contacts for a user
     */
    @Select("SELECT COUNT(*) FROM contacts WHERE user_id = #{userId}")
    int countByUserId(Long userId);
    
    /**
     * Get all contacts (for admin purposes)
     */
    @Select("SELECT * FROM contacts ORDER BY created_at DESC LIMIT #{limit}")
    List<Contact> findAll(@Param("limit") int limit);
}
