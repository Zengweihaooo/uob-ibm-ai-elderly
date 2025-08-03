package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.pojo.Podcast;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 播客服务类
 * 负责与Listen Notes API进行交互，提供播客搜索、推荐等功能
 */
@Service
public class PodcastService {

    // Listen Notes API的基础URL
    private static final String LISTEN_NOTES_API_BASE_URL = "https://listen-api.listennotes.com/api/v2";
    // API访问令牌
    private static final String API_TOKEN = "a3432f0d55d940e3bbe3d18f7acdeea6";
    // HTTP请求客户端
    private final RestTemplate restTemplate;
    // JSON解析器
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     * 初始化HTTP客户端和JSON解析器
     */
    public PodcastService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 根据关键词搜索播客
     * @param query 搜索关键词
     * @param language 语言过滤器（可选）
     * @param region 地区过滤器（可选）
     * @param sortBy 排序方式（可选）：relevance（相关性）、rating（评分）、latest（最新）
     * @param type 播客类型过滤器（可选）
     * @return 匹配搜索条件的播客列表
     */
    public Map<String, Object> searchPodcasts(String query, String language, String region, String sortBy, String type) {
        try {
            // 构建API请求URL
            String url = LISTEN_NOTES_API_BASE_URL + "/search";
            
            // 使用UriComponentsBuilder构建查询参数
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("q", query)                    // 搜索关键词
                    .queryParam("type", "podcast")             // 搜索类型为播客
                    .queryParam("offset", 0)                   // 偏移量，从0开始
                    .queryParam("limit", 10);                  // 限制返回结果数量为10个
            
            // 添加可选参数
            if (language != null && !language.isEmpty()) {
                builder.queryParam("language", language);       // 语言过滤
            }
            if (region != null && !region.isEmpty()) {
                builder.queryParam("region", region);           // 地区过滤
            }
            if (sortBy != null && !sortBy.isEmpty()) {
                builder.queryParam("sort_by", sortBy);         // 排序方式
            }
            if (type != null && !type.isEmpty()) {
                builder.queryParam("type", type);              // 类型过滤
            }

            // 设置HTTP请求头，包含API认证令牌
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-ListenAPI-Key", API_TOKEN);
            
            // 创建HTTP请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发送GET请求到Listen Notes API
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // 解析API响应
            return parseSearchResponse(response.getBody());
            
        } catch (Exception e) {
            // 处理异常，返回错误信息
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error searching podcasts: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 根据用户兴趣获取播客推荐
     * @param interests 用户兴趣列表
     * @return 推荐的播客列表
     */
    public Map<String, Object> getPodcastRecommendations(List<String> interests) {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Podcast> recommendations = new ArrayList<>();
            
            // 遍历每个兴趣，搜索相关播客
            for (String interest : interests) {
                // 为每个兴趣搜索播客
                Map<String, Object> searchResult = searchPodcasts(interest, "en", "us", "relevance", "podcast");
                
                // 如果搜索成功，添加前2个播客到推荐列表
                if ((Boolean) searchResult.get("success")) {
                    List<Podcast> podcasts = (List<Podcast>) searchResult.get("podcasts");
                    if (podcasts != null && !podcasts.isEmpty()) {
                        // 每个兴趣最多添加2个播客
                        recommendations.addAll(podcasts.subList(0, Math.min(2, podcasts.size())));
                    }
                }
            }
            
            // 构建成功响应
            response.put("success", true);
            response.put("recommendations", recommendations);
            response.put("totalRecommendations", recommendations.size());
            response.put("interests", interests);
            
            return response;
            
        } catch (Exception e) {
            // 处理异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting recommendations: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 根据播客ID获取播客详细信息
     * @param podcastId 播客ID
     * @return 播客详细信息
     */
    public Map<String, Object> getPodcastDetails(String podcastId) {
        try {
            // 构建获取播客详情的API URL
            String url = LISTEN_NOTES_API_BASE_URL + "/podcasts/" + podcastId;
            
            // 设置HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-ListenAPI-Key", API_TOKEN);
            
            // 创建HTTP请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发送GET请求
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // 解析播客详情响应
            return parsePodcastDetailsResponse(response.getBody());
            
        } catch (Exception e) {
            // 处理异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting podcast details: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 获取指定播客的剧集列表
     * @param podcastId 播客ID
     * @param nextEpisodePubDate 下一页剧集的发布日期（用于分页，可选）
     * @return 剧集列表
     */
    public Map<String, Object> getPodcastEpisodes(String podcastId, String nextEpisodePubDate) {
        try {
            // 构建获取剧集的API URL
            String url = LISTEN_NOTES_API_BASE_URL + "/podcasts/" + podcastId + "/episodes";
            
            // 构建查询参数
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("limit", 10);  // 限制返回10个剧集
            
            // 如果提供了下一页日期，添加到查询参数
            if (nextEpisodePubDate != null && !nextEpisodePubDate.isEmpty()) {
                builder.queryParam("next_episode_pub_date", nextEpisodePubDate);
            }

            // 设置HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-ListenAPI-Key", API_TOKEN);
            
            // 创建HTTP请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发送GET请求
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // 解析剧集响应
            return parseEpisodesResponse(response.getBody());
            
        } catch (Exception e) {
            // 处理异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting episodes: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 获取热门播客
     * @param region 地区（可选）
     * @return 热门播客列表
     */
    public Map<String, Object> getTrendingPodcasts(String region) {
        try {
            // 构建获取热门播客的API URL
            String url = LISTEN_NOTES_API_BASE_URL + "/podcasts/trending";
            
            // 构建查询参数
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            
            // 如果提供了地区参数，添加到查询参数
            if (region != null && !region.isEmpty()) {
                builder.queryParam("region", region);
            }

            // 设置HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-ListenAPI-Key", API_TOKEN);
            
            // 创建HTTP请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发送GET请求
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // 解析热门播客响应
            return parseTrendingResponse(response.getBody());
            
        } catch (Exception e) {
            // 处理异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting trending podcasts: " + e.getMessage());
            return errorResponse;
        }
    }

    // ==================== 私有方法：解析API响应 ====================

    /**
     * 解析搜索响应
     * 将Listen Notes API的JSON响应解析为Java对象
     */
    private Map<String, Object> parseSearchResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 将JSON字符串解析为JsonNode对象
            JsonNode root = objectMapper.readTree(responseBody);
            
            // 提取总数量
            if (root.has("count")) {
                result.put("totalCount", root.get("count").asInt());
            }
            
            // 解析搜索结果列表
            List<Podcast> podcasts = new ArrayList<>();
            if (root.has("results")) {
                JsonNode results = root.get("results");
                for (JsonNode podcastNode : results) {
                    // 将每个播客节点转换为Podcast对象
                    Podcast podcast = parsePodcastFromJson(podcastNode);
                    podcasts.add(podcast);
                }
            }
            
            // 构建成功响应
            result.put("success", true);
            result.put("podcasts", podcasts);
            
        } catch (Exception e) {
            // 处理解析异常
            result.put("success", false);
            result.put("message", "Error parsing response: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析播客详情响应
     */
    private Map<String, Object> parsePodcastDetailsResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析JSON响应
            JsonNode podcastNode = objectMapper.readTree(responseBody);
            // 转换为Podcast对象
            Podcast podcast = parsePodcastFromJson(podcastNode);
            
            // 构建成功响应
            result.put("success", true);
            result.put("podcast", podcast);
            
        } catch (Exception e) {
            // 处理异常
            result.put("success", false);
            result.put("message", "Error parsing podcast details: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析剧集响应
     */
    private Map<String, Object> parseEpisodesResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析JSON响应
            JsonNode root = objectMapper.readTree(responseBody);
            
            // 解析剧集列表
            List<Podcast.Episode> episodes = new ArrayList<>();
            if (root.has("items")) {
                JsonNode items = root.get("items");
                for (JsonNode episodeNode : items) {
                    // 将每个剧集节点转换为Episode对象
                    Podcast.Episode episode = parseEpisodeFromJson(episodeNode);
                    episodes.add(episode);
                }
            }
            
            // 构建成功响应
            result.put("success", true);
            result.put("episodes", episodes);
            result.put("totalEpisodes", episodes.size());
            
            // 提取下一页日期（用于分页）
            if (root.has("next_episode_pub_date")) {
                result.put("nextEpisodePubDate", root.get("next_episode_pub_date").asText());
            }
            
        } catch (Exception e) {
            // 处理异常
            result.put("success", false);
            result.put("message", "Error parsing episodes: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析热门播客响应
     */
    private Map<String, Object> parseTrendingResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析JSON响应
            JsonNode root = objectMapper.readTree(responseBody);
            
            // 解析热门播客列表
            List<Podcast> podcasts = new ArrayList<>();
            if (root.has("podcasts")) {
                JsonNode podcastsNode = root.get("podcasts");
                for (JsonNode podcastNode : podcastsNode) {
                    // 将每个播客节点转换为Podcast对象
                    Podcast podcast = parsePodcastFromJson(podcastNode);
                    podcasts.add(podcast);
                }
            }
            
            // 构建成功响应
            result.put("success", true);
            result.put("trendingPodcasts", podcasts);
            result.put("totalTrending", podcasts.size());
            
        } catch (Exception e) {
            // 处理异常
            result.put("success", false);
            result.put("message", "Error parsing trending response: " + e.getMessage());
        }
        
        return result;
    }

    // ==================== 私有方法：JSON到Java对象转换 ====================

    /**
     * 将JSON节点解析为Podcast对象
     * @param podcastNode JSON节点
     * @return Podcast对象
     */
    private Podcast parsePodcastFromJson(JsonNode podcastNode) {
        Podcast podcast = new Podcast();
        
        // 逐个解析JSON字段并设置到Podcast对象中
        if (podcastNode.has("id")) {
            podcast.setId(podcastNode.get("id").asText());
        }
        if (podcastNode.has("title")) {
            podcast.setTitle(podcastNode.get("title").asText());
        }
        if (podcastNode.has("description")) {
            podcast.setDescription(podcastNode.get("description").asText());
        }
        if (podcastNode.has("publisher")) {
            podcast.setPublisher(podcastNode.get("publisher").asText());
        }
        if (podcastNode.has("image")) {
            podcast.setImage(podcastNode.get("image").asText());
        }
        if (podcastNode.has("thumbnail")) {
            podcast.setThumbnail(podcastNode.get("thumbnail").asText());
        }
        if (podcastNode.has("listennotes_url")) {
            podcast.setListennotesUrl(podcastNode.get("listennotes_url").asText());
        }
        if (podcastNode.has("rss")) {
            podcast.setRss(podcastNode.get("rss").asText());
        }
        if (podcastNode.has("language")) {
            podcast.setLanguage(podcastNode.get("language").asText());
        }
        if (podcastNode.has("country")) {
            podcast.setCountry(podcastNode.get("country").asText());
        }
        if (podcastNode.has("website")) {
            podcast.setWebsite(podcastNode.get("website").asText());
        }
        if (podcastNode.has("is_claimed")) {
            podcast.setClaimed(podcastNode.get("is_claimed").asBoolean());
        }
        if (podcastNode.has("type")) {
            podcast.setType(podcastNode.get("type").asText());
        }
        if (podcastNode.has("total_episodes")) {
            podcast.setTotalEpisodes(podcastNode.get("total_episodes").asInt());
        }
        
        return podcast;
    }

    /**
     * 将JSON节点解析为Episode对象
     * @param episodeNode JSON节点
     * @return Episode对象
     */
    private Podcast.Episode parseEpisodeFromJson(JsonNode episodeNode) {
        Podcast.Episode episode = new Podcast.Episode();
        
        // 逐个解析JSON字段并设置到Episode对象中
        if (episodeNode.has("id")) {
            episode.setId(episodeNode.get("id").asText());
        }
        if (episodeNode.has("title")) {
            episode.setTitle(episodeNode.get("title").asText());
        }
        if (episodeNode.has("description")) {
            episode.setDescription(episodeNode.get("description").asText());
        }
        if (episodeNode.has("audio")) {
            episode.setAudio(episodeNode.get("audio").asText());
        }
        if (episodeNode.has("image")) {
            episode.setImage(episodeNode.get("image").asText());
        }
        if (episodeNode.has("thumbnail")) {
            episode.setThumbnail(episodeNode.get("thumbnail").asText());
        }
        if (episodeNode.has("listennotes_url")) {
            episode.setListennotesUrl(episodeNode.get("listennotes_url").asText());
        }
        if (episodeNode.has("audio_length")) {
            episode.setAudioLength(episodeNode.get("audio_length").asText());
        }
        if (episodeNode.has("pub_date_ms")) {
            // 将毫秒时间戳转换为LocalDateTime
            long pubDateMs = episodeNode.get("pub_date_ms").asLong();
            LocalDateTime pubDate = LocalDateTime.ofEpochSecond(pubDateMs / 1000, 0, java.time.ZoneOffset.UTC);
            episode.setPublishedDate(pubDate);
        }
        if (episodeNode.has("language")) {
            episode.setLanguage(episodeNode.get("language").asText());
        }
        if (episodeNode.has("country")) {
            episode.setCountry(episodeNode.get("country").asText());
        }
        if (episodeNode.has("website")) {
            episode.setWebsite(episodeNode.get("website").asText());
        }
        if (episodeNode.has("is_claimed")) {
            episode.setClaimed(episodeNode.get("is_claimed").asBoolean());
        }
        if (episodeNode.has("type")) {
            episode.setType(episodeNode.get("type").asText());
        }
        
        return episode;
    }
} 