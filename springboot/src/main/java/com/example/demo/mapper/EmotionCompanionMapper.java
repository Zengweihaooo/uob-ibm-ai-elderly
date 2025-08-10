package com.example.demo.mapper;

import com.example.demo.pojo.EmotionCompanion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

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
}
