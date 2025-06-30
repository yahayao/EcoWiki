package com.ecowiki.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 文章实体类
 * <p>
 * 对应数据库中的articles表，存储Wiki文章的完整信息。
 * 包含文章内容、元数据、统计信息等字段，支持文章的完整生命周期管理。
 * <p>
 * <b>设计说明：</b>
 * - 支持大文本内容存储（使用@Lob注解）
 * - 包含完整的验证约束
 * - 提供便利方法进行统计数据操作
 * - 自动更新时间戳管理
 * - 适用于Wiki内容管理、文章展示、统计分析等场景
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@Entity
@Table(name = "articles")
public class Article {
    /** 文章主键ID，自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;
    
    /** 文章标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    @Column(nullable = false, length = 255)
    private String title;
    
    /** 文章作者 */
    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名称长度不能超过50个字符")
    @Column(nullable = false, length = 50)
    private String author;
    
    /** 文章内容（大文本字段） */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /** 文章发布时间 */
    @NotNull(message = "发布时间不能为空")
    @Column(name = "publish_date", nullable = false)
    private LocalDateTime publishDate;
    
    /** 文章分类 */
    @Size(max = 50, message = "分类长度不能超过50个字符")
    @Column(length = 50)
    private String category;
    
    /** 浏览次数 */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer views = 0;
    
    /** 点赞数 */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer likes = 0;
    
    /** 文章标签（多个标签以逗号分隔） */
    @Size(max = 255, message = "标签长度不能超过255个字符")
    @Column(length = 255)
    private String tags;
    
    /** 评论数 */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer comments = 0;
    
    /** 最后更新时间（自动更新） */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 构造函数
    public Article() {}
    
    public Article(String title, String author, String content, String category) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.category = category;
        this.publishDate = LocalDateTime.now();
        this.views = 0;
        this.likes = 0;
        this.comments = 0;
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
    
    /**
     * 增加浏览次数
     */
    public void incrementViews() {
        this.views = (this.views == null ? 0 : this.views) + 1;
    }
    
    /**
     * 增加点赞数
     */
    public void incrementLikes() {
        this.likes = (this.likes == null ? 0 : this.likes) + 1;
    }
    
    /**
     * 减少点赞数（最小为0）
     */
    public void decrementLikes() {
        this.likes = Math.max(0, (this.likes == null ? 0 : this.likes) - 1);
    }
    
    /**
     * 增加评论数
     */
    public void incrementComments() {
        this.comments = (this.comments == null ? 0 : this.comments) + 1;
    }
    
    /**
     * 减少评论数（最小为0）
     */
    public void decrementComments() {
        this.comments = Math.max(0, (this.comments == null ? 0 : this.comments) - 1);
    }
}
