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

@Entity
@Table(name = "articles")
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    @Column(nullable = false, length = 255)
    private String title;
    
    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名称长度不能超过50个字符")
    @Column(nullable = false, length = 50)
    private String author;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @NotNull(message = "发布时间不能为空")
    @Column(name = "publish_date", nullable = false)
    private LocalDateTime publishDate;
    
    @Size(max = 50, message = "分类长度不能超过50个字符")
    @Column(length = 50)
    private String category;
    
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer views = 0;
    
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer likes = 0;
    
    @Size(max = 255, message = "标签长度不能超过255个字符")
    @Column(length = 255)
    private String tags;
    
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer comments = 0;
    
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
    
    // 便利方法
    public void incrementViews() {
        this.views = (this.views == null ? 0 : this.views) + 1;
    }
    
    public void incrementLikes() {
        this.likes = (this.likes == null ? 0 : this.likes) + 1;
    }
    
    public void decrementLikes() {
        this.likes = Math.max(0, (this.likes == null ? 0 : this.likes) - 1);
    }
    
    public void incrementComments() {
        this.comments = (this.comments == null ? 0 : this.comments) + 1;
    }
    
    public void decrementComments() {
        this.comments = Math.max(0, (this.comments == null ? 0 : this.comments) - 1);
    }
}
