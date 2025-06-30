package com.ecowiki.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文章数据传输对象
 * <p>
 * 用于文章信息的前后端传输，包含文章的完整信息和统计数据。
 * 支持数据验证、序列化传输等功能。
 * <p>
 * <b>设计说明：</b>
 * - 包含完整的文章信息（标题、内容、分类、标签等）
 * - 包含统计信息（浏览量、点赞数、评论数）
 * - 支持Bean Validation进行数据验证
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
public class ArticleDto {
    /** 文章ID */
    private Long articleId;
    
    /** 文章标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    /** 文章作者 */
    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名称长度不能超过50个字符")
    private String author;
    
    /** 文章内容 */
    private String content;
    
    /** 发布时间 */
    private LocalDateTime publishDate;
    
    /** 文章分类 */
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
    /** 浏览次数 */
    private Integer views;
    
    /** 点赞数 */
    private Integer likes;
    
    /** 文章标签（多个标签以逗号分隔） */
    @Size(max = 255, message = "标签长度不能超过255个字符")
    private String tags;
    
    /** 评论数 */
    private Integer comments;
    
    /** 最后更新时间 */
    private LocalDateTime updateTime;
    
    // 构造函数
    public ArticleDto() {}
    
    public ArticleDto(String title, String author, String content, String category) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Integer getViews() {
        return views;
    }
    
    public void setViews(Integer views) {
        this.views = views;
    }
    
    public Integer getLikes() {
        return likes;
    }
    
    public void setLikes(Integer likes) {
        this.likes = likes;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Integer getComments() {
        return comments;
    }
    
    public void setComments(Integer comments) {
        this.comments = comments;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
