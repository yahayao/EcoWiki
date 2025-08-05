/**
 * 文章数据传输对象DTO类
 * 
 * 功能包括：
 * - 文章信息前后端传输
 * - 文章数据封装和验证
 * - 文章统计信息传递
 * - 序列化和反序列化支持
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto.article;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
    
    /** 作者头像URL */
    private String authorAvatar;
    
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
    
    /** 更新时间 */
    private LocalDateTime updateTime;

    // 无参构造函数
    public ArticleDto() {}

    // 带参构造函数
    public ArticleDto(Long articleId, String title, String author, String authorAvatar, 
                     String content, LocalDateTime publishDate, String category, 
                     Integer views, Integer likes, String tags, Integer comments, 
                     LocalDateTime updateTime) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.authorAvatar = authorAvatar;
        this.content = content;
        this.publishDate = publishDate;
        this.category = category;
        this.views = views;
        this.likes = likes;
        this.tags = tags;
        this.comments = comments;
        this.updateTime = updateTime;
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
    
    public String getAuthorAvatar() {
        return authorAvatar;
    }
    
    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
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
