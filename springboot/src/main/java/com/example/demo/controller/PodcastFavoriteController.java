package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.PodcastFavoriteService;
import com.example.demo.util.UserContextUtil;

/**
 * 播客收藏控制器
 * 提供播客收藏相关的REST API接口
 */
@RestController
@RequestMapping("/api/podcast/favorites")
@CrossOrigin(origins = "*")
public class PodcastFavoriteController {

    @Autowired
    private PodcastFavoriteService podcastFavoriteService;
    
    @Autowired
    private UserContextUtil userContextUtil;

    /**
     * 添加播客收藏
     * @param podcastData 播客数据
     * @param authHeader 授权头
     * @return 添加结果
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @RequestBody Map<String, Object> podcastData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = podcastFavoriteService.addFavorite(userId, podcastData);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                response.put("favorite", result.get("favorite"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加收藏失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 移除播客收藏
     * @param podcastId 播客ID
     * @param authHeader 授权头
     * @return 移除结果
     */
    @DeleteMapping("/{podcastId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable String podcastId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = podcastFavoriteService.removeFavorite(userId, podcastId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "移除收藏失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户的所有收藏
     * @param authHeader 授权头
     * @return 收藏列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserFavorites(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = podcastFavoriteService.getUserFavorites(userId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("favorites", result.get("favorites"));
                response.put("totalCount", result.get("totalCount"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取收藏列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查用户是否已收藏某个播客
     * @param podcastId 播客ID
     * @param authHeader 授权头
     * @return 检查结果
     */
    @GetMapping("/check/{podcastId}")
    public ResponseEntity<Map<String, Object>> checkFavoriteStatus(
            @PathVariable String podcastId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = podcastFavoriteService.checkFavoriteStatus(userId, podcastId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("isFavorited", result.get("isFavorited"));
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查收藏状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 搜索用户的收藏
     * @param keyword 搜索关键词
     * @param authHeader 授权头
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFavorites(
            @RequestParam String keyword,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = podcastFavoriteService.searchFavorites(userId, keyword);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("favorites", result.get("favorites"));
                response.put("keyword", result.get("keyword"));
                response.put("totalCount", result.get("totalCount"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索收藏失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据类型获取用户的收藏
     * @param type 播客类型
     * @param authHeader 授权头
     * @return 收藏列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getFavoritesByType(
            @PathVariable String type,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = podcastFavoriteService.getFavoritesByType(userId, type);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("favorites", result.get("favorites"));
                response.put("type", result.get("type"));
                response.put("totalCount", result.get("totalCount"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取收藏失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户的收藏统计信息
     * @param authHeader 授权头
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFavoriteStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = podcastFavoriteService.getFavoriteStatistics(userId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("statistics", result.get("statistics"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 