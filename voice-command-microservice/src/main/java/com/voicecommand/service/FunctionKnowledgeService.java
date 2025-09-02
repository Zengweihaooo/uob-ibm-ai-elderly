package com.voicecommand.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicecommand.model.FunctionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FunctionKnowledgeService {
    
    private static final Logger logger = LoggerFactory.getLogger(FunctionKnowledgeService.class);
    private static final String KNOWLEDGE_BASE_FILE = "function-knowledge-base.json";
    
    private List<FunctionInfo> functionKnowledgeBase;
    private final ObjectMapper objectMapper;
    
    public FunctionKnowledgeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void loadKnowledgeBase() {
        try {
            ClassPathResource resource = new ClassPathResource(KNOWLEDGE_BASE_FILE);
            InputStream inputStream = resource.getInputStream();
            
            Map<String, Object> knowledgeData = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> functionsData = (List<Map<String, Object>>) knowledgeData.get("functions");
            
            this.functionKnowledgeBase = objectMapper.convertValue(functionsData, 
                new TypeReference<List<FunctionInfo>>() {});
            
            logger.info("Function knowledge base loaded successfully, loaded {} functions", functionKnowledgeBase.size());
            
        } catch (IOException e) {
            logger.error("Failed to load function knowledge base: {}", e.getMessage(), e);
            this.functionKnowledgeBase = List.of(); // Empty list as fallback
        }
    }
    
    /**
     * Get all function information
     */
    public List<FunctionInfo> getAllFunctions() {
        return functionKnowledgeBase;
    }
    
    /**
     * Get function information by function name
     */
    public Optional<FunctionInfo> getFunctionByName(String functionName) {
        return functionKnowledgeBase.stream()
                .filter(func -> func.getName().equals(functionName))
                .findFirst();
    }
    
    /**
     * Search functions by keywords
     */
    public List<FunctionInfo> searchFunctionsByKeyword(String keyword) {
        return functionKnowledgeBase.stream()
                .filter(func -> func.getKeywords().stream()
                        .anyMatch(kw -> kw.toLowerCase().contains(keyword.toLowerCase())))
                .toList();
    }
    
    /**
     * Get JSON string representation of function knowledge base
     */
    public String getKnowledgeBaseAsJson() {
        try {
            return objectMapper.writeValueAsString(functionKnowledgeBase);
        } catch (Exception e) {
            logger.error("Failed to serialize function knowledge base: {}", e.getMessage(), e);
            return "[]";
        }
    }
    
    /**
     * Check if knowledge base is loaded
     */
    public boolean isKnowledgeBaseLoaded() {
        return functionKnowledgeBase != null && !functionKnowledgeBase.isEmpty();
    }
}

