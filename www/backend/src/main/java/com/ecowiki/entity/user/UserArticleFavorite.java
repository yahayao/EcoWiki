/**
 * 用户文章收藏实体类
 * 
 * 功能：
 * - 记录用户收藏的文章信息
 * - 支持用户个人文章管理功能
 * - 提供收藏时间记录和统计
 * - 对应数据库中的user_article_favorites表
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.entity.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "user_article_favorites")
public class UserArticleFavorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "article_id", nullable = false)
    private Long articleId;
    
    @Column(name = "favorite_time", nullable = false)
    private LocalDateTime favoriteTime;
    
    // 默认构造函数
    public UserArticleFavorite() {
        this.favoriteTime = LocalDateTime.now();
    }
    
    // 构造函数
    public UserArticleFavorite(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
        this.favoriteTime = LocalDateTime.now();
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
    
    public LocalDateTime getFavoriteTime() {
        return favoriteTime;
    }
    
    public void setFavoriteTime(LocalDateTime favoriteTime) {
        this.favoriteTime = favoriteTime;
    }
}
