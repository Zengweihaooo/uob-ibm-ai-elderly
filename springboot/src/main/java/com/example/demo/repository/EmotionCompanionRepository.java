package com.example.demo.repository;

import com.example.demo.pojo.EmotionCompanion;
import java.util.List;
import java.util.Optional;

public interface EmotionCompanionRepository {
    
    Optional<EmotionCompanion> findById(Long id);
    
    EmotionCompanion save(EmotionCompanion emotionCompanion);
    
    EmotionCompanion update(EmotionCompanion emotionCompanion);
    
    void deleteById(Long id);
    
    List<EmotionCompanion> findAll();
    
    long count();
}
