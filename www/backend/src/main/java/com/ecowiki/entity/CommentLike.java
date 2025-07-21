package com.ecowiki.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 评论点赞实体类
 * 
 * 记录用户对评论的点赞情况，防止重复点赞
 * 对应数据库中的comment_likes表
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
@Entity
@Table(name = "comment_likes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
public class CommentLike {
    
    /**
     * 点赞记录ID，主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;
    
    /**
     * 评论ID，关联到comments表
     */
    @Column(name = "comment_id", nullable = false)
    private Long commentId;
    
    /**
     * 用户ID，关联到user表
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 点赞时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime likedAt;
    
    // 默认构造函数
    public CommentLike() {
        this.likedAt = LocalDateTime.now();
    }
    
    // 带参数的构造函数
    public CommentLike(Long commentId, Long userId) {
        this();
        this.commentId = commentId;
        this.userId = userId;
    }
    
    // Getter和Setter方法
    public Long getLikeId() {
        return likeId;
    }
    
    public void setLikeId(Long likeId) {
        this.likeId = likeId;
    }
    
    public Long getCommentId() {
        return commentId;
    }
    
    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getLikedAt() {
        return likedAt;
    }
    
    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }
    
    @Override
    public String toString() {
        return "CommentLike{" +
                "likeId=" + likeId +
                ", commentId=" + commentId +
                ", userId=" + userId +
                ", likedAt=" + likedAt +
                '}';
    }
}
