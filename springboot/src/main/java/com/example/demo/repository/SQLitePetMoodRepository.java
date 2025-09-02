package com.example.demo.repository;

import com.example.demo.pojo.PetMood;
import com.example.demo.mapper.PetMoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * SQLite implementation of pet mood data access
 * Keeps existing SQLite functionality as local cache
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("sqlitePetMoodRepository")
// @Profile("!aws") // Temporarily commented out to ensure Bean creation
public class SQLitePetMoodRepository implements PetMoodRepository {
    
    @Autowired
    private PetMoodMapper petMoodMapper;
    
    @Override
    public Optional<PetMood> findByUserId(Long userId) {
        try {
            PetMood petMood = petMoodMapper.findByUserId(userId);
            return Optional.ofNullable(petMood);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public PetMood save(PetMood petMood) {
        try {
            if (petMood.getId() == null) {
                // New record, insert
                petMoodMapper.insert(petMood);
            } else {
                // Existing record, update
                petMoodMapper.update(petMood);
            }
            return petMood;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save pet mood", e);
        }
    }
    
    @Override
    public PetMood update(PetMood petMood) {
        try {
            petMoodMapper.update(petMood);
            return petMood;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update pet mood", e);
        }
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        try {
            // Note: adjust according to actual mapper methods
            // If mapper lacks deleteByUserId, find record first then delete
            PetMood existing = petMoodMapper.findByUserId(userId);
            if (existing != null) {
                // Assume mapper has deleteById
                // petMoodMapper.deleteById(existing.getId());
                throw new UnsupportedOperationException("Delete operation not implemented in mapper");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete pet mood", e);
        }
    }
    
    @Override
    public boolean existsByUserId(Long userId) {
        try {
            PetMood existing = petMoodMapper.findByUserId(userId);
            return existing != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<PetMood> findAll() {
        try {
            // Note: adjust according to actual mapper methods
            // If mapper lacks findAll, return empty list
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<PetMood> findByMoodScoreBetween(int minScore, int maxScore) {
        try {
            // Note: adjust according to actual mapper methods
            // If mapper lacks range query, return empty list
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    @Override
    public long count() {
        try {
            // Note: adjust according to actual mapper methods
            // If mapper lacks count, return 0
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

