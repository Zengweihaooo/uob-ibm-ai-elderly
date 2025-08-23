package com.example.demo.repository;

import com.example.demo.pojo.PetMood;
import java.util.List;
import java.util.Optional;

/**
 * 宠物情绪数据访问接口
 * 定义统一的数据访问方法，支持多种数据库实现
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
public interface PetMoodRepository {
    
    /**
     * 根据用户ID查找宠物情绪
     * @param userId 用户ID
     * @return 宠物情绪对象，如果不存在返回Optional.empty()
     */
    Optional<PetMood> findByUserId(Long userId);
    
    /**
     * 保存宠物情绪记录
     * @param petMood 宠物情绪对象
     * @return 保存后的宠物情绪对象
     */
    PetMood save(PetMood petMood);
    
    /**
     * 更新宠物情绪记录
     * @param petMood 宠物情绪对象
     * @return 更新后的宠物情绪对象
     */
    PetMood update(PetMood petMood);
    
    /**
     * 根据用户ID删除宠物情绪记录
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * 检查用户是否有宠物情绪记录
     * @param userId 用户ID
     * @return 是否存在记录
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 获取所有宠物情绪记录
     * @return 所有记录列表
     */
    List<PetMood> findAll();
    
    /**
     * 根据情绪分数范围查找记录
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @return 符合条件的记录列表
     */
    List<PetMood> findByMoodScoreBetween(int minScore, int maxScore);
    
    /**
     * 统计记录总数
     * @return 记录总数
     */
    long count();
}


