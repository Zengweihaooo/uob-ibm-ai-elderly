package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.pojo.Memo;

/**
 * 备忘录数据访问接口
 * 定义统一的数据访问方法，支持多种数据库实现
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
public interface MemoRepositoryInterface {
    
    /**
     * 保存备忘录
     * @param memo 备忘录对象
     * @return 保存后的备忘录对象
     */
    Memo save(Memo memo);
    
    /**
     * 根据ID查找备忘录
     * @param id 备忘录ID
     * @return 备忘录对象，如果不存在返回Optional.empty()
     */
    Optional<Memo> findById(Long id);
    
    /**
     * 根据用户ID和备忘录ID查找未删除的备忘录
     * @param userId 用户ID
     * @param memoId 备忘录ID
     * @return 备忘录对象，如果不存在返回Optional.empty()
     */
    Optional<Memo> findByUserIdAndId(Long userId, Long memoId);
    
    /**
     * 根据用户ID查找所有未删除的备忘录
     * @param userId 用户ID
     * @return 备忘录列表
     */
    List<Memo> findByUserId(Long userId);
    
    /**
     * 根据用户ID和类型查找未删除的备忘录
     * @param userId 用户ID
     * @param type 备忘录类型
     * @return 备忘录列表
     */
    List<Memo> findByUserIdAndType(Long userId, String type);
    
    /**
     * Find important memos by user ID
     * @param userId user ID
     * @return important memo list
     */
    List<Memo> findImportantByUserId(Long userId);
    
    /**
     * 根据用户ID和PIN码查找备忘录
     * @param userId 用户ID
     * @param pinCode PIN码
     * @return 备忘录列表
     */
    List<Memo> findByUserIdAndPinCode(Long userId, String pinCode);
    
    /**
     * 搜索备忘录（标题或内容包含关键词）
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    List<Memo> searchByKeyword(Long userId, String keyword);
    
    /**
     * 软删除备忘录
     * @param userId 用户ID
     * @param memoId 备忘录ID
     * @return 影响的行数
     */
    int softDelete(Long userId, Long memoId);
    
    /**
     * 统计用户备忘录数量
     * @param userId 用户ID
     * @return 备忘录数量
     */
    long countByUserId(Long userId);
    
    /**
     * Count important memos by user ID
     * @param userId user ID
     * @return number of important memos
     */
    long countImportantByUserId(Long userId);
    
    /**
     * 统计用户各类型备忘录数量
     * @param userId 用户ID
     * @param type 备忘录类型
     * @return 该类型备忘录数量
     */
    long countByUserIdAndType(Long userId, String type);
    
    /**
     * 检查用户是否有指定PIN码的备忘录
     * @param userId 用户ID
     * @param pinCode PIN码
     * @return 是否存在
     */
    boolean existsByUserIdAndPinCode(Long userId, String pinCode);
    
    /**
     * 获取所有备忘录（用于数据迁移）
     * @return 所有备忘录列表
     */
    List<Memo> findAll();
    
    /**
     * 统计记录总数（用于数据迁移）
     * @return 记录总数
     */
    long count();
} 
 
 