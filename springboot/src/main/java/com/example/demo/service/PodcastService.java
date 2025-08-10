package com.example.demo.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.pojo.Podcast;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 播客服务类
 * 负责与iTunes API进行交互，提供播客搜索、推荐等功能
 */
@Service
public class PodcastService {

    // Podcastindex API配置
    private static final String PODCASTINDEX_API_BASE_URL = "https://api.podcastindex.org/api/1.0";
    private static final String PODCASTINDEX_API_KEY = "LS3YFSDAHTZSGEYYYYHP";
    private static final String PODCASTINDEX_API_SECRET = "#Cxs^5GbcdCnccxgZvbEWkSzH9hv^H4jnvESBVGa";
    
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
     * 生成Podcastindex API认证头
     * @return 包含认证信息的HttpHeaders
     */
    private HttpHeaders generateAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        long timestamp = System.currentTimeMillis() / 1000;
        String userAgent = "UOB-IBM-AI-Elderly-Project/1.0";
        
        // 创建认证字符串
        String authString = PODCASTINDEX_API_KEY + PODCASTINDEX_API_SECRET + timestamp;
        
        // 生成SHA1哈希
        String hash = generateSHA1Hash(authString);
        
        headers.set("User-Agent", userAgent);
        headers.set("X-Auth-Key", PODCASTINDEX_API_KEY);
        headers.set("X-Auth-Date", String.valueOf(timestamp));
        headers.set("Authorization", hash);
        
