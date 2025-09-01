package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.pojo.PodcastFavorite;

/**
 * 播客收藏数据访问接口
 * 提供播客收藏的增删改查功能
 */
@Mapper
public interface PodcastFavoriteRepository {
    
    /**
     * 插入新的播客收藏
     * @param favorite 播客收藏对象
     * @return 影响的行数
     */
    int insert(PodcastFavorite favorite);
    
    /**
     * 更新播客收藏信息
     * @param favorite 播客收藏对象
     * @return 影响的行数
     */
    int update(PodcastFavorite favorite);
    
    /**
     * 根据ID查找播客收藏
     * @param id 收藏ID
     * @return 播客收藏对象
     */
    Optional<PodcastFavorite> findById(Long id);
    
    /**
     * 根据用户ID和播客ID查找收藏
     * @param userId 用户ID
     * @param podcastId 播客ID
     * @return 播客收藏对象
     */
    Optional<PodcastFavorite> findByUserIdAndPodcastId(@Param("userId") Long userId, @Param("podcastId") String podcastId);
    
    /**
     * 获取用户的所有收藏
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<PodcastFavorite> findByUserId(@Param("userId") Long userId);
    
    /**
     * 检查用户是否已收藏某个播客
     * @param userId 用户ID
     * @param podcastId 播客ID
     * @return 是否已收藏
     */
    boolean existsByUserIdAndPodcastId(@Param("userId") Long userId, @Param("podcastId") String podcastId);
    
    /**
     * 软删除收藏（设置is_active为false）
     * @param userId 用户ID
     * @param podcastId 播客ID
     * @return 影响的行数
     */
    int softDelete(@Param("userId") Long userId, @Param("podcastId") String podcastId);
    
    /**
     * 统计用户的收藏数量
     * @param userId 用户ID
     * @return 收藏数量
     */
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和播客类型查找收藏
     * @param userId 用户ID
     * @param type 播客类型
     * @return 收藏列表
     */
    List<PodcastFavorite> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    /**
     * 搜索用户的收藏（按标题和描述）
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 收藏列表
     */
    List<PodcastFavorite> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
} 