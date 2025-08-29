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
            
            logger.info("功能知识库加载成功，共加载 {} 个功能", functionKnowledgeBase.size());
            
        } catch (IOException e) {
            logger.error("加载功能知识库失败: {}", e.getMessage(), e);
            this.functionKnowledgeBase = List.of(); // 空列表作为fallback
        }
    }
    
    /**
     * 获取所有功能信息
     */
    public List<FunctionInfo> getAllFunctions() {
        return functionKnowledgeBase;
    }
    
    /**
     * 根据功能名称获取功能信息
     */
    public Optional<FunctionInfo> getFunctionByName(String functionName) {
        return functionKnowledgeBase.stream()
                .filter(func -> func.getName().equals(functionName))
                .findFirst();
    }
    
    /**
     * 根据关键词搜索功能
     */
    public List<FunctionInfo> searchFunctionsByKeyword(String keyword) {
        return functionKnowledgeBase.stream()
                .filter(func -> func.getKeywords().stream()
                        .anyMatch(kw -> kw.toLowerCase().contains(keyword.toLowerCase())))
                .toList();
    }
    
    /**
     * 获取功能知识库的JSON字符串表示
     */
    public String getKnowledgeBaseAsJson() {
        try {
            return objectMapper.writeValueAsString(functionKnowledgeBase);
        } catch (Exception e) {
            logger.error("序列化功能知识库失败: {}", e.getMessage(), e);
            return "[]";
        }
    }
    
    /**
     * 检查知识库是否已加载
     */
    public boolean isKnowledgeBaseLoaded() {
        return functionKnowledgeBase != null && !functionKnowledgeBase.isEmpty();
    }
}

