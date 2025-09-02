package com.example.demo.mapper;

import com.example.demo.pojo.PetMood;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Data access interface for pet mood
 * Provides CRUD operations for pet mood data
 */
@Mapper
public interface PetMoodMapper {

    /**
     * Find pet mood by user ID
     * @param userId User ID
     * @return Pet mood object, or null if not found
     */
    @Select("SELECT * FROM pet_mood WHERE user_id = #{userId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "moodScore", column = "mood_score"),
        @Result(property = "happiness", column = "happiness"),
        @Result(property = "health", column = "health"),
        @Result(property = "energy", column = "energy"),
        @Result(property = "moodEmoji", column = "mood_emoji"),
        @Result(property = "status", column = "status"),
        @Result(property = "level", column = "level"),
        @Result(property = "experience", column = "experience"),
        @Result(property = "lastInteraction", column = "last_interaction"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    PetMood findByUserId(Long userId);

    /**
     * Insert a new pet mood record
     * @param petMood Pet mood object
     * @return Number of affected rows
     */
    @Insert("INSERT INTO pet_mood (user_id, mood_score, happiness, health, energy, mood_emoji, status, level, experience, last_interaction, created_at, updated_at) " +
            "VALUES (#{userId}, #{moodScore}, #{happiness}, #{health}, #{energy}, #{moodEmoji}, #{status}, #{level}, #{experience}, #{lastInteraction}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PetMood petMood);

    /**
     * Update pet mood record
     * @param petMood Pet mood object
     * @return Number of affected rows
     */
    @Update("UPDATE pet_mood SET mood_score = #{moodScore}, happiness = #{happiness}, health = #{health}, " +
            "energy = #{energy}, mood_emoji = #{moodEmoji}, status = #{status}, level = #{level}, " +
            "experience = #{experience}, last_interaction = #{lastInteraction}, updated_at = #{updatedAt} " +
            "WHERE user_id = #{userId}")
    int update(PetMood petMood);

    /**
     * Insert or update pet mood record (upsert)
     * @param petMood Pet mood object
     * @return Number of affected rows
     */
    @Insert("INSERT INTO pet_mood (user_id, mood_score, happiness, health, energy, mood_emoji, status, level, experience, last_interaction, created_at, updated_at) " +
            "VALUES (#{userId}, #{moodScore}, #{happiness}, #{health}, #{energy}, #{moodEmoji}, #{status}, #{level}, #{experience}, #{lastInteraction}, #{createdAt}, #{updatedAt}) " +
            "ON CONFLICT(user_id) DO UPDATE SET " +
            "mood_score = #{moodScore}, happiness = #{happiness}, health = #{health}, " +
            "energy = #{energy}, mood_emoji = #{moodEmoji}, status = #{status}, level = #{level}, " +
            "experience = #{experience}, last_interaction = #{lastInteraction}, updated_at = #{updatedAt}")
    int upsert(PetMood petMood);

    /**
     * Delete pet mood record by user ID
     * @param userId User ID
     * @return Number of affected rows
     */
    @Delete("DELETE FROM pet_mood WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    /**
     * Check if the user has a pet mood record
     * @param userId User ID
     * @return Number of records
     */
    @Select("SELECT COUNT(*) FROM pet_mood WHERE user_id = #{userId}")
    int countByUserId(Long userId);
    
    /**
     * Get all pet mood records
     * @return List of all pet mood records
     */
    @Select("SELECT * FROM pet_mood")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "moodScore", column = "mood_score"),
        @Result(property = "happiness", column = "happiness"),
        @Result(property = "health", column = "health"),
        @Result(property = "energy", column = "energy"),
        @Result(property = "moodEmoji", column = "mood_emoji"),
        @Result(property = "status", column = "status"),
        @Result(property = "level", column = "level"),
        @Result(property = "experience", column = "experience"),
        @Result(property = "lastInteraction", column = "last_interaction"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<PetMood> findAll();
    
    /**
     * Get total record count
     * @return Total count
     */
    @Select("SELECT COUNT(*) FROM pet_mood")
    long count();
}
