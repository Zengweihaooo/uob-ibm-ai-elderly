package com.example.demo.mapper;

import com.example.demo.pojo.Email;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Mapper interface for Email operations
 * 
 * @author AI Assistant
 * @version 1.0
 */
@Mapper
public interface EmailMapper {
    
    /**
     * Insert a new email record
     */
    @Insert("INSERT INTO emails (from_email, to_email, subject, content, status, created_at, sent_at, error_message) " +
            "VALUES (#{fromEmail}, #{toEmail}, #{subject}, #{content}, #{status}, #{createdAt}, #{sentAt}, #{errorMessage})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Email email);
    
    /**
     * Update email status
     */
    @Update("UPDATE emails SET status = #{status}, sent_at = #{sentAt}, error_message = #{errorMessage} WHERE id = #{id}")
    int updateStatus(Email email);
    
    /**
     * Find email by ID
     */
    @Select("SELECT * FROM emails WHERE id = #{id}")
    Email findById(Long id);
    
    /**
     * Find all emails sent from a specific email address
     */
    @Select("SELECT * FROM emails WHERE from_email = #{fromEmail} ORDER BY created_at DESC")
    List<Email> findByFromEmail(String fromEmail);
    
    /**
     * Find all emails sent to a specific email address
     */
    @Select("SELECT * FROM emails WHERE to_email = #{toEmail} ORDER BY created_at DESC")
    List<Email> findByToEmail(String toEmail);
    
    /**
     * Find all emails by status
     */
    @Select("SELECT * FROM emails WHERE status = #{status} ORDER BY created_at DESC")
    List<Email> findByStatus(String status);
    
    /**
     * Get all emails (for admin purposes)
     */
    @Select("SELECT * FROM emails ORDER BY created_at DESC LIMIT #{limit}")
    List<Email> findAll(@Param("limit") int limit);
    
    /**
     * Delete email by ID
     */
    @Delete("DELETE FROM emails WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * Count emails by status
     */
    @Select("SELECT COUNT(*) FROM emails WHERE status = #{status}")
    int countByStatus(String status);
    
    /**
     * Get recent emails for a user (sent by them)
     */
    @Select("SELECT * FROM emails WHERE from_email = #{fromEmail} ORDER BY created_at DESC LIMIT #{limit}")
    List<Email> findRecentByFromEmail(@Param("fromEmail") String fromEmail, @Param("limit") int limit);
    
    /**
     * Update draft content
     */
    @Update("UPDATE emails SET from_email = #{fromEmail}, to_email = #{toEmail}, subject = #{subject}, " +
            "content = #{content}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 'DRAFT'")
    int updateDraft(Email email);
}
