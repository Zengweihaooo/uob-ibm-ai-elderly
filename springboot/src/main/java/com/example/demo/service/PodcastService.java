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
 * Podcast service class
 * Responsible for interacting with iTunes API, providing podcast search and recommendation features
 */
@Service
public class PodcastService {

    // Podcastindex API configuration
    private static final String PODCASTINDEX_API_BASE_URL = "https://api.podcastindex.org/api/1.0";
    private static final String PODCASTINDEX_API_KEY = "LS3YFSDAHTZSGEYYYYHP";
    private static final String PODCASTINDEX_API_SECRET = "#Cxs^5GbcdCnccxgZvbEWkSzH9hv^H4jnvESBVGa";
    
    // HTTP request client
    private final RestTemplate restTemplate;
    // JSON parser
    private final ObjectMapper objectMapper;

    /**
     * Constructor
     * Initialize HTTP client and JSON parser
     */
    public PodcastService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Generate Podcastindex API authentication headers
     * @return HttpHeaders containing authentication information
     */
    private HttpHeaders generateAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        long timestamp = System.currentTimeMillis() / 1000;
        String userAgent = "UOB-IBM-AI-Elderly-Project/1.0";
        
        // Create authentication string
        String authString = PODCASTINDEX_API_KEY + PODCASTINDEX_API_SECRET + timestamp;
        
        // Generate SHA1 hash
        String hash = generateSHA1Hash(authString);
        
        headers.set("User-Agent", userAgent);
        headers.set("X-Auth-Key", PODCASTINDEX_API_KEY);
        headers.set("X-Auth-Date", String.valueOf(timestamp));
        headers.set("Authorization", hash);
        
