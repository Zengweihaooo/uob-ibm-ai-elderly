package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.PodcastService;

/**
 * Podcast controller
 * Provides podcast search, recommendation, details and other API endpoints
 */
@RestController
@RequestMapping("/api/podcast")
@CrossOrigin(origins = "*")
public class PodcastController {

    // Inject podcast service
    @Autowired
    private PodcastService podcastService;

    /**
     * Search podcasts by keyword
     * Supports multiple filter conditions: language, region, sort method, type, etc.
     * 
     * @param query search keyword (required)
     * @param language language filter (optional)
     * @param region region filter (optional)
     * @param sortBy sort method (optional): relevance, rating, latest
     * @param type podcast type filter (optional)
     * @return podcast list matching search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPodcasts(
            @RequestParam String query,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String type) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Call podcast service for search
            Map<String, Object> searchResult = podcastService.searchPodcasts(query, language, region, sortBy, type);
            
            // 检查搜索是否成功
            if ((Boolean) searchResult.get("success")) {
                // Build success response
                response.put("success", true);
                response.put("podcasts", searchResult.get("podcasts"));
                response.put("totalCount", searchResult.get("totalCount"));
                response.put("query", query);
                return ResponseEntity.ok(response);
            } else {
                // 搜索失败，返回错误信息
                response.put("success", false);
                response.put("message", searchResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error searching podcasts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get podcast recommendations based on user interests
     * Receive user interest list and return related podcast recommendations
     * 
     * @param interestsData request body containing user interest list
     * @return recommended podcast list
     */
    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getPodcastRecommendations(
            @RequestBody Map<String, Object> interestsData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从请求体中提取兴趣列表
            @SuppressWarnings("unchecked")
            List<String> interests = (List<String>) interestsData.get("interests");
            
            // Validate if interest list is empty
            if (interests == null || interests.isEmpty()) {
                response.put("success", false);
                response.put("message", "Interests list is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Call podcast service to get recommendations
            Map<String, Object> recommendationsResult = podcastService.getPodcastRecommendations(interests);
            
            // Check if recommendations were successful
            if ((Boolean) recommendationsResult.get("success")) {
                // Build success response
                response.put("success", true);
                response.put("recommendations", recommendationsResult.get("recommendations"));
                response.put("totalRecommendations", recommendationsResult.get("totalRecommendations"));
                response.put("interests", interests);
                return ResponseEntity.ok(response);
            } else {
                // 推荐失败，返回错误信息
                response.put("success", false);
                response.put("message", recommendationsResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get podcast details by podcast ID
     * 
     * @param podcastId podcast ID (path parameter)
     * @return podcast details
     */
    @GetMapping("/{podcastId}")
    public ResponseEntity<Map<String, Object>> getPodcastDetails(@PathVariable String podcastId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Call podcast service to get details
            Map<String, Object> detailsResult = podcastService.getPodcastDetails(podcastId);
            
            // Check if getting details was successful
            if ((Boolean) detailsResult.get("success")) {
                // Build success response
                response.put("success", true);
                response.put("podcast", detailsResult.get("podcast"));
                return ResponseEntity.ok(response);
            } else {
                // 获取详情失败，返回错误信息
                response.put("success", false);
                response.put("message", detailsResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting podcast details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get episode list for a specific podcast
     * Supports pagination
     * 
     * @param podcastId podcast ID (path parameter)
     * @param nextEpisodePubDate Next episode publication date (for pagination, optional)
     * @return episode list
     */
    @GetMapping("/{podcastId}/episodes")
    public ResponseEntity<Map<String, Object>> getPodcastEpisodes(
            @PathVariable String podcastId,
            @RequestParam(required = false) String nextEpisodePubDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Call podcast service to get episode list
            Map<String, Object> episodesResult = podcastService.getPodcastEpisodes(podcastId, nextEpisodePubDate);
            
            // Check if getting episodes was successful
            if ((Boolean) episodesResult.get("success")) {
                // Build success response
                response.put("success", true);
                response.put("episodes", episodesResult.get("episodes"));
                response.put("totalEpisodes", episodesResult.get("totalEpisodes"));
                
                // If next page date exists, add to response
                if (episodesResult.containsKey("nextEpisodePubDate")) {
                    response.put("nextEpisodePubDate", episodesResult.get("nextEpisodePubDate"));
                }
                return ResponseEntity.ok(response);
            } else {
                // Failed to get episodes, return error message
                response.put("success", false);
                response.put("message", episodesResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting episodes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get trending podcast list
     * 
     * @param region Region (optional), for getting trending podcasts in specific region
     * @return trending podcast list
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingPodcasts(
            @RequestParam(required = false) String region) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Call podcast service to get trending podcasts
            Map<String, Object> trendingResult = podcastService.getTrendingPodcasts(region);
            
            // Check if getting trending podcasts was successful
            if ((Boolean) trendingResult.get("success")) {
                // Build success response
                response.put("success", true);
                response.put("trendingPodcasts", trendingResult.get("trendingPodcasts"));
                response.put("totalTrending", trendingResult.get("totalTrending"));
                return ResponseEntity.ok(response);
            } else {
                // Failed to get trending podcasts, return error message
                response.put("success", false);
                response.put("message", trendingResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting trending podcasts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get podcast recommendations designed for elderly users
     * 使用预定义的老年用户兴趣列表
     * 
     * @return podcast recommendations suitable for elderly users
     */
    @GetMapping("/elderly-recommendations")
    public ResponseEntity<Map<String, Object>> getElderlyPodcastRecommendations() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 定义老年用户的常见兴趣
            List<String> elderlyInterests = List.of(
                "health and wellness",      // 健康与保健
                "meditation",              // 冥想
                "classical music",         // 古典音乐
                "history",                 // 历史
                "gardening",               // 园艺
                "cooking",                 // 烹饪
                "travel stories",          // 旅行故事
                "inspirational stories",   // 励志故事
                "memory exercises",        // 记忆练习
                "relaxation"               // 放松
            );
            
            // Call podcast service to get recommendations
            Map<String, Object> recommendationsResult = podcastService.getPodcastRecommendations(elderlyInterests);
            
            // 检查推荐是否成功
            if ((Boolean) recommendationsResult.get("success")) {
                // Build success response
                response.put("success", true);
                response.put("recommendations", recommendationsResult.get("recommendations"));
                response.put("totalRecommendations", recommendationsResult.get("totalRecommendations"));
                response.put("targetAudience", "elderly");  // 标识目标用户群体
                response.put("interests", elderlyInterests);
                return ResponseEntity.ok(response);
            } else {
                // 推荐失败，返回错误信息
                response.put("success", false);
                response.put("message", recommendationsResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // 处理异常
            response.put("success", false);
            response.put("message", "Error getting elderly recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 