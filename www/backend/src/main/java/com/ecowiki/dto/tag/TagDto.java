package com.ecowiki.dto.tag;

import java.time.LocalDateTime;

/**
 * 标签数据传输对象
 * 
 * 用于在控制器层和服务层之间传递标签数据，
 * 避免直接暴露实体类的内部结构。
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
public class TagDto {
    private Long tagId;
    private String tagName;
    private String description;
    private LocalDateTime createdTime;
    private Integer articleCount;
    public TagDto() {}
    public TagDto(Long tagId, String tagName, String description, LocalDateTime createdTime, Integer articleCount) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.description = description;
        this.createdTime = createdTime;
        this.articleCount = articleCount;
    }

    // ==================== Getter和Setter方法 ====================
    
    public Long getTagId() {
        return tagId;
    }
    
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
    
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public Integer getArticleCount() {
        return articleCount;
    }
    
    public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
    }
    
    @Override
    public String toString() {
        return "TagDto{" +
                "tagId=" + tagId +
                ", tagName='" + tagName + '\'' +
                ", description='" + description + '\'' +
                ", createdTime=" + createdTime +
                ", articleCount=" + articleCount +
                '}';
    }
}