        return headers;
    }
    
    /**
     * Generate SHA1 hash
     * @param input input string
     * @return SHA1 hash value
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
     * Search podcasts by keyword
     * @param query search keyword
     * @param language language filter (optional)
     * @param region region filter (optional)
     * @param sortBy sorting method (optional): relevance, rating, latest
     * @param type podcast type filter (optional)
     * @return podcast list matching search criteria
     */
    public Map<String, Object> searchPodcasts(String query, String language, String region, String sortBy, String type) {
        try {
            // Build API request URL
            String url = PODCASTINDEX_API_BASE_URL + "/search/byterm";
            
            // Use UriComponentsBuilder to build query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("q", query)                       // Search keyword
                    .queryParam("max", 20);                       // Limit to 20 results
            
            // Generate authentication headers
            HttpHeaders headers = generateAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Send GET request to Podcastindex API
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Parse API response
            return parseSearchResponse(response.getBody());
            
        } catch (Exception e) {
            // Handle exceptions, return error message
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error searching podcasts: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Get podcast recommendations based on user interests
     * @param interests user interests list
     * @return recommended podcast list
     */
    public Map<String, Object> getPodcastRecommendations(List<String> interests) {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Podcast> recommendations = new ArrayList<>();
            
            // Iterate through each interest, search for related podcasts
            for (String interest : interests) {
                Map<String, Object> searchResult = searchPodcasts(interest, null, null, "relevance", null);
                
                if ((Boolean) searchResult.get("success")) {
                    @SuppressWarnings("unchecked")
                    List<Podcast> podcasts = (List<Podcast>) searchResult.get("podcasts");
                    if (podcasts != null && !podcasts.isEmpty()) {
                        // Add the first 2 podcasts to the recommendations list
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
     * Get podcast details by podcast ID
     * @param podcastId podcast ID
     * @return podcast details
     */
    public Map<String, Object> getPodcastDetails(String podcastId) {
        try {
            // Build API request URL
            String url = PODCASTINDEX_API_BASE_URL + "/podcasts/byfeedid";
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("id", podcastId);
            
            // Generate authentication headers
            HttpHeaders headers = generateAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Send GET request
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
     * Get episode list for a specific podcast
     * @param podcastId podcast ID
     * @param nextEpisodePubDate publication date of the next episode for pagination (optional)
     * @return episode list
     */
    public Map<String, Object> getPodcastEpisodes(String podcastId, String nextEpisodePubDate) {
        try {
            // First, try to get episodes using the Podcast Index API's episodes interface
            System.out.println("Trying Podcast Index episodes API for podcast ID: " + podcastId);
            
            List<Podcast.Episode> allEpisodes = new ArrayList<>();
            String lastPubDate = null;
            int maxAttempts = 10; // Max 10 attempts to avoid infinite loop
            int attempt = 0;
            
            while (attempt < maxAttempts) {
                attempt++;
                System.out.println("Attempt " + attempt + " to fetch episodes");
                
                String episodesUrl = PODCASTINDEX_API_BASE_URL + "/episodes/byfeedid";
                String url = episodesUrl + "?id=" + podcastId + "&max=1000"; // Get 1000 episodes each time
                
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
                    
                    // Try to parse episodes API response
                    Map<String, Object> episodesResult = parseEpisodesApiResponse(responseBody);
                    
                    if ((Boolean) episodesResult.get("success")) {
                        @SuppressWarnings("unchecked")
                        List<Podcast.Episode> episodes = (List<Podcast.Episode>) episodesResult.get("episodes");
                        
                        if (episodes != null && !episodes.isEmpty()) {
                            // Record the earliest publication date for the next request
                            Podcast.Episode oldestEpisode = episodes.get(episodes.size() - 1);
                            if (oldestEpisode.getPublishedDate() != null) {
                                lastPubDate = String.valueOf(oldestEpisode.getPublishedDate().toEpochSecond(java.time.ZoneOffset.UTC));
                            }
                            
                            allEpisodes.addAll(episodes);
                            System.out.println("Added " + episodes.size() + " episodes, total now: " + allEpisodes.size());
                            
                            // If fewer than 1000 episodes are returned, it means all episodes have been fetched
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
                
                // Avoid excessive API calls
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
            
            // If API method fails, fall back to RSS parsing
            System.out.println("Falling back to RSS parsing method");
            return getPodcastEpisodesFromRss(podcastId, nextEpisodePubDate);
            
        } catch (Exception e) {
            System.err.println("Error in episodes API call: " + e.getMessage());
            e.printStackTrace();
            
            // Fall back to RSS parsing
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
     * Get episodes from RSS feed (fallback method)
     */
    private Map<String, Object> getPodcastEpisodesFromRss(String podcastId, String nextEpisodePubDate) {
        try {
            // First, get podcast information to get the feedUrl
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
            
            // Parse RSS feed to get episodes
            return parseRssFeed(feedUrl);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting episodes from RSS: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Get trending podcast list
     * @param region (optional), for getting trending podcasts in a specific region
     * @return trending podcast list
     */
    public Map<String, Object> getTrendingPodcasts(String region) {
        try {
            // Use search API to get trending podcasts
            return searchPodcasts("popular", null, region, "relevance", null);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error getting trending podcasts: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Parse search response
     * @param responseBody API response body
     * @return parsed podcast list
     */
    private Map<String, Object> parseSearchResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("Raw API response: " + responseBody.substring(0, Math.min(500, responseBody.length())));
            
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            System.out.println("Parsed JSON root node keys: " + rootNode.fieldNames());
            
            // Check API status
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
     * Parse podcast details response
     * @param responseBody API response body
     * @return parsed podcast details
     */
    private Map<String, Object> parsePodcastDetailsResponse(String responseBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("Podcast details response: " + responseBody.substring(0, Math.min(500, responseBody.length())));
            
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // Check API status
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
     * Parse episode response
     * @param responseBody API response body
     * @return parsed episode list
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
     * Parse trending podcast response
     * @param responseBody API response body
     * @return parsed trending podcast list
     */
    private Map<String, Object> parseTrendingResponse(String responseBody) {
        return parseSearchResponse(responseBody);
    }
    
    /**
     * Parse RSS feed to get episodes
     * @param feedUrl RSS feed URL
     * @return episode list
     */
    private Map<String, Object> parseRssFeed(String feedUrl) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("Parsing RSS feed: " + feedUrl);
            
            // Set User-Agent and follow redirects
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
            
            // Parse XML
            org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(rssContent.getBytes("UTF-8")));
            
            List<Podcast.Episode> episodes = new ArrayList<>();
            
            // Find all item tags (RSS) or entry tags (Atom)
            org.w3c.dom.NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() == 0) {
                items = doc.getElementsByTagName("entry"); // Try Atom format
            }
            
            System.out.println("Found " + items.getLength() + " episodes");
            
            for (int i = 0; i < Math.min(items.getLength(), 10); i++) { // Limit to 10 episodes
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
     * Parse episode from XML element
     * @param item XML element
     * @return episode object
     */
    private Podcast.Episode parseEpisodeFromXmlElement(org.w3c.dom.Element item) {
        try {
            Podcast.Episode episode = new Podcast.Episode();
            
            // Get title
            String title = getElementText(item, "title");
            episode.setTitle(title != null ? title : "Unknown Episode");
            
            // Get description
            String description = getElementText(item, "description");
            if (description == null) {
                description = getElementText(item, "summary");
            }
            if (description == null) {
                description = getElementText(item, "content");
            }
            episode.setDescription(description != null ? description : "");
            
            // Get audio URL - supports multiple formats
            String audioUrl = getAudioUrlFromElement(item);
            episode.setAudio(audioUrl);
            
            // Debug information
            if (audioUrl != null) {
                System.out.println("Found audio URL: " + audioUrl);
            } else {
                System.out.println("No audio URL found for episode: " + title);
                // Try to extract audio URL from description
                if (description != null && !description.isEmpty()) {
                    String extractedUrl = extractUrlFromText(description);
                    if (extractedUrl != null) {
                        episode.setAudio(extractedUrl);
                        System.out.println("Extracted audio URL from description: " + extractedUrl);
                    }
                }
            }
            
            // Get image - enhanced image extraction logic
            String image = getElementText(item, "image");
            if (image == null) {
                image = getElementText(item, "itunes:image");
            }
            if (image == null) {
                org.w3c.dom.NodeList mediaContents = item.getElementsByTagName("media:content");
                for (int i = 0; i < mediaContents.getLength(); i++) {
                    org.w3c.dom.Element mediaContent = (org.w3c.dom.Element) mediaContents.item(i);
                    String medium = mediaContent.getAttribute("medium");
                    String url = mediaContent.getAttribute("url");
                    if ("image".equalsIgnoreCase(medium) && url != null && !url.isEmpty()) {
                        image = url;
                        break;
                    }
                }
            }
            if (image == null) {
                org.w3c.dom.NodeList mediaThumbnails = item.getElementsByTagName("media:thumbnail");
                if (mediaThumbnails.getLength() > 0) {
                    org.w3c.dom.Element mediaThumbnail = (org.w3c.dom.Element) mediaThumbnails.item(0);
                    image = mediaThumbnail.getAttribute("url");
                }
            }
            if (image == null) {
                org.w3c.dom.NodeList imageNodes = item.getElementsByTagName("image");
                if (imageNodes.getLength() > 0) {
                    org.w3c.dom.Element imageElement = (org.w3c.dom.Element) imageNodes.item(0);
                    image = getElementText(imageElement, "url");
                }
            }
            // Try to extract image URL from description
            if (image == null && description != null && !description.isEmpty()) {
                image = extractImageUrlFromText(description);
            }
            episode.setImage(image);
            episode.setThumbnail(image);
            
            // Get duration
            String duration = getElementText(item, "duration");
            if (duration == null) {
                duration = getElementText(item, "itunes:duration");
            }
            episode.setAudioLength(duration != null ? duration : "0");
            
            // Get publication date
            String pubDate = getElementText(item, "pubDate");
            if (pubDate == null) {
                pubDate = getElementText(item, "published");
            }
            if (pubDate == null) {
                pubDate = getElementText(item, "updated");
            }
            
            if (pubDate != null) {
                try {
                    // Try multiple date formats
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
                            // Continue to the next format
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
     * Get audio URL from XML element
     * @param item XML element
     * @return audio URL
     */
    private String getAudioUrlFromElement(org.w3c.dom.Element item) {
        // 1. Try enclosure tag (most common audio URL format)
        org.w3c.dom.NodeList enclosures = item.getElementsByTagName("enclosure");
        for (int i = 0; i < enclosures.getLength(); i++) {
            org.w3c.dom.Element enclosure = (org.w3c.dom.Element) enclosures.item(i);
            String type = enclosure.getAttribute("type");
            String url = enclosure.getAttribute("url");
            
            // Check audio type or directly check URL extension
            if (url != null && !url.isEmpty()) {
                if (type != null && (type.startsWith("audio/") || type.equals("application/octet-stream"))) {
                    return url;
                }
                // If no type attribute, check URL extension
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
        
        // 2. Try media:content tag
        org.w3c.dom.NodeList mediaContents = item.getElementsByTagName("media:content");
        for (int i = 0; i < mediaContents.getLength(); i++) {
            org.w3c.dom.Element mediaContent = (org.w3c.dom.Element) mediaContents.item(i);
            String type = mediaContent.getAttribute("type");
            String url = mediaContent.getAttribute("url");
            
            if (url != null && !url.isEmpty()) {
                if (type != null && type.startsWith("audio/")) {
                    return url;
                }
                // Check URL extension
                String lowerUrl = url.toLowerCase();
                if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                    lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                    lowerUrl.endsWith(".aac")) {
                    return url;
                }
            }
        }
        
        // 3. Try link tag
        org.w3c.dom.NodeList links = item.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            org.w3c.dom.Element link = (org.w3c.dom.Element) links.item(i);
            String type = link.getAttribute("type");
            String url = link.getAttribute("href");
            
            if (url != null && !url.isEmpty()) {
                if (type != null && type.startsWith("audio/")) {
                    return url;
                }
                // Check URL extension
                String lowerUrl = url.toLowerCase();
                if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                    lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                    lowerUrl.endsWith(".aac")) {
                    return url;
                }
            }
        }
        
        // 4. Try media:group/media:content (nested structure)
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
                    // Check URL extension
                    String lowerUrl = url.toLowerCase();
                    if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                        lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                        lowerUrl.endsWith(".aac")) {
                        return url;
                    }
                }
            }
        }
        
        // 5. Try atom:link tag (Atom format)
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
                    // Check URL extension
                    String lowerUrl = url.toLowerCase();
                    if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                        lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                        lowerUrl.endsWith(".aac")) {
                        return url;
                    }
                }
            }
        }
        
        // 6. Try to find any URL containing an audio extension
        org.w3c.dom.NodeList allElements = item.getElementsByTagName("*");
        for (int i = 0; i < allElements.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) allElements.item(i);
            String textContent = element.getTextContent();
            if (textContent != null && !textContent.isEmpty()) {
                String lowerText = textContent.toLowerCase();
                if (lowerText.contains(".mp3") || lowerText.contains(".m4a") || 
                    lowerText.contains(".wav") || lowerText.contains(".ogg") ||
                    lowerText.contains(".aac")) {
                    // Extract URL
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
     * Extract URL from text
     * @param text text containing URL
     * @return extracted URL
     */
    private String extractUrlFromText(String text) {
        // Simple URL extraction regex
        String urlPattern = "https?://[^\\s<>\"']+";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(urlPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String url = matcher.group();
            String lowerUrl = url.toLowerCase();
            // Only return audio file URLs
            if (lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4a") || 
                lowerUrl.endsWith(".wav") || lowerUrl.endsWith(".ogg") ||
                lowerUrl.endsWith(".aac")) {
                return url;
            }
        }
        
        return null;
    }
    
    /**
     * Get text content of XML element
     * @param parent parent element
     * @param tagName tag name
     * @return text content
     */
    private String getElementText(org.w3c.dom.Element parent, String tagName) {
        org.w3c.dom.NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Parse podcast object from JSON node
     * @param podcastNode JSON node
     * @return podcast object
     */
    private Podcast parsePodcastFromJson(JsonNode podcastNode) {
        Podcast podcast = new Podcast();
        
        // Podcastindex API field mapping
        podcast.setId(podcastNode.path("id").asText());
        podcast.setTitle(podcastNode.path("title").asText());
        podcast.setDescription(podcastNode.path("description").asText());
        podcast.setPublisher(podcastNode.path("author").asText());
        podcast.setImage(podcastNode.path("image").asText());
        podcast.setThumbnail(podcastNode.path("artwork").asText());
        
        // Debug RSS URL
        String rssUrl = podcastNode.path("url").asText();
        System.out.println("RSS URL from API: " + rssUrl);
        podcast.setRss(rssUrl);
        
        podcast.setLanguage(podcastNode.path("language").asText("en"));
        podcast.setCountry(podcastNode.path("country").asText());
        podcast.setWebsite(podcastNode.path("link").asText());
        podcast.setClaimed(false);
        podcast.setType("podcast");
        podcast.setTotalEpisodes(podcastNode.path("episodeCount").asInt());
        
        // Parse categories
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
     * Parse episode object from JSON node
     * @param episodeNode JSON node
     * @return episode object
     */
    private Podcast.Episode parseEpisodeFromJson(JsonNode episodeNode) {
        Podcast.Episode episode = new Podcast.Episode();
        
        // Podcastindex API field mapping
        episode.setId(episodeNode.path("id").asText());
        episode.setTitle(episodeNode.path("title").asText());
        episode.setDescription(episodeNode.path("description").asText());
        episode.setAudio(episodeNode.path("enclosureUrl").asText());
        episode.setImage(episodeNode.path("image").asText());
        episode.setThumbnail(episodeNode.path("image").asText());
        episode.setAudioLength(episodeNode.path("length").asText());
        
        // Parse publication date
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
     * Get podcast recommendations specifically designed for elderly users
     * @return podcast recommendations suitable for elderly users
     */
    public Map<String, Object> getElderlyPodcastRecommendations() {
        // Define common interests for elderly users
        List<String> elderlyInterests = List.of(
            "health and wellness",      // Health and Wellness
            "meditation",              // Meditation
            "classical music",         // Classical Music
            "history",                 // History
            "gardening",               // Gardening
            "cooking",                 // Cooking
            "travel stories",          // Travel Stories
            "inspirational stories",   // Inspirational Stories
            "memory exercises",        // Memory Exercises
            "relaxation"               // Relaxation
        );
        
        return getPodcastRecommendations(elderlyInterests);
    }
    
    /**
     * Parse Podcast Index episodes API response
     * @param responseBody API response body
     * @return parsed episode list
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
     * Parse episode object from API JSON node
     * @param episodeNode JSON node
     * @return episode object
     */
    private Podcast.Episode parseEpisodeFromApiJson(JsonNode episodeNode) {
        try {
            Podcast.Episode episode = new Podcast.Episode();
            
            // Basic information
            episode.setId(episodeNode.path("id").asText());
            episode.setTitle(episodeNode.path("title").asText());
            episode.setDescription(episodeNode.path("description").asText());
            
            // Audio URL - prioritize enclosureUrl
            String audioUrl = episodeNode.path("enclosureUrl").asText();
            if (audioUrl == null || audioUrl.isEmpty()) {
                audioUrl = episodeNode.path("enclosure").path("url").asText();
            }
            if (audioUrl == null || audioUrl.isEmpty()) {
                audioUrl = episodeNode.path("audio").asText();
            }
            episode.setAudio(audioUrl);
            
            // Duration
            String duration = episodeNode.path("duration").asText();
            if (duration == null || duration.isEmpty()) {
                duration = episodeNode.path("enclosure").path("length").asText();
            }
            episode.setAudioLength(duration != null ? duration : "0");
            
            // Publication date
            long datePublished = episodeNode.path("datePublished").asLong(0);
            if (datePublished > 0) {
                episode.setPublishedDate(LocalDateTime.ofEpochSecond(datePublished, 0, java.time.ZoneOffset.UTC));
            } else {
                episode.setPublishedDate(LocalDateTime.now());
            }
            
            // Image
            String image = episodeNode.path("image").asText();
            episode.setImage(image);
            episode.setThumbnail(image);
            
            // Link
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
    
    /**
     * Extract image URL from text
     * @param text text containing image URL
     * @return image URL, null if not found
     */
    private String extractImageUrlFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        // Use regex to match image URLs
        String imageUrlPattern = "https?://[^\\s<>\"']+\\.(jpg|jpeg|png|gif|webp|svg)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(imageUrlPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }
} 