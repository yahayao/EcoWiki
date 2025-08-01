package com.ecowiki.entity.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 用户文章点赞实体
 * 
 * 记录用户点赞的文章信息，支持用户个人文章管理功能。
 * 
 * @author EcoWiki Development Team
 * @version 1.0
 * @since 2025-08-01
 */
@Entity
@Table(name = "user_article_likes")
public class UserArticleLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "article_id", nullable = false)
    private Long articleId;
    
    @Column(name = "like_time", nullable = false)
    private LocalDateTime likeTime;
    
    // 默认构造函数
    public UserArticleLike() {
        this.likeTime = LocalDateTime.now();
    }
    
    // 构造函数
    public UserArticleLike(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
        this.likeTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public LocalDateTime getLikeTime() {
        return likeTime;
    }
    
    public void setLikeTime(LocalDateTime likeTime) {
        this.likeTime = likeTime;
    }
}
