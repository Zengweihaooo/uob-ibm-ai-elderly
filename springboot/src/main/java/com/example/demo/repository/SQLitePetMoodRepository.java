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
 * SQLite版本的宠物情绪数据访问实现
 * 保持现有的SQLite功能，作为本地缓存使用
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("sqlitePetMoodRepository")
@Profile("!aws") // 在非AWS环境下使用
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
                // 新记录，插入
                petMoodMapper.insert(petMood);
            } else {
                // 已存在，更新
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
            // 注意：这里需要根据实际的Mapper方法调整
            // 如果Mapper没有deleteByUserId方法，可以先用findByUserId找到记录再删除
            PetMood existing = petMoodMapper.findByUserId(userId);
            if (existing != null) {
                // 假设Mapper有deleteById方法
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
            // 注意：这里需要根据实际的Mapper方法调整
            // 如果Mapper没有findAll方法，返回空列表
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<PetMood> findByMoodScoreBetween(int minScore, int maxScore) {
        try {
            // 注意：这里需要根据实际的Mapper方法调整
            // 如果Mapper没有范围查询方法，返回空列表
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    @Override
    public long count() {
        try {
            // 注意：这里需要根据实际的Mapper方法调整
            // 如果Mapper没有count方法，返回0
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

