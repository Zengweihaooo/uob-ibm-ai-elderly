package com.example.demo.mapper;

import com.example.demo.pojo.EmotionCompanion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EmotionCompanionMapper {
    int insert(EmotionCompanion e);
    int upsert(EmotionCompanion e); // user_id 唯一：存在则更新，不存在则插入
    EmotionCompanion findByUserId(@Param("userId") Long userId);
    int updateState(EmotionCompanion e); // 更新情绪/数值/位置等大部分字段
    int touchInteraction(@Param("userId") Long userId,
                         @Param("when") LocalDateTime when);
    int touchChat(@Param("userId") Long userId,
                  @Param("when") LocalDateTime when);
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * Find all emotion companions
     * @return List of all emotion companions
     */
    @Select("SELECT * FROM emotion_companions")
    List<EmotionCompanion> findAll();
    
    /**
     * Count total number of emotion companions
     * @return Total number of emotion companions
     */
    @Select("SELECT COUNT(*) FROM emotion_companions")
    long count();
}
