package com.example.demo.mapper;

import com.example.demo.pojo.PetMood;
import org.apache.ibatis.annotations.*;

/**
 * 宠物情绪数据访问接口
 * 提供宠物情绪数据的增删改查操作
 */
@Mapper
public interface PetMoodMapper {

    /**
     * 根据用户ID查找宠物情绪
     * @param userId 用户ID
     * @return 宠物情绪对象，如果不存在返回null
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
     * 插入新的宠物情绪记录
     * @param petMood 宠物情绪对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO pet_mood (user_id, mood_score, happiness, health, energy, mood_emoji, status, level, experience, last_interaction, created_at, updated_at) " +
            "VALUES (#{userId}, #{moodScore}, #{happiness}, #{health}, #{energy}, #{moodEmoji}, #{status}, #{level}, #{experience}, #{lastInteraction}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PetMood petMood);

    /**
     * 更新宠物情绪记录
     * @param petMood 宠物情绪对象
     * @return 影响的行数
     */
    @Update("UPDATE pet_mood SET mood_score = #{moodScore}, happiness = #{happiness}, health = #{health}, " +
            "energy = #{energy}, mood_emoji = #{moodEmoji}, status = #{status}, level = #{level}, " +
            "experience = #{experience}, last_interaction = #{lastInteraction}, updated_at = #{updatedAt} " +
            "WHERE user_id = #{userId}")
    int update(PetMood petMood);

    /**
     * 插入或更新宠物情绪记录（upsert操作）
     * @param petMood 宠物情绪对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO pet_mood (user_id, mood_score, happiness, health, energy, mood_emoji, status, level, experience, last_interaction, created_at, updated_at) " +
            "VALUES (#{userId}, #{moodScore}, #{happiness}, #{health}, #{energy}, #{moodEmoji}, #{status}, #{level}, #{experience}, #{lastInteraction}, #{createdAt}, #{updatedAt}) " +
            "ON CONFLICT(user_id) DO UPDATE SET " +
            "mood_score = #{moodScore}, happiness = #{happiness}, health = #{health}, " +
            "energy = #{energy}, mood_emoji = #{moodEmoji}, status = #{status}, level = #{level}, " +
            "experience = #{experience}, last_interaction = #{lastInteraction}, updated_at = #{updatedAt}")
    int upsert(PetMood petMood);

    /**
     * 根据用户ID删除宠物情绪记录
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM pet_mood WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    /**
     * 检查用户是否有宠物情绪记录
     * @param userId 用户ID
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM pet_mood WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
