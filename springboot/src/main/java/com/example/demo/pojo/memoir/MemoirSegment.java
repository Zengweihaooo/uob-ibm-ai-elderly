package com.example.demo.pojo.memoir;

/**
 * Memoir segment entity
 * Each segment corresponds to an oral/transcribed text under a chapter-theme
 */
public class MemoirSegment {
    private Integer id;
    private Integer projectId;
    private String chapter;
    private String theme;
    private Integer promptId;
    private Integer orderIndex;
    private String text;
    private String audioUrl;
    private String tags;
    private String createdAt;
    private String updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getPromptId() { return promptId; }
    public void setPromptId(Integer promptId) { this.promptId = promptId; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
