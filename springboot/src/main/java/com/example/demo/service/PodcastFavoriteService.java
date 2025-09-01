package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.PodcastFavorite;
import com.example.demo.repository.PodcastFavoriteRepository;

/**
 * 播客收藏服务类
 * 提供播客收藏的业务逻辑处理
 */
@Service
public class PodcastFavoriteService {
    
    @Autowired
    private PodcastFavoriteRepository podcastFavoriteRepository;
    
    /**
     * 添加播客收藏
     * @param userId 用户ID
     * @param podcastData 播客数据
     * @return 添加结果
     */
    public Map<String, Object> addFavorite(Long userId, Map<String, Object> podcastData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证输入参数
            String podcastId = (String) podcastData.get("podcastId");
            String title = (String) podcastData.get("title");
            
            if (podcastId == null || podcastId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "播客ID不能为空");
                return result;
            }
            
            if (title == null || title.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "播客标题不能为空");
                return result;
            }
            
            // 检查是否已经收藏
            boolean alreadyFavorited = podcastFavoriteRepository.existsByUserIdAndPodcastId(userId, podcastId.trim());
            if (alreadyFavorited) {
                result.put("success", false);
                result.put("message", "该播客已经收藏过了");
                return result;
            }
            
            // 创建收藏对象
            PodcastFavorite favorite = new PodcastFavorite();
            favorite.setUserId(userId);
            favorite.setPodcastId(podcastId.trim());
            favorite.setTitle(title.trim());
            favorite.setDescription((String) podcastData.get("description"));
            favorite.setPublisher((String) podcastData.get("publisher"));
            favorite.setImage((String) podcastData.get("image"));
            favorite.setThumbnail((String) podcastData.get("thumbnail"));
            favorite.setLanguage((String) podcastData.get("language"));
            favorite.setCountry((String) podcastData.get("country"));
            favorite.setType((String) podcastData.get("type"));
            
            // 处理总集数
            Object totalEpisodesObj = podcastData.get("totalEpisodes");
            if (totalEpisodesObj != null) {
                if (totalEpisodesObj instanceof Integer) {
                    favorite.setTotalEpisodes((Integer) totalEpisodesObj);
                } else if (totalEpisodesObj instanceof String) {
                    try {
                        favorite.setTotalEpisodes(Integer.parseInt((String) totalEpisodesObj));
                    } catch (NumberFormatException e) {
                        // 忽略解析错误
                    }
                }
            }
            
            // 处理分类标签（JSON格式）
            Object genresObj = podcastData.get("genres");
            if (genresObj != null) {
                if (genresObj instanceof String) {
                    favorite.setGenres((String) genresObj);
                } else if (genresObj instanceof List) {
                    // 将List转换为JSON字符串
                    favorite.setGenres(genresObj.toString());
                }
            }
            
            favorite.setRss((String) podcastData.get("rss"));
            favorite.setWebsite((String) podcastData.get("website"));
            favorite.setCreatedAt(LocalDateTime.now());
            favorite.setUpdatedAt(LocalDateTime.now());
            favorite.setIsActive(true);
            
            // 保存到数据库
            int insertResult = podcastFavoriteRepository.insert(favorite);
            
            if (insertResult > 0) {
                result.put("success", true);
                result.put("message", "播客收藏成功");
                result.put("favorite", favorite);
            } else {
                result.put("success", false);
                result.put("message", "播客收藏失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加收藏失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 移除播客收藏
     * @param userId 用户ID
     * @param podcastId 播客ID
     * @return 移除结果
     */
    public Map<String, Object> removeFavorite(Long userId, String podcastId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (podcastId == null || podcastId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "播客ID不能为空");
                return result;
            }
            
            // 检查是否已收藏
            boolean exists = podcastFavoriteRepository.existsByUserIdAndPodcastId(userId, podcastId.trim());
            if (!exists) {
                result.put("success", false);
                result.put("message", "该播客未收藏");
                return result;
            }
            
            // 软删除收藏
            int deleteResult = podcastFavoriteRepository.softDelete(userId, podcastId.trim());
            
            if (deleteResult > 0) {
                result.put("success", true);
                result.put("message", "播客收藏已移除");
            } else {
                result.put("success", false);
                result.put("message", "移除收藏失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "移除收藏失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取用户的所有收藏
     * @param userId 用户ID
     * @return 收藏列表
     */
    public Map<String, Object> getUserFavorites(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<PodcastFavorite> favorites = podcastFavoriteRepository.findByUserId(userId);
            
            result.put("success", true);
            result.put("favorites", favorites);
            result.put("totalCount", favorites.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取收藏列表失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查用户是否已收藏某个播客
     * @param userId 用户ID
     * @param podcastId 播客ID
     * @return 检查结果
     */
    public Map<String, Object> checkFavoriteStatus(Long userId, String podcastId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (podcastId == null || podcastId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "播客ID不能为空");
                return result;
            }
            
            boolean isFavorited = podcastFavoriteRepository.existsByUserIdAndPodcastId(userId, podcastId.trim());
            
            result.put("success", true);
            result.put("isFavorited", isFavorited);
            result.put("message", isFavorited ? "已收藏" : "未收藏");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查收藏状态失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 搜索用户的收藏
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    public Map<String, Object> searchFavorites(Long userId, String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "搜索关键词不能为空");
                return result;
            }
            
            List<PodcastFavorite> searchResults = podcastFavoriteRepository.searchByKeyword(userId, keyword.trim());
            
            result.put("success", true);
            result.put("favorites", searchResults);
            result.put("keyword", keyword);
            result.put("totalCount", searchResults.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "搜索收藏失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据类型获取用户的收藏
     * @param userId 用户ID
     * @param type 播客类型
     * @return 收藏列表
     */
    public Map<String, Object> getFavoritesByType(Long userId, String type) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (type == null || type.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "播客类型不能为空");
                return result;
            }
            
            List<PodcastFavorite> favorites = podcastFavoriteRepository.findByUserIdAndType(userId, type.trim());
            
            result.put("success", true);
            result.put("favorites", favorites);
            result.put("type", type);
            result.put("totalCount", favorites.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取收藏失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取用户的收藏统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getFavoriteStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long totalCount = podcastFavoriteRepository.countByUserId(userId);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", totalCount);
            
            result.put("success", true);
            result.put("statistics", statistics);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return result;
    }
} 