        return headers;
    }
    
    /**
     * 生成SHA1哈希
     * @param input 输入字符串
     * @return SHA1哈希值
     */
    private String generateSHA1Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
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
            String url = PODCASTINDEX_API_BASE_URL + "/search/byterm";
            
            // 使用UriComponentsBuilder构建查询参数
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("q", query)                       // 搜索关键词
                    .queryParam("max", 20);                       // 限制返回结果数量为20个
            
            // 生成认证头
            HttpHeaders headers = generateAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送GET请求到Podcastindex API
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
                Map<String, Object> searchResult = searchPodcasts(interest, null, null, "relevance", null);
                
                if ((Boolean) searchResult.get("success")) {
                    @SuppressWarnings("unchecked")
                    List<Podcast> podcasts = (List<Podcast>) searchResult.get("podcasts");
                    if (podcasts != null && !podcasts.isEmpty()) {
                        // 添加前2个播客到推荐列表
                        recommendations.addAll(podcasts.subList(0, Math.min(2, podcasts.size())));
                    }
                }
            }
            
            response.put("success", true);
            response.put("recommendations", recommendations);
            response.put("totalRecommendations", recommendations.size());
            
            return response;
            
        } catch (Exception e) {
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
            // 构建API请求URL
            String url = PODCASTINDEX_API_BASE_URL + "/podcasts/byfeedid";
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("id", podcastId);
            
            // 生成认证头
            HttpHeaders headers = generateAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发送GET请求
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            return parsePodcastDetailsResponse(response.getBody());
            
        } catch (Exception e) {
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
            // 首先尝试使用Podcast Index API的episodes接口获取所有剧集
            System.out.println("Trying Podcast Index episodes API for podcast ID: " + podcastId);
            
            List<Podcast.Episode> allEpisodes = new ArrayList<>();
            String lastPubDate = null;
            int maxAttempts = 10; // 最多尝试10次，避免无限循环
            int attempt = 0;
            
            while (attempt < maxAttempts) {
                attempt++;
                System.out.println("Attempt " + attempt + " to fetch episodes");
                
                String episodesUrl = PODCASTINDEX_API_BASE_URL + "/episodes/byfeedid";
                String url = episodesUrl + "?id=" + podcastId + "&max=1000"; // 每次获取1000个剧集
                
                if (lastPubDate != null) {
                    url += "&since=" + lastPubDate;
                } else if (nextEpisodePubDate != null && !nextEpisodePubDate.isEmpty()) {
                    url += "&since=" + nextEpisodePubDate;
                }
                
                System.out.println("Calling episodes API: " + url);
                
                HttpHeaders headers = generateAuthHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    String responseBody = response.getBody();
                    System.out.println("Episodes API response length: " + (responseBody != null ? responseBody.length() : 0));
                    
                    // 尝试解析episodes API响应
                    Map<String, Object> episodesResult = parseEpisodesApiResponse(responseBody);
                    
                    if ((Boolean) episodesResult.get("success")) {
                        @SuppressWarnings("unchecked")
                        List<Podcast.Episode> episodes = (List<Podcast.Episode>) episodesResult.get("episodes");
                        
                        if (episodes != null && !episodes.isEmpty()) {
                            // 记录最早的发布日期，用于下次请求
                            Podcast.Episode oldestEpisode = episodes.get(episodes.size() - 1);
                            if (oldestEpisode.getPublishedDate() != null) {
                                lastPubDate = String.valueOf(oldestEpisode.getPublishedDate().toEpochSecond(java.time.ZoneOffset.UTC));
                            }
                            
                            allEpisodes.addAll(episodes);
                            System.out.println("Added " + episodes.size() + " episodes, total now: " + allEpisodes.size());
                            
                            // 如果返回的剧集数量少于1000，说明已经获取完所有剧集
                            if (episodes.size() < 1000) {
                                System.out.println("Reached end of episodes, breaking loop");
                                break;
                            }
                        } else {
                            System.out.println("No episodes returned, breaking loop");
                            break;
                        }
                    } else {
                        System.out.println("Episodes API failed, breaking loop");
                        break;
                    }
                } else {
                    System.out.println("API call failed with status: " + response.getStatusCode());
                    break;
                }
                
                // 避免过于频繁的API调用
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (!allEpisodes.isEmpty()) {
                System.out.println("Successfully collected " + allEpisodes.size() + " episodes from Podcast Index API");
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("episodes", allEpisodes);
                result.put("totalCount", allEpisodes.size());
                return result;
            } else {
                System.out.println("No episodes collected from API, falling back to RSS parsing");
            }
            
            // 如果API方法失败，回退到RSS解析
            System.out.println("Falling back to RSS parsing method");
            return getPodcastEpisodesFromRss(podcastId, nextEpisodePubDate);
            
        } catch (Exception e) {
            System.err.println("Error in episodes API call: " + e.getMessage());
            e.printStackTrace();
            
            // 回退到RSS解析
            try {
                System.out.println("Falling back to RSS parsing due to API error");
                return getPodcastEpisodesFromRss(podcastId, nextEpisodePubDate);
            } catch (Exception rssError) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error getting episodes: " + e.getMessage() + " | RSS fallback failed: " + rssError.getMessage());
                return errorResponse;
            }
        }
    }
    
    /**
     * 从RSS feed获取播客剧集（回退方法）
     */
    private Map<String, Object> getPodcastEpisodesFromRss(String podcastId, String nextEpisodePubDate) {
        try {
            // 首先获取播客信息以获取feedUrl
            Map<String, Object> podcastDetails = getPodcastDetails(podcastId);
            
            if (!(Boolean) podcastDetails.get("success")) {
                return podcastDetails;
            }
            
            Podcast podcast = (Podcast) podcastDetails.get("podcast");
            String feedUrl = podcast.getRss();
            
            if (feedUrl == null || feedUrl.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "No RSS feed URL available for this podcast");
                return errorResponse;
            }
            
            // 解析RSS feed获取剧集
            return parseRssFeed(feedUrl);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting episodes from RSS: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 获取热门播客列表
     * @param region 地区（可选），用于获取特定地区的热门播客
     * @return 热门播客列表
     */
    public Map<String, Object> getTrendingPodcasts(String region) {
        try {
            // 使用搜索API获取热门播客
            return searchPodcasts("popular", null, region, "relevance", null);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting trending podcasts: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 解析搜索响应
     * @param responseBody API响应体
     * @return 解析后的播客列表
     */
    private Map<String, Object> parseSearchResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("Raw API response: " + responseBody.substring(0, Math.min(500, responseBody.length())));
            
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            System.out.println("Parsed JSON root node keys: " + rootNode.fieldNames());
            
            // 检查API状态
            if (rootNode.has("status")) {
                String status = rootNode.get("status").asText();
                System.out.println("API status: " + status);
                
                if ("true".equals(status)) {
                    if (rootNode.has("feeds")) {
                        JsonNode feedsNode = rootNode.get("feeds");
                        System.out.println("Found " + feedsNode.size() + " feeds");
                        
                        List<Podcast> podcasts = new ArrayList<>();
                        
                        for (JsonNode feedNode : feedsNode) {
                            try {
                                Podcast podcast = parsePodcastFromJson(feedNode);
                                podcasts.add(podcast);
                                System.out.println("Parsed podcast: " + podcast.getTitle());
                            } catch (Exception e) {
                                System.err.println("Error parsing individual podcast: " + e.getMessage());
                            }
                        }
                        
                        result.put("success", true);
                        result.put("podcasts", podcasts);
                        result.put("totalCount", rootNode.path("count").asInt(podcasts.size()));
                        System.out.println("Successfully parsed " + podcasts.size() + " podcasts");
                    } else {
                        result.put("success", false);
                        result.put("message", "No feeds found in response");
                        System.out.println("No feeds field found in response");
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "API returned error status: " + status);
                    System.out.println("API returned error status: " + status);
                }
            } else {
                result.put("success", false);
                result.put("message", "No status field found in response");
                System.out.println("No status field found in response");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error parsing search response: " + e.getMessage());
            System.err.println("Error parsing search response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 解析播客详情响应
     * @param responseBody API响应体
     * @return 解析后的播客详情
     */
    private Map<String, Object> parsePodcastDetailsResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("Podcast details response: " + responseBody.substring(0, Math.min(500, responseBody.length())));
            
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // 检查API状态
            if (rootNode.has("status") && "true".equals(rootNode.get("status").asText())) {
                if (rootNode.has("feed")) {
                    JsonNode feedNode = rootNode.get("feed");
                    Podcast podcast = parsePodcastFromJson(feedNode);
                    result.put("success", true);
                    result.put("podcast", podcast);
                    System.out.println("Successfully parsed podcast: " + podcast.getTitle());
                } else {
                    result.put("success", false);
                    result.put("message", "No feed found in response");
                    System.out.println("No feed field found in response");
                }
            } else {
                result.put("success", false);
                result.put("message", "API returned error status: " + rootNode.path("status").asText());
                System.out.println("API returned error status: " + rootNode.path("status").asText());
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error parsing podcast details response: " + e.getMessage());
            System.err.println("Error parsing podcast details response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 解析剧集响应
     * @param responseBody API响应体
     * @return 解析后的剧集列表
     */
    private Map<String, Object> parseEpisodesResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            if (rootNode.has("episodes")) {
                JsonNode episodesNode = rootNode.get("episodes");
                List<Podcast.Episode> episodes = new ArrayList<>();
                
                for (JsonNode episodeNode : episodesNode) {
                    Podcast.Episode episode = parseEpisodeFromJson(episodeNode);
                    episodes.add(episode);
                }
                
                result.put("success", true);
                result.put("episodes", episodes);
                result.put("totalEpisodes", episodes.size());
            } else {
                result.put("success", false);
                result.put("message", "No episodes found in response");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error parsing episodes response: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析热门播客响应
     * @param responseBody API响应体
     * @return 解析后的热门播客列表
     */
    private Map<String, Object> parseTrendingResponse(String responseBody) {
        return parseSearchResponse(responseBody);
    }
    
    /**
     * 解析RSS feed获取剧集
     * @param feedUrl RSS feed URL
     * @return 剧集列表
     */
    private Map<String, Object> parseRssFeed(String feedUrl) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("Parsing RSS feed: " + feedUrl);
            
            // 设置User-Agent和跟随重定向
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    feedUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            String rssContent = response.getBody();
            System.out.println("RSS content length: " + (rssContent != null ? rssContent.length() : 0));
            
            if (rssContent == null || rssContent.isEmpty()) {
                result.put("success", false);
                result.put("message", "Empty RSS content");
                return result;
            }
            
            // 解析XML
            org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(rssContent.getBytes("UTF-8")));
            
            List<Podcast.Episode> episodes = new ArrayList<>();
            
            // 查找所有item标签（RSS）或entry标签（Atom）
            org.w3c.dom.NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() == 0) {
                items = doc.getElementsByTagName("entry"); // 尝试Atom格式
            }
            
            System.out.println("Found " + items.getLength() + " episodes");
            
            for (int i = 0; i < Math.min(items.getLength(), 10); i++) { // 限制10个剧集
                org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(i);
                Podcast.Episode episode = parseEpisodeFromXmlElement(item);
                if (episode != null) {
                    episodes.add(episode);
                }
            }
            
            result.put("success", true);
            result.put("episodes", episodes);
            result.put("totalEpisodes", episodes.size());
            
        } catch (Exception e) {
            System.err.println("Error parsing RSS feed: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error parsing RSS feed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 从XML元素解析剧集
     * @param item XML元素
     * @return 剧集对象
     */
    private Podcast.Episode parseEpisodeFromXmlElement(org.w3c.dom.Element item) {
        try {
            Podcast.Episode episode = new Podcast.Episode();
            
            // 获取标题
            String title = getElementText(item, "title");
            episode.setTitle(title != null ? title : "Unknown Episode");
            
            // 获取描述
            String description = getElementText(item, "description");
            if (description == null) {
                description = getElementText(item, "summary");
            }
            if (description == null) {
                description = getElementText(item, "content");
            }
            episode.setDescription(description != null ? description : "");
            
            // 获取音频URL - 支持多种格式
            String audioUrl = getAudioUrlFromElement(item);
            episode.setAudio(audioUrl);
            
            // 调试信息
            if (audioUrl != null) {
                System.out.println("Found audio URL: " + audioUrl);
            } else {
                System.out.println("No audio URL found for episode: " + title);
                // 尝试从描述中提取音频URL
                if (description != null && !description.isEmpty()) {
                    String extractedUrl = extractUrlFromText(description);
                    if (extractedUrl != null) {
                        episode.setAudio(extractedUrl);
                        System.out.println("Extracted audio URL from description: " + extractedUrl);
                    }
                }
            }
            
            // 获取图片
            String image = getElementText(item, "image");
            if (image == null) {
                image = getElementText(item, "itunes:image");
            }
            if (image == null) {
                org.w3c.dom.NodeList imageNodes = item.getElementsByTagName("image");
                if (imageNodes.getLength() > 0) {
                    org.w3c.dom.Element imageElement = (org.w3c.dom.Element) imageNodes.item(0);
                    image = getElementText(imageElement, "url");
                }
            }
            episode.setImage(image);
            episode.setThumbnail(image);
            
            // 获取时长
            String duration = getElementText(item, "duration");
            if (duration == null) {
                duration = getElementText(item, "itunes:duration");
            }
            episode.setAudioLength(duration != null ? duration : "0");
            
            // 获取发布日期
            String pubDate = getElementText(item, "pubDate");
            if (pubDate == null) {
                pubDate = getElementText(item, "published");
            }
            if (pubDate == null) {
                pubDate = getElementText(item, "updated");
            }
            
            if (pubDate != null) {
                try {
                    // 尝试多种日期格式
                    java.text.SimpleDateFormat[] formats = {
                        new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US),
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US),
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US),
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                    };
                    
                    java.util.Date date = null;
                    for (java.text.SimpleDateFormat sdf : formats) {
                        try {
                            date = sdf.parse(pubDate);
                            break;
                        } catch (Exception e) {
                            // 继续尝试下一个格式
                        }
                    }
                    
                    if (date != null) {
                        episode.setPublishedDate(date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    } else {
                        episode.setPublishedDate(LocalDateTime.now());
                    }
                } catch (Exception e) {
                    episode.setPublishedDate(LocalDateTime.now());
                }
            } else {
                episode.setPublishedDate(LocalDateTime.now());
            }
            
            episode.setLanguage("en");
            episode.setClaimed(false);
            episode.setType("episode");
            
            return episode;
            
        } catch (Exception e) {
            System.err.println("Error parsing episode from XML: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 从XML元素获取音频URL
     * @param item XML元素
     * @return 音频URL
     */
    private String getAudioUrlFromElement(org.w3c.dom.Element item) {
        // 1. 尝试enclosure标签（最常见的音频URL格式）
        org.w3c.dom.NodeList enclosures = item.getElementsByTagName("enclosure");
        for (int i = 0; i < enclosures.getLength(); i++) {
            org.w3c.dom.Element enclosure = (org.w3c.dom.Element) enclosures.item(i);
            String type = enclosure.getAttribute("type");
            String url = enclosure.getAttribute("url");
            
            // 检查音频类型或直接检查URL扩展名
            if (url != null && !url.isEmpty()) {
                if (type != null && (type.startsWith("audio/") || type.equals("application/octet-stream"))) {
                    return url;
                }
                // 如果没有type属性，检查URL扩展名
                if (type == null || type.isEmpty()) {
                    String lowerUrl = url.toLowerCase();
                    if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                        lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                        lowerUrl.endsWith(".aac") || lowerUrl.contains("audio") ||
                        lowerUrl.contains("podcast") || lowerUrl.contains("episode")) {
                        return url;
                    }
                }
            }
        }
        
        // 2. 尝试media:content标签
        org.w3c.dom.NodeList mediaContents = item.getElementsByTagName("media:content");
        for (int i = 0; i < mediaContents.getLength(); i++) {
            org.w3c.dom.Element mediaContent = (org.w3c.dom.Element) mediaContents.item(i);
            String type = mediaContent.getAttribute("type");
            String url = mediaContent.getAttribute("url");
            
            if (url != null && !url.isEmpty()) {
                if (type != null && type.startsWith("audio/")) {
                    return url;
                }
                // 检查URL扩展名
                String lowerUrl = url.toLowerCase();
                if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                    lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                    lowerUrl.endsWith(".aac")) {
                    return url;
                }
            }
        }
        
        // 3. 尝试link标签
        org.w3c.dom.NodeList links = item.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            org.w3c.dom.Element link = (org.w3c.dom.Element) links.item(i);
            String type = link.getAttribute("type");
            String url = link.getAttribute("href");
            
            if (url != null && !url.isEmpty()) {
                if (type != null && type.startsWith("audio/")) {
                    return url;
                }
                // 检查URL扩展名
                String lowerUrl = url.toLowerCase();
                if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                    lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                    lowerUrl.endsWith(".aac")) {
                    return url;
                }
            }
        }
        
        // 4. 尝试media:group/media:content（嵌套结构）
        org.w3c.dom.NodeList mediaGroups = item.getElementsByTagName("media:group");
        for (int i = 0; i < mediaGroups.getLength(); i++) {
            org.w3c.dom.Element mediaGroup = (org.w3c.dom.Element) mediaGroups.item(i);
            org.w3c.dom.NodeList groupContents = mediaGroup.getElementsByTagName("media:content");
            for (int j = 0; j < groupContents.getLength(); j++) {
                org.w3c.dom.Element groupContent = (org.w3c.dom.Element) groupContents.item(j);
                String type = groupContent.getAttribute("type");
                String url = groupContent.getAttribute("url");
                
                if (url != null && !url.isEmpty()) {
                    if (type != null && type.startsWith("audio/")) {
                        return url;
                    }
                    // 检查URL扩展名
                    String lowerUrl = url.toLowerCase();
                    if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                        lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                        lowerUrl.endsWith(".aac")) {
                        return url;
                    }
                }
            }
        }
        
        // 5. 尝试atom:link标签（Atom格式）
        org.w3c.dom.NodeList atomLinks = item.getElementsByTagName("atom:link");
        for (int i = 0; i < atomLinks.getLength(); i++) {
            org.w3c.dom.Element atomLink = (org.w3c.dom.Element) atomLinks.item(i);
            String rel = atomLink.getAttribute("rel");
            String type = atomLink.getAttribute("type");
            String url = atomLink.getAttribute("href");
            
            if (url != null && !url.isEmpty()) {
                if ("enclosure".equals(rel) || "alternate".equals(rel)) {
                    if (type != null && type.startsWith("audio/")) {
                        return url;
                    }
                    // 检查URL扩展名
                    String lowerUrl = url.toLowerCase();
                    if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                        lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                        lowerUrl.endsWith(".aac")) {
                        return url;
                    }
                }
            }
        }
        
        // 6. 尝试查找任何包含音频扩展名的URL
        org.w3c.dom.NodeList allElements = item.getElementsByTagName("*");
        for (int i = 0; i < allElements.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) allElements.item(i);
            String textContent = element.getTextContent();
            if (textContent != null && !textContent.isEmpty()) {
                String lowerText = textContent.toLowerCase();
                if (lowerText.contains(".mp3") || lowerText.contains(".m4a") || 
                    lowerText.contains(".wav") || lowerText.contains(".ogg") ||
                    lowerText.contains(".aac")) {
                    // 提取URL
                    String url = extractUrlFromText(textContent);
                    if (url != null && !url.isEmpty()) {
                        return url;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 从文本中提取URL
     * @param text 包含URL的文本
     * @return 提取的URL
     */
    private String extractUrlFromText(String text) {
        // 简单的URL提取正则表达式
        String urlPattern = "https?://[^\\s<>\"']+";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(urlPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String url = matcher.group();
            String lowerUrl = url.toLowerCase();
            // 只返回音频文件URL
            if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                lowerUrl.endsWith(".aac")) {
                return url;
            }
        }
        
        return null;
    }
    
    /**
     * 获取XML元素的文本内容
     * @param parent 父元素
     * @param tagName 标签名
     * @return 文本内容
     */
    private String getElementText(org.w3c.dom.Element parent, String tagName) {
        org.w3c.dom.NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    /**
     * 从JSON节点解析播客对象
     * @param podcastNode JSON节点
     * @return 播客对象
     */
    private Podcast parsePodcastFromJson(JsonNode podcastNode) {
        Podcast podcast = new Podcast();
        
        // Podcastindex API字段映射
        podcast.setId(podcastNode.path("id").asText());
        podcast.setTitle(podcastNode.path("title").asText());
        podcast.setDescription(podcastNode.path("description").asText());
        podcast.setPublisher(podcastNode.path("author").asText());
        podcast.setImage(podcastNode.path("image").asText());
        podcast.setThumbnail(podcastNode.path("artwork").asText());
        
        // 调试RSS URL
        String rssUrl = podcastNode.path("url").asText();
        System.out.println("RSS URL from API: " + rssUrl);
        podcast.setRss(rssUrl);
        
        podcast.setLanguage(podcastNode.path("language").asText("en"));
        podcast.setCountry(podcastNode.path("country").asText());
        podcast.setWebsite(podcastNode.path("link").asText());
        podcast.setClaimed(false);
        podcast.setType("podcast");
        podcast.setTotalEpisodes(podcastNode.path("episodeCount").asInt());
        
        // 解析分类
        List<String> genres = new ArrayList<>();
        if (podcastNode.has("categories")) {
            JsonNode categoriesNode = podcastNode.get("categories");
            for (JsonNode categoryNode : categoriesNode) {
                genres.add(categoryNode.asText());
            }
        }
        podcast.setGenres(genres);
        
        podcast.setCreatedAt(LocalDateTime.now());
        podcast.setUpdatedAt(LocalDateTime.now());
        
        return podcast;
    }

    /**
     * 从JSON节点解析剧集对象
     * @param episodeNode JSON节点
     * @return 剧集对象
     */
    private Podcast.Episode parseEpisodeFromJson(JsonNode episodeNode) {
        Podcast.Episode episode = new Podcast.Episode();
        
        // Podcastindex API字段映射
        episode.setId(episodeNode.path("id").asText());
        episode.setTitle(episodeNode.path("title").asText());
        episode.setDescription(episodeNode.path("description").asText());
        episode.setAudio(episodeNode.path("enclosureUrl").asText());
        episode.setImage(episodeNode.path("image").asText());
        episode.setThumbnail(episodeNode.path("image").asText());
        episode.setAudioLength(episodeNode.path("length").asText());
        
        // 解析发布日期
        long pubDate = episodeNode.path("datePublished").asLong();
        if (pubDate > 0) {
            episode.setPublishedDate(LocalDateTime.ofEpochSecond(pubDate, 0, java.time.ZoneOffset.UTC));
        }
        
        episode.setLanguage(episodeNode.path("language").asText("en"));
        episode.setCountry(episodeNode.path("country").asText());
        episode.setWebsite(episodeNode.path("link").asText());
        episode.setClaimed(false);
        episode.setType("episode");
        
        return episode;
    }

    /**
     * 获取专为老年用户设计的播客推荐
     * @return 适合老年用户的播客推荐
     */
    public Map<String, Object> getElderlyPodcastRecommendations() {
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
        
        return getPodcastRecommendations(elderlyInterests);
    }
    
    /**
     * 解析Podcast Index episodes API响应
     * @param responseBody API响应体
     * @return 解析后的剧集列表
     */
    private Map<String, Object> parseEpisodesApiResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("Parsing episodes API response...");
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            if (rootNode.has("status") && "true".equals(rootNode.get("status").asText())) {
                if (rootNode.has("items")) {
                    JsonNode itemsNode = rootNode.get("items");
                    System.out.println("Found " + itemsNode.size() + " episodes in API response");
                    
                    List<Podcast.Episode> episodes = new ArrayList<>();
                    
                    for (JsonNode episodeNode : itemsNode) {
                        try {
                            Podcast.Episode episode = parseEpisodeFromApiJson(episodeNode);
                            if (episode != null && episode.getAudio() != null && !episode.getAudio().isEmpty()) {
                                episodes.add(episode);
                                System.out.println("Parsed episode with audio: " + episode.getTitle());
                            } else {
                                System.out.println("Skipped episode without audio: " + 
                                    (episode != null ? episode.getTitle() : "Unknown"));
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing individual episode: " + e.getMessage());
                        }
                    }
                    
                    if (!episodes.isEmpty()) {
                        result.put("success", true);
                        result.put("episodes", episodes);
                        result.put("totalCount", episodes.size());
                        System.out.println("Successfully parsed " + episodes.size() + " episodes with audio");
                    } else {
                        result.put("success", false);
                        result.put("message", "No episodes with audio found in API response");
                        System.out.println("No episodes with audio found in API response");
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "No items found in episodes API response");
                    System.out.println("No items field found in episodes API response");
                }
            } else {
                result.put("success", false);
                result.put("message", "API returned error status");
                System.out.println("API returned error status");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error parsing episodes API response: " + e.getMessage());
            System.err.println("Error parsing episodes API response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 从API JSON节点解析剧集对象
     * @param episodeNode JSON节点
     * @return 剧集对象
     */
    private Podcast.Episode parseEpisodeFromApiJson(JsonNode episodeNode) {
        try {
            Podcast.Episode episode = new Podcast.Episode();
            
            // 基本信息
            episode.setId(episodeNode.path("id").asText());
            episode.setTitle(episodeNode.path("title").asText());
            episode.setDescription(episodeNode.path("description").asText());
            
            // 音频URL - 优先使用enclosureUrl
            String audioUrl = episodeNode.path("enclosureUrl").asText();
            if (audioUrl == null || audioUrl.isEmpty()) {
                audioUrl = episodeNode.path("enclosure").path("url").asText();
            }
            if (audioUrl == null || audioUrl.isEmpty()) {
                audioUrl = episodeNode.path("audio").asText();
            }
            episode.setAudio(audioUrl);
            
            // 时长
            String duration = episodeNode.path("duration").asText();
            if (duration == null || duration.isEmpty()) {
                duration = episodeNode.path("enclosure").path("length").asText();
            }
            episode.setAudioLength(duration != null ? duration : "0");
            
            // 发布日期
            long datePublished = episodeNode.path("datePublished").asLong(0);
            if (datePublished > 0) {
                episode.setPublishedDate(LocalDateTime.ofEpochSecond(datePublished, 0, java.time.ZoneOffset.UTC));
            } else {
                episode.setPublishedDate(LocalDateTime.now());
            }
            
            // 图片
            String image = episodeNode.path("image").asText();
            episode.setImage(image);
            episode.setThumbnail(image);
            
            // 链接
            episode.setWebsite(episodeNode.path("link").asText());
            
            episode.setLanguage("en");
            episode.setClaimed(false);
            episode.setType("episode");
            
            return episode;
            
        } catch (Exception e) {
            System.err.println("Error parsing episode from API JSON: " + e.getMessage());
            return null;
        }
    }
} 