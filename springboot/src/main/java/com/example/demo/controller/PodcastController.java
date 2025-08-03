

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
 * 播客控制器
 * 提供播客搜索、推荐、详情等API端点
 */
@RestController
@RequestMapping("/api/podcast")
@CrossOrigin(origins = "*")
public class PodcastController {

    // 注入播客服务
    @Autowired
    private PodcastService podcastService;

    /**
     * 根据关键词搜索播客
     * 支持多种过滤条件：语言、地区、排序方式、类型等
     * 
     * @param query 搜索关键词（必填）
     * @param language 语言过滤器（可选）
     * @param region 地区过滤器（可选）
     * @param sortBy 排序方式（可选）：relevance（相关性）、rating（评分）、latest（最新）
     * @param type 播客类型过滤器（可选）
     * @return 匹配搜索条件的播客列表
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
            // 调用播客服务进行搜索
            Map<String, Object> searchResult = podcastService.searchPodcasts(query, language, region, sortBy, type);
            
            // 检查搜索是否成功
            if ((Boolean) searchResult.get("success")) {
                // 构建成功响应
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
     * 根据用户兴趣获取播客推荐
     * 接收用户兴趣列表，返回相关的播客推荐
     * 
     * @param interestsData 包含用户兴趣列表的请求体
     * @return 推荐的播客列表
     */
    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getPodcastRecommendations(
            @RequestBody Map<String, Object> interestsData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从请求体中提取兴趣列表
            @SuppressWarnings("unchecked")
            List<String> interests = (List<String>) interestsData.get("interests");
            
            // 验证兴趣列表是否为空
            if (interests == null || interests.isEmpty()) {
                response.put("success", false);
                response.put("message", "Interests list is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 调用播客服务获取推荐
            Map<String, Object> recommendationsResult = podcastService.getPodcastRecommendations(interests);
            
            // 检查推荐是否成功
            if ((Boolean) recommendationsResult.get("success")) {
                // 构建成功响应
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
     * 根据播客ID获取播客详细信息
     * 
     * @param podcastId 播客ID（路径参数）
     * @return 播客详细信息
     */
    @GetMapping("/{podcastId}")
    public ResponseEntity<Map<String, Object>> getPodcastDetails(@PathVariable String podcastId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 调用播客服务获取详情
            Map<String, Object> detailsResult = podcastService.getPodcastDetails(podcastId);
            
            // 检查获取详情是否成功
            if ((Boolean) detailsResult.get("success")) {
                // 构建成功响应
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
     * 获取指定播客的剧集列表
     * 支持分页功能
     * 
     * @param podcastId 播客ID（路径参数）
     * @param nextEpisodePubDate 下一页剧集的发布日期（用于分页，可选）
     * @return 剧集列表
     */
    @GetMapping("/{podcastId}/episodes")
    public ResponseEntity<Map<String, Object>> getPodcastEpisodes(
            @PathVariable String podcastId,
            @RequestParam(required = false) String nextEpisodePubDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 调用播客服务获取剧集列表
            Map<String, Object> episodesResult = podcastService.getPodcastEpisodes(podcastId, nextEpisodePubDate);
            
            // 检查获取剧集是否成功
            if ((Boolean) episodesResult.get("success")) {
                // 构建成功响应
                response.put("success", true);
                response.put("episodes", episodesResult.get("episodes"));
                response.put("totalEpisodes", episodesResult.get("totalEpisodes"));
                
                // 如果存在下一页日期，添加到响应中
                if (episodesResult.containsKey("nextEpisodePubDate")) {
                    response.put("nextEpisodePubDate", episodesResult.get("nextEpisodePubDate"));
                }
                return ResponseEntity.ok(response);
            } else {
                // 获取剧集失败，返回错误信息
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
     * 获取热门播客列表
     * 
     * @param region 地区（可选），用于获取特定地区的热门播客
     * @return 热门播客列表
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingPodcasts(
            @RequestParam(required = false) String region) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 调用播客服务获取热门播客
            Map<String, Object> trendingResult = podcastService.getTrendingPodcasts(region);
            
            // 检查获取热门播客是否成功
            if ((Boolean) trendingResult.get("success")) {
                // 构建成功响应
                response.put("success", true);
                response.put("trendingPodcasts", trendingResult.get("trendingPodcasts"));
                response.put("totalTrending", trendingResult.get("totalTrending"));
                return ResponseEntity.ok(response);
            } else {
                // 获取热门播客失败，返回错误信息
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
     * 获取专为老年用户设计的播客推荐
     * 使用预定义的老年用户兴趣列表
     * 
     * @return 适合老年用户的播客推荐
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
            
            // 调用播客服务获取推荐
            Map<String, Object> recommendationsResult = podcastService.getPodcastRecommendations(elderlyInterests);
            
            // 检查推荐是否成功
            if ((Boolean) recommendationsResult.get("success")) {
                // 构建成功响应
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